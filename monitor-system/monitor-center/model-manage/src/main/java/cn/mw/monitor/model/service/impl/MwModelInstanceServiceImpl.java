package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dao.MwModelSysLogDao;
import cn.mw.monitor.model.dao.MwModelViewDao;
import cn.mw.monitor.model.data.AddUpdModelInstanceContext;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelSysLogService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelAssetsByESService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.systemLog.api.MwSysLogService;
import cn.mw.monitor.service.systemLog.dto.SysLogDTO;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import cn.mw.monitor.service.model.param.MwSyncZabbixAssetsParam;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2021/2/25 9:10
 */
//@Service
@Slf4j
public class MwModelInstanceServiceImpl implements MwModelInstanceService, ModelServer {
    //操作日志记录
    private static final Logger mwlogger = LoggerFactory.getLogger("MWDBLogger");
    //位异或密钥
    private static final int KEY = 5;


    //数组类型
    static List typeList = Arrays.asList(
            ModelPropertiesType.STRUCE.getCode(),
            ModelPropertiesType.MULTIPLE_ENUM.getCode(),
            ModelPropertiesType.MULTIPLE_RELATION.getCode(),
            ModelPropertiesType.ORG.getCode(),
            ModelPropertiesType.USER.getCode(),
            ModelPropertiesType.GROUP.getCode(),
            ModelPropertiesType.LAYOUTDATA.getCode());

    //字符串类型
    static List arrList = Arrays.asList(
            ModelPropertiesType.STRING.getCode(),
            ModelPropertiesType.SINGLE_RELATION.getCode(),
            ModelPropertiesType.IP.getCode(),
            ModelPropertiesType.SINGLE_ENUM.getCode());
    @Value("${es.duration.timeNum}")
    private int timeNum;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private LicenseManagementService licenseManagement;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Resource
    private MwModelSysLogDao mwModelSysLogDao;
    @Resource
    private MwModelSysLogService MwModelSysLogService;
    @Resource
    private MwModelAssetsDiscoveryServiceImpl mwModelAssetsDiscoveryServiceImpl;
    @Value("${System.isFlag}")
    private Boolean isFlag;
    @Resource
    private MwModelViewDao mwModelViewDao;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private MwModelManageServiceImplV1 mwModelManageService;

    @Autowired
    private MWUserService userService;

    //    @Value("${model-image.upload.path}")
//    private String imgPath;
//图片上传目录
//      static final String MODULE = "upload";
    @Value("${file.url}")
    private String imgPath;

    static final String MODULE = "file-upload";

    //每次滚动查询es数据的条数
    static final int scrollSize = 200;

