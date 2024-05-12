//package cn.mw.monitor.model.service.impl;
//
//import cn.mw.monitor.common.constant.ErrorConstant;
//import cn.mw.monitor.common.util.PageList;
//import cn.mw.monitor.customPage.model.MwCustomcolTable;
//import cn.mw.monitor.model.dao.*;
//import cn.mw.monitor.model.dto.*;
//import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
//import cn.mw.monitor.model.exception.ModelManagerException;
//import cn.mw.monitor.model.param.*;
//import cn.mw.monitor.model.service.MwModelManageService;
//import cn.mw.monitor.model.service.MwModelManageServiceV2;
//import cn.mw.monitor.model.service.MwModelRelationsService;
//import cn.mw.monitor.model.service.MwModelRelationsServiceV2;
//import cn.mw.monitor.service.activitiAndMoudle.ModelSever;
//import cn.mw.monitor.service.model.dto.*;
//import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
//import cn.mw.monitor.service.model.service.ModelPropertiesType;
//import cn.mw.monitor.service.model.service.MwModelViewCommonService;
//import cn.mw.monitor.service.model.service.PropertyCatolog;
//import cn.mw.monitor.service.user.api.MWCommonService;
//import cn.mw.monitor.service.user.api.MWUserCommonService;
//import cn.mw.monitor.service.user.dto.DeleteDto;
//import cn.mw.monitor.service.user.dto.InsertDto;
//import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
//import cn.mw.monitor.state.DataType;
//import cn.mw.monitor.user.dto.GlobalUserInfo;
//import cn.mw.monitor.user.service.MWUserService;
//import cn.mw.monitor.util.IDModelType;
//import cn.mw.monitor.util.ModuleIDManager;
//import cn.mw.zbx.MWTPServerAPI;
//import cn.mw.zbx.MWTPServerProxy;
//import cn.mw.zbx.MWZabbixAPIResult;
//import cn.mwpaas.common.constant.PaasConstant;
//import cn.mwpaas.common.model.Reply;
//import cn.mwpaas.common.utils.CollectionUtils;
//import cn.mwpaas.common.utils.StringUtils;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//import com.google.common.base.Strings;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.beanutils.PropertyUtils;
//import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.action.support.master.AcknowledgedResponse;
//import org.elasticsearch.client.GetAliasesResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.CreateIndexResponse;
//import org.elasticsearch.client.indices.GetIndexRequest;
//import org.elasticsearch.cluster.metadata.AliasMetadata;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
//
///**
// * @author xhy
// * @date 2021/2/5 15:10
// */
//@Service
//@Slf4j
//@Primary
//public class MwModelManageServiceImplV2 implements MwModelManageServiceV2 {
//    @Value("${datasource.check}")
//    private String DATACHECK;
//    public static final String DATEBASEMYSQL = "mysql";
//    public static final String DATEBASEORACLE = "oracle";
//    @Resource
//    private MwModelRelationsDaoV2 mwModelRelationsDao;
//    @Autowired
//    private ILoginCacheInfo iLoginCacheInfo;
//    @Autowired
//    private MWCommonService mwCommonService;
//    @Resource
//    private MwModelManageDaoV2 mwModelManageDao;
//    @Resource
//    private MWModelTemplateDao mwModelTemplateDao;
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//    @Autowired
//    private ModelSever modelSever;
//    @Autowired
//    private MWUserService userService;
//    @Value("${System.isFlag}")
//    private Boolean isFlag;
//    @Autowired
//    private ModuleIDManager moduleIDManager;
//    @Autowired
//    private MwModelRelationsServiceV2 mwModelRelationsService;
//    @Resource
//    private MWUserCommonService mwUserCommonService;
//
//
//    @Override
//    @Transactional
//    public Reply creatModel(AddAndUpdateModelParamV2 addAndUpdateModelParam) throws IOException {
//        //先校验创建es索引
//        String modelIndex = addAndUpdateModelParam.getModelIndex();
//        Boolean esIndex = createEsIndex(modelIndex);
//        if (esIndex) {//如果索引已经存在则不能创建模型
//            throw new ModelManagerException("es中模型资源Id已存在,请修改模型资源ID再提交");
//        }
//        //将前端传入的list<String>类型的父模型id 转为String类型，存入数据库
//        String pids = "";
//        if (addAndUpdateModelParam.getPidList() != null) {
//            for (String str : addAndUpdateModelParam.getPidList()) {
//                pids += str + ",";
//            }
//            addAndUpdateModelParam.setPids(pids);
//        }
//        addAndUpdateModelParam.setCreator(iLoginCacheInfo.getLoginName());
//        addAndUpdateModelParam.setModifier(iLoginCacheInfo.getLoginName());
//
//        Long groupId = -1l;
//        //获取该模型的模型分组所有节点
//        if (addAndUpdateModelParam.getModelGroupSubId() != null && addAndUpdateModelParam.getModelGroupSubId().size() > 0) {
//            groupId = addAndUpdateModelParam.getModelGroupSubId().get(addAndUpdateModelParam.getModelGroupSubId().size() - 1);
//        }
//        String groupNodes = mwModelManageDao.selectGroupNodes(groupId);
//        if (!Strings.isNullOrEmpty(groupNodes)) {
//            addAndUpdateModelParam.setGroupNodes(groupNodes);
//        }
//        if (addAndUpdateModelParam.getIsShow() == null) {
//            addAndUpdateModelParam.setIsShow(true);
//        }
//        //模型分为 0:内置模型，1:自定义模型，页面上新增的都属于自定义模型，可被删除修改
//        //内置模型不可删除，不可导入导出，不可页面上创建，暂定由数据库或者脚本生成。
//        if (addAndUpdateModelParam.getModelLevel() == null) {
//            addAndUpdateModelParam.setModelLevel(ModelLevel.UserDefine.getLevel());
//        }
//        if (groupId != -1) {
//            addAndUpdateModelParam.setModelGroupId(groupId);
//        }
//
//        //根据模型视图创建内置属性
//        ModelViewV2 modelView = ModelViewV2.valueOf(addAndUpdateModelParam.getModelView());
//        List<PropertyInfoV2> propertyInfos = modelView.propertyAdd().add(addAndUpdateModelParam);
//        addAndUpdateModelParam.addAllPropertyInfo(propertyInfos);
//
//        addAndUpdateModelParam.setModelId(moduleIDManager.getID(IDModelType.Model));
//        mwModelManageDao.creatModel(addAndUpdateModelParam);
//        if (!isFlag) {
//            //设置负责人，用户组，机构/部门
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setUserIds(addAndUpdateModelParam.getUserIds());
//            param.setOrgIds(addAndUpdateModelParam.getOrgIds());
//            param.setGroupIds(addAndUpdateModelParam.getGroupIds());
//            param.setId(addAndUpdateModelParam.getModelIndex());
//            param.setType(DataType.MODEL_MANAGE.getName());
//            param.setDesc(DataType.MODEL_MANAGE.getDesc());
//            addMapperAndPerm(param);
//        }
//        return Reply.ok();
//    }
//
//    /**
//     * 创建模型索引
//     *
//     * @param
//     * @return
//     */
//
//    public Boolean createEsIndex(String index) throws IOException {
//        //  index = index + System.currentTimeMillis();
//        GetIndexRequest request = new GetIndexRequest(index);//创建索引
//        boolean exists = false;
//        exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
//        if (!exists) {
//            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);//创建索引
//            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
//        }
//        return exists;
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModel(AddAndUpdateModelParamV2 addAndUpdateModelParam) {
//        //先判断是否是内置模型
///*        if(addAndUpdateModelParam.getModelLevel()==0){
//            return Reply.fail("内置模型不可编辑");
//        }*/
//        Long groupId = 0l;
//        //获取该模型的模型分组所有节点
//        if (addAndUpdateModelParam.getModelGroupSubId() != null && addAndUpdateModelParam.getModelGroupSubId().size() > 0) {
//            groupId = addAndUpdateModelParam.getModelGroupSubId().get(addAndUpdateModelParam.getModelGroupSubId().size() - 1);
//        }
//        String groupNodes = mwModelManageDao.selectGroupNodes(groupId);
//        addAndUpdateModelParam.setGroupNodes(groupNodes);
//        addAndUpdateModelParam.setModelGroupId(groupId);
//        String pids = "";
//        if (addAndUpdateModelParam.getPidList() != null) {
//            for (String str : addAndUpdateModelParam.getPidList()) {
//                pids += str + ",";
//            }
//            addAndUpdateModelParam.setPids(pids);
//        }
//        addAndUpdateModelParam.setModifier(iLoginCacheInfo.getLoginName());
//        mwModelManageDao.updateModel(addAndUpdateModelParam);
//
//        if (!isFlag) {
//            //对用户名、机构、用户组修改
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setType(DataType.MODEL_MANAGE.getName());
//            param.setUserIds(addAndUpdateModelParam.getUserIds());
//            param.setOrgIds(addAndUpdateModelParam.getOrgIds());
//            param.setGroupIds(addAndUpdateModelParam.getGroupIds());
//            param.setId(addAndUpdateModelParam.getModelIndex());
//            param.setDesc(DataType.MODEL_MANAGE.getDesc());
//            //先删除后新增
//            deleteMapperAndPerm(param);
//            addMapperAndPerm(param);
//        }
//
//        return Reply.ok();
//    }
//
//    @Override
//    public Reply queryParentModelInfo() {
//        List<MwModelInfoDTOV2> list = mwModelManageDao.queryParentModelInfo();
//        return Reply.ok(list);
//    }
//
//    @Override
//    public Reply queryOrdinaryModelInfo(AddAndUpdateModelGroupParamV2 groupParam) {
//        List<MwModelInfoDTOV2> list = mwModelManageDao.queryOrdinaryModelInfo(groupParam);
//        return Reply.ok(list);
//    }
//
//    /**
//     * 数据关联使用 模型信息查询
//     *
//     * @return
//     */
//    @Override
//    public Reply selectOrdinaryModel(RelationModelDataParamV2 param) {
//        List<MwModelInfoDTOV2> list = new ArrayList<>();
//        //是否关联模型数据
//        if (param.getIsRelation()) {
//            //关联模型，下拉数据从模型关系中获取
//            list = mwModelManageDao.selectOrdinaryModelByOwnModelId(param.getModelId());
//        } else {
//            //获取所有模型数据
//            list = mwModelManageDao.selectOrdinaryModel();
//        }
//        return Reply.ok(list);
//    }
//
//    /**
//     * @param modelParam
//     * @return 删除模型时要删除模型，模型属性 ，模型关系，模型实例 todo删除es的索引 todo
//     * 删除模型的时候要判断是否时父模型，如果时父模型，删除的时候要先删除父模型下面的所有子模型和es中的子模型索引
//     */
//    @Override
//    public Reply deleteModel(AddAndUpdateModelParamV2 modelParam, Boolean isDelete) throws Exception {
//        //先判断是否是内置模型
//        log.info("内置未加载");
//        if (modelParam.getModelLevel() == 0) {
//            return Reply.fail("内置模型不可删除");
//        }
//        //先判断是否是父模型
//        if (ModelType.FATHER_MODEL.getTypeId().equals(modelParam.getModelTypeId())) {
//            //如果是父模型判断父模型下是否有子模型 如果有子模型先删除子模型的数据  一个父模型可能有多个子模型
//            List<Map<String, Object>> modelList = mwModelManageDao.selectSonAndFatherModelList(modelParam.getNodes());
//            if (null != modelList && modelList.size() > 0) {
//                for (Map<String, Object> model : modelList) {
//                    deleteModelById(Long.valueOf(model.get("modelId").toString()), model.get("modelIndex").toString(), isDelete);
//                }
//            } else {
//                //普通模型和子模型
//                deleteModelById(modelParam.getModelId(), modelParam.getModelIndex(), isDelete);
//            }
//        } else {
//            deleteModelById(modelParam.getModelId(), modelParam.getModelIndex(), isDelete);
//        }
//        if (!isFlag) {
//            //对用户名、机构、用户组删除
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setType(DataType.MODEL_MANAGE.getName());
//            param.setId(modelParam.getModelIndex());
//            deleteMapperAndPerm(param);
//        }
//
//        return Reply.ok();
//    }
//
//    void deleteModelById(Long modelId, String index, Boolean isDelete) throws IOException {
//        //检查模型下是否存在关系
//        boolean hasRelation = false;
//        try {
//            hasRelation = mwModelRelationsService.hasRelation(modelId);
//        } catch (Exception e) {
//            log.error("实例拓扑查询失败");
//        }
//        if (hasRelation) {
//            throw new ModelManagerException("模型下存在关系,请先删除关系");
//        }
//        //1删除模型
//        mwModelManageDao.deleteModel(modelId);
//        try {
//            mwModelRelationsService.deleteModelNode(modelId);
//        } catch (Exception e) {
//            log.error("删除实例拓扑失败");
//        }
//        //2删除模型关系
//        DeleteModelRelationGroupParamV2 deleteModelRelationGroupParam = DeleteModelRelationGroupParamV2.builder().ownModelId(modelId).build();
//        mwModelRelationsDao.deleteModelRelationsGroup(deleteModelRelationGroupParam);
//        //3删除模型所在实例(4.1删除模型实例表，4.2删除模型实例关系表)
//        List<Long> instanceId = mwModelManageDao.selectInstanceIdsByModelId(modelId);
//        if (instanceId != null && instanceId.size() > 0) {
//            DeleteModelInstanceParamV2 deleteParam = new DeleteModelInstanceParamV2();
//            deleteParam.setInstanceIds(instanceId);
//            deleteParam.setModelIndex(index);
//            Object obj = (Object) deleteParam;
//            modelSever.deleteModelInstance(obj, 0);
//        }
//        //5删除模型在es中存的数据
//        if (StringUtils.isNotEmpty(index)) {
//            if (isDelete) {
//                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
//                AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
//            }
//        }
//    }
//
//    @Override
//    public Reply selectModelList(ModelParamV2 modelParam) {
//        try {
//            if (!isFlag) {
//                GlobalUserInfo globalUser = userService.getGlobalUser();
//                List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
//                modelParam.setModelIndexs(allTypeIdList);
//                //默认状态
//                if (modelParam.getModelGroupId() == null || modelParam.getModelGroupId() == 0l) {
//                    modelParam.setModelGroupIds(allTypeIdList);
//                }
//            }
//            PageHelper.startPage(modelParam.getPageNumber(), modelParam.getPageSize());
//            Map priCriteria = PropertyUtils.describe(modelParam);
//
//            List<MwModelManageDtoV2> list = mwModelManageDao.selectModelList(priCriteria);
//            for (MwModelManageDtoV2 dto : list) {
//                // usergroup重新赋值使页面可以显示
//                List<Integer> groupIds = new ArrayList<>();
//                dto.getGroups().forEach(
//                        groupDTO -> groupIds.add(groupDTO.getGroupId())
//                );
//                dto.setGroupIds(groupIds);
//                // user重新赋值
//                List<Integer> userIds = new ArrayList<>();
//                dto.getPrincipal().forEach(
//                        userDTO -> userIds.add(userDTO.getUserId())
//                );
//                dto.setUserIds(userIds);
//                // 机构重新赋值使页面可以显示
//                List<List<Integer>> orgNodes = new ArrayList<>();
//                if (null != dto.getDepartment() && dto.getDepartment().size() > 0) {
//                    dto.getDepartment().forEach(department -> {
//                                List<Integer> orgIds = new ArrayList<>();
//                                List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
//                                nodes.forEach(node -> {
//                                    if (!"".equals(node))
//                                        orgIds.add(Integer.valueOf(node));
//                                });
//                                orgNodes.add(orgIds);
//                            }
//                    );
//                    dto.setOrgIds(orgNodes);
//                }
//            }
//            PageInfo pageInfo = new PageInfo<>(list);
//            return Reply.ok(pageInfo);
//        } catch (Exception e) {
//            log.error("fail to selectModelList modelParam{}, case by {}", modelParam, e);
//            return Reply.fail(ErrorConstant.MODEL_SELECT_CODE_313001, ErrorConstant.MODEL_SELECT_MSG_313001);
//        }
//    }
//
//
//    /**
//     * 创建模型分组不限层级
//     *
//     * @param groupParam
//     * @return
//     */
//    @Override
//    @Transactional
//    public Reply creatModelGroup(AddAndUpdateModelGroupParamV2 groupParam) {
//        groupParam.setCreator(iLoginCacheInfo.getLoginName());
//        groupParam.setModifier(iLoginCacheInfo.getLoginName());
//        ModelManageTypeDtoV2 modelManageTypeDto = null;
//        if (null != groupParam.getPid() && groupParam.getPid() != -1) {
//            modelManageTypeDto = mwModelManageDao.getModelGroupByPid(groupParam.getPid());
//            if (modelManageTypeDto == null) {
//                //modelManageTypeDto 为null，说明数据库中的数据已经被删除，不存在父级。则默认它本身为父级。
//                groupParam.setDeep(1);
//                groupParam.setIsNode(false);
//                groupParam.setPid(-1l);
//            } else {
//                groupParam.setDeep(modelManageTypeDto.getDeep() + 1);
//                groupParam.setIsNode(true);
//            }
//        } else {
//            groupParam.setDeep(1);
//            groupParam.setIsNode(false);
//        }
//        //页面新增的为普通类型，可删除
//        if (groupParam.getGroupLevel() == null) {
//            groupParam.setGroupLevel(1);
//        }
//        groupParam.setModelGroupId(moduleIDManager.getID(IDModelType.Model));
//        mwModelManageDao.creatModelGroup(groupParam);
//        if (!isFlag) {
//            //添加用户，用户组，机构
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setUserIds(groupParam.getUserIds());
//            param.setOrgIds(groupParam.getOrgIds());
//            param.setGroupIds(groupParam.getGroupIds());
//            param.setId(String.valueOf(groupParam.getModelGroupId()));
//            param.setType(DataType.MODEL_MANAGE.getName());
//            param.setDesc(DataType.MODEL_MANAGE.getDesc());
//            addMapperAndPerm(param);
//        }
//        if (null != groupParam.getPid() && groupParam.getPid() != -1) {//子模型
//            String nodes = modelManageTypeDto.getNodes();
//            groupParam.setNodes(nodes + groupParam.getModelGroupId() + ",");
//        } else {
//            groupParam.setNodes("," + groupParam.getModelGroupId() + ",");
//        }
//        mwModelManageDao.updateModelGroupNodes(groupParam.getModelGroupId(), groupParam.getNodes());
//        Reply reply = null;
//        //是否同步创建zabbix分组id
//        if (groupParam.getSyncZabbix() != null && groupParam.getSyncZabbix() == 1) {
//            reply = addGroupInfoByZabbix(groupParam);
//        }
//        String msg = "";
//        if (null != reply && reply.getData() != null && reply.getRes() == PaasConstant.RES_SUCCESS) {
//            msg = reply.getData().toString();
//        }
//        return Reply.ok(msg);
//    }
//
//    private Reply addGroupInfoByZabbix(AddAndUpdateModelGroupParam groupParam) {
//        String msg = "";
//        //查询zabbix分组信息和模型分组关联信息
//        List<Map> listM = mwModelManageDao.getZabbixGroupIdByMdoelGroupId(groupParam.getModelGroupId());
//        Map<String, String> infoMap = new HashMap();
//        for (Map m : listM) {
//            infoMap.put(m.get("monitorServerId").toString(), m.get("groupId").toString());
//        }
//        List<MWTPServerAPI> mwtpServerAPIS = MWTPServerProxy.getMWTPServerAPIList();
//        if (listM != null && listM.size() > 0) {
//            //说明zabbix分组信息和模型分组已经关联，数据存在，执行修改操作
//            for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
//                MWZabbixAPIResult resultData2 = mwtpServerAPI.hostgroupUpdate(mwtpServerAPI.getServerId(), infoMap.get(mwtpServerAPI.getServerId()), "[分组]" + groupParam.getNetwork());
//                if (0 != resultData2.getCode()) {
//                    msg += "zabbixServerId：" + mwtpServerAPI.getServerId() + "，修改主机群组失败；";
//                }
//            }
//        } else {
//            //新增操作
//            //调用Zabbix接口创建主机组，并将返回的groupids保存到对象里.
//            //新增多Zabbix的添加保存
//            List<MwModelAssetsGroupTable> groupTables = new ArrayList<>();
//            for (MWTPServerAPI mwtpServerAPI : mwtpServerAPIS) {
//                MWZabbixAPIResult resultData = mwtpServerAPI.hostgroupCreate(mwtpServerAPI.getServerId(), "[分组]" + groupParam.getNetwork());
//                if (resultData.getCode() == 0) {
//                    JsonNode node = (JsonNode) resultData.getData();
//                    if (node.size() > 0) {
//                        String groupid = "";
//                        if (node.size() > 0) {
//                            JsonNode a2 = node.get("groupids");
//                            groupid = a2.get(0).asText();
//                        }
//                        MwModelAssetsGroupTable groupTable = new MwModelAssetsGroupTable();
//                        groupTable.setAssetsSubtypeId(groupParam.getModelGroupId());
//                        groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
//                        groupTable.setGroupId(groupid);
//                        groupTables.add(groupTable);
//                    } else {
//                        msg += "zabbixServerId：" + mwtpServerAPI.getServerId() + "，创建主机群组失败；";
//                    }
//                } else {
//                    msg += "zabbixServerId：" + mwtpServerAPI.getServerId() + "，创建主机群组失败；";
//                }
//            }
//            //建立gorupid与多Zabbix对应关系
//            if (groupTables.size() > 0) {
//                mwModelTemplateDao.insertBatchGroupServerMap(groupTables);
//            }
//        }
//        return Reply.ok(msg);
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModelGroup(AddAndUpdateModelGroupParam groupParam) {
//        groupParam.setModifier(iLoginCacheInfo.getLoginName());
//        if (!isFlag) {
//            //添加用户，用户组，机构
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setUserIds(groupParam.getUserIds());
//            param.setOrgIds(groupParam.getOrgIds());
//            param.setGroupIds(groupParam.getGroupIds());
//            param.setId(String.valueOf(groupParam.getModelGroupId()));
//            param.setType(DataType.MODEL_MANAGE.getName());
//            param.setDesc(DataType.MODEL_MANAGE.getDesc());
//            //先删除后新增
//            deleteMapperAndPerm(param);
//            addMapperAndPerm(param);
//        }
//        Reply reply = null;
//        //是否同步创建zabbix分组id
//        if (groupParam.getSyncZabbix() != null && groupParam.getSyncZabbix() == 1) {
//            reply = addGroupInfoByZabbix(groupParam);
//        }
//        String msg = "";
//        if (null != reply && reply.getData() != null && reply.getRes() == PaasConstant.RES_SUCCESS) {
//            msg = reply.getData().toString();
//        }
//        mwModelManageDao.updateModelGroup(groupParam);
//        return Reply.ok(msg);
//    }
//
//    @Override
//    public Reply queryModelGroupById(AddAndUpdateModelGroupParam groupParam) {
//        AddAndUpdateModelGroupParam param = mwModelManageDao.queryModelGroupById(groupParam.getModelGroupId());
//        // usergroup重新赋值使页面可以显示
//        List<Integer> groupIds = new ArrayList<>();
//        param.getGroups().forEach(
//                groupDTO -> groupIds.add(groupDTO.getGroupId())
//        );
//        param.setGroupIds(groupIds);
//        // user重新赋值
//        List<Integer> userIds = new ArrayList<>();
//        param.getPrincipal().forEach(
//                userDTO -> userIds.add(userDTO.getUserId())
//        );
//        param.setUserIds(userIds);
//        // 机构重新赋值使页面可以显示
//        List<List<Integer>> orgNodes = new ArrayList<>();
//        if (null != param.getDepartment() && param.getDepartment().size() > 0) {
//            param.getDepartment().forEach(department -> {
//                        List<Integer> orgIds = new ArrayList<>();
//                        List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
//                        nodes.forEach(node -> {
//                            if (!"".equals(node))
//                                orgIds.add(Integer.valueOf(node));
//                        });
//                        orgNodes.add(orgIds);
//                    }
//            );
//            param.setOrgIds(orgNodes);
//        }
//        return Reply.ok(param);
//    }
//
//    @Override
//    public Reply getAllModelGroupInfo() {
//        try {
//            List<MwModelGroupDTO> list = mwModelManageDao.getAllModelGroupInfo();
//            return Reply.ok(list);
//        } catch (Throwable e) {
//            log.error("fail to getAllModelGroupInfo , case by {}", "", e);
//            return Reply.fail(500, "获取模型所有分组数据失败");
//        }
//    }
//
//    @Override
//    @Transactional
//    public Reply deleteModelGroup(DeleteModelGroupParam deleteModelGroupParam) {
//        if (deleteModelGroupParam.getGroupLevel() != null && deleteModelGroupParam.getGroupLevel() == 0) {
//            throw new ModelManagerException("内置模型分类，不可删除");
//        }
//        //删除模型分类之前要判断该分类下面是否有模型，如果有提示先删除模型，才能删除模型分类
//        int count = mwModelManageDao.selectModelCount(deleteModelGroupParam.getModelGroupId());
//        if (count != 0) {
//            throw new ModelManagerException("该模型分类下存在模型，需要删除模型再删除分类");
//        }
//        if (!isFlag) {
//            //删除用户，用户组，机构
//            ModelPermControlParam param = new ModelPermControlParam();
//            param.setId(String.valueOf(deleteModelGroupParam.getModelGroupId()));
//            param.setType(DataType.MODEL_MANAGE.getName());
//            deleteMapperAndPerm(param);
//        }
//        mwModelManageDao.deleteModelGroup(deleteModelGroupParam.getModelGroupId());
//        return Reply.ok();
//    }
//
//    @Override
//    public Reply queryModelListInfo() {
//        try {
//            //查询结构体模型导入数据，将结构体、部门、机构、用户组的类型去除
//            List<ModelManageStructDto> list = mwModelManageDao.queryModelListInfo();
//            for (ModelManageStructDto dto : list) {
//                List<ModelPropertiesStructDto> structList = mwModelManageDao.queryProperticesInfoByModelId(dto);
//                for (ModelPropertiesStructDto structDto : structList) {
//                    //结构体数据为9单选和10多选时，将数据String转为lsit数值
//                    if ((structDto.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || structDto.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode()) && (!Strings.isNullOrEmpty(structDto.getStructStrValue()))) {
//                        structDto.setStructListValue(Arrays.asList(structDto.getStructStrValue().split(",")));
//                    }
//                }
//                dto.setPropertiesStruct(structList);
//            }
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to queryModelListInfo , case by {}", "", e);
//            return Reply.fail(500, "查询结构体模型导入数据");
//        }
//    }
//
//    /**
//     * 创建关系的时候要避免形成环状
//     * 判断是否是父模型 如果是父模型不能添加自己的子模型
//     * 如果是子模型不能添加自己的父模型
//     * 也不能添加自己和自己的关系
//     *
//     * @param modelParam
//     * @return
//     */
//    @Override
//    public Reply selectModelListByModelId(ModelParam modelParam) throws Exception {
//
//        if (ModelType.SON_MODEL.getTypeId().equals(modelParam.getModelTypeId())) {
//            Integer fatherModelIdBySonModelId = mwModelManageDao.getFatherModelIdBySonModelId(modelParam.getModelId());
//            modelParam.setRelationQueryNode("," + fatherModelIdBySonModelId + ",");
//        } else {
//            modelParam.setRelationQueryNode("," + modelParam.getModelId() + ",");
//        }
//        if (!isFlag) {
//            GlobalUserInfo globalUser = userService.getGlobalUser();
//            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
//            modelParam.setModelIndexs(allTypeIdList);
//        }
//        Map priCriteria = PropertyUtils.describe(modelParam);
//        List<MwModelManageDto> mwModelManageDtos = mwModelManageDao.selectModelList(priCriteria);
//        return Reply.ok(mwModelManageDtos);
//    }
//
//    @Override
//    public Reply selectFatherModelList() {
//        List<MwModelInfoDTOV2> list = mwModelManageDao.selectFatherModelList();
//        return Reply.ok(list);
//    }
//
//    @Override
//    public Reply selectModelTypeListTree(QueryModelTypeParam param) {
//        if (!isFlag) {
//            GlobalUserInfo globalUser = userService.getGlobalUser();
//            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
//            param.setModelGroupIds(allTypeIdList);
//        }
//        List<MwModelManageTypeDto> list = mwModelManageDao.selectModelTypeList(param);
//        List<MwModelManageTypeDto> orgTopList = new ArrayList<>();
//        List<MwModelManageTypeDto> childList = new ArrayList<>();
//        list.forEach(mwModelManageTypeDto -> {
//            if (mwModelManageTypeDto.getDeep() == 1) {
//                orgTopList.add(mwModelManageTypeDto);
//            } else {
//                childList.add(mwModelManageTypeDto);
//
//            }
//        });
//        Set<Integer> modelGroupIdSet = new HashSet<>(childList.size());
//        orgTopList.forEach(
//                orgTop ->
//                        getModelTypeChild(orgTop, childList, modelGroupIdSet)
//        );
//        PageInfo pageInfo = new PageInfo<>(orgTopList);
//        return Reply.ok(pageInfo);
//
//    }
//
//
//    private void getModelTypeChild(MwModelManageTypeDto mwModelManageTypeDto, List<MwModelManageTypeDto> mwModelManageTypeDtoList, Set<Integer> modelGroupIdSet) {
//        List<MwModelManageTypeDto> childList = new ArrayList<>();
//        mwModelManageTypeDtoList.stream()
//                // 判断是否已循环过当前对象
//                .filter(child -> !modelGroupIdSet.contains(child.getModelGroupId()))
//                // 判断是否为父子关系
//                .filter(child -> child.getPid().equals(mwModelManageTypeDto.getModelGroupId()))
//                // orgIdSet集合大小不超过mwModelManageDtoList的大小
//                .filter(child -> modelGroupIdSet.size() <= mwModelManageTypeDtoList.size())
//                .forEach(
//                        // 放入modelIdSet,递归循环时可以跳过这个项目，提交循环效率
//                        child -> {
//                            modelGroupIdSet.add(child.getModelGroupId());
//                            //获取当前类目的子类目
//                            getModelTypeChild(child, mwModelManageTypeDtoList, modelGroupIdSet);
//                            childList.add(child);
//                        }
//                );
//        mwModelManageTypeDto.addChild(childList);
//    }
//
//    private void getChild(MwModelManageDto mwModelManageDto, List<MwModelManageDto> mwModelManageDtoList, Set<Integer> modelIdSet) {
//        List<MwModelManageDto> childList = new ArrayList<>();
//        mwModelManageDtoList.stream()
//                // 判断是否已循环过当前对象
//                .filter(child -> !modelIdSet.contains(child.getModelId()))
//                // 判断是否为父子关系
//                .filter(child -> child.getPid().equals(mwModelManageDto.getModelId()))
//                // orgIdSet集合大小不超过mwModelManageDtoList的大小
//                .filter(child -> modelIdSet.size() <= mwModelManageDtoList.size())
//                .forEach(
//                        // 放入modelIdSet,递归循环时可以跳过这个项目，提交循环效率
//                        child -> {
//                            modelIdSet.add(child.getModelId());
//                            //获取当前类目的子类目
//                            getChild(child, mwModelManageDtoList, modelIdSet);
//                            childList.add(child);
//                        }
//                );
//        mwModelManageDto.addChild(childList);
//    }
//
//    @Override
//    @Transactional
//    public Reply deleteModelRelations(DeleteModelRelationParam param) {
//        try {
//            List<Integer> ids = new ArrayList<>();
//            mwModelManageDao.bathDeleteModelRelations(ids);
//            return Reply.ok();
//        } catch (Exception e) {
//            log.error("fail to deleteModelRelations param{}, case by {}", param, e);
//            return Reply.fail(500, "模型关系删除失败");
//        }
//    }
//
//    @Override
//    public Reply selectModelRelationsByModelId(SelectRelationParam param) {
//
//        try {
//            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
//            Map priCriteria = PropertyUtils.describe(param);
//            List<ModelRelationsDto> list = mwModelManageDao.selectModelRelationsByModelId(priCriteria);
//            PageInfo pageInfo = new PageInfo<>(list);
//            pageInfo.setList(list);
//            return Reply.ok(pageInfo);
//
//        } catch (Exception e) {
//            log.error("fail to selectModelRelationsByModelId param{}, case by {}", param, e);
//            return Reply.fail(ErrorConstant.RELATION_MODEL_SELECT_CODE_313002, ErrorConstant.RELATION_MODEL_SELECT_MSG_313002);
//        }
//
//    }
//
//
//    @Override
//    public Reply selectModelRelationsGroup(SelectRelationParam param) {
//        try {
//            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
//            Map priCriteria = PropertyUtils.describe(param);
//            List<ModelRelationsGroupDto> list = mwModelManageDao.selectModelRelationsGroup(priCriteria);
//            PageInfo pageInfo = new PageInfo<>(list);
//            pageInfo.setList(list);
//            return Reply.ok(pageInfo);
//
//        } catch (Exception e) {
//            log.error("fail to selectModelRelationsGroup param{}, case by {}", param, e);
//            return Reply.fail(ErrorConstant.RELATION_GROUP_MODEL_SELECT_CODE_313003, ErrorConstant.RELATION_GROUP_MODEL_SELECT_MSG_313003);
//        }
//    }
//
//    @Override
//    public Reply editorPropertiesSort(List<ModelPropertiesSortParam> param) {
//        try {
//            //获取前段返回数据中最小的sort值
//            int sort = param.stream().mapToInt(s -> s.getSort() == null ? 0 : s.getSort()).min().orElse(0);
//            Integer modelId = 0;
//            //将PropertiesId和sort作为key，value存入map
//            Map<Integer, Integer> sortMap = new HashMap();
//            for (ModelPropertiesSortParam p : param) {
//                modelId = p.getModelId();
//                p.setSort(sort);
//                sortMap.put(p.getPropertiesId(), sort);
//                sort += 1;
//            }
//            //查询模型信息
//            ModelInfo modelInfo = mwModelManageDao.selectModelInfoWithPropertyById(modelId);
//            if (modelInfo != null && CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
//                //循环模型属性信息
//                for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//                    if (sortMap != null && sortMap.get(propertyInfo.getPropertiesId()) != null) {
//                        //通过propertiesId和map的key比较，相同则获取排序后的sort
//                        propertyInfo.setSort(sortMap.get(propertyInfo.getPropertiesId()));
//                    }
//                }
//            }
//            AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//            addAndUpdateModelParam.setModelId(modelId);
//            String modifier = iLoginCacheInfo.getLoginName();
//            addAndUpdateModelParam.setModifier(modifier);
//            addAndUpdateModelParam.setPropertyInfos(modelInfo.getPropertyInfos());
//            mwModelManageDao.updateModel(addAndUpdateModelParam);
//            return Reply.ok("修改属性排序成功");
//        } catch (Exception e) {
//            log.error("fail to editorPropertiesSort param{}, case by {}", param, e);
//            return Reply.fail(500, "修改属性排序失败");
//        }
//    }
//
//    @Override
//    @Transactional
//    public Reply creatModelProperties(AddAndUpdateModelPropertiesParam propertiesParam) {
//
//        //查询模型信息
//        ModelInfo modelInfo = mwModelManageDao.selectModelInfoWithPropertyById(propertiesParam.getModelId());
//        int maxSort = 0;
//        int countIndexId = 0;
//        int countName = 0;
//        int propertiesMaxId = 0;
//        List<PropertyInfo> newPropertyList = new ArrayList<>();
//        //创建模型属性的时候要考虑，modelIndexId在同一个model中不能重复
//        if (CollectionUtils.isNotEmpty(modelInfo.getPropertyInfos())) {
//            for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//                if (propertiesParam.getIndexId().equals(propertyInfo.getIndexId())) {
//                    countIndexId++;
//                }
//                if (propertiesParam.getPropertiesName().equals(propertyInfo.getPropertiesName())) {
//                    countName++;
//                }
//                if (null != propertyInfo.getSort() && maxSort < propertyInfo.getSort()) {
//                    maxSort = propertyInfo.getSort();
//                }
//            }
//            //获取当前模型中最大的属性Id
//            propertiesMaxId = modelInfo.getPropertyInfos().stream().mapToInt(s -> s.getPropertiesId() == null ? 0 : s.getSort()).max().orElse(0);
//            newPropertyList = modelInfo.getPropertyInfos();
//        }
//
//        if (countIndexId > 0) {
//            throw new ModelManagerException("模型属性ID重复请重新输入");
//        }
//        if (countName > 0) {
//            throw new ModelManagerException("模型属性名称重复请重新输入");
//        }
//
//        //设置属性Id
//        Integer propertiesId = propertiesMaxId + 1;
//        PropertyInfo propertyInfo = new PropertyInfo();
//        MwModelUtils.copyProperties(propertiesParam, propertyInfo);
//        if (null == propertyInfo.getPropertiesLevel()) {
//            propertyInfo.setPropertiesLevel(ModelLevel.UserDefine.getLevel());
//        }
//        if (Strings.isNullOrEmpty(propertyInfo.getPropertiesType().trim())) {
//            propertyInfo.setPropertiesType(PropertyCatolog.Default.getDesc());
//        }
//        AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//        addAndUpdateModelParam.setModelId(propertiesParam.getModelId());
//        String modifier = iLoginCacheInfo.getLoginName();
//        addAndUpdateModelParam.setModifier(modifier);
//
//        int sort = maxSort + 1;
//        propertyInfo.setSort(sort);
//        propertyInfo.setPropertiesId(propertiesId);
//        newPropertyList.add(propertyInfo);
//        addAndUpdateModelParam.setPropertyInfos(newPropertyList);
//
//        mwModelManageDao.updateModel(addAndUpdateModelParam);
//
//        //创建mw_cmdbmd_pagefield_table表数据
//        AddAndUpdateModelPageFieldParam pageFieldParam = new AddAndUpdateModelPageFieldParam();
//        pageFieldParam.setModelId(propertiesParam.getModelId());
//        pageFieldParam.setProp(propertiesParam.getIndexId());
//        pageFieldParam.setLabel(propertiesParam.getPropertiesName());
//        pageFieldParam.setOrderNumber(sort);
//        pageFieldParam.setVisible(propertiesParam.getIsShow());
//        pageFieldParam.setType(propertiesParam.getPropertiesTypeId());
//        pageFieldParam.setModelPropertiesId(propertiesParam.getPropertiesId());
//        //属性新增时，往mw_pagefield_table字段表中同步数据
//        mwModelManageDao.createModelPropertiesToPageField(pageFieldParam);
//
//        //mw_Customcol_table个性化字段表中同步初始数据
//        //获取用户表所有用户id
//        List<Integer> userIds = mwUserCommonService.selectAllUserId();
//        List<MwCustomcolTable> tableList = new ArrayList<>();
//        for (Integer userId : userIds) {
//            MwCustomcolTable table = new MwCustomcolTable();
//            table.setColId(pageFieldParam.getId());
//            table.setUserId(userId);
//            table.setSortable(true);
//            table.setWidth(null);
//            table.setVisible(propertiesParam.getIsShow());
//            table.setOrderNumber(sort);
//            tableList.add(table);
//        }
//        //数据库为oracle,先调用序列将实体类赋值，再批量插入
//        if (DATACHECK.equals(DATEBASEORACLE)) {
//            mwModelManageDao.initInsertPropertiesToCol(tableList);
//        }
//        mwModelManageDao.insertPropertiesToCol(tableList);
//
//        //查询该模型属性是否创建了实例，没有则在实例创建时设置es的数据类型
//        //创建了，则需要在新增属性的时候，往es中添加对应的数据类型。
//        int count = mwModelManageDao.countInstanceIdsByModelId(propertiesParam.getModelId());
//        if (count > 0) {
//            List<String> fields = new ArrayList<>();
//            fields.add(propertiesParam.getIndexId());
//
//            Map<String, AddModelInstancePropertiesParam> typeMap = new HashMap<>();
//            AddModelInstancePropertiesParam param = new AddModelInstancePropertiesParam();
//            param.setPropertiesType(propertiesParam.getPropertiesTypeId());
//            typeMap.put(propertiesParam.getIndexId(), param);
//
//            String modelIndex = "";
//            if (Strings.isNullOrEmpty(propertiesParam.getModelIndex())) {
//                modelIndex = mwModelManageDao.selectModelIndexById(propertiesParam.getModelId());
//                propertiesParam.setModelIndex(modelIndex);
//            }
//            MwModelUtils.batchSetESMapping(propertiesParam.getModelIndex(), fields, typeMap, restHighLevelClient);
//        }
//
//        return Reply.ok();
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModelProperties(AddAndUpdateModelPropertiesParam propertiesParam) {
//
//        boolean isChange = false;
//        if (Strings.isNullOrEmpty(propertiesParam.getPropertiesType().trim())) {
//            propertiesParam.setPropertiesType(PropertyCatolog.Default.getDesc());
//        }
//        if (propertiesParam.getPropertiesValue() != null) {
//            isChange = true;
//        }
//        //修改结构体数据,先删除，在新增
//        if (propertiesParam.getPropertiesStruct() != null && propertiesParam.getPropertiesStruct().size() > 0) {
//            isChange = true;
//        }
//
//        if (isChange) {
//            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(propertiesParam.getModelId());
//            PropertyInfo propertyInfo = null;
//            if (null != modelInfo.getPropertyInfos()) {
//                for (PropertyInfo pinfo : modelInfo.getPropertyInfos()) {
//                    if (propertiesParam.getIndexId().equals(pinfo.getIndexId())) {
//                        propertyInfo = pinfo;
//                        break;
//                    }
//                }
//            }
//
//            if (null != propertyInfo) {
//                MwModelUtils.copyProperties(propertiesParam, propertyInfo);
//
//                if (null == propertyInfo.getPropertiesLevel()) {
//                    propertyInfo.setPropertiesLevel(ModelLevel.UserDefine.getLevel());
//                }
//
//                if (Strings.isNullOrEmpty(propertyInfo.getPropertiesType().trim())) {
//                    propertyInfo.setPropertiesType(PropertyCatolog.Default.getDesc());
//                }
//
//                AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//                addAndUpdateModelParam.setModelId(propertiesParam.getModelId());
//                String modifier = iLoginCacheInfo.getLoginName();
//                addAndUpdateModelParam.setModifier(modifier);
//
//                addAndUpdateModelParam.setPropertyInfos(modelInfo.getPropertyInfos());
//
//                mwModelManageDao.updateModel(addAndUpdateModelParam);
//            }
//        }
//
//        return Reply.ok();
//    }
//
//    /**
//     * 更新模型属性，添加运维监控，自动化，日志管理、配置管理属性
//     *
//     * @return
//     */
//    @Override
//    public Reply updateAllModelProperties() {
//        //获取所有模型的属性信息
//        List<ModelInfo> modelInfos = mwModelManageDao.getBaseModelInfosDisParent();
//        for (ModelInfo modelInfo : modelInfos) {
//            if (null != modelInfo.getPropertyInfos()) {
//                List<PropertyInfo> propertyInfoList = new ArrayList<>();
//                propertyInfoList.addAll(modelInfo.getPropertyInfos());
//                //获取最大的sort值
//                int maxSort = modelInfo.getPropertyInfos().stream().mapToInt(s -> s.getSort() == null ? 0 : s.getSort()).max().orElse(0);
//                int sort = maxSort + 1;
//                int maxPropertiesId = modelInfo.getPropertyInfos().stream().mapToInt(s -> s.getPropertiesId() == null ? 0 : s.getPropertiesId()).max().orElse(0);
//                int propertiesId = maxPropertiesId + 1;
//                //判断模型属性中是否有operationMonitor，有则不需要继续添加
//                //没有则添加模型属性operationMonitor, autoManage, logManage,propManage
//                boolean isFlag = modelInfo.getPropertyInfos().stream().filter(item -> MwModelViewCommonService.OPERATION_MONITOR.equals(item.getIndexId())).findAny().isPresent();
//                if (!isFlag) {
//                    String[] strName = {"运维监控", "自动化", "日志管理", "配置管理"};
//                    String[] strField = {MwModelViewCommonService.OPERATION_MONITOR, MwModelViewCommonService.AUTO_MANAGE,
//                            MwModelViewCommonService.LOG_MANAGE, MwModelViewCommonService.PROP_MANAGE};
//                    for (int x = 0; x < strName.length; x++) {
//                        PropertyInfo pinfo = new PropertyInfo();
//                        pinfo.setPropertiesLevel(0);
//                        pinfo.setPropertiesId(propertiesId + x);
//                        pinfo.setSort(sort + x);
//                        pinfo.setIndexId(strField[x]);
//                        pinfo.setPropertiesName(strName[x]);
//                        pinfo.setPropertiesType("默认属性");
//                        pinfo.setPropertiesTypeId(17);
//                        pinfo.setIsMust(false);
//                        pinfo.setIsOnly(false);
//                        pinfo.setIsRead(false);
//                        pinfo.setIsShow(false);
//                        pinfo.setIsLookShow(false);
//                        pinfo.setIsEditorShow(false);
//                        pinfo.setIsInsertShow(false);
//                        pinfo.setIsListShow(false);
//                        propertyInfoList.add(pinfo);
//                    }
//                }
//                AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//                addAndUpdateModelParam.setModelId(modelInfo.getModelId());
//                String modifier = iLoginCacheInfo.getLoginName();
//                addAndUpdateModelParam.setModifier(modifier);
//                addAndUpdateModelParam.setPropertyInfos(propertyInfoList);
//                mwModelManageDao.updateModel(addAndUpdateModelParam);
//            }
//        }
//        return Reply.ok();
//    }
//
//    /**
//     * 根据模型分组，插入机柜机房对应字段属性
//     *
//     * @return
//     */
//    @Override
//    public Reply updateModelPropertiesByGroup(AddAndUpdateModelGroupParam param) {
//        //获取所有模型的属性信息
//        List<ModelInfo> modelInfos = mwModelManageDao.selectChildreModelInfoByGroupId(param.getModelGroupId());
//        for (ModelInfo modelInfo : modelInfos) {
//            if (null != modelInfo.getPropertyInfos()) {
//                List<PropertyInfo> propertyInfoList = new ArrayList<>();
//                propertyInfoList.addAll(modelInfo.getPropertyInfos());
//                //获取最大的sort值
//                int maxSort = modelInfo.getPropertyInfos().stream().mapToInt(s -> s.getSort() == null ? 0 : s.getSort()).max().orElse(0);
//                int sort = maxSort + 1;
//                int maxPropertiesId = modelInfo.getPropertyInfos().stream().mapToInt(s -> s.getPropertiesId() == null ? 0 : s.getPropertiesId()).max().orElse(0);
//                int propertiesId = maxPropertiesId + 1;
//                //判断模型属性中是否有operationMonitor，有则不需要继续添加
//                //没有则添加模型属性operationMonitor, autoManage, logManage,propManage
//                boolean isFlag = modelInfo.getPropertyInfos().stream().filter(item -> RELATIONSITEROOM.getField().equals(item.getIndexId())).findAny().isPresent();
//                if (!isFlag) {
//                    String[] strName = {RELATIONSITEROOM.getFieldName(), POSITIONBYROOM.getFieldName(), RELATIONSITECABINET.getFieldName(), POSITIONBYCABINET.getFieldName()};
//                    String[] strField = {RELATIONSITEROOM.getField(), POSITIONBYROOM.getField(), RELATIONSITECABINET.getField(), POSITIONBYCABINET.getField()};
//                    for (int x = 0; x < strName.length; x++) {
//                        PropertyInfo propertyInfo = new PropertyInfo();
//                        propertyInfo.setPropertiesLevel(0);
//                        propertyInfo.setPropertiesId(propertiesId + x);
//                        propertyInfo.setSort(sort + x);
//                        propertyInfo.setIndexId(strField[x]);
//                        propertyInfo.setPropertiesName(strName[x]);
//                        propertyInfo.setPropertiesType("默认属性");
//                        propertyInfo.setIsMust(true);
//                        propertyInfo.setIsOnly(false);
//                        propertyInfo.setIsRead(false);
//                        propertyInfo.setIsShow(true);
//                        propertyInfo.setIsLookShow(true);
//                        propertyInfo.setIsEditorShow(true);
//                        propertyInfo.setIsInsertShow(true);
//                        propertyInfo.setIsListShow(true);
//                        //机房属性设置为列表，查看可见
//                        if (RELATIONSITEROOM.getField().equals(strField[x]) || POSITIONBYROOM.getField().equals(strField[x])) {
//                            propertyInfo.setIsEditorShow(false);
//                            propertyInfo.setIsInsertShow(false);
//                        }
//                        if (POSITIONBYCABINET.getField().equals(strField[x]) || POSITIONBYROOM.getField().equals(strField[x])) {
//                            //布局数据设为数组结构
//                            propertyInfo.setPropertiesTypeId(16);
//                        } else if (RELATIONSITECABINET.getField().equals(strField[x])) {
//                            //所属机房为外部关联类型
//                            propertyInfo.setPropertiesTypeId(5);
//                        } else {
//                            propertyInfo.setPropertiesTypeId(1);
//                        }
//                        propertyInfoList.add(propertyInfo);
//                    }
//                }
//                AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//                addAndUpdateModelParam.setModelId(modelInfo.getModelId());
//                String modifier = iLoginCacheInfo.getLoginName();
//                addAndUpdateModelParam.setModifier(modifier);
//                addAndUpdateModelParam.setPropertyInfos(propertyInfoList);
//                mwModelManageDao.updateModel(addAndUpdateModelParam);
//            }
//        }
//        return Reply.ok();
//    }
//
//    /**
//     * 插入属性结构体时对数据进行处理
//     *
//     * @param propertiesParam
//     * @return
//     */
//    private List<ModelPropertiesStructDto> getStructDtoList(AddAndUpdateModelPropertiesParam propertiesParam) {
//        List<ModelPropertiesStructDto> structDtoList = new ArrayList<>();
//        for (ModelPropertiesStructDto modelPropertiesStructDto : propertiesParam.getPropertiesStruct()) {
//            ModelPropertiesStructDto structDto = new ModelPropertiesStructDto();
//            structDto.setModelId(propertiesParam.getModelId());
//            structDto.setPropertiesIndexId(propertiesParam.getIndexId());
//            structDto.setStructName(modelPropertiesStructDto.getStructName());
//            structDto.setStructId(modelPropertiesStructDto.getStructId());
//            structDto.setStructType(modelPropertiesStructDto.getStructType());
//            //结构体类型为 枚举型(单选)、多选枚举型时，传入的数据为数组类型，转化为string类型，方便存入数据库
//            if (modelPropertiesStructDto.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || modelPropertiesStructDto.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode()) {
//                String structStrValue = "";
//                if (modelPropertiesStructDto.getStructListValue() != null && modelPropertiesStructDto.getStructListValue().size() > 0) {
//                    for (String str : modelPropertiesStructDto.getStructListValue()) {
//                        structStrValue += str + ",";
//                    }
//                    structDto.setStructStrValue(structStrValue);
//                }
//            } else {
//                structDto.setStructStrValue(modelPropertiesStructDto.getStructStrValue());
//            }
//            structDtoList.add(structDto);
//        }
//        return structDtoList;
//    }
//
//    @Override
//    @Transactional
//    public Reply deleteModelProperties(List<AddAndUpdateModelPropertiesParam> propertiesParamList) throws IOException {
//        //如果为内置属性，不可删除
//        if (propertiesParamList.size() > 0) {
//            String modelIndex = propertiesParamList.get(0).getModelIndex();
//            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoByIndex(modelIndex);
//
//            Set<String> pendingDelId = new HashSet<>();
//            for (AddAndUpdateModelPropertiesParam propertiesParam : propertiesParamList) {
//                if (propertiesParam.getPropertiesLevel() != null && propertiesParam.getPropertiesLevel() == 0) {
//                    throw new ModelManagerException("内置属性不可删除");
//                }
//                pendingDelId.add(propertiesParam.getIndexId());
//            }
//
//            if (null != modelInfo.getPropertyInfos() && !pendingDelId.isEmpty()) {
//                Iterator<PropertyInfo> ite = modelInfo.getPropertyInfos().iterator();
//                while (ite.hasNext()) {
//                    PropertyInfo propertyInfo = ite.next();
//                    if (pendingDelId.contains(propertyInfo.getIndexId())) {
//                        ite.remove();
//                    }
//                }
//
//                AddAndUpdateModelParamV2 addAndUpdateModelParam = new AddAndUpdateModelParamV2();
//                addAndUpdateModelParam.setModelId(modelInfo.getModelId());
//                addAndUpdateModelParam.setPropertyInfos(modelInfo.getPropertyInfos());
//                String modifier = iLoginCacheInfo.getLoginName();
//                addAndUpdateModelParam.setModifier(modifier);
//
//                mwModelManageDao.updateModel(addAndUpdateModelParam);
//            }
//        }
//
//
//        return Reply.ok();
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModelPropertiesShowStatus(AddAndUpdateModelPropertiesParam propertiesParam) {
//        if (propertiesParam.getPropertiesIdList() != null && propertiesParam.getPropertiesIdList().size() > 0) {
//            //修改模型属性的可见性，同步修改pageFiled表、Customcol表数据
//            mwModelManageDao.updateModelPropertiesShowStatus(propertiesParam);
//        }
//        return Reply.ok("设置成功！");
//    }
//
//    /**
//     * 获取纳管属性
//     *
//     * @return
//     */
//    @Override
//    public Reply getPropertiesByManage(Integer modelId) {
//        try {
//            List<String> propertiesIds = mwModelManageDao.getPropertiesByManage(modelId);
//            return Reply.ok(propertiesIds);
//        } catch (Exception e) {
//            log.error("fail to getPropertiesByManage param{}, case by {}", modelId, e);
//            return Reply.fail(500, "获取纳管属性失败");
//        }
//    }
//
//    /**
//     * 获取基础资产展示属性
//     *
//     * @return
//     */
//    @Override
//    public Reply getPropertiesByBaseShow(Integer showType, Integer modelId) {
//        try {
//            List<String> propertiesIds = mwModelManageDao.getPropertiesByBaseShow(showType, modelId);
//            return Reply.ok(propertiesIds);
//        } catch (Exception e) {
//            log.error("fail to getPropertiesByBaseShow param{}, case by {}", modelId, e);
//            return Reply.fail(500, "获取基础资产展示属性失败");
//        }
//    }
//
//
//    /**
//     * 查询资产实例新增第二阶段属性id
//     *
//     * @param modelId
//     * @return
//     */
//    @Override
//    public Reply selectModelInstanceFiledBySecond(Integer modelId) {
//        try {
//            List<String> propertiesIds = mwModelManageDao.selectModelInstanceFiledBySecond(modelId);
//            return Reply.ok(propertiesIds);
//        } catch (Exception e) {
//            log.error("fail to selectModelInstanceFiledBySecond param{}, case by {}", modelId, e);
//            return Reply.fail(500, "获取资产新增第二阶段属性失败");
//        }
//    }
//
//    @Override
//    public Reply getPropertiesInfoByModelId(ModelParam param) {
//        try {
//            List<MWModelPropertiesInfoDto> mapList = new ArrayList<>();
//            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(param.getModelId());
//            List<Integer> list = Arrays.asList(6, 11, 12, 13);
//            if (null != modelInfo.getPropertyInfos()) {
//                for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//                    if (!list.contains(propertyInfo.getPropertiesTypeId())) {
//                        MWModelPropertiesInfoDto dto = new MWModelPropertiesInfoDto();
//                        dto.setPropertiesName(propertyInfo.getPropertiesName());
//                        dto.setModelId(param.getModelId() + "");
//                        dto.setIndexId(propertyInfo.getIndexId());
//                        dto.setPropertiesTypeId(propertyInfo.getPropertiesTypeId() + "");
//                        mapList.add(dto);
//                    }
//                }
//            }
//            return Reply.ok(mapList);
//        } catch (Exception e) {
//            log.error("fail to getPropertiesInfoByModelId param{}, case by {}", param, e);
//            return Reply.fail(500, "获取模型属性失败");
//        }
//    }
//
//
//    /**
//     * 查询模型属性的时候，如果改模型有父模型，能够查询改模型和父模型的属性
//     *
//     * @param param
//     * @return
//     */
//    @Override
//    public Reply selectModelPropertiesList(ModelParam param) {
//        try {
//            Map priCriteria = PropertyUtils.describe(param);
//            List<ModelInfo> modelInfoList = mwModelManageDao.selectModelPropertiesListWithParent(priCriteria);
//
//            List<PropertyInfo> allProperties = new ArrayList<>();
//
//            for (ModelInfo modelInfo : modelInfoList) {
//                if (null != modelInfo.getPropertyInfos()) {
//                    allProperties.addAll(modelInfo.getPropertyInfos());
//                }
//            }
//
//            Collections.sort(allProperties);
//
//            PageList pageList = new PageList();
//            List<PropertyInfo> pageInfos = pageList.getList(allProperties, param.getPageNumber(), param.getPageSize());
//
//            List<ModelPropertiesDto> list = new ArrayList<>();
//            for (PropertyInfo propertyInfo : pageInfos) {
//                ModelPropertiesDto modelPropertiesDto = new ModelPropertiesDto();
//                modelPropertiesDto.extractFrom(propertyInfo);
//                modelPropertiesDto.setModelId(param.getModelId());
//                list.add(modelPropertiesDto);
//            }
//            PageInfo pageInfo = new PageInfo<>(list);
//            pageInfo.setList(list);
//            return Reply.ok(pageInfo);
//        } catch (Exception e) {
//            log.error("fail to selectModelPropertiesList param{}, case by {}", param, e);
//            return Reply.fail(ErrorConstant.MODEL_PROPERTIES_SELECT_CODE_313004, ErrorConstant.MODEL_PROPERTIES_SELECT_MSG_313004);
//        }
//    }
//
//
//    /**
//     * 修改模型属性分类
//     *
//     * @param param
//     * @return
//     */
//    @Override
//    @Transactional
//    public Reply editorModelPropertiesType(AddAndUpdateModelPropertiesParam param) {
//        try {
//            mwModelManageDao.editorModelPropertiesType(param);
//            return Reply.ok();
//        } catch (Exception e) {
//            log.error("fail to editorModelPropertiesType param{}, case by {}", param, e);
//            return Reply.fail(500, "修改属性分类失败");
//        }
//    }
//
//    @Override
//    public Reply queryPropertiesTypeList(AddAndUpdateModelPropertiesParam param) {
//        try {
//            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(param.getModelId());
//            Set<String> set = new HashSet<>();
//            if (null != modelInfo.getPropertyInfos()) {
//                for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
//                    set.add(propertyInfo.getPropertiesType());
//                }
//            }
//
//            List<String> list = new ArrayList<>(set);
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to queryPropertiesTypeList param{}, case by {}", param, e);
//            return Reply.fail(500, "获取属性分类列表失败");
//        }
//    }
//
//    /**
//     * 获取所有es索引
//     */
//    public List<String> getAllIndices() {
//        try {
//            GetAliasesRequest request = new GetAliasesRequest();
//            GetAliasesResponse getAliasesResponse = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
//            getAliasesResponse.getAliases();
//            Map<String, Set<AliasMetadata>> map = getAliasesResponse.getAliases();
//            Set<String> indices = map.keySet();
//            List<String> list = new ArrayList<>(indices);
//            return list;
//        } catch (IOException e) {
//            log.error("fail to getAllIndices case by {}", e);
//        }
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Reply checkModelByES() {
//        //获取es中所有的索引
//        List<String> esIndexList = getAllIndices();
//        //获取mysql所有的索引
//        List<AddAndUpdateModelParamV2> list = mwModelManageDao.queryModelIndexList();
//        //获取es中没有而mysql存在的脏数据
//        List<AddAndUpdateModelParamV2> reduceList = list.stream().filter(item -> !esIndexList.contains(item.getModelIndex())).collect(Collectors.toList());
//        if (reduceList.size() > 0) {
//            try {
//                for (AddAndUpdateModelParamV2 dto : reduceList) {
//                    //删除本地数据库中的相关记录  false表示不对es库对删除操作
//                    deleteModel(dto, false);
//                }
//            } catch (Exception e) {
//                log.error("fail to checkModelByES case by {}", e);
//                return Reply.fail(500, "数据校验失败");
//            }
//        }
//        return Reply.ok("数据校验成功");
//    }
//
//    @Override
//    public Reply syncModelToES() {
//        try {
//            //获取es中所有的索引
//            List<String> esIndexList = getAllIndices();
//            //获取mysql所有的索引
//            List<AddAndUpdateModelParamV2> list = mwModelManageDao.queryModelIndexList();
//            //获取es中没有而mysql存在的数据
//            List<AddAndUpdateModelParamV2> reduceList = list.stream().filter(item -> !esIndexList.contains(item.getModelIndex())).collect(Collectors.toList());
//            //将数据同步至ES中
//            for (AddAndUpdateModelParamV2 addAndUpdateModelParam : reduceList) {
//                String modelIndex = addAndUpdateModelParam.getModelIndex();
//                Boolean esIndex = null;
//                esIndex = createEsIndex(modelIndex);
//                if (esIndex) {//如果索引已经存在则不能创建模型
//                    continue;
//                }
//            }
//            return Reply.ok("数据同步成功");
//        } catch (IOException e) {
//            return Reply.fail(500, "数据同步失败！");
//        }
//    }
//
//    @Override
//    public Reply cleanAllModelInfo() {
//        try {
//            List<ModelInfo> modelInfos = mwModelManageDao.selectAllModelInfo();
//            List<String> modelIndexs = new ArrayList<>();
//            for (ModelInfo modelInfo : modelInfos) {
//                modelInfo.getModelIndex();
//            }
//            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(String.join(",", modelIndexs));
//            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            log.error("fail to cleanAllModelInfo case by {}", e);
//            return Reply.fail(500, "删除ES模型数据失败！");
//        }
//        return null;
//    }
//
//    @Override
//    public Reply selectGroupServerMap(Integer assetsSubTypeId) {
//        try {
//            List<MwModelAssetsGroupTable> list = mwModelManageDao.selectGroupServerMap(assetsSubTypeId);
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to selectGroupServerMap case by {}", e);
//            return Reply.fail(500, "数据获取成功失败");
//        }
//    }
//
//    @Override
//    public Reply queryPropertiesGanged(QueryModelGangedFieldParam param) {
//        try {
//            List<String> list = mwModelManageDao.queryPropertiesGanged(param);
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to queryPropertiesGanged case by {}", e);
//            return Reply.fail(500, "联动关联字段查询失败");
//        }
//    }
//
//    @Override
//    public Reply getPropertiesFieldByGanged(AddAndUpdateModelPropertiesParam param) {
//        try {
//            List<MWModelPropertiesGangedDto> gangeList = new ArrayList<>();
//            List<ModelInfo> modelInfos = new ArrayList<>();
//            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(param.getModelId());
//            if (null != modelInfo) {
//                //若有父id,则获取父模型属性
//                if (StringUtils.isNotEmpty(modelInfo.getPids())) {
//                    List<ModelInfo> modelInfoList = mwModelManageDao.selectModelInfoByPids(modelInfo.getPids());
//                    modelInfos.addAll(modelInfoList);
//                } else {
//                    modelInfos.add(modelInfo);
//                }
//            }
//
//            //获取联动属性
//            List<PropertyInfo> propertyInfos = new ArrayList<>();
//            for (ModelInfo mInfo : modelInfos) {
//                List<PropertyInfo> gangedList = mInfo.findGangedList();
//                if (null != gangedList) {
//                    propertyInfos.addAll(gangedList);
//                }
//            }
//
//            for (PropertyInfo propertyInfo : propertyInfos) {
//                MWModelPropertiesGangedDto gangedDto = new MWModelPropertiesGangedDto();
//                gangedDto.extractFromPropertyInfo(propertyInfo);
//                gangeList.add(gangedDto);
//            }
//            return Reply.ok(gangeList);
//        } catch (Throwable e) {
//            log.error("fail to getPropertiesFieldByGanged case by {}", e);
//            return Reply.fail(500, "获取上级联动字段属性");
//        }
//
//    }
//
//    @Transactional
//    protected void addMapperAndPerm(ModelPermControlParam param) {
//        InsertDto insertDto = InsertDto.builder()
//                .groupIds(param.getGroupIds())  //用户组
//                .userIds(param.getUserIds())  //责任人
//                .orgIds(param.getOrgIds())      //机构
//                .typeId(String.valueOf(param.getId())) //数据主键
//                .type(param.getType())        //链路
//                .desc(param.getDesc()).build(); //链路
//        mwCommonService.addMapperAndPerm(insertDto);
//    }
//
//    /**
//     * 删除负责人，用户组，机构 权限关系
//     *
//     * @param param
//     */
//    @Transactional
//    protected void deleteMapperAndPerm(ModelPermControlParam param) {
//        DeleteDto deleteDto = DeleteDto.builder()
//                .typeId(String.valueOf(param.getId()))
//                .type(param.getType())
//                .build();
//        mwCommonService.deleteMapperAndPerm(deleteDto);
//    }
//}
