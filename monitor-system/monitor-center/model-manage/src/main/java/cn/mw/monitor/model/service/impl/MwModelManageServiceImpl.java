//package cn.mw.monitor.model.service.impl;
//
//import cn.mw.monitor.common.constant.ErrorConstant;
//import cn.mw.monitor.customPage.model.MwCustomcolTable;
//import cn.mw.monitor.model.dao.MWModelTemplateDao;
//import cn.mw.monitor.model.dao.MwModelManageDao;
//import cn.mw.monitor.model.dao.MwModelRelationsDao;
//import cn.mw.monitor.model.dto.*;
//import cn.mw.monitor.model.exception.ModelManagerException;
//import cn.mw.monitor.model.param.*;
//import cn.mw.monitor.model.service.MwModelManageService;
//import cn.mw.monitor.service.activitiAndMoudle.ModelSever;
//import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
//import cn.mw.monitor.service.model.service.ModelPropertiesType;
//import cn.mw.monitor.service.user.api.MWCommonService;
//import cn.mw.monitor.service.user.api.MWUserCommonService;
//import cn.mw.monitor.service.user.dto.DeleteDto;
//import cn.mw.monitor.service.user.dto.InsertDto;
//import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
//import cn.mw.monitor.state.DataType;
//import cn.mw.monitor.user.dto.GlobalUserInfo;
//import cn.mw.monitor.user.service.MWUserService;
//import cn.mw.zbx.MWTPServerAPI;
//import cn.mw.zbx.MWTPServerProxy;
//import cn.mw.zbx.MWZabbixAPIResult;
//import cn.mwpaas.common.constant.PaasConstant;
//import cn.mwpaas.common.model.Reply;
//import cn.mwpaas.common.utils.StringUtils;
//import com.alibaba.fastjson.JSONArray;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.github.pagehelper.PageHelper;
//import com.github.pagehelper.PageInfo;
//import com.google.common.base.Strings;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.beanutils.PropertyUtils;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.action.support.master.AcknowledgedResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.CreateIndexResponse;
//import org.elasticsearch.client.indices.GetIndexRequest;
//import org.elasticsearch.client.indices.GetIndexResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @author xhy
// * @date 2021/2/5 15:10
// */
////@Service
//@Slf4j
//public class MwModelManageServiceImpl implements MwModelManageService {
//    @Resource
//    private MwModelRelationsDao mwModelRelationsDao;
//    @Autowired
//    private ILoginCacheInfo iLoginCacheInfo;
//    @Autowired
//    private MWCommonService mwCommonService;
//    @Resource
//    private MwModelManageDao mwModelManageDao;
//    @Resource
//    private MWModelTemplateDao mwModelTemplateDao;
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//    @Resource
//    private MWUserCommonService mwUserCommonService;
////    private MWUserDao mwuserDao;
//    @Resource
//    private MwModelInstanceServiceImplV1 mwModelInstanceServiceImplV1;
//    @Autowired
//    private ModelSever modelSever;
//    @Autowired
//    private MWUserService userService;
//    @Value("${System.isFlag}")
//    private Boolean isFlag;
//
//
//    @Override
//    @Transactional
//    public Reply creatModel(AddAndUpdateModelParam addAndUpdateModelParam) throws IOException {
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
//        Map map = new HashMap();
//        Integer groupId = -1;
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
//        if(addAndUpdateModelParam.getModelLevel()==null){
//            addAndUpdateModelParam.setModelLevel(1);
//        }
//        if(groupId != -1){
//            addAndUpdateModelParam.setModelGroupId(groupId);
//        }
//        mwModelManageDao.creatModel(addAndUpdateModelParam);
//        int modelId = addAndUpdateModelParam.getModelId();
//        mwModelManageDao.updateNodes(modelId, addAndUpdateModelParam.getNodes());
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
//
//        //根据模型视图创建内置属性
//        if (addAndUpdateModelParam.getModelView() != null && addAndUpdateModelParam.getModelView() == 1) {//机房视图
//            String[] strName = {"名称", "编号", "描述", "行", "列", "布局"};
//            String[] strField = {"instanceName", "instanceCode", "desc", "rowNum", "colNum", "layoutData"};
//            for (int x = 0; x < strName.length; x++) {
//                AddAndUpdateModelPropertiesParam propertiesParam = new AddAndUpdateModelPropertiesParam();
//                propertiesParam.setPropertiesLevel(0);
//                propertiesParam.setSort(x);
//                propertiesParam.setIndexId(strField[x]);
//                propertiesParam.setModelId(modelId);
//                propertiesParam.setPropertiesName(strName[x]);
//                propertiesParam.setPropertiesType("默认属性");
//                if ("rowNum".equals(strField[x]) || "colNum".equals(strField[x])) {
//                    //布局数据设为数值整形结构
//                    propertiesParam.setPropertiesTypeId(2);
//                } else if ("layoutData".equals(strField[x])) {
//                    //布局数据设为数组结构
//                    propertiesParam.setPropertiesTypeId(16);
//                } else {
//                    propertiesParam.setPropertiesTypeId(1);
//                }
//                if ("desc".equals(strField[x])) {
//                    propertiesParam.setIsMust(false);
//                } else {
//                    propertiesParam.setIsMust(true);
//                }
//                propertiesParam.setIsOnly(false);
//                propertiesParam.setIsRead(false);
//                propertiesParam.setIsShow(true);
//                propertiesParam.setIsLookShow(true);
//                propertiesParam.setIsEditorShow(true);
//                propertiesParam.setIsInsertShow(true);
//                propertiesParam.setIsListShow(true);
//                creatModelProperties(propertiesParam);
//            }
//        }else if (addAndUpdateModelParam.getModelView() != null && addAndUpdateModelParam.getModelView() == 2) {//机柜视图
//            String[] strName = {"名称", "编号", "描述", "所属机房", "位置", "U位数", "布局"};
//            String[] strField = {"instanceName", "instanceCode", "desc", "relationSite", "position", "UNum", "layoutData"};
//            for (int x = 0; x < strName.length; x++) {
//                AddAndUpdateModelPropertiesParam propertiesParam = new AddAndUpdateModelPropertiesParam();
//                propertiesParam.setPropertiesLevel(0);
//                propertiesParam.setSort(x);
//                propertiesParam.setIndexId(strField[x]);
//                propertiesParam.setModelId(modelId);
//                propertiesParam.setPropertiesName(strName[x]);
//                propertiesParam.setPropertiesType("默认属性");
//                if ("UNum".equals(strField[x])) {
//                    //布局数据设为整形数值结构
//                    propertiesParam.setPropertiesTypeId(2);
//                } else if ("position".equals(strField[x]) || "layoutData".equals(strField[x])) {
//                    //布局数据设为数组结构
//                    propertiesParam.setPropertiesTypeId(16);
//                } else if ("relationSite".equals(strField[x])) {
//                    //所属机房为外部关联类型
//                    propertiesParam.setPropertiesTypeId(5);
//                } else {
//                    propertiesParam.setPropertiesTypeId(1);
//                }
//                if ("desc".equals(strField[x])) {
//                    propertiesParam.setIsMust(false);
//                } else {
//                    propertiesParam.setIsMust(true);
//                }
//                propertiesParam.setIsOnly(false);
//                propertiesParam.setIsRead(false);
//                propertiesParam.setIsShow(true);
//                propertiesParam.setIsLookShow(true);
//                propertiesParam.setIsEditorShow(true);
//                propertiesParam.setIsInsertShow(true);
//                propertiesParam.setIsListShow(true);
//                creatModelProperties(propertiesParam);
//            }
//        }else {   //创建模型时，同时新增内置模型属性
//            //默认视图
//            //父模型时，不新增属性
//            if (addAndUpdateModelParam.getModelTypeId() != 2) {
//                String[] strName;
//                String[] strField;
//                //是否西藏邮储环境
//                strName = new String[]{"名称"};
//                strField = new String[]{"instanceName"};
//                for (int x = 0; x < strName.length; x++) {
//                    AddAndUpdateModelPropertiesParam propertiesParam = new AddAndUpdateModelPropertiesParam();
//                    propertiesParam.setPropertiesLevel(0);
//                    propertiesParam.setSort(x);
//                    propertiesParam.setIndexId(strField[x]);
//                    propertiesParam.setModelId(modelId);
//                    propertiesParam.setPropertiesName(strName[x]);
//                    propertiesParam.setPropertiesType("默认属性");
//                    propertiesParam.setPropertiesTypeId(1);
//                    propertiesParam.setIsMust(true);
//                    propertiesParam.setIsOnly(false);
//                    propertiesParam.setIsRead(false);
//                    propertiesParam.setIsShow(true);
//                    propertiesParam.setIsLookShow(true);
//                    propertiesParam.setIsEditorShow(true);
//                    propertiesParam.setIsInsertShow(true);
//                    propertiesParam.setIsListShow(true);
//                    creatModelProperties(propertiesParam);
//                }
//            }
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
//    public Reply updateModel(AddAndUpdateModelParam addAndUpdateModelParam) {
//        //先判断是否是内置模型
///*        if(addAndUpdateModelParam.getModelLevel()==0){
//            return Reply.fail("内置模型不可编辑");
//        }*/
//        Integer groupId = 0;
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
//        List<MwModelInfoDTO> list = mwModelManageDao.queryParentModelInfo();
//        return Reply.ok(list);
//    }
//
//    @Override
//    public Reply queryOrdinaryModelInfo(AddAndUpdateModelGroupParam groupParam) {
//        List<MwModelInfoDTO> list = mwModelManageDao.queryOrdinaryModelInfo(groupParam);
//        return Reply.ok(list);
//    }
//
//    /**
//     * 数据关联使用 模型信息查询
//     *
//     * @return
//     */
//    @Override
//    public Reply selectOrdinaryModel(RelationModelDataParam param) {
//        List<MwModelInfoDTO> list = new ArrayList<>();
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
//    public Reply deleteModel(AddAndUpdateModelParam modelParam, Boolean isDelete) throws IOException {
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
//                    deleteModelById(Integer.valueOf(model.get("modelId").toString()), model.get("modelIndex").toString(), isDelete);
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
//
//    void deleteModelById(Integer modelId, String index, Boolean isDelete) throws IOException {
//        //1删除模型
//        mwModelManageDao.deleteModel(modelId);
//        //2关联删除 删除模型属性、模型ageField表、模型customcol表
//        mwModelManageDao.deleteModelPropertiesByModelId(modelId);
//        //3删除模型关系
//        DeleteModelRelationGroupParam deleteModelRelationGroupParam = DeleteModelRelationGroupParam.builder().ownModelId(modelId).build();
//        mwModelRelationsDao.deleteModelRelationsGroup(deleteModelRelationGroupParam);
//        //4删除模型所在实例(4.1删除模型实例表，4.2删除模型实例关系表)
//        List<Integer> instanceId = mwModelManageDao.selectInstanceIdsByModelId(modelId);
//        if (instanceId != null && instanceId.size() > 0) {
//            DeleteModelInstanceParam deleteParam = new DeleteModelInstanceParam();
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
//    public Reply selectModelList(ModelParam modelParam) {
//        try {
//            if (!isFlag) {
//                GlobalUserInfo globalUser = userService.getGlobalUser();
//                List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
//                modelParam.setModelIndexs(allTypeIdList);
//                //默认状态
//                if (modelParam.getModelGroupId() == null || modelParam.getModelGroupId() == 0) {
//                    modelParam.setModelGroupIds(allTypeIdList);
//                }
//            }
//            PageHelper.startPage(modelParam.getPageNumber(), modelParam.getPageSize());
//            Map priCriteria = PropertyUtils.describe(modelParam);
//
//            List<MwModelManageDto> list = mwModelManageDao.selectModelList(priCriteria);
//            for (MwModelManageDto dto : list) {
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
//    public Reply creatModelGroup(AddAndUpdateModelGroupParam groupParam) {
//        groupParam.setCreator(iLoginCacheInfo.getLoginName());
//        groupParam.setModifier(iLoginCacheInfo.getLoginName());
//        ModelManageTypeDto modelManageTypeDto = null;
//        if (null != groupParam.getPid() && groupParam.getPid() != -1) {
//            modelManageTypeDto = mwModelManageDao.getModelGroupByPid(groupParam.getPid());
//            if (modelManageTypeDto == null) {
//                //modelManageTypeDto 为null，说明数据库中的数据已经被删除，不存在父级。则默认它本身为父级。
//                groupParam.setDeep(1);
//                groupParam.setIsNode(false);
//                groupParam.setPid(-1);
//            } else {
//                groupParam.setDeep(modelManageTypeDto.getDeep() + 1);
//                groupParam.setIsNode(true);
//            }
//        } else {
//            groupParam.setDeep(1);
//            groupParam.setIsNode(false);
//        }
//        //页面新增的为普通类型，可删除
//        if(groupParam.getGroupLevel()==null){
//            groupParam.setGroupLevel(1);
//        }
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
//        List<MwModelInfoDTO> list = mwModelManageDao.selectFatherModelList();
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
//            OptionalInt minSort = param.stream().mapToInt(s -> s.getSort() == null ? 0 : s.getSort()).min();
//            int sort = minSort.getAsInt();
//            for (ModelPropertiesSortParam p : param) {
//                p.setSort(sort);
//                sort += 1;
//                mwModelManageDao.updateModelPropertiesSort(p);
//            }
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
//        /**
//         * 创建模型属性的时候要考虑，modelIndexId在同一个model中不能重复
//         */
//        //模型属性分为内置模型和自定义模型，0:内置属性，1:自定义属性;其中内置属性不可删除
//        //页面上新增的都为自定义属性
//        if (propertiesParam.getPropertiesLevel() == null) {
//            propertiesParam.setPropertiesLevel(1);
//        }
//        int sort = 0;
//        //查询模型属性ID重复
//        int countIndexId = mwModelManageDao.selectModelIndexIdCount(propertiesParam);
//        //查询模型属性sort排序
//        sort = mwModelManageDao.selectModelPropertiesSort(propertiesParam);
//        propertiesParam.setSort(sort + 1);
//        //查询模型属性名称重复
//        int countName = mwModelManageDao.selectModelPropertiesNameCount(propertiesParam);
//        if (countIndexId > 0) {
//            throw new ModelManagerException("模型属性ID重复请重新输入");
//        }
//        if (countName > 0) {
//            throw new ModelManagerException("模型属性名称重复请重新输入");
//        }
//        //保存属性值相关信息
//        Integer propertiesValueId = null;
//        if (propertiesParam.getPropertiesValue() != null) {
//            PropertiesValueParam propertiesValueParam = propertiesParam.getPropertiesValue();
//            String dropOpStr = "";
//            if (propertiesValueParam.getDropOp() != null && propertiesValueParam.getDropOp().size() > 0) {
//                for (String str : propertiesValueParam.getDropOp()) {
//                    dropOpStr += str + ",";
//                }
//                propertiesValueParam.setDropOpStr(dropOpStr);
//            }
//            if (propertiesValueParam.getDropArrObj() != null && propertiesValueParam.getDropArrObj().size() > 0) {
//                propertiesValueParam.setDropArrObjStr(propertiesValueParam.getDropArrObj().toString());
//            }
//            String defaultValueListStr = "";
//            if (propertiesValueParam.getDefaultValueList() != null && propertiesValueParam.getDefaultValueList().size() > 0) {
//                for (String str : propertiesValueParam.getDefaultValueList()) {
//                    defaultValueListStr += str + ",";
//                }
//                propertiesValueParam.setDefaultValueListStr(defaultValueListStr);
//            }
//            if (propertiesValueParam.getGangedValueList() != null && propertiesValueParam.getGangedValueList().size() > 0) {
//                //最后加入空字符串，用户数据库like查询方便。
//                propertiesValueParam.getGangedValueList().add("");
//                String str = JSONArray.toJSONString(propertiesValueParam.getGangedValueList());
//                propertiesValueParam.setGangedValueListStr(str);
//            }
//            //有默认值时，才插入默认值数据
//            mwModelManageDao.insertPropertiesValueInfo(propertiesValueParam);
//            propertiesValueId = propertiesValueParam.getId();
//        }
//        //新增模型属性
//        if (countIndexId == 0 && countName == 0) {
//            propertiesParam.setPropertiesValueId(propertiesValueId);
//            if (Strings.isNullOrEmpty(propertiesParam.getPropertiesType().trim())) {
//                propertiesParam.setPropertiesType("默认属性");
//            }
//            mwModelManageDao.creatModelProperties(propertiesParam);
//        }
//        AddAndUpdateModelPageFieldParam param = new AddAndUpdateModelPageFieldParam();
//        param.setModelId(propertiesParam.getModelId());
//        param.setProp(propertiesParam.getIndexId());
//        param.setLabel(propertiesParam.getPropertiesName());
//        param.setOrderNumber(sort);
//        param.setVisible(propertiesParam.getIsShow());
//        param.setType(propertiesParam.getPropertiesTypeId());
//        param.setModelPropertiesId(propertiesParam.getPropertiesId());
//        //属性新增时，往mw_pagefield_table字段表中同步数据
//        mwModelManageDao.createModelPropertiesToPageField(param);
//
//        //mw_Customcol_table个性化字段表中同步初始数据
//        //获取用户表所有用户id
//        List<Integer> userIds = mwUserCommonService.selectAllUserId();
//        List<MwCustomcolTable> tableList = new ArrayList<>();
//        for (Integer userId : userIds) {
//            MwCustomcolTable table = new MwCustomcolTable();
//            table.setColId(param.getId());
//            table.setUserId(userId);
//            table.setSortable(true);
//            table.setWidth(null);
//            table.setVisible(propertiesParam.getIsShow());
//            table.setOrderNumber(sort);
//            table.setModelPropertiesId(propertiesParam.getPropertiesId());
//            tableList.add(table);
//        }
//        mwModelManageDao.insertPropertiesToCol(tableList);
//        //属性类型为6，结构体
//        if (propertiesParam.getPropertiesTypeId() == 6 && propertiesParam.getPropertiesStruct() != null) {
//            //模型属性结构体新增。
//            List<ModelPropertiesStructDto> structDtoList = getStructDtoList(propertiesParam);
//            if (structDtoList != null && structDtoList.size() > 0) {
//                mwModelManageDao.creatModelPropertiesStruct(structDtoList);
//            }
//        }
//
//        //查询该模型属性是否创建了实例，没有则在实例创建时设置es的数据类型
//        //创建了，则需要在新增属性的时候，往es中添加对应的数据类型。
//        List<Integer> instanceIds = mwModelManageDao.selectInstanceIdsByModelId(propertiesParam.getModelId());
//        if (instanceIds != null && instanceIds.size() > 0) {
//            String modelIndex = "";
//            if (Strings.isNullOrEmpty(propertiesParam.getModelIndex())) {
//                modelIndex = mwModelManageDao.selectModelIndexById(propertiesParam.getModelId());
//                propertiesParam.setModelIndex(modelIndex);
//            }
//            Integer type = propertiesParam.getPropertiesTypeId();
//            //时间类型
//            if (type == ModelPropertiesType.DATE.getCode()) {
//                //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
//                mwModelInstanceServiceImplV1.setESMappingByDate(propertiesParam.getModelIndex(), propertiesParam.getIndexId());
//            }
//            if (type == ModelPropertiesType.STRUCE.getCode()) {
//                //数据类型为结构体时，设置为es嵌套类型
//                mwModelInstanceServiceImplV1.setESMappingByStruct(propertiesParam.getModelIndex(), propertiesParam.getIndexId());
//            }
//        }
//        return Reply.ok();
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModelProperties(AddAndUpdateModelPropertiesParam propertiesParam) {
//        //page_filed表和costcol表数据同步修改
//        //保存属性值相关信息
//        Integer propertiesValueId = null;
//        if (Strings.isNullOrEmpty(propertiesParam.getPropertiesType().trim())) {
//            propertiesParam.setPropertiesType("默认属性");
//        }
//        if (propertiesParam.getPropertiesValue() != null) {
//            PropertiesValueParam propertiesValueParam = propertiesParam.getPropertiesValue();
//            propertiesValueParam.setPropertiesValueType(propertiesParam.getPropertiesTypeId());
//            String dropOpStr = "";
//            if (propertiesValueParam.getDropOp() != null && propertiesValueParam.getDropOp().size() > 0) {
//                for (String str : propertiesValueParam.getDropOp()) {
//                    dropOpStr += str + ",";
//                }
//                propertiesValueParam.setDropOpStr(dropOpStr);
//            }
//            if (propertiesValueParam.getDropArrObj() != null && propertiesValueParam.getDropArrObj().size() > 0) {
//                propertiesValueParam.setDropArrObjStr(propertiesValueParam.getDropArrObj().toString());
//            }
//            String defaultValueListStr = "";
//            if (propertiesValueParam.getDefaultValueList() != null && propertiesValueParam.getDefaultValueList().size() > 0) {
//                for (String str : propertiesValueParam.getDefaultValueList()) {
//                    defaultValueListStr += str + ",";
//                }
//                propertiesValueParam.setDefaultValueListStr(defaultValueListStr);
//            }
//            if (propertiesValueParam.getGangedValueList() != null && propertiesValueParam.getGangedValueList().size() > 0) {
//                //最后加入空字符串，用户数据库like查询方便。
//                propertiesValueParam.getGangedValueList().add("");
//                String str = JSONArray.toJSONString(propertiesValueParam.getGangedValueList());
//                propertiesValueParam.setGangedValueListStr(str);
//            }
//            propertiesParam.setPropertiesValue(propertiesValueParam);
//            if (propertiesValueParam.getId() == null) {
//                //PropertiesValueId为null时，插入属性值数据
//                mwModelManageDao.insertPropertiesValueInfo(propertiesValueParam);
//                propertiesValueId = propertiesValueParam.getId();
//                propertiesParam.setPropertiesValueId(propertiesValueId);
//            }
//        }
//        //修改结构体数据,先删除，在新增
//        if (propertiesParam.getPropertiesStruct() != null && propertiesParam.getPropertiesStruct().size() > 0) {
//            mwModelManageDao.deletePropertiesStruct(propertiesParam.getModelId(), propertiesParam.getIndexId());
//            List<ModelPropertiesStructDto> structDtoList = getStructDtoList(propertiesParam);
//            if (structDtoList != null && structDtoList.size() > 0) {
//                mwModelManageDao.creatModelPropertiesStruct(structDtoList);
//            }
//        }
//        mwModelManageDao.updateModelProperties(propertiesParam);
//
//        return Reply.ok();
//    }
//
//    @Override
//    public Reply updateAllModelProperties() {
//        return null;
//    }
//
//    @Override
//    public Reply updateModelPropertiesByGroup(AddAndUpdateModelGroupParam param) {
//        return null;
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
//            for (AddAndUpdateModelPropertiesParam propertiesParam : propertiesParamList) {
//                if (propertiesParam.getPropertiesLevel() != null && propertiesParam.getPropertiesLevel() == 0) {
//                    throw new ModelManagerException("内置属性不可删除");
//                }
//                //判断该模型是否有实例，如果有实例需要删除所有实例之后才能删除模型属性
////        int count = mwModelManageDao.selectCountInstanceBymodelId(propertiesParam.getModelId());
//////        if (count != 0) {
//////            throw new ModelManagerException("该属性被实例使用，需要先删除实例");
//////        }
//                mwModelManageDao.deleteModelPropertiesByPropertiesId(propertiesParam.getPropertiesId());
//            }
//        }
//
//
//        return Reply.ok();
//    }
//
//    @Override
//    @Transactional
//    public Reply updateModelPropertiesShowStatus(EditorPropertiesNewParam propertiesParam) {
//
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
//            List<MWModelPropertiesInfoDto> mapList = mwModelManageDao.getPropertiesInfoByModelId(param.getModelId());
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
//            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
//            Map priCriteria = PropertyUtils.describe(param);
//            List<ModelPropertiesDto> list = mwModelManageDao.selectModelPropertiesList(priCriteria);
//            List<ModelPropertiesStructDto> structList = new ArrayList<>();
//            for (ModelPropertiesDto dto : list) {
//                if (dto.getPropertiesValue() != null) {
//                    PropertiesValueParam pv = dto.getPropertiesValue();
//                    if (!Strings.isNullOrEmpty(pv.getDropOpStr())) {
//                        pv.setDropOp(Arrays.asList(pv.getDropOpStr().split(",")));
//                    }
//                    if (!Strings.isNullOrEmpty(pv.getDropArrObjStr())) {
//                        List lists = JSONArray.parseArray(pv.getDropArrObjStr());
//                        pv.setDropArrObj(lists);
//                    }
//                    if (!Strings.isNullOrEmpty(pv.getDefaultValueListStr())) {
//                        pv.setDefaultValueList(Arrays.asList(pv.getDefaultValueListStr().split(",")));
//                    }
//                    if (!Strings.isNullOrEmpty(pv.getGangedValueListStr())) {
//                        List list1 = JSONArray.parseArray(pv.getGangedValueListStr());
//                        list1.remove("");
//                        pv.setGangedValueList(list1);
//                    }
//                } else {
//                    PropertiesValueParam pv = new PropertiesValueParam();
//                    dto.setPropertiesValue(pv);
//                }
//                //属性类型为结构体
//                if (dto.getPropertiesTypeId() == ModelPropertiesType.STRUCE.getCode()) {
//                    //根据modelId和属性IndexId，查询属性结构体信息
//                    structList = mwModelManageDao.getProperticesStructInfo(dto.getModelId(), dto.getIndexId());
//                    if (structList != null) {
//                        for (ModelPropertiesStructDto mps : structList) {
//                            //结构体数据类型为9和10时，代表的数据类型为数组格式，需要转换
//                            if (mps.getStructType() != null && (mps.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || mps.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode())) {
//                                if (!Strings.isNullOrEmpty(mps.getStructStrValue())) {
//                                    mps.setStructListValue(Arrays.asList(mps.getStructStrValue().split(",")));
//                                }
//                            }
//                        }
//                        dto.setPropertiesStruct(structList);
//                    } else {
//                        dto.setPropertiesStruct(structList);
//                    }
//                }
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
//            List<String> list = mwModelManageDao.queryPropertiesTypeList(param);
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
//            GetIndexRequest request = new GetIndexRequest("*");
//            //判断索引是否存在
//            GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
//            String[] indices = response.getIndices();
//            List<String> list = Arrays.asList(indices);
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
//        List<AddAndUpdateModelParam> list = mwModelManageDao.queryModelIndexList();
//        //获取es中没有而mysql存在的脏数据
//        List<AddAndUpdateModelParam> reduceList = list.stream().filter(item -> !esIndexList.contains(item.getModelIndex())).collect(Collectors.toList());
//        if (reduceList.size() > 0) {
//            try {
//                for (AddAndUpdateModelParam dto : reduceList) {
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
//            List<AddAndUpdateModelParam> list = mwModelManageDao.queryModelIndexList();
//            //获取es中没有而mysql存在的数据
//            List<AddAndUpdateModelParam> reduceList = list.stream().filter(item -> !esIndexList.contains(item.getModelIndex())).collect(Collectors.toList());
//            //将数据同步至ES中
//            for (AddAndUpdateModelParam addAndUpdateModelParam : reduceList) {
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
//            List<MWModelPropertiesInfoDto> list = mwModelManageDao.selectPropertiesFieldByGanged(param.getModelId());
//            if (list != null && list.size() > 0) {
//                for (MWModelPropertiesInfoDto m : list) {
//                    MWModelPropertiesGangedDto gangedDto = new MWModelPropertiesGangedDto();
//                    List lists = new ArrayList();
//                    if (String.valueOf(ModelPropertiesType.MONITOR_MODE.getCode()).equals(m.getPropertiesTypeId()) ||
//                            String.valueOf(ModelPropertiesType.RELATION_ENUM.getCode()).equals(m.getPropertiesTypeId())) {
//                        lists = JSONArray.parseArray(m.getDropOP());
//                    } else {
//                        lists = Arrays.asList(m.getDropOP().split(","));
//                    }
//                    gangedDto.setLabel(m.getPropertiesName());
//                    gangedDto.setValue(m.getIndexId());
//                    gangedDto.setChildren(lists);
//                    gangeList.add(gangedDto);
//                }
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