    private int pageSize = 10000;
    private int pageFrom = 0;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MwSysLogService mwSysLogService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwModelViewService mwModelViewService;
    @Autowired
    private MwModelAssetsByESService mwModelAssetsByESService;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Reply creatModelInstance(Object instanceParam, Integer types) {
        AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
        try {
            if (types == 0) {
                param = (AddAndUpdateModelInstanceParam) instanceParam;
            } else {
                param = JSONObject.parseObject(instanceParam.toString(), AddAndUpdateModelInstanceParam.class);
            }
            //将实例的名称、modelId存入数据库

            mwModelManageDao.creatModelInstance(param);
            //不是西藏邮储环境,添加负责人权限
            if (!isFlag) {
                //设置负责人，用户组，机构/部门
                ModelPermControlParam controlParam = new ModelPermControlParam();
                controlParam.setUserIds(param.getUserIds());
                controlParam.setOrgIds(param.getOrgIds());
                controlParam.setGroupIds(param.getGroupIds());
                controlParam.setId(String.valueOf(param.getInstanceId()));
                controlParam.setType(DataType.INSTANCE_MANAGE.getName());
                controlParam.setDesc(DataType.INSTANCE_MANAGE.getDesc());
                mwModelManageService.addMapperAndPerm(controlParam);
            }
            param.setTargetInstanceId(param.getInstanceId());
            MwModelInfoDTO dto = new MwModelInfoDTO();
            if (param.getModelId() != null) {
                dto = mwModelInstanceDao.getModelIndexInfo(param.getModelId());
                if (dto != null && dto.getModelIndex() != null) {
                    param.setModelIndex(dto.getModelIndex());
                }
                if (dto != null && dto.getModelName() != null) {
                    param.setModelName(dto.getModelName());
                }
            }
            AddAndUpdateModelInstanceParam finalParam = param;
            param.getPropertiesList().forEach(properties -> {
                if (properties.getPropertiesType() != null) {
                    Integer type = properties.getPropertiesType();
                    //时间类型
                    if (type == ModelPropertiesType.STRING.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为字符串
                            setESMappingByString(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    //时间类型
                    if (type == ModelPropertiesType.DATE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                            setESMappingByDate(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.STRUCE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为结构体时，设置为es嵌套类型
                            setESMappingByStruct(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.SWITCH.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型开关型，设置为esBoolean
                            setESMappingByBoolean(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                }
            });
            addInstanceToEs(param);
            //是否纳管资产
            if (param.isManage()) {
                param.getManageParam().setAssetsTypeId(param.getModelGroupId());
                param.getManageParam().setAssetsTypeSubId(param.getModelId());
                param.getManageParam().setInstanceId(param.getInstanceId());
                param.getManageParam().setInstanceName(param.getInstanceName());
                mwModelAssetsByESService.doInsertAssetsToESByView(param.getManageParam(), false);
            }

            //是否同步，添加同步信息
            if (param.isSync()) {
                mwModelViewService.saveAuthInfoByModel(param.getSyncParams());
//                RSAUtils.decryptData(param.getSyncParam().getMwMacrosDTO().get(1).getValue(),RSAUtils.privateKey);
            }
            //模型实例变更记录
//        Integer version = mwModelSysLogDao.getChangeHistoryVersion("instance_" + param.getInstanceId());
            if (!Strings.isNullOrEmpty(param.getTargetModelName())) {
                //修改转移前实例的历史记录
                //添加到模型管理日志
                MwModelSysLogService.updateInstaceChangeHistory("instance_" + param.getInstanceId(), "instance_" + param.getOwnInstanceId());
                //转移记录
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.SHIFT_INSTANCE.getName())
                        .objName(param.getModelName() == null ? param.getInstanceName() : param.getModelName() + "/" + param.getInstanceName())
                        .operateDes(OperationTypeEnum.SHIFT_INSTANCE.getName() + ":" + param.getInstanceName() + "：从" + param.getOwnGroupName() + "/" + param.getOwnModelName() + "转移到" + param.getTargetGroupName() + "/" + param.getTargetModelName())
                        .operateDesBefore("").type("instance_" + param.getInstanceId()).version(1).build();
                //添加到系统操作日志
                mwlogger.info(JSON.toJSONString(builder));
                //添加到模型管理日志
                MwModelSysLogService.saveInstaceChangeHistory(builder);
            } else {
                //新增记录
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_INSTANCE.getName())
                        .objName(param.getModelName() == null ? param.getInstanceName() : param.getModelName() + "/" + param.getInstanceName())
                        .operateDes(OperationTypeEnum.CREATE_INSTANCE.getName() + ":" + param.getInstanceName()).operateDesBefore("").type("instance_" + param.getInstanceId()).version(1).build();
                //添加到系统操作日志
                mwlogger.info(JSON.toJSONString(builder));
                //添加到模型管理日志
                MwModelSysLogService.saveInstaceChangeHistory(builder);
            }
        } catch (Throwable throwable) {
            try {
                deleteEsInfoByQuery(Arrays.asList(param.getModelIndex()), Arrays.asList(param.getInstanceId()));
            } catch (Exception e) {
                log.error("新增实例出错时删除es数据失败");
            }
            log.error("fail to creatModelInstance with auParam={}, cause:{}", param, throwable.getMessage());
            throw new RuntimeException(throwable.getMessage());
        }
        return Reply.ok(param.getInstanceId());
    }

    private void addInstanceToEs(AddAndUpdateModelInstanceParam param) {
        SimpleDateFormat UTC2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UTC2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        HashMap<String, Object> hashMap = new HashMap<>();
        //设置基础属性字段。
        hashMap.put("modelId", param.getModelId());
        hashMap.put(MwModelViewCommonService.INSTANCE_ID_KEY, param.getInstanceId());
        hashMap.put("modelIndex", param.getModelIndex());
        //设置修改人修改时间。
        hashMap.put("modifier", iLoginCacheInfo.getLoginName());
        hashMap.put("modificationDate", DateUtils.formatDateTime(new Date()));
        //设置创建人创建时间。
        hashMap.put("creator", iLoginCacheInfo.getLoginName());
        hashMap.put("createDate", DateUtils.formatDateTime(new Date()));
        //获取所有模型的groupNodes
        List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
        Map<String, String> groupNodeMap = new HashMap();
        for (MwModelManageDTO manageDTO : groupNodeList) {
            groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
        }
        String groupNodes = groupNodeMap.get(param.getModelIndex());
        hashMap.put("groupNodes", groupNodes);
        String groupNodeId = "";
        //截取groupNodes最后一位作为模型的父节点id
        if (groupNodes.split(",").length > 1) {
            groupNodeId = groupNodes.split(",")[groupNodes.split(",").length - 1];
        }
        hashMap.put("assetsTypeSubId", param.getModelId() + "");
        hashMap.put("assetsTypeId", groupNodeId);
        List<Integer> userId = new ArrayList<>();
        List<List<Integer>> orgId = new ArrayList<>();
        List<Integer> groupId = new ArrayList<>();

        List<Integer> coordinate = new ArrayList<>();
        List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
        List<Integer> reationInstanceIds = new ArrayList<>();
        param.getPropertiesList().forEach(properties -> {
                    if (properties.getPropertiesType() != null && (!Strings.isNullOrEmpty(properties.getPropertiesValue()))) {
                        if (properties.getPropertiesType() != null) {
                            Integer type = properties.getPropertiesType();
                            if (typeList.contains(type)) {//type类型为10、11、12、13,16都为数组类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    hashMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.STRUCE.getCode()) {//type类型6 为结构体类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    hashMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.DATE.getCode()) {//type类型8 为时间类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    hashMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                                }
                            } else if (type == ModelPropertiesType.SWITCH.getCode()) {//type类型17 为开关型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    hashMap.put(properties.getPropertiesIndexId(), Boolean.valueOf(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.PASSWORD.getCode()) {//type类型19 为密码
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    hashMap.put(properties.getPropertiesIndexId(), RSAUtils.encryptData(properties.getPropertiesValue(),RSAUtils.RSA_PUBLIC_KEY));
                                }
                            } else {
                                hashMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                            }
                            //西藏邮储环境，添加资产模型权限控制
                            if (isFlag) {
                                if (type == ModelPropertiesType.ORG.getCode()) {//type类型 11 机构/部门
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        List<List<Integer>> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                        orgId.addAll(list);
                                    }
                                } else if (type == ModelPropertiesType.USER.getCode()) {//type类型12 负责人
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        List<Integer> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                        userId.addAll(list);
                                    }
                                } else if (type == ModelPropertiesType.GROUP.getCode()) {//type类型13 用户组
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        groupId.addAll((List<? extends Integer>) JSONArray.parse(properties.getPropertiesValue()));
                                    }
                                }
                            }
                            if (param.getModelViewType() != null) {
                                if (param.getModelViewType() == 1 || param.getModelViewType() == 2) {
                                    if (type == ModelPropertiesType.LAYOUTDATA.getCode() && "position".equals(properties.getPropertiesIndexId())) {
                                        //type类型为16，获取机柜的位置数据
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            coordinate.addAll((List) JSONArray.parse(properties.getPropertiesValue()));
                                        }
                                    }
                                    if (type == ModelPropertiesType.SINGLE_RELATION.getCode() && "relationSite".equals(properties.getPropertiesIndexId())) {
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            Integer instanceId = Integer.valueOf(properties.getPropertiesValue());
                                            reationInstanceIds.add(instanceId);
                                        }
                                    }
                                    if (type == ModelPropertiesType.LAYOUTDATA.getCode() && "positionByCabinet".equals(properties.getPropertiesIndexId())) {
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            CabinetLayoutDataParam cdParam = JSONObject.parseObject(properties.getPropertiesValue(), CabinetLayoutDataParam.class);
                                            QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                            assetsListParam.setAssetsId(param.getInstanceId() + "");
                                            assetsListParam.setAssetsName(param.getInstanceName());
                                            cdParam.setInfo(assetsListParam);
                                            cdParamList.add(cdParam);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        );

        if (param.getModelViewType() != null) {
            if (param.getModelViewType() == 1) {
                //机房视图；修改机房布局
                QueryBatchSelectDataParam qparam = new QueryBatchSelectDataParam();
                List<QuerySelectDataListParam> paramList = new ArrayList<>();
                QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                //外部关联的实例Id
                if (reationInstanceIds != null && reationInstanceIds.size() > 0) {
                    dataParam.setInstanceId(reationInstanceIds.get(0));
                }
                //外部关联的modelIndex
                dataParam.setModelIndex(param.getRelationModelIndex());
                dataParam.setCoordinate(coordinate);
                paramList.add(dataParam);
                qparam.setLayoutDataList(paramList);
                updateRoomLayout(qparam);
            }
            if (param.getModelViewType() == 2) {
                //机柜视图；修改机柜布局
                QueryCabinetLayoutListParam qparam = new QueryCabinetLayoutListParam();
                List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
                QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                //外部关联的实例Id
                if (reationInstanceIds != null && reationInstanceIds.size() > 0) {
                    clParam.setInstanceId(reationInstanceIds.get(0));
                }
                //外部关联的modelIndex
                clParam.setModelIndex(param.getRelationModelIndex());
                if (cdParamList != null && cdParamList.size() > 0) {
                    clParam.setCurrentData(cdParamList.get(0));
                }
                clParamList.add(clParam);
                qparam.setCabinetLayoutList(clParamList);
                updateCabinetLayout(qparam);
            }
        }


        //西藏邮储环境，添加资产模型权限控制
        if (isFlag) {
            //设置负责人，用户组，机构/部门
            ModelPermControlParam controlParam = new ModelPermControlParam();
            controlParam.setUserIds(userId);
            controlParam.setOrgIds(orgId);
            controlParam.setGroupIds(groupId);
            controlParam.setId(String.valueOf(param.getInstanceId()));
            controlParam.setType(DataType.INSTANCE_MANAGE.getName());
            controlParam.setDesc(DataType.INSTANCE_MANAGE.getDesc());
            mwModelManageService.addMapperAndPerm(controlParam);
        }
        BulkRequest bulkRequest = new BulkRequest();
        JSONObject json = (JSONObject) JSONObject.toJSON(hashMap);
        IndexRequest newRequest = new IndexRequest(param.getModelIndex()).id(param.getModelIndex() + param.getInstanceId()).source(json);
        bulkRequest.add(newRequest);
        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                if (bulkItemResponses.hasFailures()) {
                    //新增失败时，删除插入数据库的实例数据
                    mwModelManageDao.deleteModelInstances(Arrays.asList(param.getInstanceId()));
                    log.error("异步执行批量添加模型insertModelInstanceProperties属性到es中失败");
                    throw new ModelManagerException("异步执行批量添加模型属性到es中失败");
                }
            }

            @Override
            public void onFailure(Exception e) {
                //新增失败时，删除插入数据库的实例数据
                mwModelManageDao.deleteModelInstances(Arrays.asList(param.getInstanceId()));
                log.error("异步执行批量添加模型属性到es中失败");
                throw new ModelManagerException("异步执行批量添加模型属性到es中失败");
            }
        });
    }

    @Override
    @Transactional
    public Reply updateModelInstance(Object instanceParams, Integer types) {
        try {
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            List<Integer> userId = new ArrayList<>();
            List<List<Integer>> orgId = new ArrayList<>();
            List<Integer> groupId = new ArrayList<>();

            if (types == 0) {
                instanceParam = (AddAndUpdateModelInstanceParam) instanceParams;
            } else {
                instanceParam = JSONObject.parseObject(instanceParams.toString(), AddAndUpdateModelInstanceParam.class);
            }
            AddAndUpdateModelInstanceParam finalParam = instanceParam;
            //修改数据库中实例名称
            if (instanceParam.getInstanceId() != null) {
                mwModelInstanceDao.updataInstanceName(instanceParam.getInstanceName(), instanceParam.getInstanceId());
            }
            for (AddModelInstancePropertiesParam properties : instanceParam.getPropertiesList()) {
                if (properties.getPropertiesType() != null) {
                    Integer type = properties.getPropertiesType();
                    //时间类型
                    if (type == ModelPropertiesType.DATE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                            setESMappingByDate(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.STRUCE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为结构体时，设置为es嵌套类型
                            setESMappingByStruct(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.SWITCH.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型开关型，设置为esBoolean
                            setESMappingByBoolean(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                }
            }
            List<Integer> coordinate = new ArrayList<>();
            List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
            List<Integer> reationInstanceIds = new ArrayList<>();
            UpdateRequest updateRequest = new UpdateRequest(instanceParam.getModelIndex(), instanceParam.getEsId());
            updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            Map<String, Object> jsonMap = new HashMap<>();
            instanceParam.getPropertiesList().forEach(properties -> {
                        if (properties.getPropertiesType() != null) {
                            Integer type = properties.getPropertiesType();
                            if (typeList.contains(type)) {//type类型为10、11、12、13,16都为数组类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.STRUCE.getCode()) {//type类型6 为结构体类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.DATE.getCode()) {//type类型8 为时间类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                                }
                            } else if (type == ModelPropertiesType.SWITCH.getCode()) {//type类型17 为布尔类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), Boolean.valueOf(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.PASSWORD.getCode()) {//type类型19 为密码
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {

                                    jsonMap.put(properties.getPropertiesIndexId(), RSAUtils.encryptData(properties.getPropertiesValue(),RSAUtils.RSA_PUBLIC_KEY));
                                    ;
                                }
                            } else {
                                jsonMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                            }

                            //西藏邮储环境，添加资产模型权限控制
                            if (isFlag) {
                                if (type == ModelPropertiesType.ORG.getCode()) {//type类型 11 机构/部门
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        List<List<Integer>> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                        orgId.addAll(list);
                                    }
                                } else if (type == ModelPropertiesType.USER.getCode()) {//type类型12 负责人
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        List<Integer> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                        userId.addAll(list);
                                    }
                                } else if (type == ModelPropertiesType.GROUP.getCode()) {//type类型13 用户组
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        groupId.addAll((List<? extends Integer>) JSONArray.parse(properties.getPropertiesValue()));
                                    }
                                }
                            }

                            if (finalParam.getModelViewType() != null) {
                                if (finalParam.getModelViewType() == 1 || finalParam.getModelViewType() == 2) {
                                    if (type == ModelPropertiesType.LAYOUTDATA.getCode() && "position".equals(properties.getPropertiesIndexId())) {
                                        //type类型为16，获取机柜的位置数据
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            coordinate.addAll((List) JSONArray.parse(properties.getPropertiesValue()));
                                        }
                                    }
                                    if (type == ModelPropertiesType.SINGLE_RELATION.getCode() && "relationSite".equals(properties.getPropertiesIndexId())) {
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            Integer instanceId = Integer.valueOf(properties.getPropertiesValue());
                                            reationInstanceIds.add(instanceId);
                                        }
                                    }
                                    if (type == ModelPropertiesType.LAYOUTDATA.getCode() && "positionByCabinet".equals(properties.getPropertiesIndexId())) {
                                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                            CabinetLayoutDataParam cdParam = JSONObject.parseObject(properties.getPropertiesValue(), CabinetLayoutDataParam.class);
                                            QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                            assetsListParam.setAssetsId(finalParam.getInstanceId() + "");
                                            assetsListParam.setAssetsName(finalParam.getInstanceName());
                                            cdParam.setInfo(assetsListParam);
                                            cdParamList.add(cdParam);
                                        }
                                    }
                                }
                            }

                        }
                    }
            );
            //设置修改人修改时间。
            jsonMap.put("modifier", iLoginCacheInfo.getLoginName());
            jsonMap.put("modificationDate", DateUtils.formatDateTime(new Date()));
            updateRequest.doc(jsonMap);
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            RestStatus status = update.status();
            if (status.getStatus() == 200) {
                //模型实例变更记录
                Integer version = mwModelSysLogDao.getChangeHistoryVersion("instance_" + instanceParam.getInstanceId());
                if (version != null) {
                    version = version + 1;
                } else {
                    version = 1;
                }
                if (!Strings.isNullOrEmpty(instanceParam.getOperateDes())) {
                    SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.EDITOR_INSTANCE.getName())
                            .objName(instanceParam.getModelName() == null ? instanceParam.getInstanceName() : instanceParam.getModelName() + "/" + instanceParam.getInstanceName())
                            .operateDes(OperationTypeEnum.EDITOR_INSTANCE.getName() + ":" + instanceParam.getOperateDes()).operateDesBefore(instanceParam.getOperateDesBefore()).type("instance_" + instanceParam.getInstanceId()).version(version).build();
                    //添加到系统操作日志
                    mwlogger.info(JSON.toJSONString(builder));
                    //添加到模型管理日志
                    MwModelSysLogService.saveInstaceChangeHistory(builder);
                }
                if (isFlag) {
                    instanceParam.setUserIds(userId);
                    instanceParam.setGroupIds(groupId);
                    instanceParam.setOrgIds(orgId);
                }
                //对用户名、机构、用户组修改
                ModelPermControlParam param = new ModelPermControlParam();
                param.setType(DataType.INSTANCE_MANAGE.getName());
                param.setUserIds(instanceParam.getUserIds());
                param.setOrgIds(instanceParam.getOrgIds());
                param.setGroupIds(instanceParam.getGroupIds());
                param.setId(String.valueOf(instanceParam.getInstanceId()));
                param.setDesc(DataType.INSTANCE_MANAGE.getDesc());
                //先删除后新增
                mwModelManageService.deleteMapperAndPerm(param);
                mwModelManageService.addMapperAndPerm(param);

                if (instanceParam.getModelViewType() != null && (instanceParam.getModelViewType() == 1 || instanceParam.getModelViewType() == 2)) {


                    List<String> fieldList = Arrays.asList("position", "positionByCabinet");
                    List<Map<String, Object>> roomLayout = getModelInstanceInfoByEs(instanceParam.getModelIndex(), instanceParam.getInstanceId(), fieldList);
                    //机房机柜修改之前的位置数据
                    CabinetLayoutDataParam lastData = new CabinetLayoutDataParam();
                    List<Integer> beforeCoordinate = new ArrayList<>();
                    for (Map<String, Object> map : roomLayout) {
                        if (map != null && map.get("position") != null) {
                            Object obj = map.get("position");
                            beforeCoordinate = (List<Integer>) JSONArray.parse(obj.toString());
                        }
                        if (map != null && map.get("positionByCabinet") != null) {
                            Object obj = map.get("positionByCabinet");
                            lastData = JSONObject.parseObject(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                        }
                    }
                    if (instanceParam.getModelViewType() == 1) {
                        //机房视图；修改机房布局
                        QueryBatchSelectDataParam qparam = new QueryBatchSelectDataParam();
                        List<QuerySelectDataListParam> paramList = new ArrayList<>();
                        QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                        //外部关联的实例Id
                        if (reationInstanceIds != null && reationInstanceIds.size() > 0) {
                            dataParam.setInstanceId(reationInstanceIds.get(0));
                        }
                        //修改之前的位置信息
                        if (beforeCoordinate != null && beforeCoordinate.size() > 0) {
                            dataParam.setBeforeCoordinate(beforeCoordinate);
                        }
                        //外部关联的modelIndex
                        dataParam.setModelIndex(instanceParam.getRelationModelIndex());
                        //当前的位置信息
                        dataParam.setCoordinate(coordinate);
                        paramList.add(dataParam);
                        qparam.setLayoutDataList(paramList);
                        updateRoomLayout(qparam);
                    }
                    if (instanceParam.getModelViewType() == 2) {
                        //机柜视图；修改机柜布局
                        QueryCabinetLayoutListParam qparam = new QueryCabinetLayoutListParam();
                        List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
                        QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                        //外部关联的实例Id
                        if (reationInstanceIds != null && reationInstanceIds.size() > 0) {
                            clParam.setInstanceId(reationInstanceIds.get(0));
                        }
                        //外部关联的modelIndex
                        clParam.setModelIndex(instanceParam.getRelationModelIndex());
                        if (cdParamList != null && cdParamList.size() > 0) {
                            clParam.setCurrentData(cdParamList.get(0));
                        }
                        clParam.setLastData(lastData);
                        clParamList.add(clParam);
                        qparam.setCabinetLayoutList(clParamList);
                        updateCabinetLayout(qparam);
                    }
                }
                return Reply.ok();
            } else {
                return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313007, ErrorConstant.MODEL_INSTANCE_MSG_313007);
            }
        } catch (Exception e) {
            log.error("fail to updateModelInstance param{}, case by {}", instanceParams, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313007, ErrorConstant.MODEL_INSTANCE_MSG_313007);
        }
    }

    @Override
    public Reply selectModelInstanceFiledList(QueryCustomModelCommonParam queryCustomModelParam) {

        return null;
    }
    @Override
    public Reply editorData(List<AddAndUpdateModelInstanceParam> instanceParams) {
        return null;
    }

    /**
     * 模型实例信息查看
     *
     * @param instanceParam
     * @return
     */
    @Override
    public Reply lookModelInstance(AddAndUpdateModelInstanceParam instanceParam) {
        QueryModelInstanceParam param = new QueryModelInstanceParam();
        param.setModelIndex(instanceParam.getModelIndex());
        param.setModelId(instanceParam.getModelId());
        param.setModelInstanceId(instanceParam.getInstanceId());
        //根据实例Id查询该es中实例的详细数据
        List<Map<String, Object>> listMap = getInstanceInfoByModelId(param);

        //根据modelId获取属性的分类
        List<String> propertiesTypeList = mwModelInstanceDao.getModelPropertiesType(instanceParam.getModelId());
        List<Map> propertiesNameList = mwModelInstanceDao.getPropertiesNameByModelId(instanceParam.getModelId());
        Map<String, Map> maps = new HashMap();
        if (propertiesNameList != null && propertiesNameList.size() > 0) {
            for (Map m : propertiesNameList) {
                if (m != null && m.get("indexId") != null) {
                    maps.put(m.get("indexId").toString(), m);
                }
            }
        }
        Map<String, List> datas = new LinkedHashMap<>();

        List<MWModelInstanceFiled> instanceFiledList = new ArrayList<>();
        for (String type : propertiesTypeList) {
            //根据类型和modelId查询不同属性分类下的属性。
            List<ModelPropertiesDto> list = mwModelInstanceDao.getModelPropertiesByType(instanceParam.getModelId(), type);
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            for (Map data : listMap) {
                List<MWModelInstancePropertiesFiledDTO> lists = new ArrayList();
                List<MWModelInstancePropertiesFiledDTO> listSort = new ArrayList();
                for (ModelPropertiesDto m : list) {
                    MWModelInstancePropertiesFiledDTO dto = new MWModelInstancePropertiesFiledDTO();
                    String propertiesIndexId = m.getIndexId();
                    dto.setId(propertiesIndexId);
                    if (maps.get(propertiesIndexId) != null) {
                        Map infoMap = maps.get(propertiesIndexId);
                        dto.setName(infoMap.get("name") != null ? infoMap.get("name").toString() : "");
                        dto.setType(infoMap.get("type") != null ? Integer.valueOf(infoMap.get("type").toString()) : 1);
                        if (data.get(propertiesIndexId) != null) {
                            dto.setValue(data.get(propertiesIndexId));
                        } else {
                            if (typeList.contains(m.getPropertiesTypeId())) {
                                dto.setValue(new ArrayList<>());
                            } else {
                                dto.setValue("");
                            }

                        }
                        if (m.getPropertiesTypeId() == ModelPropertiesType.RELATION_ENUM.getCode()) {
                            String arrObjStr = mwModelManageDao.getProperticesArrObj(instanceParam.getModelId(), m.getIndexId());
                            if (!Strings.isNullOrEmpty(arrObjStr)) {
                                dto.setDataArrObj(JSONArray.parseArray(arrObjStr));
                            } else {
                                dto.setDataArrObj(new ArrayList<>());
                            }
                        }
                        List<ModelPropertiesStructDto> structList = new ArrayList<>();
                        if (m.getPropertiesTypeId() == ModelPropertiesType.STRUCE.getCode()) {
                            structList = mwModelManageDao.getProperticesStructInfo(instanceParam.getModelId(), m.getIndexId());
                            dto.setPropertiesStruct(structList);
                            //将类型为结构体的数据另存入一个list中
                            listSort.add(dto);
                        } else if (m.getPropertiesTypeId() == ModelPropertiesType.LAYOUTDATA.getCode()) {
                            //存入布局数据
                            listSort.add(dto);
                        } else {
                            dto.setPropertiesStruct(structList);
                            lists.add(dto);
                        }
                    }
                }
                //结构体数据放入list的最后面。方便前端排序
                lists.addAll(listSort);
                MWModelInstanceFiled instanceFiled = new MWModelInstanceFiled();
                instanceFiled.setType(type);
                instanceFiled.setFiledDTOS(lists);
                instanceFiledList.add(instanceFiled);
            }
        }
        return Reply.ok(instanceFiledList);
    }

    @Override
    public Reply updateSyncZabbixName(MwSyncZabbixAssetsParam param) {
        String hostId = param.getHostId();
        String assetsName = param.getInstanceName();
        Integer serverId = param.getMonitorServerId();
        if (StringUtils.isNotBlank(hostId) && StringUtils.isNotBlank(assetsName)) {
            //调用zabbix接口根据主机ID修改可见名称
            MWZabbixAPIResult result = mwtpServerAPI.hostUpdateSoName(serverId, hostId, assetsName);
            if (result != null && !result.isFail()) {
                return Reply.ok("同步zabbix资产可见名称成功");
            }
            //如果名称已重复，则在实例名称后添加4个随机的数字字母。
            if (result.getData().toString().endsWith('"' + assetsName + '"' + " already exists.")) {
                assetsName = param.getInstanceName() + "_" + UuidUtil.get16Uid();
                MWZabbixAPIResult result1 = mwtpServerAPI.hostUpdateSoName(serverId, hostId, assetsName);
                if (result1 != null && !result1.isFail()) {
                    return Reply.ok("同步zabbix资产可见名称成功");
                }
            }
        }
        return Reply.fail(500, "同步zabbix资产可见名称失败");
    }

    @Override
    public Reply batchUpdateSyncZabbixName(List<MwSyncZabbixAssetsParam> paramList) {
        return null;
    }

    @Override
    public Reply batchUpdatePowerByEs() {
        return null;
    }

    @Override
    public Reply batchUpdatePower(BatchUpdatePowerParam param) {
        return null;
    }

    @Override
    public Reply updatePollingEngine(UpdatePollingEngineParam param) {
        return null;
    }



    /**
     * 流程模型实例单个信息查看
     *
     * @param instanceParam
     * @return
     */
    @Override
    public Reply lookModelInstanceByAction(AddAndUpdateModelInstanceParam instanceParam) {
        Map mapInfo = new HashMap();
        List<Map> listInfo = new ArrayList<>();
        //根据实例Id查询该es中实例的详细数据
        List<Map<String, Object>> listMap = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();

        if (instanceParam.getPropertiesList() != null) {
            for (AddModelInstancePropertiesParam dto : instanceParam.getPropertiesList()) {
                dto.getPropertiesIndexId();
                dto.getPropertiesValue();
                m1.put(dto.getPropertiesIndexId(), dto.getPropertiesValue());
                listMap.add(m1);
            }
        }
        if (instanceParam.getInstanceId() != null) {
            mwModelInstanceDao.getModelPropertiesType(instanceParam.getModelId());
        }
        //根据modelId获取属性的分类
        List<String> propertiesTypeList = mwModelInstanceDao.getModelPropertiesType(instanceParam.getModelId());
        List<Map> propertiesNameList = mwModelInstanceDao.getPropertiesNameByModelId(instanceParam.getModelId());
        Map<String, Map> maps = new HashMap();

        if (propertiesNameList != null && propertiesNameList.size() > 0) {
            for (Map m : propertiesNameList) {
                if (m != null && m.get("indexId") != null) {
                    maps.put(m.get("indexId").toString(), m);
                }
            }
        }
        Map<String, List> datas = new LinkedHashMap<>();
        for (String type : propertiesTypeList) {
            //根据类型和modelId查询不同属性分类下的属性。
            List<ModelPropertiesDto> list = mwModelInstanceDao.getModelPropertiesByType(instanceParam.getModelId(), type);
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            for (Map data : listMap) {
                List lists = new ArrayList();
                List listSort = new ArrayList();
                for (ModelPropertiesDto m : list) {
                    Map<String, Object> map = new HashMap();
                    String propertiesIndexId = m.getIndexId();
                    map.put("id", propertiesIndexId);
                    map.put("name", maps.get(propertiesIndexId).get("name"));
                    map.put("type", maps.get(propertiesIndexId).get("type"));
                    if (data.get(propertiesIndexId) != null) {
                        map.put("value", data.get(propertiesIndexId));
                    } else {
                        if (typeList.contains(m.getPropertiesTypeId())) {
                            map.put(propertiesIndexId, new ArrayList<>());
                        } else {
                            map.put(propertiesIndexId, "");
                        }

                    }
                    List<ModelPropertiesStructDto> structList = new ArrayList<>();
                    if (m.getPropertiesTypeId() == ModelPropertiesType.STRUCE.getCode()) {
                        structList = mwModelManageDao.getProperticesStructInfo(instanceParam.getModelId(), m.getIndexId());
                        map.put("propertiesStruct", structList);
                        //将类型为结构体的数据另存入一个list中
                        listSort.add(map);
                    } else {
                        map.put("propertiesStruct", structList);
                        lists.add(map);
                    }
                }
                //结构体数据放入list的最后面。方便前端排序
                lists.addAll(listSort);
                datas.put(type, lists);
            }
        }
        mapInfo.put("name", instanceParam.getInstanceName());
        mapInfo.put("data", datas);
        listInfo.add(mapInfo);
        return Reply.ok(listInfo);
    }


    /**
     * 流程模型实多个信息查看
     *
     * @param instanceParam
     * @return
     */
    @Override
    public Reply lookModelInstanceByActionDelete(DeleteModelInstanceParam instanceParam) {
        List<Map> listMaps = new ArrayList<>();
        if (instanceParam.getParamList() != null) {
            for (MwModelInstanceParam params : instanceParam.getParamList()) {
                Map mapInfo = new HashMap();
                //根据实例Id查询该es中实例的详细数据
                QueryModelInstanceParam param = new QueryModelInstanceParam();
                param.setModelIndex(instanceParam.getModelIndex());
                param.setModelId(instanceParam.getModelId());
                param.setModelInstanceId(params.getInstanceId());

                //根据实例Id查询该es中实例的详细数据
                List<Map<String, Object>> listMap = getInstanceInfoByModelId(param);
                //根据modelId获取属性的分类
                List<String> propertiesTypeList = mwModelInstanceDao.getModelPropertiesType(instanceParam.getModelId());
                List<Map> propertiesNameList = mwModelInstanceDao.getPropertiesNameByModelId(instanceParam.getModelId());
                Map<String, Map> maps = new HashMap();
                if (propertiesNameList != null && propertiesNameList.size() > 0) {
                    for (Map m : propertiesNameList) {
                        if (m != null && m.get("indexId") != null) {
                            maps.put(m.get("indexId").toString(), m);
                        }
                    }
                }
                Map<String, List> datas = new LinkedHashMap<>();
                for (String type : propertiesTypeList) {
                    //根据类型和modelId查询不同属性分类下的属性。
                    List<ModelPropertiesDto> list = mwModelInstanceDao.getModelPropertiesByType(instanceParam.getModelId(), type);
                    //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
                    for (Map data : listMap) {
                        List lists = new ArrayList();
                        List listSort = new ArrayList();
                        for (ModelPropertiesDto m : list) {
                            Map<String, Object> map = new HashMap();
                            String propertiesIndexId = m.getIndexId();
                            map.put("id", propertiesIndexId);
                            map.put("name", maps.get(propertiesIndexId).get("name"));
                            map.put("type", maps.get(propertiesIndexId).get("type"));
                            if (data.get(propertiesIndexId) != null) {
                                map.put("value", data.get(propertiesIndexId));
                            } else {
                                if (typeList.contains(m.getPropertiesTypeId())) {
                                    map.put(propertiesIndexId, new ArrayList<>());
                                } else {
                                    map.put(propertiesIndexId, "");
                                }

                            }
                            List<ModelPropertiesStructDto> structList = new ArrayList<>();
                            if (m.getPropertiesTypeId() == ModelPropertiesType.STRUCE.getCode()) {
                                structList = mwModelManageDao.getProperticesStructInfo(instanceParam.getModelId(), m.getIndexId());
                                map.put("propertiesStruct", structList);
                                //将类型为结构体的数据另存入一个list中
                                listSort.add(map);
                            } else {
                                map.put("propertiesStruct", structList);
                                lists.add(map);
                            }
                        }
                        //结构体数据放入list的最后面。方便前端排序
                        lists.addAll(listSort);
                        datas.put(type, lists);
                    }
                }
                mapInfo.put("name", params.getInstanceName());
                mapInfo.put("data", datas);
                listMaps.add(mapInfo);
            }
        }
        return Reply.ok(listMaps);
    }


    /**
     * 删除模型实例的时候要判断其是否存在模型实例的关系,如果存在模型实例的关系则需要先删除模型实例的关系,
     *
     * @param instanceParams
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply deleteModelInstance(Object instanceParams, Integer types) {
        try {
            DeleteModelInstanceParam deleteModelInstanceParam = new DeleteModelInstanceParam();
            if (types == 0) {
                deleteModelInstanceParam = (DeleteModelInstanceParam) instanceParams;
            } else {
                deleteModelInstanceParam = JSONObject.parseObject(instanceParams.toString(), DeleteModelInstanceParam.class);

            }
            if (null != deleteModelInstanceParam.getInstanceIds() && deleteModelInstanceParam.getInstanceIds().size() > 0) {
                mwModelManageDao.deleteModelInstances(deleteModelInstanceParam.getInstanceIds());
            }
            for (Integer instanceId : deleteModelInstanceParam.getInstanceIds()) {
                //对用户名、机构、用户组删除
                ModelPermControlParam param = new ModelPermControlParam();
                param.setType(DataType.INSTANCE_MANAGE.getName());
                param.setId(String.valueOf(instanceId));
                //删除
                mwModelManageService.deleteMapperAndPerm(param);
            }
            QueryBatchSelectDataParam jfParam = new QueryBatchSelectDataParam();
            QueryCabinetLayoutListParam jgParam = new QueryCabinetLayoutListParam();
            List<QuerySelectDataListParam> paramList = new ArrayList<>();
            List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
            //对机房机柜视图下的实例删除，需要修改对应的所属机房、所属机柜的布局
            if (deleteModelInstanceParam.getModelViewType() != null && (deleteModelInstanceParam.getModelViewType() == 1 || deleteModelInstanceParam.getModelViewType() == 2)) {
                for (Integer instanceId : deleteModelInstanceParam.getInstanceIds()) {
                    QueryModelInstanceParam params = new QueryModelInstanceParam();
                    params.setModelIndex(deleteModelInstanceParam.getModelIndex());
                    params.setInstanceIdList(Arrays.asList(instanceId));
                    //指定返回所属机房，所属机柜数据
                    params.setFieldList(Arrays.asList("relationSite"));
                    Integer relationSite = 0;
                    for (Map<String, Object> map : getModelInstanceDataByInstanceId(params)) {
                        if (map != null && map.get("relationSite") != null) {
                            relationSite = Integer.valueOf(map.get("relationSite").toString());
                        }
                    }
                    //获取实例资产在机房机柜中的占用位置信息
                    List<String> fieldList = Arrays.asList("position", "positionByCabinet");
                    List<Map<String, Object>> roomLayout = getModelInstanceInfoByEs(deleteModelInstanceParam.getModelIndex(), instanceId, fieldList);
                    //机房机柜修改之前的位置数据
                    CabinetLayoutDataParam lastData = new CabinetLayoutDataParam();
                    List<Integer> beforeCoordinate = new ArrayList<>();
                    for (Map<String, Object> map : roomLayout) {
                        if (map != null && map.get("position") != null) {
                            Object obj = map.get("position");
                            beforeCoordinate = (List<Integer>) JSONArray.parse(obj.toString());
                        }
                        if (map != null && map.get("positionByCabinet") != null) {
                            Object obj = map.get("positionByCabinet");
                            lastData = JSONObject.parseObject(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                        }
                    }
                    if (deleteModelInstanceParam.getModelViewType() == 1) {
                        //机房视图；修改机房布局
                        QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                        //外部关联的实例Id
                        dataParam.setInstanceId(relationSite);
                        //外部关联的modelIndex
                        dataParam.setModelIndex(deleteModelInstanceParam.getRelationModelIndex());
                        dataParam.setBeforeCoordinate(beforeCoordinate);
                        paramList.add(dataParam);

                    }
                    if (deleteModelInstanceParam.getModelViewType() == 2) {
                        //机柜视图；修改机柜布局
                        QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                        //外部关联的实例Id
                        clParam.setInstanceId(relationSite);
                        //外部关联的modelIndex
                        clParam.setModelIndex(deleteModelInstanceParam.getRelationModelIndex());
                        clParam.setLastData(lastData);
                        clParamList.add(clParam);
                    }
                }
                if (paramList.size() > 0) {
                    jfParam.setLayoutDataList(paramList);
                    updateRoomLayout(jfParam);
                }
                if (clParamList.size() > 0) {
                    jgParam.setCabinetLayoutList(clParamList);
                    updateCabinetLayout(jgParam);
                }
            }

            //删除对应es中的数据  使用的是查询删除  也可以根据es的id进行删除
            if (deleteModelInstanceParam.getInstanceIds() != null && deleteModelInstanceParam.getInstanceIds().size() > 0) {
                //删除对应es数据
                deleteEsInfoByQuery(Arrays.asList(deleteModelInstanceParam.getModelIndex()),deleteModelInstanceParam.getInstanceIds());
            }
            //资产数据，删除时，需要同步删除关联数据：zabbix主机
            List<DeleteTangAssetsID> ids = new ArrayList<>();
            if (deleteModelInstanceParam.getEsIdList() != null && deleteModelInstanceParam.getEsIdList().size() > 0) {
                BulkResponse bulkResponse = deleteEsInfoById(deleteModelInstanceParam.getEsIdList());
                if(bulkResponse!=null){
                    RestStatus status = bulkResponse.status();
                    if (status.getStatus() == 200 && deleteModelInstanceParam.getParamList() != null) {
                        for (MwModelInstanceParam param : deleteModelInstanceParam.getParamList()) {
                            //模型实例变更记录
                            Integer version = mwModelSysLogDao.getChangeHistoryVersion("instance_" + param.getInstanceId());
                            if (version != null) {
                                version = version + 1;
                            } else {
                                version = 1;
                            }
                            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.DELETE_INSTANCE.getName())
                                    .objName(param.getInstanceName()).operateDes(OperationTypeEnum.DELETE_INSTANCE.getName() + ":" + param.getInstanceName()).operateDesBefore("").type("instance_" + param.getInstanceId()).version(version).build();
                            //添加到系统操作日志
                            mwlogger.info(JSON.toJSONString(builder));
                            //添加到模型管理日志
                            if (deleteModelInstanceParam.getIsShift() == null || (!deleteModelInstanceParam.getIsShift())) {
                                //非转移实例时，保存历史记录
                                MwModelSysLogService.saveInstaceChangeHistory(builder);
                            }
                            //实例数据是否纳管，纳管数据删除时，需要同步删除关联数据：zabbix主机
                            if (!Strings.isNullOrEmpty(param.getAssetsId())) {
                                DeleteTangAssetsID deleteTangAssetsID = new DeleteTangAssetsID();
                                deleteTangAssetsID.setAssetsId(param.getAssetsId());
                                deleteTangAssetsID.setMonitorMode(param.getMonitorMode());
                                deleteTangAssetsID.setMonitorServerId(param.getMonitorServerId());
                                deleteTangAssetsID.setId(param.getInstanceId() + "");
                                ids.add(deleteTangAssetsID);
                            }
                        }
                        //刪除关联数据
                        mwModelAssetsDiscoveryServiceImpl.deleteAssetsRelationInfo(ids);
                    } else {
                        return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313008, ErrorConstant.MODEL_INSTANCE_MSG_313008);
                    }
                }
            }
            //刪除实例之后调用许可接口
            Integer count = selectCountInstances();
            licenseManagement.getLicenseManagemengt("model_manage", count, 0);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to deleteModelInstance param{}, case by {}", instanceParams, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313008, ErrorConstant.MODEL_INSTANCE_MSG_313008);
        }
    }

    /**
     * 通过查询条件删除es数据
     * @param modelIndexs
     * @param instanceIds
     * @throws IOException
     */
    private BulkByScrollResponse deleteEsInfoByQuery(List<String> modelIndexs,List<Integer> instanceIds ) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(String.join(",", modelIndexs));
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (Integer instanceId : instanceIds) {
            queryBuilder = queryBuilder.should(QueryBuilders.termQuery("modelInstanceId", instanceId));
        }
        deleteByQueryRequest.setQuery(queryBuilder);
        BulkByScrollResponse response = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        return response;
    }

    /**
     * 通过esId删除es数据
     * @param esIdList
     * @return
     * @throws IOException
     */
    private BulkResponse deleteEsInfoById(List<String> esIdList) throws IOException {
        BulkRequest request = new BulkRequest();
        for (String esId : esIdList) {
            DeleteRequest deleteRequest = new DeleteRequest(esId);
            request.add(deleteRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    /**
     * 实例列表数据查询
     *
     * @param param
     * @return
     */
    @Override
    public Reply selectModelInstance(QueryInstanceModelParam param) {
       return null;
    }

    /**
     * 数据唯一性校验
     * 模型属性中选择唯一的字段，都需要校验
     *
     * @param param
     * @return
     */
    @Override
    public Reply modelInstanceFieldUnique(QueryModelInstanceParam param) {
        List infoList = new ArrayList();
        List<Map<String, Object>> listMap = modelInstanceFieldUniqueCheck(param);
        Map<String, List> maps = new HashMap();
        for (Map<String, Object> map : listMap) {
            map.forEach((k, v) -> {
                if (maps.containsKey(k)) {
                    List list1 = maps.get(k);
                    list1.add(v);
                    maps.put(k, list1);
                } else {
                    List list = new ArrayList();
                    list.add(v);
                    maps.put(k, list);
                }
            });
        }
        for (AddModelInstancePropertiesParam param1 : param.getPropertiesList()) {
            String propertiesIndexId = param1.getPropertiesIndexId();
            String propertiesValue = param1.getPropertiesValue();
            List list = maps.get(propertiesIndexId);
            if (list != null && list.size() > 0) {
                boolean isContain = list.contains(propertiesValue);
                if (isContain) {
                    infoList.add(param1.getPropertiesName());
                }
            }
        }
        return Reply.ok(infoList);
    }

    @SneakyThrows
    private List<Map<String, Object>> modelInstanceFieldUniqueCheck(QueryModelInstanceParam param) {
        Map priCriteria = PropertyUtils.describe(param);
        //修改验证时，排除自身的instanceId
        List<ModelInstanceDto> list = mwModelManageDao.selectModelInstanceByUniqueCheck(priCriteria);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (StringUtils.isNotEmpty(param.getModelIndex())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));

            BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
            if (list.size() > 0) {
                for (ModelInstanceDto dto : list) {
                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
                }
            }
            queryBuilder.must(queryBuilder1);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
            searchSourceBuilder.size(param.getPageSize());
            //返回指定字段数据
            String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);
            FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
            searchSourceBuilder.fetchSource(sourceContext);
            //设置超时时间
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(param.getModelIndex());
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//                restHighLevelClient
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }


    /**
     * scroll滚动查询 多次查询所有
     *
     * @param param
     * @return
     */
    @SneakyThrows
    protected List<Map<String, Object>> getInstanceInfoByTimeOut(QueryModelInstanceParam param) {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);
//        Map priCriteria = PropertyUtils.describe(param);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        List<String> modelIndexs = param.getModelIndexs();
        BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();

        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        List<ModelInstanceDto> list = mwModelManageDao.selectModelInstanceByTimeOut(modelIndexs, allTypeIdList);
        for (ModelInstanceDto dto : list) {
            queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
        }
        queryBuilder.must(queryBuilder1);
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.scroll(scroll);
        StringBuilder sb = new StringBuilder();
        for (String str : modelIndexs) {
            sb.append(str);
            sb.append(",");
        }
        String indices = sb.toString();
        searchRequest.indices(indices);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            listMap.add(sourceAsMap);
        }
        //遍历搜索命中的数据，直到没有数据
        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        }

        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        //也可以选择setScrollIds()将多个scrollId一起使用
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("getInstanceInfoByTimeOut获取es滚动查询数据失败", e);
        }
        return listMap;
    }

    /**
     * scroll滚动查询 多次查询所有Index
     *
     * @param param
     * @return
     */
    @SneakyThrows
    public List<Map<String, Object>> getInstanceInfoByIndexs(QueryModelInstanceParam param) {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        List<String> instanceIdListAll = new ArrayList<>();
        instanceIdListAll.addAll(allTypeIdList);
        if (param.getInstanceIdList() != null && param.getInstanceIdList().size() > 0) {
            List<String> instanceIds = param.getInstanceIdList().stream().map(String::valueOf).collect(Collectors.toList());
            instanceIdListAll.addAll(instanceIds);
        }
        param.setModelInstanceIds(instanceIdListAll);
        Map priCriteria = PropertyUtils.describe(param);
        List<ModelInstanceDto> list = mwModelManageDao.selectModelInstance(priCriteria);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (!Strings.isNullOrEmpty(param.getModelIndex())) {
            queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
        }

        if (list.size() > 0) {
            BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
            for (ModelInstanceDto dto : list) {
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
            }
            queryBuilder.must(queryBuilder1);
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                NestedQueryBuilder nested = null;
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    if (m.getPropertiesType() != null) {
                        //时间
                        if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                        }
                        //字符串
                        if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + m.getPropertiesValue() + "*"));
                        }
                        //数字
                        if ((m.getPropertiesType().intValue() == ModelPropertiesType.INTEGER.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Integer.parseInt(m.getPropertiesValue())));
                        }
                        //数组
                        if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                        }
                        //布尔类型
                        if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                        }
                        //结构体 使用嵌套查询
                        if ((!Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                        }
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
        }
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
        if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
            searchRequest.indices(String.join(",", param.getModelIndexs()));
        } else if (!Strings.isNullOrEmpty(param.getModelIndex())) {
            searchRequest.indices(param.getModelIndex());
        }
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            listMap.add(sourceAsMap);
        }
        //遍历搜索命中的数据，直到没有数据
        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        }

        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        //也可以选择setScrollIds()将多个scrollId一起使用
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to getInstanceInfoByExport param{}, case by {}", param, e);
        }
        return listMap;
    }

    /**
     * scroll滚动查询 多索引导出
     *
     * @param param
     * @return
     */
    @SneakyThrows
    public List<Map<String, Object>> getInstanceInfoByMoreIndexs(QueryInstanceModelParam param) {
        List<Map> groupNodesList = mwModelManageDao.getModelGroupNodesAll();
        Map<String, String> groupNodeMap = new HashMap<>();
        for (Map m : groupNodesList) {
            groupNodeMap.put(m.get("modelIndex").toString(), m.get("groupNodes").toString());
        }

        //获取所有基础设施下的模型Index
        List<AddAndUpdateModelParam> modelList = mwModelViewDao.getModelIdByBase();
        List<String> modelIndexs = new ArrayList<>();
        for (AddAndUpdateModelParam aParam : modelList) {
            modelIndexs.add(aParam.getModelIndex());
        }
        List<Integer> instanceIdList = new ArrayList<>();
        //获取所有基础设施下的实例Id
        if(CollectionUtils.isNotEmpty(modelIndexs)){
            instanceIdList = mwModelViewDao.getInstanceIdByBase(modelIndexs);
        }
        param.setModelIndexs(modelIndexs);
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        if (globalUser.isSystemUser()) {//系统管理员，查询所有实例id
            param.setInstanceIds(instanceIdList);
        } else {//普通用户，获取对应的实例id
            List<Integer> intIds = allTypeIdList.stream().filter(str ->str.matches("\\d+"))//过滤非数字的数据
                    .map(Integer::parseInt).collect(Collectors.toList());
            param.setInstanceIds(intIds);
        }
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);

        if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            for (String modelIndex : param.getModelIndexs()) {
                queryBuilder = queryBuilder.should(QueryBuilders.termQuery("modelIndex", modelIndex));
            }
            BoolQueryBuilder queryBuilder0 = QueryBuilders.boolQuery();
            if (param.getInstanceIds() != null && param.getInstanceIds().size() > 0) {
                for (Integer instanceId : param.getInstanceIds()) {
                    queryBuilder0 = queryBuilder0.should(QueryBuilders.termQuery("modelInstanceId", instanceId));
                }
            }
            queryBuilder.must(queryBuilder0);
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                NestedQueryBuilder nested = null;
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    if ((m.getIsTreeQuery() != null && m.getIsTreeQuery())) {
                        if ((!org.elasticsearch.common.Strings.isNullOrEmpty(m.getPropertiesValue()) && ("未知".equals(m.getPropertiesValue()) || "-2".equals(m.getPropertiesValue())))) {
                            //表示树结构查询,且查询数据为未知或为-2
                            queryBuilder.mustNot(QueryBuilders.existsQuery(m.getPropertiesIndexId()));
                        } else {
                            //时间
                            if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                            }
                            //字符串
                            if (arrList.contains(m.getPropertiesType()) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                String value = m.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + value + "*"));
                            }
                            //数组
                            if ((typeList.contains(m.getPropertiesType())) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                            }
                            //布尔类型
                            if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                            }
                            //结构体 使用嵌套查询
                            if ((!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                            }
                            queryBuilder.must(queryBuilder1);
                        }
                    } else {
                        if (m.getPropertiesType() != null) {
                            //时间
                            if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                            }
                            //字符串
                            if (arrList.contains(m.getPropertiesType()) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                String value = m.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + value + "*"));
                            }
                            //数组
                            if ((typeList.contains(m.getPropertiesType())) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                            }
                            //布尔类型
                            if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                            }
                            //结构体 使用嵌套查询
                            if ((!com.google.common.base.Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                            }
                        }
                    }

                }
                queryBuilder.must(queryBuilder2);
            }
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.scroll(scroll);
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(String.join(",", modelIndexs));

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
            //遍历搜索命中的数据，直到没有数据
            while (searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
                if (searchHits != null && searchHits.length > 0) {
                    for (SearchHit searchHit : searchHits) {
                        Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                        sourceAsMap.put("esId", searchHit.getId());
                        listMap.add(sourceAsMap);
                    }
                }
            }
            //清除滚屏
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            //也可以选择setScrollIds()将多个scrollId一起使用
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = null;
            try {
                clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("fail to getInstanceInfoByMoreIndexs param{}, case by {}", param, e);
            }
        }
        return listMap;
    }


    /**
     * scroll滚动查询 多次查询所有
     *
     * @param param
     * @return
     */
    @SneakyThrows
    public List<Map<String, Object>> getInstanceInfoByExport(QueryModelInstanceParam param) {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);
        List<String> allTypeIdList = new ArrayList<>();
        //不为定时任务启动
        if (param.getIsTimeTask() == null || !param.getIsTimeTask()) {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        }
        List<String> instanceIdListAll = new ArrayList<>();
        instanceIdListAll.addAll(allTypeIdList);
        if (param.getInstanceIdList() != null && param.getInstanceIdList().size() > 0) {
            List<String> instanceIds = param.getInstanceIdList().stream().map(String::valueOf).collect(Collectors.toList());
            instanceIdListAll.addAll(instanceIds);
        }
        param.setModelInstanceIds(instanceIdListAll);
        Map priCriteria = PropertyUtils.describe(param);
        List<ModelInstanceDto> list = mwModelManageDao.selectModelInstance(priCriteria);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
        if (list.size() > 0) {
            BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
            for (ModelInstanceDto dto : list) {
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
            }
            queryBuilder.must(queryBuilder1);
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                NestedQueryBuilder nested = null;
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    if (m.getPropertiesType() != null) {
                        //时间
                        if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                        }
                        //字符串
                        if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + m.getPropertiesValue() + "*"));
                        }
                        //数字
                        if ((m.getPropertiesType().intValue() == ModelPropertiesType.INTEGER.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Integer.parseInt(m.getPropertiesValue())));
                        }
                        //数组
                        if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                        }
                        //布尔类型
                        if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                        }
                        //结构体 使用嵌套查询
                        if ((!Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                        }
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
        }
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(param.getModelIndex());

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            listMap.add(sourceAsMap);
        }
        //遍历搜索命中的数据，直到没有数据
        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
            if (searchHits != null && searchHits.length > 0) {
                for (SearchHit searchHit : searchHits) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        }

        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        //也可以选择setScrollIds()将多个scrollId一起使用
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = null;
        try {
            clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to getInstanceInfoByExport param{}, case by {}", param, e);
        }
        return listMap;
    }

    @Override
    public List<Map<String, Object>> getInstanceInfoByModelIndexs(QueryInstanceModelParam param) {
        return null;
    }

    /**
     * scroll滚动查询 多次查询所有
     *
     * @param param
     * @return
     */
    @SneakyThrows
    protected List<Map<String, Object>> getInstanceInfoByExportGroup(QueryModelInstanceParam param) {

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);
        List<ModelInstanceDto> list;
//        List<Map<String, Object>> listMap = new ArrayList<>();
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        param.setModelInstanceIds(allTypeIdList);
        Map priCriteria = PropertyUtils.describe(param);
        List<String> modelIndexs = mwModelManageDao.getAllModelIndexByGroup(priCriteria);
        list = mwModelManageDao.selectModelInstanceBySystemIsFlag(priCriteria);
        if (list.size() > 0) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            if (StringUtils.isNotEmpty(param.getModelIndex())) {
                queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
            }
            BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
            for (ModelInstanceDto dto : list) {
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
            }
            queryBuilder.must(queryBuilder1);
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                NestedQueryBuilder nested = null;
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    if (m.getPropertiesType() != null) {
                        //时间
                        if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                        }
                        //字符串
                        if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + m.getPropertiesValue() + "*"));
                        }
                        //数组
                        if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                        }
                        //布尔类型
                        if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                        }
                        //结构体 使用嵌套查询
                        if ((!Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                            queryBuilder2 = queryBuilder2.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                        }
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.scroll(scroll);
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(String.join(",", modelIndexs));
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
            //遍历搜索命中的数据，直到没有数据
            while (searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
                if (searchHits != null && searchHits.length > 0) {
                    for (SearchHit searchHit : searchHits) {
                        Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                        sourceAsMap.put("esId", searchHit.getId());
                        listMap.add(sourceAsMap);
                    }
                }
            }

            //清除滚屏
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            //也可以选择setScrollIds()将多个scrollId一起使用
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = null;
            try {
                clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("fail to getInstanceInfoByExportGroup param{}, case by {}", param, e);
            }
        }
        return listMap;
    }


    @SneakyThrows
    protected List<Map<String, Object>> getInstanceInfoByModelId(QueryModelInstanceParam param) {
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        param.setModelInstanceIds(allTypeIdList);
        Map priCriteria = PropertyUtils.describe(param);
        List<ModelInstanceDto> list = mwModelManageDao.selectModelInstance(priCriteria);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (StringUtils.isNotEmpty(param.getModelIndex())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
            if (list.size() > 0) {
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                for (ModelInstanceDto dto : list) {
                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", dto.getInstanceId()));
                }
                queryBuilder.must(queryBuilder1);
                //全字段模糊查询
                if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                    BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                    NestedQueryBuilder nested = null;
                    for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                        if (m.getPropertiesType() != null) {
                            //时间
                            if ((m.getStartTime() != null || m.getEndTime() != null) && (m.getPropertiesType() == ModelPropertiesType.DATE.getCode())) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.rangeQuery(m.getPropertiesIndexId()).from(DateUtils.format(m.getStartTime(), "yyyy-MM-dd HH:mm:ss")).to(DateUtils.format(m.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
                            }
                            //字符串
                            if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + m.getPropertiesValue() + "*"));
                            }
                            //数组
                            if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                            }
                            //布尔类型
                            if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                            }
                            //结构体 使用嵌套查询
                            if ((!Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.nestedQuery(m.getPropertiesIndexId(), QueryBuilders.matchPhraseQuery(m.getPropertiesIndexId() + "." + m.getPropertiesInstanceStruct(), m.getPropertiesValue()), ScoreMode.None));
                            }
                        }
                    }
                    queryBuilder.must(queryBuilder2);
                }
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                searchSourceBuilder.size(param.getPageSize());
                //设置超时时间
                searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.source(searchSourceBuilder);
                searchRequest.indices(param.getModelIndex());
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//                restHighLevelClient
                for (SearchHit searchHit : search.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        }
        return listMap;
    }

    @SneakyThrows
    public List<Map<String, Object>> getInfoByInstanceId(QueryModelInstanceParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (StringUtils.isNotEmpty(param.getModelIndex())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
            if (param.getModelInstanceId() != null && param.getModelInstanceId() != 0) {
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", param.getModelInstanceId()));
                queryBuilder.must(queryBuilder1);
            }
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    if (!Strings.isNullOrEmpty(m.getPropertiesValue())) {
                        queryBuilder2 = queryBuilder2.should(QueryBuilders.matchQuery(m.getPropertiesIndexId(), m.getPropertiesValue()).fuzziness(Fuzziness.AUTO));
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
            searchSourceBuilder.size(param.getPageSize());
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(param.getModelIndex());
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : search.getHits().getHits()) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }

    @Override
    public Reply getInstanceInfoById(QueryModelInstanceParam param) {
        try {
            //type类型为10、11、12、13,16都为数组类型
            List<Map<String, Object>> listMap = new ArrayList<>();
            listMap = getInfoByInstanceId(param);
            List<Map<String, Object>> listMapNew = new ArrayList<>();
            List<MWModelPropertiesInfoDto> propertiesList = mwModelManageDao.queryPropertiesByInstance(param.getModelId());
            MWModelPropertiesInfoDto dto = new MWModelPropertiesInfoDto();
            dto.setIndexId("esId");
            dto.setPropertiesName("esId");
            propertiesList.add(dto);
            MWModelPropertiesInfoDto dto1 = new MWModelPropertiesInfoDto();
            dto1.setIndexId("modelInstanceId");
            dto1.setPropertiesName("模型实例Id");
            propertiesList.add(dto1);
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            for (Map data : listMap) {
                Map<String, Object> map = new HashMap();
                if (data.get("groupNodes") != null) {
                    String groupNodes = data.get("groupNodes").toString();
                    List<String> list = Arrays.asList(groupNodes.substring(1).split(","));
                    List<Integer> lists = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                    lists.add(param.getModelId());
                    map.put("modelGroupIdList", lists);
                }

                for (MWModelPropertiesInfoDto m : propertiesList) {
                    String propertiesIndexId = m.getIndexId();
                    if (data.get(propertiesIndexId) != null) {
                        map.put(propertiesIndexId, data.get(propertiesIndexId));
                    } else {
                        if (typeList.contains(Integer.valueOf(m.getPropertiesTypeId()))) {
                            map.put(propertiesIndexId, new ArrayList<>());
                        } else {
                            map.put(propertiesIndexId, "");
                        }

                    }
                }
                listMapNew.add(map);
            }
            return Reply.ok(listMapNew);
        } catch (Exception e) {
            log.error("fail to getInstanceInfoById param{}, case by {}", param, e);
            return Reply.fail(500, "获取实例数据失败");
        }
    }


    @Override
    public Reply getInstanceStructInfo(QueryModelInstanceParam param) {
        try {
            Map map = new HashMap();
            List<Map<String, Object>> listMap = new ArrayList<>();
            listMap = getInfoByInstanceId(param);
            List<ModelPropertiesStructDto> structList = new ArrayList<>();
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                structList = mwModelManageDao.getProperticesStructInfo(param.getModelId(), param.getPropertiesList().get(0).getPropertiesIndexId());
                if (listMap != null && listMap.size() > 0) {
                    listMap.get(0).get(param.getPropertiesList().get(0).getPropertiesIndexId());
                    map.put("data", listMap.get(0).get(param.getPropertiesList().get(0).getPropertiesIndexId()));
                } else {
                    map.put("data", new ArrayList<>());
                }
            } else {
                map.put("data", new ArrayList<>());
            }
            map.put("structList", structList);
            return Reply.ok(map);
        } catch (Exception e) {
            log.error("fail to getInstanceStructInfo param{}, case by {}", param, e);
            return Reply.fail(500, "获取实例结构体数据失败");
        }
    }

    /**
     * 获取模糊查询时提示信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getInstanceInfoByFuzzyQuery(QueryModelInstanceParam param) {
        try {
            List<Map<String, Object>> listMap;
            List<MWModelPropertiesInfoDto> propertiesList;
            if (isFlag && param.getType() != null && "group".equals(param.getType())) {
                listMap = getInstanceInfoByExportGroup(param);
                propertiesList = mwModelManageDao.queryPropertiesByInstanceFuzzyQueryGroup(param.getModelId());
            } else {
                listMap = getInstanceInfoByModelId(param);
                propertiesList = mwModelManageDao.queryPropertiesByInstanceFuzzyQuery(param.getModelId());
            }
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            List<String> datas = new ArrayList<>();
            for (Map data : listMap) {
                Map map = new HashMap();
                for (MWModelPropertiesInfoDto m : propertiesList) {
                    String propertiesIndexId = m.getIndexId();
                    if (data.get(propertiesIndexId) != null) {
                        map.put(propertiesIndexId, data.get(propertiesIndexId).toString());
                        datas.add(data.get(propertiesIndexId).toString());
                    }
                }
            }
            datas = datas.stream().distinct().collect(Collectors.toList());
            return Reply.ok(datas);
        } catch (Exception e) {
            log.error("fail to selectModelInstance param{}, case by {}", param, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_SELECT_CODE_313005, ErrorConstant.MODEL_INSTANCE_SELECT_MSG_313005);
        }
    }

    /**
     * 查询模型实例列表显示的字段名
     * 列表的
     *
     * @param queryCustomModelparam
     * @return
     */
    @Override
    public Reply selectModelInstanceFiled(QueryCustomModelparam queryCustomModelparam) {
        List<MwCustomColByModelDTO> colList;
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            colList = mwModelInstanceDao.selectFiledsByGroupList(queryCustomModelparam);
        } else {
            colList = mwModelInstanceDao.selectByModelUserIdList(queryCustomModelparam);
        }
        getColList(colList);
        return Reply.ok(colList);
    }

    @Override
    public Reply queryModelListInfo(QueryInstanceModelParam param) {
        return null;
    }

    private List<MwCustomColByModelDTO> getColList(List<MwCustomColByModelDTO> colList) {
        for (MwCustomColByModelDTO dto : colList) {
            List<ModelPropertiesStructDto> structList = new ArrayList<>();
            if (!Strings.isNullOrEmpty(dto.getDropOpStr())) {
                dto.setDropOp(Arrays.asList(dto.getDropOpStr().split(",")));
            }
            if (!Strings.isNullOrEmpty(dto.getDefaultValueListStr())) {
                dto.setDefaultValueList(Arrays.asList(dto.getDefaultValueListStr().split(",")));
            }
            if (!Strings.isNullOrEmpty(dto.getGangedValueListStr())) {
                dto.setGangedValueList(JSONArray.parseArray(dto.getGangedValueListStr()));
            }
            if (!Strings.isNullOrEmpty(dto.getDropArrObjStr())) {
                dto.setDropArrObj(JSONArray.parseArray(dto.getDropArrObjStr()));
            }
            // inputFormat 将模型属性类型转为页面下拉查询字段类型
            //1:文本 2:时间 6:下拉框
            if (String.valueOf(ModelPropertiesType.DATE.getCode()).equals(dto.getType())) {//8为时间
                dto.setInputFormat("2");
            } else if (String.valueOf(ModelPropertiesType.SINGLE_RELATION.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.MULTIPLE_RELATION.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.SINGLE_ENUM.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.ORG.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.USER.getCode()).equals(dto.getType()) ||
                    String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(dto.getType())) {
                dto.setInputFormat("6");
            } else {
                dto.setInputFormat("1");
            }
            //属性类型为结构体
            if (String.valueOf(ModelPropertiesType.STRUCE.getCode()).equals(dto.getType())) {
                //根据modelId和属性IndexId，查询属性结构体信息
                structList = mwModelManageDao.getProperticesStructInfo(dto.getModelId(), dto.getProp());
                if (structList != null) {
                    for (ModelPropertiesStructDto mps : structList) {
                        //结构体数据类型为9和10时，代表的数据类型为数组格式，需要转换
                        if (mps.getStructType() != null && (mps.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || mps.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode())) {
                            if (!Strings.isNullOrEmpty(mps.getStructStrValue())) {
                                mps.setStructListValue(Arrays.asList(mps.getStructStrValue().split(",")));
                            }
                        }
                    }
                    dto.setPropertiesStruct(structList);
                } else {
                    dto.setPropertiesStruct(structList);
                }
            }
        }
        return colList;
    }


    /**
     * 查询模型实例列表显示的字段名
     *
     * @param queryCustomModelparam
     * @return
     */
    @Override
    public Reply selectModelInstanceFiledByInsert(QueryCustomModelparam queryCustomModelparam) {
        List<MwCustomColByModelDTO> colList;
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            colList = mwModelInstanceDao.selectFiledsByGroupList(queryCustomModelparam);
        } else {
            colList = mwModelInstanceDao.selectByModelUserIdList(queryCustomModelparam);
        }

        List<ModelPropertiesStructDto> structList = new ArrayList<>();
        for (MwCustomColByModelDTO dto : colList) {
            if (!Strings.isNullOrEmpty(dto.getDropOpStr())) {
                dto.setDropOp(Arrays.asList(dto.getDropOpStr().split(",")));
            }
            if (!Strings.isNullOrEmpty(dto.getDropArrObjStr())) {
                List lists = JSONArray.parseArray(dto.getDropArrObjStr());
                dto.setDropArrObj(lists);
            }
            if (!Strings.isNullOrEmpty(dto.getDefaultValueListStr())) {
                dto.setDefaultValueList(Arrays.asList(dto.getDefaultValueListStr().split(",")));
            }
            //属性类型为结构体
            if (String.valueOf(ModelPropertiesType.STRUCE.getCode()).equals(dto.getType())) {
                //根据modelId和属性IndexId，查询属性结构体信息
                structList = mwModelManageDao.getProperticesStructInfo(dto.getModelId(), dto.getProp());
                if (structList != null) {
                    for (ModelPropertiesStructDto mps : structList) {
                        //结构体数据类型为9和10时，代表的数据类型为数组格式，需要转换
                        if (mps.getStructType() != null && (mps.getStructType() == ModelPropertiesType.SINGLE_ENUM.getCode() || mps.getStructType() == ModelPropertiesType.MULTIPLE_ENUM.getCode())) {
                            if (!Strings.isNullOrEmpty(mps.getStructStrValue())) {
                                mps.setStructListValue(Arrays.asList(mps.getStructStrValue().split(",")));
                            }
                        }
                    }
                    dto.setPropertiesStruct(structList);
                } else {
                    dto.setPropertiesStruct(structList);
                }
            }
        }
        //根据modelId获取属性的分类
        List<String> propertiesTypeList;
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            propertiesTypeList = mwModelInstanceDao.getModelPropertiesTypeByGroup(queryCustomModelparam.getModelId());
        } else {
            propertiesTypeList = mwModelInstanceDao.getModelPropertiesType(queryCustomModelparam.getModelId());
        }
        List<MWModelInstanceFiled> mapList = new ArrayList<>();
        if (propertiesTypeList != null) {
            for (String type : propertiesTypeList) {
                if (!Strings.isNullOrEmpty(type)) {
                    List<MwCustomColByModelDTO> newList = new ArrayList();
                    MWModelInstanceFiled mwModelInstanceFiled = new MWModelInstanceFiled();
                    newList = colList.stream().filter(s -> type.equals(s.getPropertiesType())).collect(Collectors.toList());
                    mwModelInstanceFiled.setType(type);
                    mwModelInstanceFiled.setData(newList);
                    mapList.add(mwModelInstanceFiled);
                }
            }
        }
        return Reply.ok(mapList);
    }

    /**
     * 实例跳转
     *
     * @return
     */
    private List<MwModelManageTypeDto> selectModelInstanceTreeByRedirect(Integer modelGroupId, Integer modelId) {
        List<MwModelManageTypeDto> list = mwModelInstanceDao.selectModelInstanceTreeByRedirect(modelGroupId, modelId);
        List<MwModelManageTypeDto> orgTopList = new ArrayList<>();
        List<MwModelManageTypeDto> childList = new ArrayList<>();
        list.forEach(mwModelManageTypeDto -> {
            mwModelManageTypeDto.setIsFlag(isFlag);
            if (mwModelManageTypeDto.getDeep() == 1) {
                orgTopList.add(mwModelManageTypeDto);
            } else {
                childList.add(mwModelManageTypeDto);
            }
        });
        Set<String> modelGroupIdSet = new HashSet<>(childList.size());
        orgTopList.forEach(
                orgTop ->
                        getModelTypeChild(orgTop, childList, modelGroupIdSet)
        );
        return orgTopList;
    }


    /**
     * 查询模型实例左侧树结构
     *
     * @return
     */
    @Override
    public Reply selectModelInstanceTree() {
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> instanceIds = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        List<String> modelIds = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
        List<MwModelManageTypeDto> list = new ArrayList<>();
        if (isFlag) {
            //西藏邮储环境，模型不做权限控制
            list = mwModelManageDao.selectModelInstanceTree(instanceIds, null);
        } else {
            list = mwModelManageDao.selectModelInstanceTree(instanceIds, modelIds);
        }
        List<MwModelManageTypeDto> orgTopList = new ArrayList<>();
        List<MwModelManageTypeDto> childList = new ArrayList<>();
        list.forEach(mwModelManageTypeDto -> {
            mwModelManageTypeDto.setIsFlag(isFlag);
            if (mwModelManageTypeDto.getDeep() == 1) {
                orgTopList.add(mwModelManageTypeDto);
            } else {
                childList.add(mwModelManageTypeDto);
            }
        });
        Set<String> modelGroupIdSet = new HashSet<>(childList.size());
        orgTopList.forEach(
                orgTop ->
                        getModelTypeChild(orgTop, childList, modelGroupIdSet)
        );
        PageInfo pageInfo = new PageInfo<>(orgTopList);
        return Reply.ok(pageInfo);
    }

    private void getModelTypeChild(MwModelManageTypeDto
                                           mwModelManageTypeDto, List<MwModelManageTypeDto> mwModelManageTypeDtoList, Set<String> modelGroupIdSet) {
        List<MwModelManageTypeDto> childList = new ArrayList<>();
        mwModelManageTypeDtoList.stream()
                // 判断是否已循环过当前对象
                .filter(child -> child.getModelGroupIdStr() != null && child.getPidStr() != null)
                .filter(child -> !modelGroupIdSet.contains(child.getModelGroupIdStr()))
                // 判断是否为父子关系
                .filter(child -> child.getPidStr().equals(mwModelManageTypeDto.getModelGroupIdStr()))
                // orgIdSet集合大小不超过mwModelManageDtoList的大小
                .filter(child -> modelGroupIdSet.size() <= mwModelManageTypeDtoList.size())
                .forEach(
                        // 放入modelIdSet,递归循环时可以跳过这个项目,提交循环效率
                        child -> {
                            modelGroupIdSet.add(child.getModelGroupIdStr());
                            //获取当前类目的子类目
                            getModelTypeChild(child, mwModelManageTypeDtoList, modelGroupIdSet);
                            childList.add(child);
                        }
                );
        mwModelManageTypeDto.addChild(childList);
    }

    private void getModelTypeChild(MwModelInstanceRelationDto
                                           modelInstanceRelationDto, List<MwModelInstanceRelationDto> modelInstanceRelationDtos, Set<Integer> set) {
        List<ModelInstanceRelationDto> childList = new ArrayList<>();
        modelInstanceRelationDtos.stream()
                // 判断是否已循环过当前对象
                .filter(child -> !set.contains(child.getInstanceRelationsId()))
                // 判断是否为父子关系
                .filter(child -> child.getLeftInstanceId().equals(modelInstanceRelationDto.getRightInstanceId()))
                // orgIdSet集合大小不超过mwModelManageDtoList的大小
                .filter(child -> set.size() <= modelInstanceRelationDtos.size())
                .forEach(
                        // 放入modelIdSet,递归循环时可以跳过这个项目,提交循环效率
                        child -> {
                            set.add(child.getRightInstanceId());
                            //获取当前类目的子类目
                            getModelTypeChild(child, modelInstanceRelationDtos, set);
                            childList.add(child);
                        }
                );
        modelInstanceRelationDto.addChilds(childList);
    }

    /**
     * 模型实例对应的模型属性
     * 一个模型有多个模型实例,每个模型实例都有相同的模型属性
     *
     * @param param
     * @return
     */
    @Override
    public Reply selectModelInstanceProperties(QueryModelInstanceParam param) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (null != param.getModelInstanceId()) {
            queryBuilder.must(QueryBuilders.termQuery("modelId", param.getModelId()));
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(param.getModelIndex());
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit searchHit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            list.add(sourceAsMap);
        }

        return Reply.ok(list);
    }

    @Override
    public Reply imageUpload(MultipartFile multipartFile, Integer instanceId) {

        if (multipartFile.isEmpty()) {
            return Reply.fail("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        //文件重命名，防止重复
        fileName = UUIDUtils.getUUID() + fileName;
        //设置放到数据库字段的值
        String fileNameInTable = fileName;
        File file = new File(new File(imgPath).getAbsolutePath() + File.separator + MODULE + File.separator + fileName);
        //检测是否存在目录
        String path = new File(imgPath).getAbsolutePath() + File.separator + MODULE + File.separator;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
        } catch (Exception e) {
            log.error("transferTo{}", e);
            return Reply.fail(e.getMessage(), multipartFile);
        }
        //修改文件权限
        Runtime runtime = Runtime.getRuntime();
        String command = "chmod 644 " + file.getAbsolutePath();
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            int exitValue = process.exitValue();
        } catch (IOException | InterruptedException e) {
            log.error("fail to imageUpload param{}, case by {}", instanceId, e);
        }
        return Reply.ok(fileNameInTable);
    }

    @Override
    public Reply instaceChangeHistory(SystemLogParam qParam) {
        Reply reply = mwSysLogService.selectSysLogByModel(qParam);
        List<SysLogDTO> logList = (List<SysLogDTO>) reply.getData();
        return reply;
    }

    @Override
    public Reply selectInstanceProperties(QueryModelInstanceParam param) throws Exception {
        Map priCriteria = PropertyUtils.describe(param);
        List<ModelPropertiesDto> list = mwModelManageDao.selectModelPropertiesList(priCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }


    private String getStringBuffer(List<String> nodes) {
        StringBuffer sb = new StringBuffer("(");
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(" nodes like '%" + nodes.get(i) + "%'");
            if (i < nodes.size() - 1) {
                sb.append(" or ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    protected Boolean checkFieldExist(String modelIndex, String propertiesIndexId) {
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.existsQuery(propertiesIndexId));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(pageFrom);
            searchSourceBuilder.size(pageSize);
            //设置超时时间
            searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            searchSourceBuilder.query(queryBuilder);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);
            searchRequest.indices(modelIndex);
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long count = search.getHits().getTotalHits().value;
            if (count > 0) {
                return true;
            }
        } catch (IOException e) {
            log.error("fail to checkFieldExist param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
        return false;
    }


    public void setESMappingByLong(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        value.put("type", "long");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByDate param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }

    public void setESMappingByDouble(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        value.put("type", "double");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByDate param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }

    public void setESMappingByBoolean(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        value.put("type", "boolean");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByDate param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }

    public void setESMappingByDate(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        value.put("type", "date");
        value.put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByDate param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }

    /**
     * 字符串类型添加不分词排序
     *
     * @param modelIndex
     * @param propertiesIndexId
     */
    public void setESMappingByString(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        Map fields = new HashMap();
        Map type = new HashMap();
        value.put("type", "text");
        value.put("fields", fields);
        fields.put("keyword", type);
        type.put("type", "keyword");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByString param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }


    //设置结构体为es嵌套类型
    public void setESMappingByStruct(String modelIndex, String propertiesIndexId) {
        PutMappingRequest request = new PutMappingRequest(modelIndex);
        request.type("_doc");
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        value.put("type", "nested");
        field.put(propertiesIndexId, value);
        properties.put("properties", field);
        request.source(JSONObject.toJSONString(properties), XContentType.JSON);
        try {
            restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("fail to setESMappingByStruct param{}, case by {}", modelIndex + ":" + propertiesIndexId, e);
        }
    }

    @Override
    public Reply selectModelNameById(Integer modelId) {
        try {
            String modelName = mwModelManageDao.selectModelNameById(modelId);
            return Reply.ok(modelName);
        } catch (Exception e) {
            log.error("fail to selectModelList modelParam{}, case by {}", modelId, e);
            return Reply.fail(ErrorConstant.MODEL_SELECT_CODE_313001, ErrorConstant.MODEL_SELECT_MSG_313001);
        }
    }


    @Override
    public Reply shiftInstanceCheck(List<AddAndUpdateModelInstanceParam> params) {
        String message = "";
        if (params != null && params.size() > 0) {
            for (AddAndUpdateModelInstanceParam param : params) {
                //获取源模型的字段属性名称和类型
                List<Map> ownPropertiesInfos = mwModelInstanceDao.getPropertiesInfoByModelId(param.getModelId());
                MwModelInfoDTO ownMaps = mwModelInstanceDao.getModelIndexInfo(param.getModelId());
                String ownModelName = "";
                String owmGroupName = "";
                if (ownMaps != null && ownMaps.getModelName() != null) {
                    ownModelName = ownMaps.getModelName();
                }
                if (ownMaps != null && ownMaps.getModelGroupName() != null) {
                    owmGroupName = ownMaps.getModelGroupName();
                }
                List<Map> ownPropertiesInfo = new ArrayList<>();
                Map ownMap = new HashMap();
                for (Map m : ownPropertiesInfos) {
                    Map h = new HashMap();
                    h.put("properties_name", m.get("properties_name"));
                    h.put("properties_type_id", m.get("properties_type_id"));
                    ownPropertiesInfo.add(h);
                    ownMap.put(m.get("index_id"), m.get("properties_name"));
                }
                for (ModelInstanceShiftParam info : param.getTargetModelInfo()) {
                    AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                    //获取目標模型的字段属性名称和类型
                    List<Map> targetPropertiesInfos = mwModelInstanceDao.getPropertiesInfoByModelId(info.getModelId());
                    List<Map> targetPropertiesInfo = new ArrayList<>();
                    instanceParam.setModelId(info.getModelId());
                    MwModelInfoDTO infoDto = new MwModelInfoDTO();
                    if (instanceParam.getModelId() != null) {
                        infoDto = mwModelInstanceDao.getModelIndexInfo(info.getModelId());
                        if (infoDto != null && infoDto.getModelIndex() != null) {
                            instanceParam.setModelIndex(infoDto.getModelIndex());
                        }
                        if (infoDto != null && infoDto.getModelName() != null) {
                            instanceParam.setModelName(infoDto.getModelName());
                            instanceParam.setTargetModelName(infoDto.getModelName());
                        }
                        if (infoDto != null && infoDto.getModelGroupName() != null) {
                            instanceParam.setTargetGroupName(infoDto.getModelGroupName());
                        }
                    }
                    instanceParam.setOwnModelName(ownModelName);
                    instanceParam.setOwnGroupName(owmGroupName);
                    instanceParam.setOwnInstanceId(param.getInstanceId());
                    Map targetMap = new HashMap();
                    for (Map m : targetPropertiesInfos) {
                        Map h = new HashMap();
                        h.put("properties_name", m.get("properties_name"));
                        h.put("properties_type_id", m.get("properties_type_id"));
                        targetPropertiesInfo.add(h);
                        targetMap.put(m.get("properties_name"), m.get("index_id"));
                    }
                    //如果目標模型的字段名称和原模型的重复一致，可以转移
                    if (targetPropertiesInfo.containsAll(ownPropertiesInfo) && param.getPropertiesList() != null) {
                        //转移操作
                        List<AddModelInstancePropertiesParam> list = param.getPropertiesList();
                        List<AddModelInstancePropertiesParam> listNew = new ArrayList<>();
                        for (AddModelInstancePropertiesParam properties : list) {
                            AddModelInstancePropertiesParam prop = new AddModelInstancePropertiesParam();
                            String targerPropertiesIndex = targetMap.get(properties.getPropertiesName()).toString();
                            if ("资产名称".equals(properties.getPropertiesName())) {
                                instanceParam.setInstanceName(properties.getPropertiesValue());
                            }
                            prop.setPropertiesIndexId(targerPropertiesIndex);
                            prop.setPropertiesValue(properties.getPropertiesValue());
                            prop.setModelId(info.getModelId());
                            prop.setPropertiesType(properties.getPropertiesType());
//                    prop.setModelIndex(param.getModelIndex());
                            listNew.add(prop);
                        }
                        instanceParam.setPropertiesList(listNew);
                        Object object = (Object) instanceParam;
                        //插入实例数据
                        creatModelInstance(object, 0);
                        //数据删除
                        DeleteModelInstanceParam deleteParam = new DeleteModelInstanceParam();
                        deleteParam.setModelIndex(param.getModelIndex());
                        deleteParam.setModelId(param.getModelId());
                        deleteParam.setEsIdList(param.getEsIdList());
                        deleteParam.setInstanceIds(Arrays.asList(param.getInstanceId()));
                        List<MwModelInstanceParam> paramsList = new ArrayList<>();
                        MwModelInstanceParam mwModelInstanceParam = new MwModelInstanceParam();
                        mwModelInstanceParam.setInstanceId(param.getInstanceId());
                        mwModelInstanceParam.setInstanceName(instanceParam.getInstanceName());
                        paramsList.add(mwModelInstanceParam);
                        deleteParam.setParamList(paramsList);
                        deleteParam.setIsShift(true);
                        Object deleteObject = (Object) deleteParam;
                        deleteModelInstance(deleteObject, 0);
                    } else {
                        message += "模型" + info.getModelName() + "字段不一致，转移失败";
                        return Reply.fail(message);
                    }
                }
            }
        }
        return Reply.ok(message);
    }

    @Override
    public Reply getTimeOutInfo() {
        QueryModelInstanceParam param = new QueryModelInstanceParam();
        List<Map<String, Object>> listMap;
        List<Map> modelTimeOutInfo = new ArrayList<>();
        //获取模型下的属性类型
        List<Map> propertiesNameList;
        Map<String, Map> timeOutPropertiesMap = new HashMap();
        //获取所有设置到期时间的模型信息
        List<Map> timeOutModelInfo = mwModelInstanceDao.getAllModelIndexInfo();
        List<Map> timeOutPropertiesInfo = mwModelInstanceDao.getTimeOutPropertiesInfo();
        if (timeOutPropertiesInfo != null && timeOutPropertiesInfo.size() > 0) {
            for (Map m : timeOutPropertiesInfo) {
                //将属性index作为key值，存入map
                timeOutPropertiesMap.put(m.get("index_id").toString(), m);
            }
        }
        List<String> modelIndexs = new ArrayList<>();
        if (timeOutModelInfo != null && timeOutModelInfo.size() > 0) {
            for (Map m : timeOutModelInfo) {
                //获取所有的modelIndex
                if (m != null && m.get("model_index") != null) {
                    modelIndexs.add(m.get("model_index").toString());
                }
            }
        }
        param.setModelIndexs(modelIndexs);
        if (modelIndexs.size() != 0) {
            //滚动查询所有modelIndex的数据
            listMap = getInstanceInfoByTimeOut(param);
            List<MWUser> users = mwModelInstanceDao.selectAllUserList();
            for (Map.Entry<String, Map> entry : timeOutPropertiesMap.entrySet()) {
                String k = entry.getKey();
                Map v = entry.getValue();
                for (Map<String, Object> maps : listMap) {
                    if (maps.get(k) != null) {
                        String date = maps.get(k).toString();
                        Object obj = (Object) maps;
                        Integer timeNum = Integer.valueOf(v.get("before_expiretime").toString());
                        String timeType = v.get("time_unit").toString();
                        //是否到期需要提示
                        Boolean isTimeOut = isTimeOut(date, timeNum, timeType);
                        if (isFlag) {
                            //西藏邮储时间只保留年月日
                            date = DateUtils.formatDate(DateUtils.parse(date));
                        }
                        if (isTimeOut) {
                            String text = maps.get(MwModelViewCommonService.INSTANCE_NAME_KEY).toString() + "将在" + date + "到期，请您尽快查验";
                            mwMessageService.sendTimeOutMessage(text, users, true, obj);
                        }
                    }
                }
            }
        }
        return Reply.ok();
    }

    @Override
    public TimeTaskRresult getTimeOutInfoByTimeTask() {
        return null;
    }

    /**
     * 获取外部关联数据
     *
     * @return
     */
    @Override
    public Reply getSelectDataInfo(List<QueryRelationInstanceInfo> paramList) {
        QueryModelInstanceParam params = new QueryModelInstanceParam();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for(QueryRelationInstanceInfo param : paramList){
                if (param.getModelIndex() != null) {
                    Integer modelId = mwModelManageDao.selectModelIdByIndexs(param.getModelIndex());
                    params.setModelId(modelId);
                }
                params.setFieldList(Arrays.asList(param.getPropertiesIndex(), "modelInstanceId", "modelId", "modelIndex"));
                params.setModelIndex(param.getModelIndex());
                params.setPageSize(pageSize);
                List<Map<String, Object>> selectList = modelInstanceFieldUniqueCheck(params);
                List<Map<String, Object>> listInfo = selectList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.get(param.getPropertiesIndex()).toString()))), ArrayList::new));

                //外部关联，暂时给定的是获取数据的实例id，后面还有其他类型，比如获取下拉数据的名称的。。。
                for (Map<String, Object> map : listInfo) {
                    String label = "";
                    String value = "";
                    Map<String, Object> m = new HashMap<>();
                    if (map != null) {
                        if (map.get(param.getPropertiesIndex()) != null) {
                            label = map.get(param.getPropertiesIndex()).toString();
                            m.put("label", label);
                            value = map.get("modelInstanceId").toString();
                            m.put("value", value);
                            list.add(m);
                        }
                    }
                }
            }
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getSelectDataInfo param{}, case by {}", paramList, e);
            return Reply.fail(500, "获取外部关联下拉数据失败");
        }
    }

    private Boolean isTimeOut(String date, Integer timeNum, String timeType) {
        Date time = new Date();
        Date timeOut = DateUtils.parse(date);
        DateUnitEnum dateUnitEnum = null;
        long dateMillis = 1l;
        switch (timeType) {
            case "秒":
                dateMillis = 1000l;
                break;
            case "分钟":
                dateMillis = 60000l;
                break;
            case "小时":
                dateMillis = 3600000l;
                break;
            case "天":
                dateMillis = 86400000l;
                break;
            case "月":
                dateMillis = 2592000000l;
                break;
            default:
        }
        float num = between(time, timeOut, dateMillis);
        if (num > 0 && num < timeNum) {
            return true;
        }
        return false;
    }

    public float between(Date beginDate, Date endDate, long dateMillis) {
        if (null == beginDate || null == endDate) {
            return 0L;
        }
        float diff = endDate.getTime() - beginDate.getTime();
        return (float) diff / dateMillis;
    }

    /**
     * 修改机房布局数据
     */
    @Override
    public Reply updateRoomLayout(QueryBatchSelectDataParam qparam) {
        try {
            if (qparam != null && qparam.getLayoutDataList() != null && qparam.getLayoutDataList().size() > 0) {
                Map<Integer, List<QuerySelectDataListParam>> m = new HashMap();
                Map<Integer, List> coordinateMap = new HashMap();
                Map<Integer, List> beforeCoordinateMap = new HashMap();
                List<QuerySelectDataListParam> list = new ArrayList<>();
                //用instanceId区分
                List<List<Integer>> coordinateList = new ArrayList();
                List<List<Integer>> beforeList = new ArrayList();
                for (QuerySelectDataListParam param : qparam.getLayoutDataList()) {
                    if (m.containsKey(param.getInstanceId())) {
                        list = m.get(param.getInstanceId());
                        list.add(param);
                        coordinateList = coordinateMap.get(param.getInstanceId());
                        beforeList = beforeCoordinateMap.get(param.getInstanceId());
                        coordinateList.add(param.getCoordinate());
                        beforeList.add(param.getBeforeCoordinate());
                        //获取相同instanceId下的所有修改坐标
                        coordinateMap.put(param.getInstanceId(), coordinateList);
                        //获取相同instanceId实例的所有修改前坐标
                        beforeCoordinateMap.put(param.getInstanceId(), beforeList);
                        m.put(param.getInstanceId(), list);
                    } else {
                        list = new ArrayList<>();
                        list.add(param);
                        coordinateList = new ArrayList();
                        beforeList = new ArrayList();
                        coordinateList.add(param.getCoordinate());
                        beforeList.add(param.getBeforeCoordinate());
                        coordinateMap.put(param.getInstanceId(), coordinateList);
                        beforeCoordinateMap.put(param.getInstanceId(), beforeList);
                        m.put(param.getInstanceId(), list);
                    }
                }
                m.forEach((k, v) -> {
                    List<QuerySelectDataListParam> listInfo = v;
                    if (v != null && v.size() > 0) {
                        QuerySelectDataListParam param = v.get(0);
                        List<String> fieldList = Arrays.asList("layoutData", "position");
                        List<Map<String, Object>> roomLayout = getModelInstanceInfoByEs(param.getModelIndex(), param.getInstanceId(), fieldList);
                        List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();

                        List<List<Integer>> coordinateLists = coordinateMap.get(k);
                        List<List<Integer>> beforecoordinateLists = beforeCoordinateMap.get(k);

                        String esId = "";
                        List lists = new ArrayList();
                        for (Map<String, Object> map : roomLayout) {
                            if (map != null && map.get("esId") != null) {
                                esId = map.get("esId").toString();
                            }
                            if (map != null && map.get("layoutData") != null) {
                                Object obj = map.get("layoutData");
                                lists = JSONArray.parseArray(JSONArray.toJSONString(obj), List.class);
                            }
                        }
                        //修改前机房坐标
                        for (int x = 0; x < lists.size(); x++) {
                            List<QueryLayoutDataParam> list2 = new ArrayList();
                            List<QueryLayoutDataParam> listNew = new ArrayList();
                            list2 = JSONArray.parseArray(JSONObject.toJSONString(lists.get(x)), QueryLayoutDataParam.class);
                            for (int y = 0; y < list2.size(); y++) {
                                //先将修改前的坐标删除

                                if (beforecoordinateLists != null && beforecoordinateLists.size() > 0 && beforecoordinateLists.get(0) != null) {
                                    for (List<Integer> beforeCoordinate : beforecoordinateLists) {
                                        if (x == (beforeCoordinate.get(0)) && (y == (beforeCoordinate.get(1)))) {
                                            //删除
                                            list2.get(y).setIsSelected(false);
                                        }
                                    }
                                }
                                //修改后的坐标新增
                                if (coordinateLists != null && coordinateLists.size() > 0 && coordinateLists.get(0) != null) {
                                    for (List<Integer> coordinate : coordinateLists) {
                                        if ((x == (coordinate.get(0))) && (y == (coordinate.get(1)))) {
                                            //新增
                                            list2.get(y).setIsSelected(true);
                                        }
                                    }
                                }
                                listNew.add(list2.get(y));
                            }
                            layoutData.add(listNew);
                        }
                        //更新es中机房的布局数据
                        updateModelInstanceInfoByEs(param.getModelIndex(), esId, 16, "layoutData", layoutData);
                    }
                });
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateRoomLayout param{}, case by {}", qparam, e);
            return Reply.fail(500, "更新机房布局数据失败");
        }
    }

    @Override
    public UpdateRequest getUpdateRequestToES(MwModelInstanceParam param) {
        return null;
    }

    @Override
    public Reply cleanFieldValueToEs(List<MwModelInstanceParam> paramList) {
        return null;
    }

    /**
     * 修改机柜布局数据
     */
    @Override
    public Reply updateCabinetLayout(QueryCabinetLayoutListParam params) {
        try {
            if (params.getCabinetLayoutList() != null && params.getCabinetLayoutList().size() > 0) {
                Map<Integer, List<QueryCabinetLayoutParam>> m = new HashMap();
                Map<Integer, List> lastDataMap = new HashMap();
                Map<Integer, List> currentDataMap = new HashMap();
                List<QueryCabinetLayoutParam> list = new ArrayList<>();
                List<CabinetLayoutDataParam> lastDataList = new ArrayList<>();
                List<CabinetLayoutDataParam> currentDataList = new ArrayList<>();

                //相同所属机柜的实例，对修改的位置进行整合，一次性修改布局数据
                for (QueryCabinetLayoutParam param : params.getCabinetLayoutList()) {
                    if (m.containsKey(param.getInstanceId())) {
                        list = m.get(param.getInstanceId());
                        list.add(param);
                        lastDataList = lastDataMap.get(param.getInstanceId());
                        currentDataList = currentDataMap.get(param.getInstanceId());
                        lastDataList.add(param.getLastData());
                        currentDataList.add(param.getCurrentData());
                        //获取相同instanceId下的所有修改位置
                        lastDataMap.put(param.getInstanceId(), lastDataList);
                        //获取相同instanceId实例的所有修改前位置
                        currentDataMap.put(param.getInstanceId(), currentDataList);
                        m.put(param.getInstanceId(), list);
                    } else {
                        list = new ArrayList<>();
                        list.add(param);
                        lastDataList = new ArrayList();
                        currentDataList = new ArrayList();
                        lastDataList.add(param.getLastData());
                        currentDataList.add(param.getCurrentData());
                        lastDataMap.put(param.getInstanceId(), lastDataList);
                        currentDataMap.put(param.getInstanceId(), currentDataList);
                        m.put(param.getInstanceId(), list);
                    }
                }

                m.forEach((k, v) -> {
                    List<QueryCabinetLayoutParam> listInfo = v;
                    if (v != null && v.size() > 0) {
                        QueryCabinetLayoutParam param = v.get(0);
                        List<CabinetLayoutDataParam> lastDataLists = lastDataMap.get(k);
                        List<CabinetLayoutDataParam> currentDataLists = currentDataMap.get(k);

                        //获取机房布局数据
                        List<String> fieldList = Arrays.asList("layoutData");
                        List<Map<String, Object>> layoutData = getModelInstanceInfoByEs(param.getModelIndex(), param.getInstanceId(), fieldList);
                        List<CabinetLayoutDataParam> cabinetLayoutInfo = new ArrayList<>();
                        String esId = "";
                        for (Map<String, Object> map : layoutData) {
                            if (map != null && map.get("esId") != null) {
                                esId = map.get("esId").toString();
                            }
                            if (map != null && map.get("layoutData") != null) {
                                Object obj = map.get("layoutData");
                                cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(obj), CabinetLayoutDataParam.class);
                            }
                        }
                        Integer lastStart = null;
                        Integer lastEnd = null;
                        Integer currentStart = null;
                        Integer currentEnd = null;

                        if (lastDataLists != null && lastDataLists.size() > 0 && lastDataLists.get(0) != null) {
                            for (CabinetLayoutDataParam cabinetParam : lastDataLists) {
                                if (cabinetParam.getStart() != null && cabinetParam.getEnd() != null) {
                                    lastStart = cabinetParam.getStart();
                                    lastEnd = cabinetParam.getEnd();
                                    int index = 0;
                                    int indexNum = 0;
                                    Boolean isFlag = true;
                                    Iterator<CabinetLayoutDataParam> layoutInfo = cabinetLayoutInfo.iterator();
                                    while (layoutInfo.hasNext()) {
                                        CabinetLayoutDataParam cParam = layoutInfo.next();
                                        //循环布局数据，和上次保存数据开始结束位置匹配时，删除该条数据
                                        if (cParam.getStart() == lastStart && cParam.getEnd() == lastEnd) {
                                            layoutInfo.remove();
                                        }
                                        if ((cParam.getStart() == lastStart && cParam.getEnd() == lastEnd) && isFlag) {
                                            indexNum = index;
                                            isFlag = false;
                                        }
                                        index++;
                                    }
                                    if (!isFlag) {
                                        //多层数据删除后，恢复单成数据模式
                                        for (int y = 0; y <= (lastEnd - lastStart); y++) {
                                            CabinetLayoutDataParam c = new CabinetLayoutDataParam();
                                            c.setStart(lastStart + y);
                                            c.setEnd(lastStart + y);
                                            c.setIsUsed(false);
                                            c.setInfo(new QueryAssetsListParam());
                                            cabinetLayoutInfo.add(indexNum + y, c);
                                        }
                                    }
                                }
                            }
                        }
                        if (currentDataLists != null && currentDataLists.size() > 0 && currentDataLists.get(0) != null) {
                            for (CabinetLayoutDataParam cabinetParam : currentDataLists) {
                                if (cabinetParam.getStart() != null && cabinetParam.getStart() != null) {
                                    currentStart = cabinetParam.getStart();
                                    currentEnd = cabinetParam.getEnd();
                                    int index = 0;
                                    int indexNum = 0;
                                    Boolean isFlag = true;
                                    Iterator<CabinetLayoutDataParam> currentLayoutInfo = cabinetLayoutInfo.iterator();
                                    while (currentLayoutInfo.hasNext()) {
                                        CabinetLayoutDataParam cParam = currentLayoutInfo.next();
                                        //循环布局数据，和本次保存数据开始结束位置匹配时，删除该条数据
                                        if (currentStart <= cParam.getStart() && cParam.getStart() <= currentEnd) {
                                            currentLayoutInfo.remove();
                                        }
                                        if (currentStart <= cParam.getStart() && isFlag) {
                                            isFlag = false;
                                            indexNum = index;
                                        }
                                        index++;
                                    }
                                    cabinetLayoutInfo.add(indexNum, param.getCurrentData());
                                }
                            }
                        }
                        updateModelInstanceInfoByEs(param.getModelIndex(), esId, 16, "layoutData", cabinetLayoutInfo);
                    }
                });
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to getRoomAndCabinetLayout param{}, case by {}", params, e);
            return Reply.fail(500, "获取机房机柜布局数据失败");
        }
    }

    /**
     * 修改es中指定字段数据
     *
     * @param modelIndex
     * @param esId
     * @param layoutData
     */
    private void updateModelInstanceInfoByEs(String modelIndex, String esId, Integer propertiesType, String
            propertiesIndexId, Object layoutData) {
        AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
        instanceParam.setModelIndex(modelIndex);
        instanceParam.setEsId(esId);
        AddModelInstancePropertiesParam param1 = new AddModelInstancePropertiesParam();
        param1.setPropertiesType(propertiesType);
        param1.setPropertiesIndexId(propertiesIndexId);
        if (typeList.contains(propertiesType)) {
            param1.setPropertiesValue(JSONArray.toJSONString(layoutData));
        } else {
            param1.setPropertiesValue(JSONObject.toJSONString(layoutData));
        }
        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
        propertiesParamList.add(param1);
        instanceParam.setPropertiesList(propertiesParamList);
        updateModelInstanceByRoomLayout(instanceParam);
    }

    /**
     * 获取es中指定字段数据
     *
     * @param modelIndex
     */
    private List<Map<String, Object>> getModelInstanceInfoByEs(String modelIndex, Integer
            instanceId, List<String> fieldList) {
        QueryModelInstanceParam params = new QueryModelInstanceParam();
        params.setModelIndex(modelIndex);
        params.setInstanceIdList(Arrays.asList(instanceId));
        //指定返回机柜布局数据
        params.setFieldList(fieldList);
        List<Map<String, Object>> layoutData = getModelInstanceDataByInstanceId(params);
        return layoutData;
    }


    /**
     * 获取机房机柜布局
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRoomAndCabinetLayout(QueryInstanceModelParam param) {
        try {
            String modelIndex = param.getModelIndex();
            Integer instanceId = param.getInstanceId();
            QueryModelInstanceParam params = new QueryModelInstanceParam();
            params.setModelIndex(modelIndex);
            params.setInstanceIdList(Arrays.asList(instanceId));
            //指定返回机房机柜布局数据
            params.setFieldList(Arrays.asList("layoutData"));
            List<Map<String, Object>> modelLayout = getModelInstanceDataByInstanceId(params);
            return Reply.ok(modelLayout);
        } catch (Exception e) {
            log.error("fail to getRoomAndCabinetLayout param{}, case by {}", param, e);
            return Reply.fail(500, "获取机房机柜布局数据失败");
        }
    }

    /**
     * 根据instanceId返回指定字段数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getModelInstanceDataByInstanceId(QueryModelInstanceParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(param.getModelIndex())) {
                //条件组合查询
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                if (param.getInstanceIdList() != null && param.getInstanceIdList().size() > 0) {
                    for (Integer instanceId : param.getInstanceIdList()) {
                        queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", instanceId));
                    }
                }
                queryBuilder.must(queryBuilder1);
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                searchSourceBuilder.size(param.getPageSize());
                //返回指定字段数据
                String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);
                FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                searchSourceBuilder.fetchSource(sourceContext);
                //设置超时时间
                searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.source(searchSourceBuilder);
                searchRequest.indices(param.getModelIndex());
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//                restHighLevelClient
                for (SearchHit searchHit : search.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        } catch (Exception e) {
            log.error("fail to getModelInstanceDataByInstanceId param{}, case by {}", param, e);
        }
        return listMap;
    }

    public Reply updateModelInstanceByRoomLayout(AddAndUpdateModelInstanceParam instanceParam) {
        try {
            AddAndUpdateModelInstanceParam finalParam = instanceParam;
            instanceParam.getPropertiesList().forEach(properties -> {
                if (properties.getPropertiesType() != null) {
                    Integer type = properties.getPropertiesType();
                    //时间类型
                    if (type == ModelPropertiesType.DATE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                            setESMappingByDate(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.STRUCE.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型为结构体时，设置为es嵌套类型
                            setESMappingByStruct(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                    if (type == ModelPropertiesType.SWITCH.getCode()) {
                        Boolean isExist = checkFieldExist(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        if (!isExist) {
                            //数据类型开关型，设置为esBoolean
                            setESMappingByBoolean(finalParam.getModelIndex(), properties.getPropertiesIndexId());
                        }
                    }
                }
            });
//            BulkRequest bulkRequest = new BulkRequest();
//            bulkRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));

            UpdateRequest updateRequest = new UpdateRequest(instanceParam.getModelIndex(), instanceParam.getEsId());
            updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            Map<String, Object> jsonMap = new HashMap<>();
            instanceParam.getPropertiesList().forEach(properties -> {
                        if (properties.getPropertiesType() != null) {
                            Integer type = properties.getPropertiesType();
                            if (typeList.contains(type)) {//type类型为10、11、12、13,16都为数组类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.STRUCE.getCode()) {//type类型6 为结构体类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), JSONArray.parse(properties.getPropertiesValue()));
                                }
                            } else if (type == ModelPropertiesType.DATE.getCode()) {//type类型8 为时间类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                                }
                            } else if (type == ModelPropertiesType.SWITCH.getCode()) {//布尔类型
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    jsonMap.put(properties.getPropertiesIndexId(), Boolean.parseBoolean(properties.getPropertiesValue()));
                                }
                            } else {
                                jsonMap.put(properties.getPropertiesIndexId(), properties.getPropertiesValue());
                            }
                        }
                    }
            );
            updateRequest.doc(jsonMap);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

//            IndexRequest newRequest = new IndexRequest(finalParam.getModelIndex()).id(finalParam.getModelIndex() + finalParam.getInstanceId()).source(jsonMap);
//            bulkRequest.add(newRequest);
//            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("fail to updateModelInstanceByRoomLayout param{}, case by {}", instanceParam, e);
            return Reply.fail(500, "更新机房布局数据失败");
        }
        return Reply.ok();
    }

    /**
     * 获取机房下每个机柜信息
     */
    public Reply getAllCabinetInfoByRoom(QueryInstanceModelParam param) {
        Map<String, Map> mapInfo = new HashMap();
        //通过机房id 去查询所有机柜实例
        try {
            List<Map<String, Object>> list = getRelationModelInfo(param);
            for (Map<String, Object> map : list) {
                String position = JSONObject.toJSONString(map.get("position"));
                mapInfo.put(position, map);
            }
            return Reply.ok(mapInfo);
        } catch (IOException e) {
            log.error("fail to getAllCabinetInfoByRoom param{}, case by {}", param, e);
            return Reply.fail(500, "获取机房里每个机柜的信息数据失败");
        }
    }

    /**
     * 根据模型id获取所有模型关系关联数据
     *
     * @return
     */
    @Override
    public Reply getModelRelationInfo(QueryInstanceRelationToPoParam param) {
        try {
            List<Map> mapList = new ArrayList<>();
            Map relationMap = new HashMap();
            if (param.getOwmRelationsParam() != null) {
                QueryInstanceRelationsParam qparam = param.getOwmRelationsParam();
                List<Map> edgesLastInfo = new ArrayList<>();
                List<Map> combosLastInfo = new ArrayList<>();
                if (param.getLastData() != null) {
                    Map<String, List> lastDataAll = (Map) param.getLastData();
                    edgesLastInfo = lastDataAll.get("edges");
                    combosLastInfo = lastDataAll.get("combos");
                }
//                if (edgesLastInfo.size() == 0 && combosLastInfo.size() == 1) {
//                    Integer modelId = Integer.valueOf(combosLastInfo.get(0).get("id").toString());
                mapList = mwModelInstanceDao.getModelRelationInfo(null, qparam.getModelId());
//                }
                //默认所有模型只有一个上级
                List<String> sourceList = new ArrayList<>();
                //由于前端新增实例数据时，获取不了 关联的模型Id和实例id
                //根据参数relationInstanceList中的新增实例的modelId，去lastData的edges中获取source
                for (Map edge : edgesLastInfo) {
                    //循环获取
                    String target = edge.get("target").toString();
                    String[] targetStr = target.split("_");
                    if (targetStr.length > 1 && targetStr[0].equals(qparam.getModelId() + "")) {
                        String source = edge.get("source").toString();
                        String lastModelId = source.split("_")[0];
                        sourceList.add(lastModelId);
                    }
                }
                //对combos数据去重
                List<Map> modelIdDistinctList = combosLastInfo.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.get("id").toString()))), ArrayList::new));
                List<String> modelIds = new ArrayList<>();
                //获取所有已存在的模型id
                for (Map m : modelIdDistinctList) {
                    modelIds.add(m.get("id").toString());
                }
                List<String> distinctList = sourceList.stream().distinct().collect(Collectors.toList());
                if (distinctList != null && distinctList.size() > 0) {
                    mapList = mwModelInstanceDao.getModelRelationInfo(Integer.valueOf(distinctList.get(0)), qparam.getModelId());
                }
                //现阶段，暂定：拓扑实例中，所有的模型只能出现一次，避免出现一个模型有多个上级的情况
                Iterator<Map> it = mapList.iterator();
                //去除已出现的模型数据
                while (it.hasNext()) {
                    Map m = it.next();
                    if (modelIds.contains(m.get("oppositeModelId").toString())) {
                        it.remove();
                    }
                }
            }
            return Reply.ok(mapList);
        } catch (Exception e) {
            log.error("fail to getModelRelationInfo param{}, case by {}", e);
            return Reply.fail(500, "根据模型id获取所有模型关系关联数据");
        }
    }

    @Override
    public Reply instanceRelationBrowse(QueryInstanceRelationToPoParam param) {
        return null;
    }

    @Override
    public Reply instanceRelationLink(QueryInstanceRelationToPoParam param) {
        return null;
    }

    @Override
    public Reply getInstanceListByModelId(QueryInstanceModelParam params) {
//        QueryModelInstanceParam param = new QueryModelInstanceParam();
//        MwModelInfoDTO m = mwModelInstanceDao.getModelIndexInfo(params.getModelId());
//        String modelIndex = "";
//        if (m != null) {
//            modelIndex = m.getModelIndex();
//        }
//        param.setModelIndex(modelIndex);
//        param.setModelId(params.getModelId());
//        param.setPageSize(pageSize);
//        Reply reply = selectModelInstance(param);
//        List instanceList = (List) ((PageInfo) reply.getData()).getList();
//        //去除实例拓扑中已选择的实例
//        Iterator<Map> its = instanceList.iterator();
//        while (its.hasNext()) {
//            Map s = its.next();
//            if (params.getInstanceIds().contains(Integer.valueOf(s.get("modelInstanceId").toString()))) {
//                its.remove();
//            }
//        }
//        return reply;
        return null;
    }

    @Override
    public Reply batchUpdateModelInstance(List<AddAndUpdateModelInstanceParam> instanceParams) {
        return null;
    }

    @Override
    public Integer selectCountInstances() {
        Integer num = mwModelInstanceDao.selectCountInstances();
        return num;
    }

    private List<Map<String, Object>> getRelationModelInfo(QueryInstanceModelParam param) throws IOException {
        List<Map<String, Object>> listMap = new ArrayList<>();
        //条件组合查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("relationSite", param.getInstanceId()));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
        searchSourceBuilder.size(pageSize);
        //返回指定字段数据
//        String[] includes = fieldList.toArray(new String[fieldList.size()]);
//        FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
//        searchSourceBuilder.fetchSource(sourceContext);
        //设置超时时间
        searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices("mw_*");
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit searchHit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put("esId", searchHit.getId());
            listMap.add(sourceAsMap);
        }
        return listMap;
    }


    /**
     * @param addUpdModelInstanceContexts 实例数据上下文List
     * @param isLicense        是否许可控制
     * @param isPower          是否用户权限控制
     * @throws ModelManagerException
     */
    @Transactional
    public void saveData(List addUpdModelInstanceContexts, Boolean isLicense, Boolean
            isPower) throws ModelManagerException {
        try {
            List<AddAndUpdateModelInstanceParam> instanceInfoList = null;
            Object dataType = addUpdModelInstanceContexts.get(0);
            if(dataType instanceof AddUpdModelInstanceContext){
                List<AddUpdModelInstanceContext> tranformContext = (List<AddUpdModelInstanceContext>)addUpdModelInstanceContexts;
                instanceInfoList = tranformContext.stream().map(AddUpdModelInstanceContext::getAddAndUpdateModelInstanceParam).collect(Collectors.toList());
            }else{
                instanceInfoList = addUpdModelInstanceContexts;
            }


            //判断新增实例数量是否达到上限
            if (isLicense != null && isLicense) {
                Integer count = selectCountInstances();
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("model_manage", count, instanceInfoList.size());
                if (responseBase.getRtnCode() != 200) {
                    throw new ModelManagerException("该模块新增数量已达许可数量上限！");
                }
            }
            //先向数据库中插入instance名称
            mwModelInstanceDao.insertInstanceName(instanceInfoList);
            //在es数据库插入数据
            BulkRequest bulkRequest = new BulkRequest();
            //获取所有模型的groupNodes
            List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
            Map<String, String> groupNodeMap = new HashMap();
            for (MwModelManageDTO manageDTO : groupNodeList) {
                groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
            }

            instanceInfoList.forEach(properties -> {
                        List<Integer> userId = new ArrayList<>();
                        List<List<Integer>> orgId = new ArrayList<>();
                        List<Integer> groupId = new ArrayList<>();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("modelId", properties.getModelId());
                        hashMap.put("modelInstanceId", properties.getInstanceId());
                        hashMap.put("modelIndex", properties.getModelIndex());
                        if (properties.getRelationInstanceId() != null) {
                            hashMap.put("relationInstanceId", properties.getRelationInstanceId());
                        }
                        String groupNodes = groupNodeMap.get(properties.getModelIndex());
                        hashMap.put("groupNodes", groupNodes);
                        AddAndUpdateModelInstanceParam finalParam = properties;
                        properties.getExportPropertiesList().forEach(p -> {
                            if (p.getPropertiesType() != null) {
                                Integer type = p.getPropertiesType();
                                //字符串类型
                                if (type == ModelPropertiesType.STRING.getCode()) {
                                    Boolean isExist = checkFieldExist(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    if (!isExist) {
                                        setESMappingByString(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    }
                                }
                                //时间类型
                                if (type == ModelPropertiesType.DATE.getCode()) {
                                    Boolean isExist = checkFieldExist(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    if (!isExist) {
                                        //数据类型为时间格式时，设置es的Mapping时间格式yyyy-MM-dd HH:mm:ss
                                        setESMappingByDate(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    }
                                }
                                if (type == ModelPropertiesType.STRUCE.getCode()) {
                                    Boolean isExist = checkFieldExist(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    if (!isExist) {
                                        //数据类型为结构体时，设置为es嵌套类型
                                        setESMappingByStruct(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    }
                                }
                                if (type == ModelPropertiesType.SWITCH.getCode()) {
                                    Boolean isExist = checkFieldExist(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    if (!isExist) {
                                        //数据类型为Boolean时，设置为es嵌套类型
                                        setESMappingByBoolean(finalParam.getModelIndex(), p.getPropertiesIndexId());
                                    }
                                }
                            }
                            //西藏邮储环境，添加资产模型权限控制
                            if (isFlag) {
                                if (p.getPropertiesType() != null) {
                                    Integer type = p.getPropertiesType();
                                    if (type == ModelPropertiesType.ORG.getCode()) {//type类型 11 机构/部门
                                        if (!Strings.isNullOrEmpty(p.getPropertiesValue().toString())) {
                                            List<List<Integer>> list = (List) JSONArray.parse(p.getPropertiesValue().toString());
                                            orgId.addAll(list);
                                        }
                                    } else if (type == ModelPropertiesType.USER.getCode()) {//type类型12 负责人
                                        if (!Strings.isNullOrEmpty(p.getPropertiesValue().toString())) {
                                            List<Integer> list = (List) JSONArray.parse(p.getPropertiesValue().toString());
                                            userId.addAll(list);
                                        }
                                    } else if (type == ModelPropertiesType.GROUP.getCode()) {//type类型13 用户组
                                        if (!Strings.isNullOrEmpty(p.getPropertiesValue().toString())) {
                                            groupId.addAll((List<? extends Integer>) JSONArray.parse(p.getPropertiesValue().toString()));
                                        }
                                    }
                                }
                            }
                            if (p.getPropertiesType() != null && (p.getPropertiesValue() != null)) {
                                Integer type = p.getPropertiesType();
                                if (ModelPropertiesType.SINGLE_ENUM.getCode() < type && type < ModelPropertiesType.ENCLOSURE_IMG.getCode()) {//type类型为10、11、12、13都为数组类型
                                    hashMap.put(p.getPropertiesIndexId(), JSONArray.parse(p.getPropertiesValue().toString()));
                                } else if (type == ModelPropertiesType.DATE.getCode() && Strings.isNullOrEmpty(p.getPropertiesValue().toString())) {
                                } else {
                                    hashMap.put(p.getPropertiesIndexId(), p.getPropertiesValue());
                                }
                                p = new ModeInstanceExportParam();
                            }

                        });
                        //西藏邮储环境，添加资产模型权限控制
                        if (isFlag) {
                            properties.setUserIds(userId);
                            properties.setGroupIds(groupId);
                            properties.setOrgIds(orgId);
                        }
                        if (isPower != null && isPower) {
                            //设置负责人，用户组，机构/部门
                            if (properties.getUserIds() != null || properties.getOrgIds() != null || properties.getGroupIds() != null) {
                                ModelPermControlParam controlParam = new ModelPermControlParam();
                                controlParam.setUserIds(properties.getUserIds());
                                controlParam.setOrgIds(properties.getOrgIds());
                                controlParam.setGroupIds(properties.getGroupIds());
                                controlParam.setId(String.valueOf(properties.getInstanceId()));
                                controlParam.setType(DataType.INSTANCE_MANAGE.getName());
                                controlParam.setDesc(DataType.INSTANCE_MANAGE.getDesc());
                                mwModelManageService.addMapperAndPerm(controlParam);
                            }
                        }
                        //设置修改人修改时间。
                        hashMap.put("modifier", iLoginCacheInfo.getLoginName());
                        hashMap.put("modificationDate", DateUtils.formatDateTime(new Date()));
                        //设置创建人创建时间。
                        hashMap.put("creator", iLoginCacheInfo.getLoginName());
                        hashMap.put("createDate", DateUtils.formatDateTime(new Date()));
                        JSONObject json = (JSONObject) JSONObject.toJSON(hashMap);
                        IndexRequest newRequest = new IndexRequest(properties.getModelIndex()).id(properties.getModelIndex() + properties.getInstanceId()).source(json);
                        bulkRequest.add(newRequest);
                    }
            );
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("异步执行批量添加模型属性到es中失败", e.getMessage());
            throw new ModelManagerException("异步执行批量添加模型属性到es中失败");
        }
    }

    /**
     * 根据modelIndexs 和InstanceIds 获取实例数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getInstanceInfoByModelId(QueryInstanceModelParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            if (param.getModelIndexs() != null && param.getModelIndexs().size() > 0) {
                //条件组合查询
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                for (String modelIndex : param.getModelIndexs()) {
                    queryBuilder = queryBuilder.should(QueryBuilders.termQuery("modelIndex", modelIndex));
                }
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                if (param.getInstanceIds() != null && param.getInstanceIds().size() > 0) {
                    for (Integer instanceId : param.getInstanceIds()) {
                        queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery("modelInstanceId", instanceId));
                    }
                }
                queryBuilder.must(queryBuilder1);
                //全字段模糊查询
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                searchSourceBuilder.size(pageSize);
                //设置超时时间
                searchSourceBuilder.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                if ((param.getFieldList() != null && param.getFieldList().size() > 0)) {
                    //返回指定字段数据
                    String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);

                    FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                    searchSourceBuilder.fetchSource(sourceContext);
                }
                if (param.getNoFieldList() != null && param.getNoFieldList().size() > 0) {
                    //不返回指定字段数据
                    String[] excludes = param.getNoFieldList().toArray(new String[param.getNoFieldList().size()]);
                    FetchSourceContext sourceContext = new FetchSourceContext(true, null, excludes);
                    searchSourceBuilder.fetchSource(sourceContext);
                }
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.source(searchSourceBuilder);
                searchRequest.indices(String.join(",", param.getModelIndexs()));
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit searchHit : search.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    listMap.add(sourceAsMap);
                }
            }
        } catch (Exception e) {
            log.error("getInstanceInfoByModelId to fail");
        }
        return listMap;
    }

    @Override
    public List<MwModelInstanceCommonParam> selectModelInstanceInfoById(Integer modelId) {
        return null;
    }

    @Override
    public Reply batchCreatModelInstance(Object batchInstanceList, Integer types) {
        return null;
    }


    /**
     * 通过modelIndexs和instanceIds批量删除es实例数据
     *
     * @param param
     * @return
     */
    public Reply batchDeleteInstanceInfo(DeleteModelInstanceParam param) {
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(String.join(",", param.getModelIndexs()));
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            if (param.getInstanceIds() != null && param.getInstanceIds().size() > 0) {
                for (Integer instanceId : param.getInstanceIds()) {
                    queryBuilder = queryBuilder.should(QueryBuilders.termQuery("modelInstanceId", instanceId));
                }
            }
            //实例同步数据删除，要根据依附的实例id作为标准删除
            if (param.getRelationInstanceId() != null) {
                queryBuilder = queryBuilder.should(QueryBuilders.termQuery("relationInstanceId", param.getRelationInstanceId()));
            }
            deleteByQueryRequest.setQuery(queryBuilder);
            restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
            if (param.getInstanceIds() != null && param.getInstanceIds().size() > 0) {
                mwModelInstanceDao.deleteBatchInstanceById(param.getInstanceIds());
            } else {
                mwModelInstanceDao.deleteBatchInstanceByIndex(param.getModelIndexs(), param.getRelationInstanceId());
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("batchDeleteInstanceInfo {}", e);
            return Reply.fail(500, "批量删除失败");
        }
    }

    @Override
    public List<Map<String, Object>> getInstanceInfoByProperties(QueryInstanceModelParam param) throws Exception {
        return null;
    }

    @Override
    public Reply batchUpdateHostState(Integer serverId, List<String> hostIds, Integer status) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getInstanceInfoByPropertiesValue(QueryInstanceModelParam param) throws Exception{
        return null;
    }

    @Override
    public List<QueryInstanceParam> getCabinetInfoByRelationCabinedId(List<Integer> instanceIds) {
        return null;
    }

    @Override
    public List<QueryInstanceParam> getAllInstanceNameById(List<Integer> instanceIds) {
        return null;
    }

    @Override
    public void esDataRefresh(ModelParam modelParams) {

    }

    @Override
    public Reply getInstanceIdByLinkRelation(QueryInstanceTopoInfoParam param) throws Exception {
        return null;
    }

    @Override
    public Reply syncAllInstanceLinkRelation() {
        return null;
    }

    @Override
    public Reply setModelAreaDataToEs() {
        return null;
    }

    @Override
    public List<AddAndUpdateModelInstanceParam> batchInsertWebMonitorInstance(List<AddAndUpdateModelInstanceParam> paramList) throws Exception {
        return null;
    }

    @Override
    public List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId) {
        return null;
    }

    @Override
    public List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId, Integer relationId) {
        return null;
    }



    @Override
    public Reply getModelPropertiesById(Integer modelId) {
        return null;
    }

    @Override
    public Reply selectAllModelMonitorItem() {
        return null;
    }

    @Override
    public Reply settingConfigPowerByIp(SettingConfigPowerParam param) {
        return null;
    }

    @Override
    public Reply getSettingConfigPowerByIp() {
        return null;
    }

    @Override
    public Reply getModelInfoParamById(Integer modelId) {
        return null;
    }

    @Override
    public Map<String, Object> selectInfoByInstanceId(Integer instanceId) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllInstanceInfoByBase() {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectInfosByModelId(Integer modelId) {
        return null;
    }
}
