package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.api.exception.AssetsException;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.graph.modelAsset.ComboParam;
import cn.mw.monitor.graph.modelAsset.LastData;
import cn.mw.monitor.graph.modelAsset.ModelAsset;
import cn.mw.monitor.model.dao.*;
import cn.mw.monitor.model.data.AddUpdModelInstanceContext;
import cn.mw.monitor.model.data.InstanceSyncContext;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.ModelAssetMonitorState;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.*;
import cn.mw.monitor.model.util.ModelUtils;
import cn.mw.monitor.model.view.InstanceTopoView;
import cn.mw.monitor.model.view.IntanceTopoSelView;
import cn.mw.monitor.model.view.ModelAssociatedView;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwAgentAssetsDTO;
import cn.mw.monitor.service.assets.model.MwPortAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.ModelMacrosParam;
import cn.mw.monitor.service.assets.param.MwIPMIAssetsDTO;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.dto.*;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.*;
import cn.mw.monitor.service.model.util.ExportExcelUtil;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.systemLog.api.MwSysLogService;
import cn.mw.monitor.service.systemLog.dto.SysLogDTO;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.EmailSendUtil;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
import static cn.mw.monitor.service.model.service.ModelPropertiesType.*;
import static cn.mw.monitor.service.model.service.ModelRoomField.LAYOUTDATA;
import static cn.mw.monitor.service.model.service.ModelRoomField.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author xhy
 * @date 2021/2/25 9:10
 */
@Service
@Slf4j
@Primary
public class MwModelInstanceServiceImplV1 implements MwModelInstanceService, ModelServer {
    //操作日志记录
    private static final Logger mwlogger = LoggerFactory.getLogger("MWDBLogger");

    @Value("${model.instance.batchFetchNum}")
    private int insBatchFetchNum;
    //数组类型
    static List typeList = Arrays.asList(
            ModelPropertiesType.STRUCE.getCode(),
            ModelPropertiesType.MULTIPLE_ENUM.getCode(),
            ModelPropertiesType.MULTIPLE_RELATION.getCode(),
            ORG.getCode(),
            USER.getCode(),
            GROUP.getCode(),
            ModelPropertiesType.LAYOUTDATA.getCode());

    //字符串类型
    static List arrList = Arrays.asList(
            ModelPropertiesType.STRING.getCode(),
            ModelPropertiesType.SINGLE_RELATION.getCode(),
            ModelPropertiesType.IP.getCode(),
            ModelPropertiesType.SINGLE_ENUM.getCode());
    //不能为空字符串类型
    static List notEmptyList = Arrays.asList(
            ModelPropertiesType.DATE.getCode());

    static List checkModelList = Arrays.asList(
            ConnectCheckModelEnum.VCENTER.getModelId(),
            ConnectCheckModelEnum.CITRIXADC.getModelId(),
            ConnectCheckModelEnum.RANCHER.getModelId()
    );

    //web监测模型Id
    private static Integer webMonitorModeId = 72;
    //web监测分组Id
    private static String webMonitorGroupId = ",11,";

    private static String queryLayoutField = "layoutData.info.assetsId";


    private static String queryLayoutBladeField = "layoutData.daoData.instanceId";

    private static String ruleName = "实例过期邮件提醒";

    @Value("${dataBase.server.ipAddress}")
    private String dataBaseIp;
    @Value("${dataBase.server.userName}")
    private String dataBaseUserName;
    @Value("${dataBase.server.password}")
    private String dataBasePassword;
    @Value("${dataBase.server.port}")
    private int dataBasePort;
    @Value("${dataBase.server.remoteFilePath}")
    private String remoteFilePath;
    @Value("${dataBase.server.fileName}")
    private String configFileName;
    @Value("${dataBase.server.localFilePath}")
    private String localFilePath;

    @Value("${es.duration.timeNum}")
    private int timeNum;

    @Value("${model.dataBase.config}")
    private boolean dataBaseConfig;

    @Autowired
    private SSHRemoteCall sshRemoteCall;

    @Value("${report.file.url}")
    private String filePath;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwInspectModeService mwInspectModeService;
    @Resource
    private MwModelExportDao mwModelExportDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;
    @Value("${datasource.check}")
    private String DATACHECK;
    @Autowired
    private EmailSendUtil emailSendUtil;
    public static final String localEngine = "localhost";
    static final String LOCALHOST_NAME = "本机";
    public static final String DATEBASEMYSQL = "mysql";
    public static final String DATEBASEORACLE = "oracle";


    @Resource
    private MwModelRelationsDao mwModelRelationsDao;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private LicenseManagementService licenseManagement;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Resource
    private MwModelSysLogDao mwModelSysLogDao;
    @Autowired
    private MwModelSysLogService mwModelSysLogService;

    @Autowired
    private MwModelWebMonitorService mwModelWebMonitorService;
    @Autowired
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

    @Autowired(required = false)
    private ConnectionPool connectionPool;

    @Autowired
    private MwModelRelationsService mwModelRelationsService;

    @Autowired
    private MwModelVirtualizationService mwModelVirtualizationService;
    @Value("${modelSystemParent.modelId}")
    private String modelSystemParentModelId;
    @Value("${file.url}")
    private String imgPath;

    @Value("${model.debug}")
    private boolean debug;

    static final String MODULE = "file-upload";

    //每次滚动查询es数据的条数
    static final int scrollSize = 500;

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
    private MwModelViewServiceImpl mwModelViewServiceImpl;
    @Autowired
    private MwModelAssetsByESService mwModelAssetsByESService;

    @Autowired
    private MwModelInstanceSyncManager mwModelInstanceSyncManager;

    @Autowired
    private MwModelCommonServiceImpl mwModelCommonServiceImpl;

    @Autowired
    MwModelCommonService mwModelCommonService;


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Reply creatModelInstance(Object instanceParam, Integer types) {
        AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
        long time1 = System.currentTimeMillis();
        try {
            //判断是否是流程审批类型
            if (types == 0) {
                param = (AddAndUpdateModelInstanceParam) instanceParam;
            } else {
                param = JSONObject.parseObject(JSONObject.toJSONString(instanceParam), AddAndUpdateModelInstanceParam.class);
            }

            if (debug) {
                log.info(param.toString());
            }
            MwModelInfoDTO dto = null;
            if (param.getModelId() != null) {
                dto = mwModelInstanceDao.getModelIndexInfo(param.getModelId());
                if (dto == null || Strings.isNullOrEmpty(dto.getModelIndex())) {
                    throw new Exception("请选择对应模型");
                }
                if (dto != null && dto.getModelIndex() != null) {
                    param.setModelIndex(dto.getModelIndex());
                }
                if (dto != null && dto.getModelName() != null) {
                    param.setModelName(dto.getModelName());
                }
            }
            //检查是否配置了同步引擎id
            String proxyId = mwModelInstanceSyncManager.findProxyId(param);
            if (StringUtils.isNotEmpty(proxyId)) {
                param.setProxyId(proxyId);
            }
            //查询更新许可，单个资产新增，每次+1；
//            Integer count = selectCountInstances();
//            ResponseBase responseBase = licenseManagement.getLicenseManagemengt("model_manage", count, 1);
//            if (responseBase.getRtnCode() != 200) {
//                throw new Exception("该模块新增数量已达许可数量上限！");
//            }
            Integer relationInstanceId = null;
            if (param.getModelViewType().equals(2) && CollectionUtils.isNotEmpty(param.getPropertiesList())) {
                //机柜实例新增时，设置关联的所属机房Id
                for (AddModelInstancePropertiesParam propertiesParam : param.getPropertiesList()) {
                    if (propertiesParam.getPropertiesIndexId().equals(RELATIONSITEROOM.getField())) {//获取所属机房Id
                        relationInstanceId = intValueConvert(propertiesParam.getPropertiesValue());
                    }
                }
            }
            if (param.getModelViewType().equals(1) && CollectionUtils.isNotEmpty(param.getPropertiesList())) {
                //机房实例新增时，设置关联的所属楼宇Id
                for (AddModelInstancePropertiesParam propertiesParam : param.getPropertiesList()) {
                    if (propertiesParam.getPropertiesIndexId().equals(RELATIONSITEFLOOR.getField())) {//获取所属楼宇Id
                        relationInstanceId = intValueConvert(propertiesParam.getPropertiesValue());
                    }
                }
            }
            synchronized (this) {
                param.setRelationInstanceId(relationInstanceId);
                mwModelManageDao.creatModelInstance(param);
            }
            long time2 = System.currentTimeMillis();
            log.info("实例添加到mysql耗时::" + (time2 - time1) + "ms");
            //不是西藏邮储环境,添加负责人权限
            //设置负责人，用户组，机构/部门
            ModelPermControlParam controlParam = new ModelPermControlParam();
            controlParam.setUserIds(param.getUserIds());
            controlParam.setOrgIds(param.getOrgIds());
            controlParam.setGroupIds(param.getGroupIds());
            controlParam.setId(String.valueOf(param.getInstanceId()));
            controlParam.setType(DataType.INSTANCE_MANAGE.getName());
            controlParam.setDesc(DataType.INSTANCE_MANAGE.getDesc());
            mwModelManageService.addMapperAndPerm(controlParam);
            param.setTargetInstanceId(param.getInstanceId());

            //检查并新增资产扫描信息
            AddModelInstancePropertiesParam extraInfo = new AddModelInstancePropertiesParam();
            extraInfo.extractFrom(param.getManageParam(), param.getInstanceId());
            if (!Strings.isNullOrEmpty(extraInfo.getPropertiesIndexId())) {
                param.getPropertiesList().add(extraInfo);
            }
            //先根据getPropertiesIndexId过滤去重
            List<AddModelInstancePropertiesParam> propertiesList = param.getPropertiesList().stream().filter(data -> data.getPropertiesIndexId() != null).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AddModelInstancePropertiesParam::getPropertiesIndexId))), ArrayList::new));

            Map<String, AddModelInstancePropertiesParam> fieldTypeMap = propertiesList.stream()
                    .collect(Collectors.toMap(AddModelInstancePropertiesParam::getPropertiesIndexId, AddModelInstancePropertiesParam -> AddModelInstancePropertiesParam, (
                            value1, value2) -> {
                        return value2;
                    }));

            //检查是否存在未定义字段
            List<String> notExistFields = MwModelUtils.getNotExistFields(param.getModelIndex(), fieldTypeMap.keySet(), restHighLevelClient);

            //新增未定义字段
            if (null != notExistFields && notExistFields.size() > 0) {
                MwModelUtils.batchSetESMapping(param.getModelIndex(), notExistFields, fieldTypeMap, restHighLevelClient);
            }

            log.info("addInstanceToEs modelIndex:{}, instanceId:{}", param.getModelIndex(), param.getInstanceId());
            if (debug) {
                log.info(propertiesList.toString());
                log.info(fieldTypeMap.toString());
            }
            boolean isWebMonitor = true;
            //web监测模型实例新增，需要特殊处理
            if (webMonitorModeId.intValue() == param.getModelId().intValue()) {
                insertWebMonitorInstance(param);
                //web监测不用进行资产纳管
                isWebMonitor = false;
            }
            addInstanceToEs(param);
            long time3 = System.currentTimeMillis();
            log.info("实例添加到ES库耗时::" + (time3 - time2) + "ms");
            //是否修改数据库配置文件
            if (dataBaseConfig) {
                configEditorDataBase(param);
            }

            if (isWebMonitor) {
                //是否纳管资产
                settingManageInfo(param, true);
            }
            long time4 = System.currentTimeMillis();
            log.info("资产纳管耗时::" + (time4 - time3) + "ms");
            //模型实例变更记录
//        Integer version = mwModelSysLogDao.getChangeHistoryVersion("instance_" + param.getInstanceId());
            if (!Strings.isNullOrEmpty(param.getTargetModelName())) {
                //修改转移前实例的历史记录
                //添加到模型管理日志
                mwModelSysLogService.updateInstaceChangeHistory("instance_" + param.getInstanceId(), "instance_" + param.getOwnInstanceId());
                //转移记录
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.SHIFT_INSTANCE.getName())
                        .objName(param.getModelName() == null ? param.getInstanceName() : param.getModelName() + "/" + param.getInstanceName())
                        .operateDes(OperationTypeEnum.SHIFT_INSTANCE.getName() + ":" + param.getInstanceName() + "：从" + param.getOwnGroupName() + "/" + param.getOwnModelName() + "转移到" + param.getTargetGroupName() + "/" + param.getTargetModelName())
                        .operateDesBefore("").type("instance_" + param.getInstanceId()).version(1).build();
                //添加到系统操作日志
                mwlogger.info(JSON.toJSONString(builder));
                //添加到模型管理日志
                mwModelSysLogService.saveInstaceChangeHistory(builder);
            } else {
                //新增记录
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_INSTANCE.getName())
                        .objName(param.getModelName() == null ? param.getInstanceName() : param.getModelName() + "/" + param.getInstanceName())
                        .operateDes(OperationTypeEnum.CREATE_INSTANCE.getName() + ":" + param.getInstanceName()).operateDesBefore("").type("instance_" + param.getInstanceId()).version(1).build();
                //添加到系统操作日志
                mwlogger.info(JSON.toJSONString(builder));
                //添加到模型管理日志
                mwModelSysLogService.saveInstaceChangeHistory(builder);
            }
        } catch (Throwable throwable) {
            try {
                deleteEsInfoByQuery(Arrays.asList(param.getModelIndex()), Arrays.asList(param.getInstanceId()));
            } catch (Exception e) {
                log.error("新增实例出错时删除es数据失败:{}", e);
            }
            log.error("fail to creatModelInstance with auParam={}, cause:{}", param, throwable);
            throw new ModelManagerException(throwable.getMessage());
        }
        long time5 = System.currentTimeMillis();
        log.info("资产新增总耗时::" + (time5 - time1) + "ms");
        return Reply.ok(param.getInstanceId());
    }

    /**
     * @param param
     */
    private void macrosParamCheck(AddAndUpdateModelInstanceParam param) {
        if (CollectionUtils.isNotEmpty(param.getSyncParams())) {
            List<AddModelInstancePropertiesParam> propertiesList = param.getPropertiesList();
            for (AddModelInstancePropertiesParam propertiesParam : propertiesList) {
                if (MWMACROS_DTO.equals(propertiesParam.getPropertiesIndexId()) && propertiesParam.getPropertiesValue().equals("[]")) {
                    propertiesParam.setPropertiesValue(JSONObject.toJSONString(param.getSyncParams()));
                }
            }
        }

    }

    private void manageParamSetting(AddUpdateTangAssetsParam manageParam
            , AddAndUpdateModelInstanceParam param) {
        manageParam.setAssetsTypeId(param.getModelGroupId());
        manageParam.setAssetsTypeSubId(param.getModelId());
        manageParam.setInstanceId(param.getInstanceId());
        manageParam.setUserIds(param.getUserIds());
        manageParam.setOrgIds(param.getOrgIds());
        manageParam.setGroupIds(param.getGroupIds());
        manageParam.setInstanceName(param.getInstanceName());

    }


    private void addInstanceToEs(AddAndUpdateModelInstanceParam param) {
        SimpleDateFormat UTC2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UTC2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        HashMap<String, Object> hashMap = new HashMap<>();
        //获取所有模型的groupNodes
        List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
        Map<String, String> groupNodeMap = new HashMap();
        for (MwModelManageDTO manageDTO : groupNodeList) {
            groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
        }
        //机房布局坐标
        List<Integer> coordinate = new ArrayList<>();
        List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
        List<Integer> reationRoomInstanceIds = new ArrayList<>();
        List<Integer> reationCabinetInstanceIds = new ArrayList<>();
        param.getPropertiesList().forEach(properties -> {
                    if (properties.getPropertiesType() != null && (!Strings.isNullOrEmpty(properties.getPropertiesValue()))) {
                        if (properties.getPropertiesType() != null) {
                            Integer type = properties.getPropertiesType();
                            ModelPropertiesType modelType = ModelPropertiesType.getTypeByCode(type);
                            if (null != modelType && !Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                hashMap.put(properties.getPropertiesIndexId(), modelType.convertToEsData(properties.getPropertiesValue()));
                            }

                            if (MWMACROS_DTO.equals(properties.getPropertiesIndexId())) {
                                //宏值转换
                                macrosConvert(properties, hashMap);
                            }
                            if (param.getModelViewType() != null && param.getModelViewType() > 0) {
                                if (type.intValue() == ModelPropertiesType.LAYOUTDATA.getCode() && ModelCabinetField.POSITIONBYROOM.getField().equals(properties.getPropertiesIndexId())) {
                                    //type类型为16，获取机柜的位置数据 (机房布局)
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        coordinate.addAll((List) JSONArray.parse(properties.getPropertiesValue()));
                                    }
                                }
                                if (type.intValue() == ModelPropertiesType.SINGLE_RELATION.getCode() && properties.getPropertiesIndexId().indexOf("relationSite") != -1) {
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        if (RELATIONSITEROOM.getField().equals(properties.getPropertiesIndexId())) {
                                            reationRoomInstanceIds.add(intValueConvert(properties.getPropertiesValue()));
                                        }
                                        if (RELATIONSITECABINET.getField().equals(properties.getPropertiesIndexId())) {
                                            reationCabinetInstanceIds.add(intValueConvert(properties.getPropertiesValue()));
                                        }
                                    }
                                }
                                if (type.intValue() == ModelPropertiesType.LAYOUTDATA.getCode() && ModelCabinetField.POSITIONBYCABINET.getField().equals(properties.getPropertiesIndexId())) {
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        CabinetLayoutDataParam cdParam = JSONObject.parseObject(properties.getPropertiesValue(), CabinetLayoutDataParam.class);
                                        QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                        assetsListParam.setAssetsId(param.getInstanceId() + "");
                                        assetsListParam.setAssetsName(param.getInstanceName());
                                        //刀片视图
                                        if (BLADE_VIEW.equals(cdParam.getType()) && CollectionUtils.isNotEmpty(cdParam.getDaoData())) {
                                            String instanceId = strValueConvert(param.getInstanceId());
                                            String instanceName = strValueConvert(param.getInstanceName());
                                            for (List<QueryBladeInstanceParam> list1 : cdParam.getDaoData()) {
                                                for (QueryBladeInstanceParam listParam : list1) {
                                                    if (listParam.getInstanceName().equals(instanceName)) {
                                                        listParam.setInstanceId(instanceId);
                                                    }
                                                }
                                            }
                                        } else {
                                            cdParam.setInfo(assetsListParam);
                                        }
                                        cdParamList.add(cdParam);
                                        SaveCabinetLayoutDataParam saveParam = new SaveCabinetLayoutDataParam();
                                        saveParam = JSONObject.parseObject(JSONObject.toJSONString(cdParam), SaveCabinetLayoutDataParam.class);
                                        //刀片布局数据类型转换，去除currentFlag字段
                                        hashMap.put(properties.getPropertiesIndexId(), saveParam);

                                    }
                                }
                            }
                        }
                    }
                }
        );

        if (param.getModelViewType() != null) {
            if (param.getModelViewType() == 2) {
                //机柜视图；修改所在机房的布局
                QueryBatchSelectDataParam qparam = new QueryBatchSelectDataParam();
                List<QuerySelectDataListParam> paramList = new ArrayList<>();
                QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                //外部关联的实例Id
                if (reationRoomInstanceIds != null && reationRoomInstanceIds.size() > 0) {
                    dataParam.setInstanceId(reationRoomInstanceIds.get(0));
                    dataParam.setCurrentRoomId(reationRoomInstanceIds.get(0));
                }
                //外部关联的modelIndex
                dataParam.setModelIndex(param.getRelationModelIndex());
                dataParam.setCoordinate(coordinate);
                paramList.add(dataParam);
                qparam.setLayoutDataList(paramList);
                batchUpdateRoomLayout(qparam);
            }
            if (param.getModelViewType() == 3) {
                //视图；修改机柜布局
                QueryCabinetLayoutListParam qparam = new QueryCabinetLayoutListParam();
                List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
                QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                //外部关联的实例Id
                if (reationCabinetInstanceIds != null && reationCabinetInstanceIds.size() > 0) {
                    clParam.setInstanceId(reationCabinetInstanceIds.get(0));
                }
                clParam.setCurrentInstanceId(strValueConvert(param.getInstanceId()));
                //外部关联的modelIndex
                clParam.setModelIndex(param.getRelationModelIndex());
                if (cdParamList != null && cdParamList.size() > 0) {
                    clParam.setCurrentData(cdParamList.get(0));
                }
                clParamList.add(clParam);
                qparam.setCabinetLayoutList(clParamList);
                batchUpdateCabinetLayout(qparam);
            }
        }
        //基础字段设置
        settingBaseByEsField(hashMap, param, groupNodeMap);
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
            List<Integer> coordinate = new ArrayList<>();
            List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
            Integer relationRoomId = 0;
            Integer relationCabinetId = 0;
            UpdateRequest updateRequest = new UpdateRequest(instanceParam.getModelIndex(), instanceParam.getEsId());
            updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
            HashMap<String, Object> jsonMap = new HashMap<>();
            //如果是web监测，则更新zabbix上web监测的数据
            if (instanceParam.getModelId().intValue() == webMonitorModeId.intValue()) {
                AddAndUpdateModelWebMonitorParam webMonitorParam = ModelInstanceParamConvertWebMonitor(instanceParam);
                Reply r = mwModelWebMonitorService.updateWebMonitor(Arrays.asList(webMonitorParam));
//                if (null != r && r.getRes() != PaasConstant.RES_SUCCESS) {
//                    return r;
//                }
                WebMonitorConvert(instanceParam, webMonitorParam);
            }
            List<UpdateRelationIdParam> upInstanceRelationList = new ArrayList<>();
            for (AddModelInstancePropertiesParam properties : instanceParam.getPropertiesList()) {
                if (properties.getPropertiesType() != null) {
                    Integer type = properties.getPropertiesType();
                    ModelPropertiesType modelType = getTypeByCode(type);
                    if (null != modelType && properties.getPropertiesValue() != null) {
                        if (!"".equals(properties.getPropertiesValue()) || !notEmptyList.contains(modelType.getCode())) {
                            jsonMap.put(properties.getPropertiesIndexId(), modelType.convertToEsData(properties.getPropertiesValue()));
                        }
                    }
                    if (type == ORG.getCode()) {//type类型 11 机构/部门
                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                            List<List<Integer>> list = (List) JSONArray.parse(properties.getPropertiesValue());
                            orgId.addAll(list);
                        }
                    } else if (type == USER.getCode()) {//type类型12 负责人
                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                            List<Integer> list = (List) JSONArray.parse(properties.getPropertiesValue());
                            userId.addAll(list);
                        }
                    } else if (type == GROUP.getCode()) {//type类型13 用户组
                        if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                            groupId.addAll((List<? extends Integer>) JSONArray.parse(properties.getPropertiesValue()));
                        }
                    }
                    if (MWMACROS_DTO.equals(properties.getPropertiesIndexId())) {
                        //宏值转换
                        macrosConvert(properties, jsonMap);
                    }
                    if (finalParam.getModelViewType() != null && finalParam.getModelViewType() > 0) {
                        if (type == ModelPropertiesType.LAYOUTDATA.getCode() && POSITIONBYROOM.getField().equals(properties.getPropertiesIndexId())) {
                            //type类型为16，获取机柜的位置数据
                            if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                coordinate.addAll((List) JSONArray.parse(properties.getPropertiesValue()));
                            }
                        }
                        if (type == SINGLE_RELATION.getCode() && properties.getPropertiesIndexId().indexOf("relationSite") != -1) {
                            if (instanceParam.getModelViewType() == 2 || instanceParam.getModelViewType() == 1) {
                                if ((RELATIONSITEROOM.getField().equals(properties.getPropertiesIndexId()) || RELATIONSITEFLOOR.getField().equals(properties.getPropertiesIndexId())) && !Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    relationRoomId = intValueConvert(properties.getPropertiesValue());
                                    //ModelViewType() == 2 表示机柜实例，修改所属机房，mysql中实例的relationInstanceId也要修改
                                    //ModelViewType() == 1 机房实例，修改所属楼宇
                                    UpdateRelationIdParam updateRelationIdParam = new UpdateRelationIdParam();
                                    updateRelationIdParam.setInstanceId(instanceParam.getInstanceId());
                                    updateRelationIdParam.setRelationInstanceId(relationRoomId);
                                    upInstanceRelationList.add(updateRelationIdParam);
                                }
                            }
                            if (RELATIONSITECABINET.getField().equals(properties.getPropertiesIndexId()) && !Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                Integer instanceId = Integer.valueOf(properties.getPropertiesValue());
                                relationCabinetId = instanceId;
                            }
                        }
                        if (type == ModelPropertiesType.LAYOUTDATA.getCode() && POSITIONBYCABINET.getField().equals(properties.getPropertiesIndexId())) {
                            if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                CabinetLayoutDataParam cdParam = JSONObject.parseObject(properties.getPropertiesValue(), CabinetLayoutDataParam.class);
                                QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                assetsListParam.setAssetsId(finalParam.getInstanceId() + "");
                                assetsListParam.setAssetsName(finalParam.getInstanceName());
                                //非刀片视图，更新info信息
                                if (!BLADE_VIEW.equals(cdParam.getType())) {
                                    cdParam.setInfo(assetsListParam);
                                }
                                cdParamList.add(cdParam);
                            }
                        }
                    }

                }
            }
            //设置修改人修改时间。
            jsonMap.put("modifier", iLoginCacheInfo.getLoginName());
            jsonMap.put("modificationDate", DateUtils.formatDateTime(new Date()));

            if (instanceParam.getModelViewType() != null && (instanceParam.getModelViewType() > 0)) {
                //机房位置数据，机柜位置数据
//              List<String> fieldList = Arrays.asList(POSITIONBYROOM.getField(), POSITIONBYCABINET.getField());
                List<String> fieldList = new ArrayList<>();

                List<Map<String, Object>> roomLayout = getModelInstanceInfoByEs(instanceParam.getModelIndex(), instanceParam.getInstanceId(), fieldList);
                //机柜修改之前的位置数据
                CabinetLayoutDataParam lastData = new CabinetLayoutDataParam();
                //机房修改前的位置数据
                List<Integer> beforeCoordinate = new ArrayList<>();
                Integer beforeRoomId = 0;
                for (Map<String, Object> map : roomLayout) {
                    if (map != null && map.get(POSITIONBYROOM.getField()) != null) {
                        Object obj = map.get(POSITIONBYROOM.getField());
                        beforeCoordinate = (List<Integer>) JSONArray.parse(obj.toString());
                    }
                    if (map != null && map.get(RELATIONSITEROOM.getField()) != null) {
                        beforeRoomId = intValueConvert(map.get(RELATIONSITEROOM.getField()));
                    }
                    if (map != null && map.get(POSITIONBYCABINET.getField()) != null) {
                        Object obj = map.get(POSITIONBYCABINET.getField());
                        lastData = JSONObject.parseObject(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                    }
                }
                if (instanceParam.getModelViewType() == 2) {
                    //机柜视图；修改机房布局
                    QueryBatchSelectDataParam qparam = new QueryBatchSelectDataParam();
                    List<QuerySelectDataListParam> paramList = new ArrayList<>();
                    QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                    //外部关联的实例Id
                    if (relationRoomId != null && relationRoomId != 0) {
                        dataParam.setInstanceId(relationRoomId);
                    }
                    //修改之前的位置信息
                    if (beforeCoordinate != null && beforeCoordinate.size() > 0) {
                        dataParam.setBeforeCoordinate(beforeCoordinate);
                    }
                    //外部关联的modelIndex
                    dataParam.setModelIndex(instanceParam.getRelationModelIndex());
                    //当前的位置信息
                    dataParam.setCoordinate(coordinate);
                    dataParam.setBeforeRoomId(beforeRoomId);
                    dataParam.setCurrentRoomId(relationRoomId);
                    paramList.add(dataParam);
                    qparam.setLayoutDataList(paramList);
                    batchUpdateRoomLayout(qparam);
                }
                if (instanceParam.getModelViewType() == 3) {
                    //机柜下属设备视图；修改机柜布局
                    QueryCabinetLayoutListParam qparam = new QueryCabinetLayoutListParam();
                    List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
                    QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                    //外部关联的实例Id
                    if (relationCabinetId != null && relationCabinetId != 0) {
                        clParam.setInstanceId(relationCabinetId);
                    }
                    //外部关联的modelIndex
                    clParam.setModelIndex(instanceParam.getRelationModelIndex());
                    if (cdParamList != null && cdParamList.size() > 0) {
                        clParam.setCurrentData(cdParamList.get(0));
                    }
                    clParam.setCurrentInstanceId(strValueConvert(instanceParam.getInstanceId()));
                    clParam.setLastData(lastData);
                    clParamList.add(clParam);
                    qparam.setCabinetLayoutList(clParamList);
                    batchUpdateCabinetLayout(qparam);
                }
            }


            //同步数据加入es中
            if (CollectionUtils.isNotEmpty(instanceParam.getSyncParams())) {
                String profileName = "";
                for (MwModelMacrosValInfoParam macrosParam : instanceParam.getSyncParams()) {
                    profileName = macrosParam.getAuthName();
                    jsonMap.put(macrosParam.getMacro(), macrosParam.getMacroVal());
                }
                jsonMap.put("profileName", profileName);
            }
            updateRequest.doc(jsonMap);
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            RestStatus status = update.status();

            //修改mysql机柜实例的关联机房Id
            if (CollectionUtils.isNotEmpty(upInstanceRelationList)) {
                mwModelInstanceDao.updateCabinetRelationId(upInstanceRelationList);
            }
            //是否重新纳管资产
            boolean isFlags = instanceParam.isEditorZabbixServer();
            settingManageInfo(instanceParam, isFlags);
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
                    mwModelSysLogService.saveInstaceChangeHistory(builder);
                }
                //对用户名、机构、用户组修改
                ModelPermControlParam param = new ModelPermControlParam();
                param.setType(DataType.INSTANCE_MANAGE.getName());
                param.setUserIds(userId);
                param.setOrgIds(orgId);
                param.setGroupIds(groupId);
                param.setId(String.valueOf(instanceParam.getInstanceId()));
                param.setDesc(DataType.INSTANCE_MANAGE.getDesc());
                if (CollectionUtils.isNotEmpty(userId) && CollectionUtils.isNotEmpty(orgId)) {
                    //先删除后新增
                    mwModelManageService.deleteMapperAndPerm(param);
                    mwModelManageService.addMapperAndPerm(param);
                }
            } else {
                return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313007, ErrorConstant.MODEL_INSTANCE_MSG_313007);
            }

        } catch (Exception e) {
            log.error("fail to updateModelInstance param {}, case by {}", instanceParams, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313007, ErrorConstant.MODEL_INSTANCE_MSG_313007);
        }
        return Reply.ok();
    }

    private AddAndUpdateModelWebMonitorParam ModelInstanceParamConvertWebMonitor(AddAndUpdateModelInstanceParam param) {
        List<AddModelInstancePropertiesParam> propertyList = param.getPropertiesList();
        Map<String, Object> map = propertyList.stream().filter(s -> !Strings.isNullOrEmpty(s.getPropertiesIndexId())).collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> propertyTypeConvert(s.getPropertiesType(), s.getPropertiesValue()), (
                value1, value2) -> {
            return value2;
        }));
        AddAndUpdateModelWebMonitorParam webMonitorParam = JSONObject.parseObject(JSONObject.toJSONString(map), AddAndUpdateModelWebMonitorParam.class);
        return webMonitorParam;
    }

    private void WebMonitorConvert(AddAndUpdateModelInstanceParam param, AddAndUpdateModelWebMonitorParam webMonitorParam) {
        Map<String, Object> m = ListMapObjUtils.beanToMap(webMonitorParam);
        List<AddModelInstancePropertiesParam> propertyList = param.getPropertiesList();
        for (AddModelInstancePropertiesParam propertiesParam : propertyList) {
            String propertiesIndexId = propertiesParam.getPropertiesIndexId();
            if (m != null && m.containsKey(propertiesIndexId)) {
                propertiesParam.setPropertiesValue(strValueConvert(m.get(propertiesIndexId)));
            }
        }
    }

    public void settingManageInfo(AddAndUpdateModelInstanceParam instanceParam, boolean isFlags) throws Exception {
        //是否纳管资产
        if (instanceParam.isManage() && isFlags) {//是否修改zabbix服务，true，需要重新纳管
            AddUpdateTangAssetsParam manageParam = instanceParam.getManageParam();
            manageParamSetting(manageParam, instanceParam);
            Reply reply = null;
            try {
                reply = mwModelAssetsByESService.doInsertAssetsToESByView(instanceParam.getManageParam(), false);
            } catch (Throwable throwable) {
                log.error("纳管资产失败", throwable);
                throw new Exception("纳管资产失败:" + throwable.getMessage());
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                //报错删除已添加的zabbixHost
                if (instanceParam.getManageParam().getMonitorServerId() != null && instanceParam.getManageParam().getMonitorServerId() != 0) {
                    ArrayList<String> hostNames = new ArrayList<>();
                    hostNames.add(instanceParam.getManageParam().getTPServerHostName());
                    MWZabbixAPIResult result = mwtpServerAPI.hostListGetByHostName(instanceParam.getManageParam().getMonitorServerId(), hostNames);
                    if (!result.isFail()) {
                        List<String> hostIds = new ArrayList<>();
                        JsonNode data = (JsonNode) result.getData();
                        data.forEach(hostId -> {
                            hostIds.add(hostId.get("hostid").asText());
                        });
                        mwtpServerAPI.hostDelete(instanceParam.getManageParam().getMonitorServerId(), hostIds);
                    }
                }
                throw new RuntimeException(reply.getMsg());
            }
        }

        //是否同步，添加同步信息
        if (instanceParam.isSync() && CollectionUtils.isNotEmpty(instanceParam.getSyncParams())) {
            //先校验数据重复性
            boolean isCheck = mwModelViewService.checkAuthenticationInfo(instanceParam.getSyncParams());
            if (isCheck) {
                mwModelViewService.saveAuthInfoByModel(instanceParam.getSyncParams());
            }
            InstanceSyncContext instanceSyncContext = new InstanceSyncContext(instanceParam);
            mwModelInstanceSyncManager.sync(instanceSyncContext);
        }
    }


    /**
     * 模型实例信息查看
     *
     * @param instanceParam
     * @return
     */
    @Override
    public Reply lookModelInstance(AddAndUpdateModelInstanceParam instanceParam) {
        List<MWModelInstanceFiled> instanceFiledList = new ArrayList<>();
        try {
            QueryModelInstanceParam param = new QueryModelInstanceParam();
            param.setModelIndex(instanceParam.getModelIndex());
            param.setModelId(instanceParam.getModelId());
            param.setModelInstanceId(instanceParam.getInstanceId());
            //根据实例Id查询该es中实例的详细数据
            List<Map<String, Object>> listMap = getInstanceInfoByModelId(param);


            QueryESWhetherExistField queryRoomParam = new QueryESWhetherExistField();
            queryRoomParam.setExistFields(Arrays.asList(ROWNUM.getField(), COLNUM.getField(), LAYOUTDATA.getField()));
            QueryESWhetherExistField queryCabinetParam = new QueryESWhetherExistField();
            queryCabinetParam.setExistFields(Arrays.asList(UNUM.getField(), POSITIONBYROOM.getField(), RELATIONSITEROOM.getField(), LAYOUTDATA.getField()));
            //获取所有机柜数据，instanceId为key
            Map<String, List<Map<String, Object>>> cabitModelInfoMap = new HashMap<>();
            //如果有所属机房所属机柜字段，获取对应关联数据
            addRoomAndCabinetInfo(listMap);

            //机构，用户组，负责人字段转换
            mwModelCommonServiceImpl.powerFieldConvert(true, listMap);

            if (instanceParam.getModelId().intValue() == webMonitorModeId.intValue()) {
                List<MwModelWebMonitorTable> webMonitorParams = MwModelUtils.convertEsData(MwModelWebMonitorTable.class, listMap);
                List<MwModelWebMonitorTable> webMonitorTables = mwModelWebMonitorService.queryWebSeverList(webMonitorParams);
                List<Map<String, Object>> newListMap = ListMapObjUtils.convertList(webMonitorTables);
                Map<String, Map<String, Object>> groupMap = newListMap.stream().collect(Collectors.toMap(s -> s.get(INSTANCE_ID_KEY).toString(), s -> s));
                for (Map<String, Object> mapParam : listMap) {
                    String instanceId = mapParam.get(INSTANCE_ID_KEY).toString();
                    if (groupMap.containsKey(instanceId)) {
                        mapParam.putAll(groupMap.get(instanceId));
                    }
                }
            }

            //根据modelId获取属性的分类
            List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(instanceParam.getModelId());
            List<PropertyInfo> propertyInfoLists = new ArrayList<>();
            Map<String, PropertyInfo> propertiesIndexMap = new HashMap<>();
            for (ModelInfo modelInfo : modelInfos) {
                propertyInfoLists.addAll(modelInfo.getPropertyInfos());
                propertiesIndexMap.putAll(modelInfo.getPropertyInfoMapByIndexId());
            }
            //按照模型类型来分组
            Map<String, List<PropertyInfo>> propertiesTypeMap = propertyInfoLists.stream().collect(Collectors.groupingBy(PropertyInfo::getPropertiesType));

            for (Map.Entry<String, List<PropertyInfo>> entry : propertiesTypeMap.entrySet()) {
                String k = entry.getKey();
                List<PropertyInfo> v = entry.getValue();
                String properticeType = k;
                List<PropertyInfo> propertyInfoList = v;
                propertiesIndexMap.get(properticeType);
                //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
                if (CollectionUtils.isNotEmpty(listMap)) {
                    Map data = listMap.get(0);
                    List<MWModelInstancePropertiesFiledDTO> lists = new ArrayList();
                    List<MWModelInstancePropertiesFiledDTO> listSort = new ArrayList();
                    for (PropertyInfo m : propertyInfoList) {
                        MWModelInstancePropertiesFiledDTO dto = new MWModelInstancePropertiesFiledDTO();
                        String propertiesIndexId = m.getIndexId();
                        dto.setId(propertiesIndexId);
                        if (propertiesIndexMap.get(propertiesIndexId) != null) {
                            PropertyInfo infoMap = propertiesIndexMap.get(propertiesIndexId);
                            //查看页面属性显示
                            if (infoMap.getIsLookShow()) {
                                dto.setName(infoMap.getPropertiesName() != null ? infoMap.getPropertiesName() : "");
                                dto.setType(infoMap.getPropertiesTypeId() != null ? infoMap.getPropertiesTypeId() : 1);
                                if (data.get(propertiesIndexId) != null) {
                                    dto.setValue(data.get(propertiesIndexId));
                                } else {
                                    if (typeList.contains(m.getPropertiesTypeId())) {
                                        dto.setValue(new ArrayList<>());
                                    } else {
                                        dto.setValue("");
                                    }
                                }
                                ModelPropertiesDto modelPropertiesDto = new ModelPropertiesDto();
                                modelPropertiesDto.extractFrom(m);
                                dto.setDataArrObj(modelPropertiesDto.getPropertiesValue().getDropOp());
                                dto.setPropertiesStruct(modelPropertiesDto.getPropertiesStruct());
                                if (m.getPropertiesTypeId() == STRUCE.getCode() ||
                                        m.getPropertiesTypeId() == ModelPropertiesType.LAYOUTDATA.getCode()) {
                                    //将类型为结构体的数据另存入一个list中
                                    listSort.add(dto);
                                } else {
                                    lists.add(dto);
                                }
                            }
                        }
                    }
                    //结构体数据放入list的最后面。方便前端排序
                    lists.addAll(listSort);
                    MWModelInstanceFiled instanceFiled = new MWModelInstanceFiled();
                    instanceFiled.setType(properticeType);
                    instanceFiled.setFiledDTOS(lists);
                    instanceFiledList.add(instanceFiled);
                }
            }
        } catch (Exception e) {
            log.error("查询资产详情失败", e);
            return Reply.fail(500, "查询资产详情失败");
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

    //批量修改主机名称
    @Override
    public Reply batchUpdateSyncZabbixName(List<MwSyncZabbixAssetsParam> paramList) {
        Map<Integer, List<MwSyncZabbixAssetsParam>> disParam = paramList.stream().filter(s -> intValueConvert(s.getMonitorServerId()) != 0).collect(Collectors.groupingBy(s -> s.getMonitorServerId()));

        disParam.forEach((k, v) -> {
            Integer serverId = k;
            List<MwSyncZabbixAssetsParam> list = v;
            //调用zabbix接口根据主机ID修改可见名称
            updateHostUpdateSoName(k, v);
        });
        return Reply.ok();
    }

    private void updateHostUpdateSoName(Integer serverId, List<MwSyncZabbixAssetsParam> list) {
        try {
            MWZabbixAPIResult result = mwtpServerAPI.hostUpdateSoName(serverId, list);
            if (result != null && !result.isFail()) {
                return;
            }
            //如果名称已重复，则在实例名称后添加4个随机的数字字母。
            String data = result.getData().toString();
            if (!Strings.isNullOrEmpty(data) && data.length() > 35) {
                int index1 = result.getData().toString().indexOf("Host with the same visible name");
                int index2 = result.getData().toString().indexOf("already exists");
                //批量修改主机名称时，如果主机名称重复，则自动修改主机名，再次同步，直到所有主机名不重复。
                if (index1 != -1 && index2 != -1) {
                    String hostName = result.getData().toString().substring(index1 + 33, index2 - 2).trim();
                    for (MwSyncZabbixAssetsParam param : list) {
                        if (strValueConvert(param.getInstanceName()).equals(hostName)) {
                            param.setInstanceName(param.getInstanceName() + "_" + UuidUtil.get16Uid());
                        }
                    }
                    updateHostUpdateSoName(serverId, list);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            log.error("updateHostUpdateSoName fail to", e);
        }

    }

    @Override
    public Reply batchUpdatePowerByEs() {
        //从es中获取所有数据
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        param.setPageSize(pageSize);
        mwModelViewServiceImpl.getInstanceListData(param);
        Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (map != null && map.get("data") != null) {
            listMap = (List<Map<String, Object>>) map.get("data");
        }
        try {
            List<ModelPermControlParam> permList = new ArrayList<>();
            String type = DataType.INSTANCE_MANAGE.getName();
            for (Map<String, Object> sqlParam : listMap) {
                List<Integer> userIdList = new ArrayList<>();
                if (sqlParam.get(USER_IDS) != null && sqlParam.get(USER_IDS) instanceof List) {
                    userIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(USER_IDS)));
                }
                List<List<Integer>> orgIdList = new ArrayList<>();

                if (sqlParam.get(ORG_IDS) != null && sqlParam.get(ORG_IDS) instanceof List) {
                    orgIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(ORG_IDS)));
                }
                List<Integer> groupIdList = new ArrayList<>();
                if (sqlParam.get(GROUP_IDS) != null && sqlParam.get(GROUP_IDS).toString() != "" && sqlParam.get(GROUP_IDS) instanceof List) {
                    groupIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(GROUP_IDS)));
                }
                String typeId = sqlParam.get(INSTANCE_ID_KEY).toString();
                ModelPermControlParam permControlParam = new ModelPermControlParam();
                permControlParam.setType(type);
                permControlParam.setId(typeId);
                permControlParam.setUserIds(userIdList);
                permControlParam.setOrgIds(orgIdList);
                permControlParam.setGroupIds(groupIdList);
                permList.add(permControlParam);
            }
            batchInsertPermList(permList);


//            //绑定机构
//            List<OrgMapper> orgMapper = new ArrayList<>();
//            List<GroupMapper> groupMapper = new ArrayList<>();
//            List<UserMapper> userMapper = new ArrayList<>();
//            List<DataPermissionDto> permissionMapper = new ArrayList<>();
//            List<String> typeIds = new ArrayList<>();
//            String type = DataType.INSTANCE_MANAGE.getName();
//            for (Map<String, Object> sqlParam : listMap) {
//                String typeId = sqlParam.get(INSTANCE_ID_KEY).toString();
//                typeIds.add(typeId);
//                DataPermissionDto dto = new DataPermissionDto();
//                dto.setType(type);     //类型
//                dto.setTypeId(typeId);  //数据主键
//                dto.setDescription(DataType.valueOf(type).getDesc()); //描述
//                List<Integer> userIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(USER_IDS)));
//                List<List<Integer>> orgIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(ORG_IDS)));
//                List<Integer> groupIdList = new ArrayList<>();
//                if (sqlParam.get(GROUP_IDS) != null && sqlParam.get(GROUP_IDS).toString() != "") {
//                    groupIdList = (List) JSONArray.parse(JSONObject.toJSONString(sqlParam.get(GROUP_IDS)));
//                }
//                orgIdList.forEach(
//                        orgId -> orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(type).build())
//                );
//                if (CollectionUtils.isNotEmpty(groupIdList)) {
//                    dto.setIsGroup(1);
//                } else {
//                    dto.setIsGroup(0);
//                }
//                groupIdList.forEach(
//                        groupId -> groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(type).build())
//                );
//                if (CollectionUtils.isNotEmpty(userIdList)) {
//                    dto.setIsUser(1);
//                } else {
//                    dto.setIsUser(0);
//                }
//                userIdList.forEach(userIds -> {
//                            log.info("userMapper.add,userid:{}", userIds);
//                            userMapper.add(UserMapper.builder().typeId(typeId).userId(userIds).type(type).build());
//                        }
//                );
//                permissionMapper.add(dto);
//            }
//
//            DeleteDto deleteDto = DeleteDto.builder()
//                    .typeIds(typeIds)
//                    .type(type)
//                    .build();
//            mwCommonService.deleteMapperAndPerms(deleteDto);
//            mwCommonService.insertGroupMapper(groupMapper);
//            mwCommonService.insertUserMapper(userMapper);
//            mwCommonService.insertOrgMapper(orgMapper);
//            mwCommonService.insertPermissionMapper(permissionMapper);


        } catch (Exception e) {
            log.error("类型转换错误！");
        }
        return Reply.ok();
    }

    @Override
    public Reply batchUpdatePower(BatchUpdatePowerParam param) {
        try {
            //绑定机构
            batchEditorPower(param);
            //修改监控状态
            if (param.getMonitorFlag() != null) {
                batchEditorSettingFlag(param);
            }
            //修改轮询引擎
            batchUpdatePollingEngine(param);
            //修改ES数据
            batchEditorConfig(param);
        } catch (Exception e) {
            Reply.fail(500, "批量修改主机状态失败");
        }
        return Reply.ok();
    }

    /**
     * 批量修改轮询引擎
     *
     * @param param
     * @return
     */
    public Reply batchUpdatePollingEngine(BatchUpdatePowerParam param) {
        try {
            if (CollectionUtils.isNotEmpty(param.getInstanceParams())) {
                //过滤没有assetsId和serverID为0的数据
                List<MwModelInstanceParam> disLists = param.getInstanceParams().stream().filter(s -> !Strings.isNullOrEmpty(s.getAssetsId()) && s.getMonitorServerId() != 0).collect(Collectors.toList());
                //用MonitorServerId为key；assetsId为value
                Map<Integer, List<String>> assetsIdListMap = disLists.stream()
                        .collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMonitorServerId()), Collectors.mapping(s -> s.getAssetsId(), Collectors.toList())));

                assetsIdListMap.forEach((keys, value) -> {
                    //获取代理ip
                    if (!Strings.isNullOrEmpty(param.getEngineId()) && !localEngine.equals(param.getEngineId())) {
                        MwModelEngineDTO proxyDto = mwModelInstanceDao.selectProxyIdById(param.getEngineId());
                        param.setPollingEngine(param.getEngineId());
                        param.setPollingEngineName(proxyDto.getEngineName());
                        Map<String, Object> updateParam = new HashMap<>();
                        String proxyId = proxyDto.getProxyId();
                        updateParam.put("proxy_hostid", proxyId);
                        MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(keys, value, updateParam);
                        if (result.isFail()) {
                            log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                            throw new AssetsException("修改主机代理失败:" + result.getData());
                        }
                    }
                    //轮询引擎为空时，表示去除代理服务
                    if (param.getEngineId() != null && localEngine.equals(param.getEngineId())) {
                        param.setPollingEngine(param.getEngineId());
                        param.setPollingEngineName(localEngine);
                        Map<String, Object> updateParam = new HashMap<>();
                        updateParam.put("proxy_hostid", null);
                        MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(keys, value, updateParam);
                        if (result.isFail()) {
                            log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                            throw new AssetsException("删除主机代理失败:" + result.getData());
                        }
                    }
                });
            }
        } catch (Exception e) {
            return Reply.fail(500, "修改轮询引擎失败");
        }
        return Reply.ok();
    }


    /**
     * 修改轮询引擎
     *
     * @param param
     * @return
     */
    @Override
    public Reply updatePollingEngine(UpdatePollingEngineParam param) {
        try {
            if (!Strings.isNullOrEmpty(param.getEngineId()) && param.getMonitorServerId() != 0 && !Strings.isNullOrEmpty(param.getAssetsId())) {
                //获取代理ip
                if (!Strings.isNullOrEmpty(param.getEngineId()) && !localEngine.equals(param.getEngineId())) {
                    MwModelEngineDTO proxyDto = mwModelInstanceDao.selectProxyIdById(param.getEngineId());
                    Map<String, Object> updateParam = new HashMap<>();
                    String proxyId = proxyDto.getProxyId();
                    updateParam.put("proxy_hostid", proxyId);
                    MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(param.getMonitorServerId(), Arrays.asList(param.getAssetsId()), updateParam);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]修改主机代理失败[][]msg:[]{}", result.getData());
                        throw new AssetsException("修改主机代理失败:" + result.getData());
                    }
                }
                //轮询引擎为空时，表示去除代理服务
                if (param.getEngineId() != null && localEngine.equals(param.getEngineId())) {
                    Map<String, Object> updateParam = new HashMap<>();
                    updateParam.put("proxy_hostid", null);
                    MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(param.getMonitorServerId(), Arrays.asList(param.getAssetsId()), updateParam);
                    if (result.isFail()) {
                        log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
                        throw new AssetsException("删除主机代理失败:" + result.getData());
                    }
                }

                List<AddAndUpdateModelInstanceParam> updateParams = new ArrayList<>();
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();


                instanceParam.setEsId(param.getEsId());
                instanceParam.setModelIndex(param.getModelIndex());
                List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                if (param.getEngineId() != null) {
                    propertiesParam = new AddModelInstancePropertiesParam();
                    propertiesParam.setPropertiesIndexId(POLLING_ENGINE);
                    propertiesParam.setPropertiesValue(param.getEngineId());
                    propertiesParam.setPropertiesType(STRING.getCode());
                    propertiesList.add(propertiesParam);
                }
                instanceParam.setPropertiesList(propertiesList);
                updateParams.add(instanceParam);
                //修改监控状态
                batchUpdateModelInstance(updateParams);
            }
        } catch (Exception e) {
            return Reply.fail(500, "修改轮询引擎失败");
        }
        return Reply.ok();
    }


    /**
     * 批量绑定机构
     *
     * @param param
     */
    private void batchEditorPower(BatchUpdatePowerParam param) {
        List<ModelPermControlParam> permList = new ArrayList<>();
        ModelPermControlParam permControlParam = new ModelPermControlParam();
        for (MwModelInstanceParam sqlParam : param.getInstanceParams()) {
            permControlParam = new ModelPermControlParam();
            permControlParam.setType(param.getType());
            permControlParam.setId(sqlParam.getInstanceId() + "");
            permControlParam.setUserIds(param.getUserIds());
            permControlParam.setOrgIds(param.getOrgIds());
            permControlParam.setGroupIds(param.getGroupIds());
            permList.add(permControlParam);
        }
        batchInsertPermList(permList);
    }

    /**
     * 修改监控状态
     *
     * @param param
     */
    private void batchEditorSettingFlag(BatchUpdatePowerParam param) throws Exception {
        if (CollectionUtils.isNotEmpty(param.getInstanceParams())) {
            //监控状态就修改
            ModelAssetMonitorState tas = ModelAssetMonitorState.valueOf(param.getMonitorFlag() != null ? param.getMonitorFlag().toString().toUpperCase() : "FALSE");
            //过滤没有assetsId和serverID为0的数据
            List<MwModelInstanceParam> disLists = param.getInstanceParams().stream().filter(s -> !Strings.isNullOrEmpty(s.getAssetsId()) && s.getMonitorServerId() != 0).collect(Collectors.toList());
            //用MonitorServerId为key；assetsId为value
            Map<Integer, List<String>> assetsIdListMap = disLists.stream()
                    .collect(Collectors.groupingBy(s -> Integer.valueOf(s.getMonitorServerId()), Collectors.mapping(s -> s.getAssetsId(), Collectors.toList())));
            assetsIdListMap.forEach((keys, value) -> {
                batchUpdateHostState(keys, value, tas.getZabbixStatus());
            });
        }
    }

    /**
     * 修改功能模块
     *
     * @param param
     */
    private void batchEditorConfig(BatchUpdatePowerParam param) throws Exception {
        List<AddAndUpdateModelInstanceParam> updateParams = new ArrayList<>();
        for (MwModelInstanceParam mParam : param.getInstanceParams()) {
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setInstanceId(mParam.getInstanceId());
            instanceParam.setEsId(mParam.getEsId());
            instanceParam.setModelIndex(mParam.getModelIndex());
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            getPowerParamMethods(param, propertiesList);
            instanceParam.setPropertiesList(propertiesList);
            updateParams.add(instanceParam);
        }
        batchUpdateModelInstance(updateParams);

    }

    private void getPowerParamMethods(Object param, List<AddModelInstancePropertiesParam> propertiesList) throws Exception {
        Field[] field = param.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
        for (int j = 0; j < field.length; j++) { // 遍历所有属性
            AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
            String nameStr = field[j].getName(); // 获取属性的名字9
            String name = nameStr.substring(0, 1).toUpperCase() + nameStr.substring(1); // 将属性的首字符大写，方便构造get，set方法
            String type = field[j].getType().getName();
            propertiesParam.setPropertiesIndexId(nameStr);
            String propertiesValue = "";
            Integer propertiesType = STRING.getCode();
            if (type.equals("java.lang.String")) { // 如果type是类类型，则前面包含"class "，后面跟类名
                Method m = param.getClass().getMethod("get" + name);
                String value = (String) m.invoke(param); // 调用getter方法获取属性值
                if (value == null) {
                    continue;
                }
                propertiesValue = value;
                propertiesType = STRING.getCode();
            }
            if (type.equals("java.util.List")) {
                Method m = param.getClass().getMethod("get" + name);
                List value = (List) m.invoke(param); // 调用getter方法获取属性值
                if (value == null) {
                    continue;
                }
                propertiesValue = JSONArray.toJSONString(value);
                switch (nameStr) {
                    case "groupIds":
                        propertiesType = GROUP.getCode();
                        break;
                    case "userIds":
                        propertiesType = USER.getCode();
                        break;
                    case "orgIds":
                        propertiesType = ORG.getCode();
                        break;
                    default:
                        break;
                }
            }
            if (type.equals("java.lang.Integer")) {
                Method m = param.getClass().getMethod("get" + name);
                Integer value = (Integer) m.invoke(param);
                if (value == null) {
                    continue;
                }
                propertiesValue = value.toString();
                propertiesType = INTEGER.getCode();
            }
            if (type.equals("java.lang.Boolean")) {
                Method m = param.getClass().getMethod("get" + name);
                Boolean value = (Boolean) m.invoke(param);
                if (value == null) {
                    continue;
                }
                propertiesValue = String.valueOf(value);
                propertiesType = SWITCH.getCode();
            }
            if (type.equals("java.util.Date")) {
                Method m = param.getClass().getMethod("get" + name);
                Date value = (Date) m.invoke(param);
                if (value == null) {
                    continue;
                }
                propertiesValue = DateUtils.formatDateTime(value);
                propertiesType = DATE.getCode();
            } // 如果有需要,可以仿照上面继续进行扩充,再增加对其它类型的判断
            //如果是Object类型，表示前端传入的是动态JSON数据，需要将key作为字段id，PropertiesValue为字段值，根据propertiesType区分属性类型
            if (type.equals("java.lang.Object")) {
                Method m = param.getClass().getMethod("get" + name);
                Object obj = (Object) m.invoke(param);
                if (obj == null && !(obj instanceof Map)) {
                    continue;
                }
                Map<String, Object> mp = ModelUtils.JSONObjectToMap(obj);
                mp.forEach((k, v) -> {
                    if (v != null) {
                        MwPropertyParam propertyParam = JSONObject.parseObject(v.toString(), MwPropertyParam.class);
                        if (propertyParam.getPropertiesValue() != null) {
                            AddModelInstancePropertiesParam propertiesParam1 = new AddModelInstancePropertiesParam();
                            propertiesParam1.setPropertiesIndexId(k);
                            propertiesParam1.setPropertiesType(propertyParam.getPropertiesTypeId());
                            propertiesParam1.setPropertiesValue(String.valueOf(propertyParam.getPropertiesValue()));
                            propertiesList.add(propertiesParam1);
                        }
                    }
                });
            } else {
                propertiesParam.setPropertiesValue(propertiesValue);
                propertiesParam.setPropertiesType(propertiesType);
                propertiesList.add(propertiesParam);
            }
        }
    }


    private void executeMethod(Map<Integer, List<String>> assetsIdListMap, Method method, Integer zabbixStaus, Object instacne) throws Exception {
        int coreSizePool = Runtime.getRuntime().availableProcessors() * 2 + 1;
        coreSizePool = (coreSizePool < assetsIdListMap.size()) ? coreSizePool : assetsIdListMap.size();//当使用cpu算出的线程数小于分页或未分页的数据条数时，使用cpu，否者使用数据条数
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(coreSizePool, assetsIdListMap.size(), 60, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        List<Integer> listInfo = new ArrayList();
        List<Future<Integer>> futureList = new ArrayList<>();
        //keys为serverId，value为assetsId
        assetsIdListMap.forEach((keys, value) -> {
            Callable<Integer> callable = new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    List<String> disList = value.stream().distinct().collect(Collectors.toList());
                    //批量修改主机状态
                    Boolean isFlag = (Boolean) method.invoke(instacne, keys, disList, zabbixStaus);
                    Integer errorServer = 0;
                    if (isFlag) {
                        errorServer = keys;
                    }
                    return errorServer;
                }
            };
            Future<Integer> submit = executorService.submit(callable);
            futureList.add(submit);
        });
        if (futureList.size() > 0) {
            futureList.forEach(f -> {
                try {
                    Integer result = f.get(30, TimeUnit.SECONDS);
                    listInfo.add(result);
                } catch (Exception e) {
                    log.error("fail to getDataInfoBydeviceName:多线程等待数据返回失败cause:{}", e);
                }
            });
        }
        executorService.shutdown();
        log.info("关闭线程池");

        boolean isFlag = false;
        StringBuffer errorInfo = new StringBuffer("服务器Id：");
        for (Integer serverId : listInfo) {
            if (serverId.intValue() != 0) {
                isFlag = true;
                errorInfo.append(serverId + ";");
            }
        }
        errorInfo.append("立即执行操作失败");
        if (isFlag) {
            throw new Exception(errorInfo.toString());
        }
    }


    public MWZabbixAPIResult updateHostState(Integer serverId, List<String> hostIds, Integer status) {
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostUpdate(serverId, hostIds, status);
        return mwZabbixAPIResult;
    }

    /**
     * @param serverId
     * @param hostIds
     * @param status
     */
    @Override
    public Reply batchUpdateHostState(Integer serverId, List<String> hostIds, Integer status) {

        MWZabbixAPIResult mwZabbixAPIResult = updateHostState(serverId, hostIds, status);
        return Reply.ok();
//        Map<String, Object> updateParam = new HashMap<>();
//        updateParam.put("proxy_hostid", null);
//        MWZabbixAPIResult result = mwtpServerAPI.hostBatchUpdate(serverId, hostIds, updateParam);
//        if (result.isFail()) {
//            log.error("[]ERROR_LOG[][]删除主机代理失败[][]msg:[]{}", result.getData());
//            throw new AssetsException("删除主机代理失败:" + result.getData());
//        }
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
    @Transactional
    public Reply deleteModelInstance(Object instanceParams, Integer types) {
        DeleteModelInstanceParam deleteModelInstanceParam = new DeleteModelInstanceParam();
        try {
            if (types == 0) {
                deleteModelInstanceParam = (DeleteModelInstanceParam) instanceParams;
            } else {
                deleteModelInstanceParam = JSONObject.parseObject(instanceParams.toString(), DeleteModelInstanceParam.class);
            }
            long time1 = System.currentTimeMillis();
            long time5 = 0l;
            long time6 = 0l;
            long time7 = 0l;
            long time8 = 0l;
            List<String> typeIds = new ArrayList<>();
            if (null != deleteModelInstanceParam.getInstanceIds() && deleteModelInstanceParam.getInstanceIds().size() > 0) {
                mwModelManageDao.deleteModelInstances(deleteModelInstanceParam.getInstanceIds());
                typeIds = deleteModelInstanceParam.getInstanceIds().stream().map(String::valueOf).collect(Collectors.toList());
            }
            //删除权限
            //根据实例Id删除，无需区别type，只要instanceId相同就删除（InstanceId唯一）
            String virtualType = DataType.MODEL_VIRTUAL.getName();
            String instanceType = DataType.INSTANCE_MANAGE.getName();
            batchDeletePermInfo(typeIds, virtualType);
            batchDeletePermInfo(typeIds, instanceType);
            long time2 = System.currentTimeMillis();
            QueryBatchSelectDataParam jfParam = new QueryBatchSelectDataParam();
            QueryCabinetLayoutListParam jgParam = new QueryCabinetLayoutListParam();
            List<QuerySelectDataListParam> paramList = new ArrayList<>();
            List<QueryCabinetLayoutParam> clParamList = new ArrayList<>();
            //对机房机柜视图下的实例删除，需要修改对应的所属机房、所属机柜的布局
            //TODO 待优化
            if (deleteModelInstanceParam.getModelViewType() != null && (deleteModelInstanceParam.getModelViewType() > 0)) {
                for (Integer instanceId : deleteModelInstanceParam.getInstanceIds()) {
                    QueryModelInstanceParam params = new QueryModelInstanceParam();
                    params.setModelIndex(deleteModelInstanceParam.getModelIndex());
                    params.setInstanceIdList(Arrays.asList(instanceId));
                    //指定返回所属机房，所属机柜数据
                    params.setFieldList(Arrays.asList(RELATIONSITEROOM.getField(), RELATIONSITECABINET.getField()));
                    //关联机房
                    Integer relationSiteRoom = 0;
                    //关联机柜
                    Integer relationSiteCabinet = 0;
                    for (Map<String, Object> map : getModelInstanceDataByInstanceId(params)) {
                        if (map != null && map.get(RELATIONSITEROOM.getField()) != null) {
                            relationSiteRoom = Integer.valueOf(map.get(RELATIONSITEROOM.getField()).toString());
                        }
                        if (map != null && map.get(RELATIONSITECABINET.getField()) != null) {
                            relationSiteCabinet = Integer.valueOf(map.get(RELATIONSITECABINET.getField()).toString());
                        }
                    }
                    //获取实例资产在机房机柜中的占用位置信息
                    List<String> fieldList = Arrays.asList(POSITIONBYROOM.getField(), POSITIONBYCABINET.getField());
                    List<Map<String, Object>> roomLayout = getModelInstanceInfoByEs(deleteModelInstanceParam.getModelIndex(), instanceId, fieldList);
                    //机房机柜修改之前的位置数据
                    CabinetLayoutDataParam lastData = new CabinetLayoutDataParam();
                    List<Integer> beforeCoordinate = new ArrayList<>();
                    for (Map<String, Object> map : roomLayout) {
                        //机柜在机房中的位置数据
                        if (map != null && map.get(POSITIONBYROOM.getField()) != null) {
                            Object obj = map.get(POSITIONBYROOM.getField());
                            beforeCoordinate = (List<Integer>) JSONArray.parse(obj.toString());
                        }
                        //下属设备在机柜中的位置数据
                        if (map != null && map.get(POSITIONBYCABINET.getField()) != null) {
                            Object obj = map.get(POSITIONBYCABINET.getField());
                            lastData = JSONObject.parseObject(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                        }
                    }
                    if (deleteModelInstanceParam.getModelViewType() == 2) {
                        //机柜视图；修改机房布局
                        QuerySelectDataListParam dataParam = new QuerySelectDataListParam();
                        //外部关联的实例Id
                        dataParam.setInstanceId(relationSiteRoom);
                        dataParam.setBeforeRoomId(relationSiteRoom);
                        //外部关联的modelIndex
                        dataParam.setModelIndex(deleteModelInstanceParam.getRelationModelIndex());
                        dataParam.setBeforeCoordinate(beforeCoordinate);
                        paramList.add(dataParam);

                    }
                    if (deleteModelInstanceParam.getModelViewType() == 3) {
                        //机柜下属设备视图；修改机柜布局
                        QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                        //
                        clParam.setCurrentInstanceId(strValueConvert(instanceId));
                        //外部关联的实例Id
                        clParam.setInstanceId(relationSiteCabinet);
                        //外部关联的modelIndex
                        clParam.setModelIndex(deleteModelInstanceParam.getRelationModelIndex());
                        clParam.setLastData(lastData);
                        clParamList.add(clParam);
                    }
                }
                if (paramList.size() > 0) {
                    jfParam.setLayoutDataList(paramList);
                    batchUpdateRoomLayout(jfParam);
                }
                if (clParamList.size() > 0) {
                    jgParam.setCabinetLayoutList(clParamList);
                    batchUpdateCabinetLayout(jgParam);
                }
            }
            long time3 = System.currentTimeMillis();
            List<String> tids = deleteModelInstanceParam.getInstanceIds().stream().map(Object::toString).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tids)) {
                mwTangibleAssetsService.deleteDeviceInfo(tids);
                log.info("第一次删除deviceCode;tids:" + tids);
            }
            long time4 = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(deleteModelInstanceParam.getParamList())) {
                //刪除服务器上的WEb监测数据
                deletWebMonitorData(deleteModelInstanceParam.getParamList());
            }
            //资产数据，删除时，需要同步删除关联数据：zabbix主机
            List<DeleteTangAssetsID> ids = new ArrayList<>();
            if (deleteModelInstanceParam.getEsIdList() != null && deleteModelInstanceParam.getEsIdList().size() > 0) {
                BulkResponse bulkResponse = deleteEsInfoById(deleteModelInstanceParam.getEsIdList(), deleteModelInstanceParam.getModelIndex());
                if (bulkResponse != null) {
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
                                mwModelSysLogService.saveInstaceChangeHistory(builder);
                            }
                            //实例数据是否纳管，纳管数据删除时，需要同步删除关联数据：zabbix主机(WEB监测数据除外)
                            if (!Strings.isNullOrEmpty(param.getAssetsId()) && deleteModelInstanceParam.getModelId().intValue() != webMonitorModeId.intValue()) {
                                //httpid有值，表示为web监测数据，不进行zabbix主机删除
                                DeleteTangAssetsID deleteTangAssetsID = new DeleteTangAssetsID();
                                deleteTangAssetsID.setAssetsId(param.getAssetsId());
                                deleteTangAssetsID.setMonitorMode(param.getMonitorMode());
                                deleteTangAssetsID.setMonitorServerId(param.getMonitorServerId());
                                deleteTangAssetsID.setId(param.getInstanceId() + "");
                                deleteTangAssetsID.setModelId(param.getModelId());
                                ids.add(deleteTangAssetsID);
                            }
                        }
                        //刪除关联数据
                        time5 = System.currentTimeMillis();
                        mwModelAssetsDiscoveryServiceImpl.deleteAssetsRelationInfo(ids);
                        time6 = System.currentTimeMillis();

                    } else {
                        return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313008, ErrorConstant.MODEL_INSTANCE_MSG_313008);
                    }
                }
            }
            //删除对应es中的数据  使用的是查询删除  也可以根据es的id进行删除
            if (deleteModelInstanceParam.getInstanceIds() != null && deleteModelInstanceParam.getInstanceIds().size() > 0) {
                //删除对应es数据
                deleteEsInfoByQuery(Arrays.asList(deleteModelInstanceParam.getModelIndex()), deleteModelInstanceParam.getInstanceIds());
            }
            //删除模型实例拓扑关系以及虚拟化资产拓扑
            if (ConnectCheckModelEnum.VCENTER.getModelId().equals(deleteModelInstanceParam.getModelId())) {
                for (Integer instanceId : deleteModelInstanceParam.getInstanceIds()) {
                    ModelVirtualDeleteContext deleteContext = mwModelVirtualizationService.deleteVirtualIntance(instanceId);
                    deleteContext.getInstanceIds().remove(instanceId);
                    //删除VCenter关联的实例
                    mwModelManageDao.deleteModelInstances(deleteContext.getInstanceIds());
                }
            } else {
                time7 = System.currentTimeMillis();
                if (connectionPool != null && connectionPool.getSession() != null) {
                    mwModelRelationsService.deleteRelationByInstances(deleteModelInstanceParam.getModelId(), deleteModelInstanceParam.getInstanceIds());
                }
                time8 = System.currentTimeMillis();
            }

            //Vcenter、citrixADC模型关联了下级设备，需要同步删除
            if (ConnectCheckModelEnum.getAllModelIds().contains(deleteModelInstanceParam.getModelId())) {
                //设置关联Id
                deleteModelInstanceParam.setRelationInstanceIds(deleteModelInstanceParam.getInstanceIds());
                //删除关联数据,将实例Id作为关联id使用，删除所有relationId的值等于该实例id的数据
                batchDeleteRelationInstances(deleteModelInstanceParam);
                deleteInstanceTopo(deleteModelInstanceParam);
            }
            //刪除实例之后调用许可接口
            Integer count = selectCountInstances();
            licenseManagement.getLicenseManagemengt("model_manage", count, 0);
            long time9 = System.currentTimeMillis();
            log.info("删除接口耗时::总耗时:" + (time9 - time1) + "ms:时间1:" + (time2 - time1) + "ms;时间2:" + (time3 - time2) + "ms;时间3:" + (time4 - time3) + "ms;时间5:" + (time6 - time5) + "ms;时间7:" + (time8 - time7) + "ms;时间8:" + (time9 - time8) + "ms;");
            return Reply.ok();
        } catch (Exception e) {
            //强制删除，确保减少脏数据
            mwModelManageDao.deleteModelInstances(deleteModelInstanceParam.getInstanceIds());
            //删除对应es数据
            try {
                deleteEsInfoByQuery(Arrays.asList(deleteModelInstanceParam.getModelIndex()), deleteModelInstanceParam.getInstanceIds());
            } catch (Exception ex) {
                log.error("fail to deleteEsInfoByQuery param{}, case by {}", deleteModelInstanceParam, e);
            }
            //刪除实例之后调用许可接口
            Integer count = selectCountInstances();
            licenseManagement.getLicenseManagemengt("model_manage", count, 0);
            log.error("fail to deleteModelInstance param{}, case by {}", instanceParams, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_CODE_313008, ErrorConstant.MODEL_INSTANCE_MSG_313008);
        } finally {
            //强制执行删除deviceCode
            List<String> tids = deleteModelInstanceParam.getInstanceIds().stream().map(Object::toString).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tids)) {
                mwTangibleAssetsService.deleteDeviceInfo(tids);
                log.info("强制执行删除deviceCode;tids:" + tids);
            }
        }

    }

    private void deletWebMonitorData(List<MwModelInstanceParam> paramList) throws Exception {
        List<MwModelInstanceParam> modelInstanceParams = paramList;
        List<HttpParam> deleteWebMonitor = new ArrayList<>();
        for (MwModelInstanceParam instanceParam : modelInstanceParams) {
            if (instanceParam.getMonitorServerId() != 0 && !Strings.isNullOrEmpty(instanceParam.getHttpId())) {
                HttpParam httpParam = new HttpParam();
                httpParam.setMonitorServerId(instanceParam.getMonitorServerId());
                httpParam.setHttpId(instanceParam.getHttpId());
                deleteWebMonitor.add(httpParam);
            }
        }
        if (CollectionUtils.isNotEmpty(deleteWebMonitor)) {
            mwModelWebMonitorService.deleteWebMonitor(deleteWebMonitor);
        }
    }

    /**
     * 通过查询条件删除es数据
     *
     * @param modelIndexs
     * @param instanceIds
     * @throws IOException
     */
    private BulkByScrollResponse deleteEsInfoByQuery(List<String> modelIndexs, List<Integer> instanceIds) throws
            IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(String.join(",", modelIndexs));
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (Integer instanceId : instanceIds) {
            queryBuilder = queryBuilder.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, instanceId));
        }
        deleteByQueryRequest.setQuery(queryBuilder);
        BulkByScrollResponse response = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        return response;
    }

    /**
     * 通过esId删除es数据
     *
     * @param esIdList
     * @return
     * @throws IOException
     */
    private BulkResponse deleteEsInfoById(List<String> esIdList, String index) throws IOException {
        BulkRequest request = new BulkRequest();
        for (String esId : esIdList) {
            DeleteRequest deleteRequest = new DeleteRequest(index);
            deleteRequest.id(esId);
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
    public Reply selectInstanceInfo(QueryInstanceModelParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            param.setPageSize(pageSize);
            mwModelViewServiceImpl.getInstanceListData(param);
            Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
            long sum = 0l;

            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            if (map != null && map.get("sum") != null) {
                sum = (long) map.get("sum");
            }
        } catch (Exception e) {
            log.error("fail to selectModelInstance param{}, case by {}", param, e);
        }
        return Reply.ok(listMap);
    }


    /**
     * 实例列表数据查询
     *
     * @param param
     * @return
     */
    @Override
    public Reply selectModelInstance(QueryInstanceModelParam param) {

        try {
            mwModelViewServiceImpl.getInstanceListData(param);
            Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
            long sum = 0l;
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }
            if (map != null && map.get("sum") != null) {
                sum = (long) map.get("sum");
            }
            long time12 = System.currentTimeMillis();
            //机构，用户组，负责人字段转换
            mwModelCommonServiceImpl.powerFieldConvert(param.getSkipDataPermission(), listMap);
            long time13 = System.currentTimeMillis();
            //外部关联类型转换（id转为名称）
            if (param.isConvertVal()) {
                mwModelCommonServiceImpl.relationFieldConvert(listMap);
            }
            addRoomAndCabinetInfo(listMap);
            //监控服务关联类型字段值转换
            mwModelCommonServiceImpl.monitorServerRelationConvert(listMap);

            long time2 = System.currentTimeMillis();
            //过滤，没有资产id和服务器id的实例 ，不进行zabbix查询获取健康状态值
            List<Map<String, Object>> newList = new ArrayList<>();
            newList = listMap;
            if (!mwInspectModeService.getInspectModeInfo()) {
                newList = new ArrayList<>();
                newList = mwModelViewServiceImpl.getAssetsStateByZabbix(listMap);
            }

            PageInfo pageInfo = new PageInfo<>(newList);
            pageInfo.setList(newList);
            pageInfo.setTotal(sum);
            return Reply.ok(pageInfo);
//            GlobalUserInfo globalUser = userService.getGlobalUser();
//            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
//            if (param.getIsTimeTask() != null && !param.getIsTimeTask()) {
//                param.setModelInstanceIds(allTypeIdList);
//            }
//            Map priCriteria = PropertyUtils.describe(param);
//            List<ModelInstanceDto> list;
//            List<Map<String, Object>> listMap = new ArrayList<>();
//            long count = 0l;
//            if (param.getIsFlag() != null && param.getIsFlag()) {
//                list = mwModelManageDao.selectModelInstanceBySystemIsFlag(priCriteria);
//            } else {
//                list = mwModelManageDao.selectModelInstance(priCriteria);
//            }
//            if (list.size() > 0) {
//                //条件组合查询
//                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//                Set<Integer> instanceIdSet = new HashSet<>();
//                for (ModelInstanceDto dto : list) {
//                    instanceIdSet.add(dto.getInstanceId());
//                }
//                //如果实例id数量太多,需要分组
//                List<List<Integer>> instanceIdGroups = null;
//                List<Integer> instanceList = new ArrayList<>(instanceIdSet);
//                param.setInstanceIds(instanceList);
//                if (null != instanceList) {
//                    instanceIdGroups = Lists.partition(instanceList, insBatchFetchNum);
//                }
//
//                QueryESWhetherExistField queryRoomParam = new QueryESWhetherExistField();
//                queryRoomParam.setExistFields(Arrays.asList(ROWNUM.getField(), COLNUM.getField(), LAYOUTDATA.getField()));
//                QueryESWhetherExistField queryCabinetParam = new QueryESWhetherExistField();
//                queryCabinetParam.setExistFields(Arrays.asList(UNUM.getField(), POSITIONBYROOM.getField(), RELATIONSITEROOM.getField(), LAYOUTDATA.getField()));
//                //获取所有机柜数据，instanceId为key
//                Map<String, List<Map<String, Object>>> cabitModelInfoMap = new HashMap<>();
//
//
//                if (param.getDoubleQuery() != null && param.getDoubleQuery()) {//西藏邮储环境，双查询
//                    //双查询
//                    if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
//                        BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
//                        for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
//                            if (m.getPropertiesType() != null) {
//                                QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
//                                if (null != qb) {
//                                    queryBuilder2.must(qb);
//                                }
//                            }
//                        }
//                        queryBuilder.must(queryBuilder2);
//                    }
//                } else {
//                    //全字段模糊查询
//                    if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
//                        BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
//                        for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
//                            if (m.getPropertiesType() != null) {
//                                //时间
//                                if (m.getPropertiesType() != null) {
//                                    QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
//                                    if (null != qb) {
//                                        queryBuilder2.should(qb);
//                                    }
//                                }
//                            }
//                        }
//                        queryBuilder.must(queryBuilder2);
//                    }
//                }
//                BoolQueryBuilder pQuery = null;
//                QueryInstanceModelParam queryInstanceModelParam = new QueryInstanceModelParam();
//                if (CollectionUtils.isEmpty(param.getModelIndexs())) {
//                    param.setModelIndexs(Arrays.asList(param.getModelIndex()));
//                }
//                queryInstanceModelParam.setInstanceIds(param.getInstanceIds());
//                PropertyUtils.copyProperties(queryInstanceModelParam, param);
//
//                if (null != instanceIdGroups) {
//                    int startRow = 0;
//                    for (List<Integer> intancedIds : instanceIdGroups) {
//                        pQuery = mwModelViewServiceImpl.createCopy(queryBuilder);
//                        //web监测数据不进行查询过滤，后面特殊处理
//                        if (param.getModelId() == webMonitorModeId) {
//                            pQuery = new BoolQueryBuilder();
//                        }
//                        QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
//                        pQuery.must(queryBuilder1);
//                        if (debug) {
//                            log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
//                        }
//                        SearchResponse search = mwModelViewServiceImpl.doEsSearchResponse(pQuery
//                                , startRow
//                                , insBatchFetchNum
//                                , queryInstanceModelParam
//                                , queryInstanceModelParam.getModelIndexs());
//                        //只有在机柜下属实例中（拥有POSITIONBYCABINET，RELATIONSITECABINET字段的），才会查es中所有机柜机房数据
//                        //isFlag 和 isFlag1 作为开关，控制是否查询es中所有机柜机房数据，且只查一次
//                        for (SearchHit searchHit : search.getHits().getHits()) {
//                            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
//                            sourceAsMap.put("esId", searchHit.getId());
//                            if (sourceAsMap.containsKey(MONITOR_FLAG) && sourceAsMap.get(MONITOR_FLAG) != null) {
//                                Object monitorFlag = sourceAsMap.get(MONITOR_FLAG);
//                                if ("true".equals(monitorFlag.toString())) {
//                                    sourceAsMap.put(MONITOR_FLAG, true);
//                                }
//                                if ("false".equals(monitorFlag.toString())) {
//                                    sourceAsMap.put(MONITOR_FLAG, false);
//                                }
//                            }
//                            listMap.add(sourceAsMap);
//                        }
//
//                        count += search.getHits().getTotalHits().value;
//                        startRow += insBatchFetchNum;
//                    }
//                    int startIndex = param.getPageNumber();
//                    int endIndex = param.getPageSize();
//                    //根据树结构ModelId:72，表示web监测模型，单独处理
//                    if (param.getModelId() == webMonitorModeId) {
//                        startIndex = 0;
//                        endIndex = pageSize;
//                    }
//                    listMap = pageList.getList(listMap, startIndex, endIndex);
//                    addRoomAndCabinetInfo(listMap);
//                }
//            }
//            //过滤，没有资产id和服务器id的实例 ，不进行zabbix查询获取健康状态值
//            long time1 = 0l;
//            long time2 = 0l;
//            long time21 = 0l;
//            long time22 = 0l;
//            long time3 = 0l;
//            long time4 = 0l;
//            time1 = System.currentTimeMillis();
//
//            //机构，用户组，负责人字段转换
//            mwModelCommonServiceImpl.powerFieldConvert(param.getIsTimeTask(), listMap);
//
//            //根据树结构ModelId:72，表示web监测模型，单独处理
//            if (param.getModelId() == webMonitorModeId) {
//                //进入web监测插叙接口
//                List<MwModelWebMonitorTable> webMonitorParams = MwModelUtils.convertEsData(MwModelWebMonitorTable.class, listMap);
//                List<MwModelWebMonitorTable> webMonitorTables = mwModelWebMonitorService.queryWebSeverList(webMonitorParams);
//                List<Map<String, Object>> newListMap = ListMapObjUtils.convertList(webMonitorTables);
//                listMap = newListMap;
//                //web监测查询处理
//                listMap = mwModelViewServiceImpl.webMonitorConvert(listMap, param);
//            }
//            //资产状态处理
//            mwModelViewServiceImpl.getAssetsStateByZabbix(listMap);
//            time4 = System.currentTimeMillis();
//            pageInfo.setList(listMap);
//            pageInfo.setTotal(count);
//            System.out.println("时间1 = " + (time2 - time1) + "ms;时间2 = " + (time3 - time2) + "ms;时间3 = " + (time4 - time3) + "ms");
//            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectModelInstance param{}, case by {}", param, e);
            return Reply.fail(ErrorConstant.MODEL_INSTANCE_SELECT_CODE_313005, ErrorConstant.MODEL_INSTANCE_SELECT_MSG_313005);
        }
    }


    public void addRoomAndCabinetInfo(List<Map<String, Object>> listMap) {
        QueryESWhetherExistField queryRoomParam = new QueryESWhetherExistField();
        queryRoomParam.setExistFields(Arrays.asList(ROWNUM.getField(), COLNUM.getField(), LAYOUTDATA.getField()));
        QueryESWhetherExistField queryCabinetParam = new QueryESWhetherExistField();
        queryCabinetParam.setExistFields(Arrays.asList(UNUM.getField(), POSITIONBYROOM.getField(), RELATIONSITEROOM.getField(), LAYOUTDATA.getField()));
        Map<String, List<Map<String, Object>>> cabitModelInfoMap = new HashMap<>();
        Boolean isFlag = false;
        Boolean isFlag1 = true;
        for (Map<String, Object> sourceAsMap : listMap) {
            String relationCabinetInstanceId = "0";
            String relationRoomInstanceId = "0";
            ////////////添加所属机柜所属机房信息/////////////////
            if (sourceAsMap.get(RELATIONSITECABINET.getField()) != null
                    && sourceAsMap.get(POSITIONBYCABINET.getField()) != null) {
                relationCabinetInstanceId = sourceAsMap.get(RELATIONSITECABINET.getField()).toString();
                isFlag = true;
            }
            if (isFlag && isFlag1) {
                cabitModelInfoMap = getInstanceInfoByExistsField(queryCabinetParam);
                //只需要查询一次，isFlag1设为false
                isFlag1 = false;
            }
            if (cabitModelInfoMap != null && CollectionUtils.isNotEmpty(cabitModelInfoMap.get(relationCabinetInstanceId))) {
                //根据所属机柜Id，查询机柜实例信息
                Map<String, Object> cabitMaps = cabitModelInfoMap.get(relationCabinetInstanceId).get(0);
                if (cabitMaps.get(RELATIONSITEROOM.getField()) != null) {
                    //根据机柜实例中的所属机房字段，查询机房实例信息；
                    relationRoomInstanceId = cabitMaps.get(RELATIONSITEROOM.getField()).toString();
                    //设置所属机房坐标
                    sourceAsMap.put(POSITIONBYROOM.getField(), cabitMaps.get(POSITIONBYROOM.getField()));
                    //设置所属机房关联Id
                    sourceAsMap.put(RELATIONSITEROOM.getField(), relationRoomInstanceId);
                }
            }
        }
    }


    /**
     * 根据指定的字段查询模型实例数据
     *
     * @param queryParam
     * @return
     */
    private Map<String, List<Map<String, Object>>> getInstanceInfoByExistsField(QueryESWhetherExistField queryParam) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        queryParam.setIsBaseData(false);
        List<Map<String, Object>> listMap = mwModelViewService.getWhetherExistsFieldModelInfo(queryParam);
        map = listMap.stream().collect(Collectors.groupingBy(m -> m.get("modelInstanceId") != null ?
                m.get("modelInstanceId").toString() : ""));
        return map;
    }

    /**
     * @param queryParam
     * @return
     */
    private List<Map<String, Object>> getInstanceInfoByES(QueryESWhetherExistField queryParam) {
        queryParam.setIsBaseData(false);
        List<Map<String, Object>> listMap = mwModelViewService.getWhetherExistsFieldModelInfo(queryParam);
        return listMap;
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
                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
            queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
        if (CollectionUtils.isNotEmpty(modelIndexs)) {
            instanceIdList = mwModelViewDao.getInstanceIdByBase(modelIndexs);
        }
        param.setModelIndexs(modelIndexs);
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        if (globalUser.isSystemUser()) {//系统管理员，查询所有实例id
            param.setInstanceIds(instanceIdList);
        } else {//普通用户，获取对应的实例id
            List<Integer> intIds = allTypeIdList.stream().filter(str -> str.matches("\\d+"))//过滤非数字的数据
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
                    queryBuilder0 = queryBuilder0.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, instanceId));
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
                            if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                String value = m.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + value + "*"));
                            }
                            //数组
                            if ((typeList.contains(m.getPropertiesType())) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), m.getPropertiesValue()));
                            }
                            //布尔类型
                            if ((m.getPropertiesType().intValue() == ModelPropertiesType.SWITCH.getCode()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(m.getPropertiesIndexId(), Boolean.parseBoolean(m.getPropertiesValue())));
                            }
                            //结构体 使用嵌套查询
                            if ((!Strings.isNullOrEmpty(m.getPropertiesValue())) && m.getPropertiesType() == ModelPropertiesType.STRUCE.getCode()) {
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
                            if (arrList.contains(m.getPropertiesType()) && (!Strings.isNullOrEmpty(m.getPropertiesValue()))) {
                                String value = m.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                                queryBuilder2 = queryBuilder2.should(QueryBuilders.wildcardQuery(m.getPropertiesIndexId() + ".keyword", "*" + value + "*"));
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
     * 根据字段值查询数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getInstanceInfoByPropertiesValue(QueryInstanceModelParam param) throws Exception {
        //资产视图树的所有数据为基础设施下的模型实例

        if (param.getSkipDataPermission() != null && param.getSkipDataPermission()) {
//            param.setInstanceIds(param.getInstanceIds());
        } else {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
            //系统管理员，或者忽略权限控制时，查询所有实例id
            if (globalUser.isSystemUser() || (param.getSkipDataPermission() != null && param.getSkipDataPermission())) {
//                param.setInstanceIds(param.getInstanceIds());
            } else {//普通用户，获取对应的实例id
                List<Integer> intIds = allTypeIdList.stream().filter(str -> str.matches("\\d+"))//过滤非数字的数据
                        .map(Integer::parseInt).collect(Collectors.toList());
                param.setInstanceIds(intIds);
            }
        }

        List<Map<String, Object>> listMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            //如果实例id数量太多,需要分组
            List<List<Integer>> instanceIdGroups = null;
            if (null != param.getInstanceIds()) {
                instanceIdGroups = Lists.partition(param.getInstanceIds(), insBatchFetchNum);
            }
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    //资产视图列表输入框查询
                    QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
                    if (null != qb) {
                        queryBuilder2.should(qb);
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
            BoolQueryBuilder pQuery = null;
            //有Id分组插叙
            if (null != instanceIdGroups) {
                int startRow = 0;
                for (List<Integer> intancedIds : instanceIdGroups) {
                    pQuery = mwModelViewServiceImpl.createCopy(queryBuilder);
                    QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                    pQuery.must(queryBuilder1);
                    if (debug) {
                        log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                    }
                    List<Map<String, Object>> ret = mwModelViewServiceImpl.doEsSearch(pQuery
                            , startRow
                            , insBatchFetchNum
                            , param
                            , param.getModelIndexs());
                    if (null != ret) {
                        listMap.addAll(ret);
                    }
                }
            } else {//直接根据属性字段和属性值查询
                int startRow = 0;
                pQuery = mwModelViewServiceImpl.createCopy(queryBuilder);
                if (debug) {
                    log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                }
                List<Map<String, Object>> ret = mwModelViewServiceImpl.doEsSearch(pQuery
                        , startRow
                        , param.getPageSize()
                        , param
                        , param.getModelIndexs());
                if (null != ret) {
                    listMap.addAll(ret);
                }
            }
        }
        return listMap;
    }


    /**
     * 多索引查询
     *
     * @param param
     * @return
     */
    @SneakyThrows
    @Override
    public List<Map<String, Object>> getInstanceInfoByModelIndexs(QueryInstanceModelParam param) {
        //资产视图树的所有数据为基础设施下的模型实例
        if (param.getIsBaseData() == null) {
            param.setIsBaseData(false);
        }
        Set<String> modelIndexSet = new HashSet<>();
        List<Integer> instanceIdList = new ArrayList<>();
        if (CollectionUtils.isEmpty(param.getModelIndexs())) {
            //获取所有模型Index和实例Id
            List<ModelInstanceBaseInfoDTO> lists = mwModelViewDao.getModelIndexANDInstanceInfo(param.getIsBaseData());
            for (ModelInstanceBaseInfoDTO aParam : lists) {
                modelIndexSet.add(aParam.getModelIndex());
                instanceIdList.add(aParam.getInstanceId());
            }
            param.setInstanceIds(instanceIdList);
            param.setModelIndexs(new ArrayList<>(modelIndexSet));
        }
        if (CollectionUtils.isEmpty(param.getInstanceIds())) {
            List<Integer> instanceIds = mwModelInstanceDao.getInstanceIdsByModelIndex(param.getModelIndexs(), null);
            param.setInstanceIds(instanceIds);
        }
        if (param.getSkipDataPermission() != null && param.getSkipDataPermission()) {
//            param.setInstanceIds(param.getInstanceIds());
        } else {
            GlobalUserInfo globalUser = userService.getGlobalUser();
            List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
            //系统管理员，或者忽略权限控制时，查询所有实例id
            if (globalUser.isSystemUser() || (param.getSkipDataPermission() != null && param.getSkipDataPermission())) {
//                param.setInstanceIds(param.getInstanceIds());
            } else {//普通用户，获取对应的实例id
                List<Integer> intIds = allTypeIdList.stream().filter(str -> str.matches("\\d+"))//过滤非数字的数据
                        .map(Integer::parseInt).collect(Collectors.toList());
                param.setInstanceIds(intIds);
            }
        }

        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            //如果实例id数量太多,需要分组
            List<List<Integer>> instanceIdGroups = null;
            if (null != param.getInstanceIds()) {
                instanceIdGroups = Lists.partition(param.getInstanceIds(), insBatchFetchNum);
            }
            //全字段模糊查询
            if (param.getPropertiesList() != null && param.getPropertiesList().size() > 0) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery();
                for (AddModelInstancePropertiesParam m : param.getPropertiesList()) {
                    //资产视图列表输入框查询
                    QueryBuilder qb = MwModelUtils.tranformEsQuery(m);
                    if (null != qb) {
                        queryBuilder2.should(qb);
                    }
                }
                queryBuilder.must(queryBuilder2);
            }
            BoolQueryBuilder pQuery = null;
            if (null != instanceIdGroups) {
                int startRow = 0;
                for (List<Integer> intancedIds : instanceIdGroups) {
                    pQuery = mwModelViewServiceImpl.createCopy(queryBuilder);
                    QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, intancedIds);
                    pQuery.must(queryBuilder1);
                    if (debug) {
                        log.info("es query1:{}", pQuery.toString().replaceAll("\r|\n", ""));
                    }
                    List<Map<String, Object>> ret = mwModelViewServiceImpl.doEsSearch(pQuery
                            , startRow
                            , insBatchFetchNum
                            , param
                            , param.getModelIndexs());
                    if (null != ret) {
                        listMap.addAll(ret);
                    }
                }
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
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
                    queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, dto.getInstanceId()));
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
                    if (sourceAsMap.containsKey(MONITOR_FLAG) && sourceAsMap.get(MONITOR_FLAG) != null) {
                        Object monitorFlag = sourceAsMap.get(MONITOR_FLAG);
                        if ("true".equals(monitorFlag.toString())) {
                            sourceAsMap.put(MONITOR_FLAG, true);
                        }
                        if ("false".equals(monitorFlag.toString())) {
                            sourceAsMap.put(MONITOR_FLAG, false);
                        }
                    }
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
                queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(INSTANCE_ID_KEY, param.getModelInstanceId()));
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
                if (sourceAsMap.containsKey(MONITOR_FLAG) && sourceAsMap.get(MONITOR_FLAG) != null) {
                    Object monitorFlag = sourceAsMap.get(MONITOR_FLAG);
                    if ("true".equals(monitorFlag.toString())) {
                        sourceAsMap.put(MONITOR_FLAG, true);
                    }
                    if ("false".equals(monitorFlag.toString())) {
                        sourceAsMap.put(MONITOR_FLAG, false);
                    }
                }
                listMap.add(sourceAsMap);
            }
        }
        return listMap;
    }

    @Override
    public Reply getInstanceInfoById(QueryModelInstanceParam param) {
        //TODO 待优化，不使用map全部改为实体对象接收
        try {
            //type类型为10、11、12、13,16都为数组类型
            List<Map<String, Object>> listMap = getInfoByInstanceId(param);
            List<Map<String, Object>> listMapNew = new ArrayList<>();
            List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(param.getModelId());
            List<PropertyInfo> propertyInfos = new ArrayList<>();
            for (ModelInfo modelInfo : modelInfos) {
                if (null != modelInfo && null != modelInfo.getPropertyInfos()) {
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }
            }

            PropertyInfo esPropertyInfo = new PropertyInfo();
            esPropertyInfo.setIndexId("esId");
            esPropertyInfo.setPropertiesName("esId");
            propertyInfos.add(esPropertyInfo);

            PropertyInfo insPropertyInfo = new PropertyInfo();
            insPropertyInfo.setIndexId("modelInstanceId");
            insPropertyInfo.setPropertiesName("模型实例Id");
            propertyInfos.add(insPropertyInfo);
            //获取模板的名称，zabbixTemplateId和监控方式
            List<MwModelTemplateInfo> listTemplateName = mwModelViewDao.getTemplateNameByModeAndId();
            //将监控方式和templateId最为key值，存入模板名称
            Map<String, String> templateNameMap = new HashMap();
            for (MwModelTemplateInfo templateInfo : listTemplateName) {
                templateNameMap.put(templateInfo.getServerTemplateId() + "_" + templateInfo.getMonitorMode(), templateInfo.getTemplateName());
            }

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


                String monitorMode = "";
                String templateId = "";
                if (data.get("monitorMode") != null) {
                    monitorMode = data.get("monitorMode").toString();
                }

                if (data.get("templateId") != null) {
                    templateId = data.get("templateId").toString();
                }

                if (data.get(SNMPV1V2) != null) {
                    List<MwSnmpv1AssetsDTO> snmpv1AssetsList = JSONArray.parseArray(JSON.toJSONString(data.get(SNMPV1V2)), MwSnmpv1AssetsDTO.class);
                    if (CollectionUtils.isNotEmpty(snmpv1AssetsList)) {
                        map.put("snmpV1AssetsDTO", snmpv1AssetsList.get(0));
                    }
                }
                if (data.get(SNMPV3) != null) {
                    List<MwSnmpAssetsDTO> snmpv3AssetsList = JSONArray.parseArray(JSON.toJSONString(data.get(SNMPV3)), MwSnmpAssetsDTO.class);
                    if (CollectionUtils.isNotEmpty(snmpv3AssetsList)) {
                        map.put("snmpAssetsDTO", snmpv3AssetsList.get(0));
                    }
                }
                if (data.get(ZABBIX_AGENT) != null) {
                    List<MwAgentAssetsDTO> agentAssetsDTOList = JSONArray.parseArray(JSON.toJSONString(data.get(ZABBIX_AGENT)), MwAgentAssetsDTO.class);
                    if (CollectionUtils.isNotEmpty(agentAssetsDTOList)) {
                        map.put("agentAssetsDTO", agentAssetsDTOList.get(0));
                    }
                }
                if (data.get(ICMP) != null) {
                    List<MwPortAssetsDTO> portAssetsDTOList = JSONArray.parseArray(JSON.toJSONString(data.get(ICMP)), MwPortAssetsDTO.class);
                    if (CollectionUtils.isNotEmpty(portAssetsDTOList)) {
                        map.put("portAssetsDTO", portAssetsDTOList.get(0));
                    }
                }
                if (data.get(IMPI) != null) {
                    List<MwIPMIAssetsDTO> mwIPMIAssetsDTOList = JSONArray.parseArray(JSON.toJSONString(data.get(IMPI)), MwIPMIAssetsDTO.class);
                    if (CollectionUtils.isNotEmpty(mwIPMIAssetsDTOList)) {
                        map.put("mwIPMIAssetsDTO", mwIPMIAssetsDTOList.get(0));
                    }
                }

                for (PropertyInfo m : propertyInfos) {
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
                //根据assetsId和ismanage判断该资产是否已经纳管，
                //纳管过的资产operationMonitor设置为true
                if (data.get("assetsId") != null && data.get("assetsId").toString() != "") {
                    map.put("operationMonitor", true);
                }
                if (data.get("monitorServerId") != null && data.get("monitorServerId").toString() != "") {
                    Integer monitorServerId = Integer.valueOf(data.get("monitorServerId").toString());
                    map.put("monitorServerId", monitorServerId);
                }
                if (data.get("pollingEngineName") != null && LOCALHOST_NAME.equals(data.get("pollingEngineName").toString())) {
                    map.put("pollingEngine", localEngine);
                }
                map.put("modelIndex", param.getModelIndex());
                map.put("modelId", param.getModelId());
                String key = templateId + "_" + monitorMode;
                if (templateNameMap != null && templateNameMap.get(key) != null) {
                    map.put("templateName", templateNameMap.get(key));
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
            List<ModelInfo> modelInfoList = null;
            if (isFlag && param.getType() != null && "group".equals(param.getType())) {
                listMap = getInstanceInfoByExportGroup(param);
                modelInfoList = mwModelManageDao.selectPropertiesByInstanceFuzzyQueryGroup(param.getModelId());
            } else {
                listMap = getInstanceInfoByModelId(param);
                modelInfoList = mwModelManageDao.selectPropertiesByInstanceFuzzyQuery(param.getModelId());
            }

            List<PropertyInfo> propertyInfos = new ArrayList<>();
            for (ModelInfo modelInfo : modelInfoList) {
                if (null != modelInfo.getPropertyInfos()) {
                    List<PropertyInfo> list = modelInfo.findPropertiesByInstanceFuzzyQuery();
                    propertyInfos.addAll(list);
                }
            }
            //将获取的es索引数据和数据库保存的属性值进行比对,相同的保留。
            List<String> datas = new ArrayList<>();
            for (Map data : listMap) {
                for (PropertyInfo m : propertyInfos) {
                    String propertiesIndexId = m.getIndexId();
                    if (data.get(propertiesIndexId) != null) {
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
        List<ModelInfo> modelInfoList = null;
        //获取模型及其父模型属性
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            modelInfoList = mwModelManageDao.selectListWithParentAndGroup(queryCustomModelparam.getModelId());
        } else {
            modelInfoList = mwModelManageDao.selectModelListWithParent(queryCustomModelparam.getModelId());
        }
        List<MwCustomColByModelDTO> colLists;
        //获取pageField表数据和costomCol表数据
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            colLists = mwModelInstanceDao.selectFiledsByGroupList(queryCustomModelparam);
        } else {
            colLists = mwModelInstanceDao.selectByModelUserIdList(queryCustomModelparam);
        }

        Map<String, PropertyInfo> allPropertyMap = new HashMap<>();
        for (ModelInfo modelInfo : modelInfoList) {
            if (null != modelInfo.getPropertyInfos()) {
                allPropertyMap.putAll(modelInfo.getPropertyInfoMapByIndexId());
            }
        }
        Map<String, PropertyInfo> maps = new HashMap<>();
        //列表字段中有所属机柜字段，则表示该实例为机柜下属设备，需要关联查出所属机房信息。
        if (allPropertyMap != null && allPropertyMap.get(RELATIONSITECABINET.getField()) != null) {
            maps = getRelationModelIndexInfo(allPropertyMap);
        }
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (ModelInfo modelInfo : modelInfoList) {
            if (null != modelInfo.getPropertyInfos()) {
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
        }

        List<MwCustomColByModelDTO> colList = new ArrayList<>();
        for (PropertyInfo propertyInfo : propertyInfoList) {
            if (propertyInfo.getIsShow()) {
                //设值所属机房关联modelIndex和propertiesIndex；
                if (maps != null && maps.get(propertyInfo.getIndexId()) != null && propertyInfo.getIndexId().equals(RELATIONSITEROOM.getField())) {
                    PropertyInfo property = maps.get(propertyInfo.getIndexId());
                    propertyInfo.setRelationModelIndex(property.getRelationModelIndex());
                    propertyInfo.setRelationPropertiesIndex(property.getRelationPropertiesIndex());
                }
                //机房机柜下属模型判断，
                MwCustomColByModelDTO mwCustomColByModelDTO = new MwCustomColByModelDTO();
                mwCustomColByModelDTO.extractFrom(propertyInfo);
                InstanceFieldQueryType tast = InstanceFieldQueryType.valueOf(queryCustomModelparam.getQueryType());
                switch (tast) {
                    case insert:
                        if (propertyInfo.getIsInsertShow()) {
                            colList.add(mwCustomColByModelDTO);
                        }
                        break;
                    case list:
                        if (propertyInfo.getIsListShow()) {
                            colList.add(mwCustomColByModelDTO);
                        }
                        break;
                    case editor:
                        if (propertyInfo.getIsEditorShow()) {
                            colList.add(mwCustomColByModelDTO);
                        }
                        break;
                    case look:
                        if (propertyInfo.getIsLookShow()) {
                            colList.add(mwCustomColByModelDTO);
                        }
                        break;
                    default:
                }
            }
        }
        try {
            //将customCol的数据赋给 MwCustomColByModelDTO
            for (MwCustomColByModelDTO customColByModelDTO : colList) {
                for (MwCustomColByModelDTO customColDTOs : colLists) {
                    if (customColByModelDTO.getProp().equals(customColDTOs.getProp())) {
//                        BeanUtils beanUtilsBean = BeanUtilsBean.getInstance();
//                        //空值不赋值，过滤customColDTOs中的空值null
//                        beanUtilsBean.getConvertUtils().register(false, true, 0);
                        BeanUtils.copyProperties(customColDTOs, customColByModelDTO, ModelUtils.getNullPropertyNames(customColDTOs));
                    }
                }
            }
            colList = colList.stream().sorted(Comparator.comparing(MwCustomColByModelDTO::getOrderNumber)).collect(Collectors.toList());
            return Reply.ok(colList);
        } catch (Exception e) {
            return Reply.fail(500, "获取失败!");
        }
    }

    private Map<String, PropertyInfo> getRelationModelIndexInfo(Map<String, PropertyInfo> allPropertyMap) {
        String cabinetIndex = "";
        PropertyInfo propertyInfo = allPropertyMap.get(RELATIONSITECABINET.getField());
        //所属机柜中的关联机柜模型modelIndex和PropertiesIndex不为空时，据此去该机柜（modelIndex）中查询所属机房信息。
        if ((!Strings.isNullOrEmpty(propertyInfo.getRelationModelIndex())) &&
                (!Strings.isNullOrEmpty(propertyInfo.getRelationPropertiesIndex()))) {
            cabinetIndex = propertyInfo.getRelationModelIndex();
        }
        Map<String, PropertyInfo> maps = new HashMap<>();
        ModelInfo relationMdeolInfo = mwModelManageDao.selectBaseModelInfoByIndex(cabinetIndex);
        if (relationMdeolInfo != null) {
            maps = relationMdeolInfo.getPropertyInfoMapByIndexId();
        }
        return maps;
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
                    String.valueOf(ORG.getCode()).equals(dto.getType()) ||
                    String.valueOf(USER.getCode()).equals(dto.getType()) ||
                    String.valueOf(GROUP.getCode()).equals(dto.getType())) {
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

    private List<MwCustomColByModelDTO> getMwCustomCol(QueryCustomModelparam queryCustomModelparam) {
        List<MwCustomColByModelDTO> ret = null;
        List<ModelInfo> modelInfos = null;
        if (queryCustomModelparam.getTreeType() != null && queryCustomModelparam.getTreeType().equals("group")) {
            modelInfos = mwModelManageDao.selectModelInfoByGroupId(queryCustomModelparam.getModelId());
        } else {
            modelInfos = mwModelManageDao.selectModelListWithParent(queryCustomModelparam.getModelId());
        }

        Map<String, PropertyInfo> allPropertyMap = new HashMap<>();
        for (ModelInfo modelInfo : modelInfos) {
            if (null != modelInfo.getPropertyInfos()) {
                allPropertyMap.putAll(modelInfo.getPropertyInfoMapByIndexId());
            }
        }
        Map<String, PropertyInfo> maps = new HashMap<>();
        //列表字段中有所属机柜字段，则表示该实例为机柜下属设备，需要关联查出所属机房信息。
        if (allPropertyMap != null && allPropertyMap.get(RELATIONSITECABINET.getField()) != null) {
            maps = getRelationModelIndexInfo(allPropertyMap);
        }

        //模型信息排序, 子模型排第一, 父模型在后面
        ModelInfo oriModelInfo = null;
        List<ModelInfo> orderModels = new ArrayList<>();
        for (ModelInfo modelInfo : modelInfos) {
            if (queryCustomModelparam.getModelId().equals(modelInfo.getModelId())) {
                oriModelInfo = modelInfo;
            } else {
                orderModels.add(modelInfo);
            }
        }
        orderModels.add(0, oriModelInfo);

        if (null != modelInfos) {
            ret = new ArrayList<>();
            Set<String> propertyNameSet = new HashSet<>();
            for (ModelInfo modelInfo : modelInfos) {
                if (null != modelInfo.getPropertyInfos() && modelInfo.getPropertyInfos().size() > 0) {
                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                        //设值所属机房关联modelIndex和propertiesIndex；
                        if (maps != null && maps.get(propertyInfo.getIndexId()) != null && propertyInfo.getIndexId().equals(RELATIONSITEROOM.getField())) {
                            PropertyInfo property = maps.get(propertyInfo.getIndexId());
                            propertyInfo.setRelationModelIndex(property.getRelationModelIndex());
                            propertyInfo.setRelationPropertiesIndex(property.getRelationPropertiesIndex());
                        }
                        //根据查询类型过滤显示字段
                        if (StringUtils.isNotEmpty(queryCustomModelparam.getQueryType())) {
                            PropertyFilterType propertyFilterType = PropertyFilterType.valueOf(queryCustomModelparam.getQueryType());
                            if (!propertyFilterType.filter(propertyInfo)) {
                                continue;
                            }
                        }
                        if (!propertyInfo.getIsShow()
                                || propertyNameSet.contains(propertyInfo.getPropertiesName())) {
                            continue;
                        }
                        MwCustomColByModelDTO mwCustomColByModelDTO = new MwCustomColByModelDTO();
                        mwCustomColByModelDTO.extractFrom(propertyInfo);
                        ret.add(mwCustomColByModelDTO);
                        propertyNameSet.add(propertyInfo.getPropertiesName());
                    }
                }
            }
        }
        Collections.sort(ret);
        return ret;
    }

    @Override
    public Reply selectModelInstanceFiledList(QueryCustomModelCommonParam queryCustomModelParam) {
        try {
            List<MwCustomFieldCommon> list = new ArrayList<>();
            //根据modelIds查询模型信息
            List<ModelInfo> modelInfoByIds = mwModelManageDao.selectModelInfoByIds(queryCustomModelParam.getModelIds());
            //查询所有的父模型信息
            List<ModelInfo> allParentModelInfos = mwModelManageDao.selectAllParentModelInfo();
            Map<Integer, ModelInfo> collect = allParentModelInfos.stream().collect(Collectors.toMap(s -> s.getModelId(), s -> s));
            //循环获取每个子模型对应的父模型
            for (ModelInfo modelInfoById : modelInfoByIds) {
                MwCustomFieldCommon mwCustomFieldCommon = new MwCustomFieldCommon();
                mwCustomFieldCommon.setModelId(modelInfoById.getModelId());
                List<ModelInfo> modelInfos = new ArrayList<>();
                List<MwCustomColByModelDTO> ret = null;
                modelInfos.add(modelInfoById);
                String pIds = modelInfoById.getPids();
                if (!Strings.isNullOrEmpty(modelInfoById.getPids())) {
                    List<String> modelIds = Arrays.asList(pIds.split(","));
                    for (String modelId : modelIds) {
                        if (collect != null && collect.containsKey(intValueConvert(modelId))) {
                            modelInfos.add(collect.get(intValueConvert(modelId)));
                        }
                    }
                }
                Map<String, PropertyInfo> allPropertyMap = new HashMap<>();
                for (ModelInfo model : modelInfos) {
                    if (null != model.getPropertyInfos()) {
                        allPropertyMap.putAll(model.getPropertyInfoMapByIndexId());
                    }
                }
                if (null != modelInfos) {
                    ret = new ArrayList<>();
                    Set<String> propertyNameSet = new HashSet<>();
                    for (ModelInfo modelInfo : modelInfos) {
                        if (null != modelInfo.getPropertyInfos() && modelInfo.getPropertyInfos().size() > 0) {
                            for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                                //设值所属机房关联modelIndex和propertiesIndex；
                                //根据查询类型过滤显示字段
                                if (StringUtils.isNotEmpty(queryCustomModelParam.getQueryType())) {
                                    PropertyFilterType propertyFilterType = PropertyFilterType.valueOf(queryCustomModelParam.getQueryType());
                                    if (!propertyFilterType.filter(propertyInfo)) {
                                        continue;
                                    }
                                }
                                if (!propertyInfo.getIsShow()
                                        || propertyNameSet.contains(propertyInfo.getPropertiesName())) {
                                    continue;
                                }
                                MwCustomColByModelDTO mwCustomColByModelDTO = new MwCustomColByModelDTO();
                                mwCustomColByModelDTO.extractFrom(propertyInfo);
                                ret.add(mwCustomColByModelDTO);
                                propertyNameSet.add(propertyInfo.getPropertiesName());
                            }
                        }
                    }
                }
                Collections.sort(ret);
                List<MwCustomColByModelDTO> colList = ret;
                Map<String, List<MwCustomColByModelDTO>> groupMap = new HashMap<>();

                //根据类型分组返回信息
                List<MwCustomColByModelDTO> groupList = null;
                for (MwCustomColByModelDTO col : colList) {
                    String type = col.getPropertiesType();
                    if (null != type) {
                        groupList = groupMap.get(type);
                        if (null == groupList) {
                            groupList = new ArrayList<>();
                            groupMap.put(type, groupList);
                        }
                        groupList.add(col);
                    }
                }

                List<MWModelInstanceFiled> mapList = new ArrayList<>();
                for (String type : groupMap.keySet()) {
                    MWModelInstanceFiled mwModelInstanceFiled = new MWModelInstanceFiled();
                    List<MwCustomColByModelDTO> newList = groupMap.get(type);
                    mwModelInstanceFiled.setType(type);
                    mwModelInstanceFiled.setData(newList);
                    mapList.add(mwModelInstanceFiled);
                }
                mwCustomFieldCommon.setFieldInfo(mapList);
                list.add(mwCustomFieldCommon);
            }
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("failed to selectModelInstanceFiledList::", e);
            return Reply.fail(500, "获取字段失败");
        }
    }


    /**
     * 查询模型实例列表显示的字段名
     *
     * @param queryCustomModelparam
     * @return
     */
    @Override
    public Reply selectModelInstanceFiledByInsert(QueryCustomModelparam queryCustomModelparam) {

        List<MwCustomColByModelDTO> colList = getMwCustomCol(queryCustomModelparam);

        Map<String, List<MwCustomColByModelDTO>> groupMap = new HashMap<>();

        //根据类型分组返回信息
        List<MwCustomColByModelDTO> groupList = null;
        for (MwCustomColByModelDTO col : colList) {
            String type = col.getPropertiesType();
            if (null != type) {
                groupList = groupMap.get(type);
                if (null == groupList) {
                    groupList = new ArrayList<>();
                    groupMap.put(type, groupList);
                }
                groupList.add(col);
            }
        }

        List<MWModelInstanceFiled> mapList = new ArrayList<>();
        for (String type : groupMap.keySet()) {
            MWModelInstanceFiled mwModelInstanceFiled = new MWModelInstanceFiled();
            List<MwCustomColByModelDTO> newList = groupMap.get(type);
            mwModelInstanceFiled.setType(type);
            mwModelInstanceFiled.setData(newList);
            mapList.add(mwModelInstanceFiled);
        }

        return Reply.ok(mapList);
    }

    /**
     * 查询模型列表信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply queryModelListInfo(QueryInstanceModelParam param) {
        GlobalUserInfo globalUser = userService.getGlobalUser();
        List<String> modelIdList = userService.getAllTypeIdList(globalUser, DataType.MODEL_MANAGE);
        List<String> instanceIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
        List<ModelListInfoDTO> modelListInfoList = new ArrayList<>();
        if (isFlag) {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            modelListInfoList = mwModelInstanceDao.queryModelListInfo(param.getModelId(), null, instanceIdList);
        } else {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            modelListInfoList = mwModelInstanceDao.queryModelListInfo(param.getModelId(), modelIdList, instanceIdList);
        }
        //模型列表实例数据数量需要同步es，防止两边数据库资产数据不对等。
        List<String> modelIndexs = modelListInfoList.stream().map(ModelListInfoDTO::getModelIndex).collect(Collectors.toList());
        QueryInstanceModelParam qParam = new QueryInstanceModelParam();
        qParam.setIsBaseData(false);
        qParam.setModelIndexs(modelIndexs);
        mwModelViewServiceImpl.getInstanceListData(qParam);
        //获取对应的es实例数据
        qParam.setPageSize(pageSize);
        Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(qParam);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (map != null && map.get("data") != null) {
            listMap = (List<Map<String, Object>>) map.get("data");
        }
        Map<String, Long> assetsTypeCollect = listMap.stream().collect(Collectors.groupingBy(m -> m.get(MODEL_ID_KEY) != null ? String.valueOf(m.get(MODEL_ID_KEY)) : "0"
                , Collectors.counting()));
        for (ModelListInfoDTO infoDTO : modelListInfoList) {
            //ModelGroupId等同于modelId
            if (assetsTypeCollect != null && assetsTypeCollect.size() > 0 && assetsTypeCollect.containsKey(infoDTO.getModelGroupId())) {
                infoDTO.setInstanceNum(assetsTypeCollect.get(infoDTO.getModelGroupId()).intValue());
            }
        }
        if (param.getSortField() != null && StringUtils.isNotEmpty(param.getSortField())) {
            ListSortUtil<ModelListInfoDTO> finalHostTableDtos = new ListSortUtil<>();
            String sort = "sort" + param.getSortField().substring(0, 1).toUpperCase() + param.getSortField().substring(1);
            //查看当前属性名称是否在对象中
            try {
                Field field = ModelListInfoDTO.class.getDeclaredField(sort);
                finalHostTableDtos.sort(modelListInfoList, sort, param.getSortType());
            } catch (NoSuchFieldException e) {
                finalHostTableDtos.sort(modelListInfoList, param.getSortField(), param.getSortType());
            }
        }
        PageInfo pageInfo = new PageInfo<>(modelListInfoList);
        pageInfo.setList(modelListInfoList);
        return Reply.ok(pageInfo);
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
                        getModelTypeChild(orgTop, childList, modelGroupIdSet, null, null, null)
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
        try {
            List<MwModelManageTypeDto> orgTopList = new ArrayList<>();
            List<MwModelManageTypeDto> childList = new ArrayList<>();
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
            QueryInstanceModelParam param = new QueryInstanceModelParam();
            param.setIsBaseData(false);
            mwModelViewServiceImpl.getInstanceListData(param);
            //获取对应的es实例数据
            param.setPageSize(pageSize);
            Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (map != null && map.get("data") != null) {
                listMap = (List<Map<String, Object>>) map.get("data");
            }

            //获取所有模型实例的分组信息。根据资产类型分类
            Map<String, Long> assetsTypeCollect = listMap.stream().collect(Collectors.groupingBy(m -> m.get(GROUP_NODES) != null ? m.get(GROUP_NODES).toString().toLowerCase() : "-1"
                    , Collectors.counting()));
            //根据资产子类型分类
            Map<String, Long> assetsSubTypeCollect = listMap.stream().collect(Collectors.groupingBy(m -> m.get(ASSETTYPE_SUB_ID) != null ? m.get(ASSETTYPE_SUB_ID).toString() : "0"
                    , Collectors.counting()));

            list.forEach(mwModelManageTypeDto -> {
                mwModelManageTypeDto.setIsFlag(isFlag);
                if (mwModelManageTypeDto.getDeep() == 1) {
                    orgTopList.add(mwModelManageTypeDto);
                } else {
                    childList.add(mwModelManageTypeDto);
                }
            });


            for (MwModelManageTypeDto parentInfo : orgTopList) {
                //对父节点做数据统计
                long pNum = 0;
                for (Map.Entry<String, Long> entry : assetsTypeCollect.entrySet()) {
                    String k = entry.getKey();
                    Long v = entry.getValue();
                    if (k.contains(parentInfo.getNodes())) {
                        pNum += v;
                    }
                }
                parentInfo.setInstanceNum(pNum + "");
            }
            Set<String> modelGroupIdSet = new HashSet<>(childList.size());
            String num = listMap.size() + "";
            orgTopList.forEach(
                    orgTop ->
                            getModelTypeChild(orgTop, childList, modelGroupIdSet, assetsTypeCollect, assetsSubTypeCollect, num)
            );
            PageInfo pageInfo = new PageInfo<>(orgTopList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("selectModelInstanceTree fail to", e);
            return Reply.fail(500, "获取树状结构失败");
        }
    }

    private void getModelTypeChild(MwModelManageTypeDto mwModelManageTypeDto, List<MwModelManageTypeDto> mwModelManageTypeDtoList,
                                   Set<String> modelGroupIdSet, Map<String, Long> assetsTypeCollect, Map<String, Long> assetsSubTypeCollect, String num) {
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
                            if (!Strings.isNullOrEmpty(child.getNodes())) {
                                List<String> list = Arrays.asList(child.getNodes().substring(1).split(","));
                                List<Integer> listInts = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                                child.setModelGroupIdList(listInts);
                            } else {
                                child.setModelGroupIdList(new ArrayList<>());
                            }
                            if ("group".equals(child.getType()) && assetsTypeCollect != null) {
//                                    根据分组groupNodes匹配
                                int instaceNUm = 0;
                                //根据分组groupNodes匹配
                                for (Map.Entry<String, Long> entry : assetsTypeCollect.entrySet()) {
                                    String k = entry.getKey();
                                    Long v = entry.getValue();
                                    if (k.indexOf("," + child.getModelGroupId() + ",") != -1) {
                                        instaceNUm += v.intValue();
                                    }
                                }
                                child.setInstanceNum(instaceNUm + "");
                            }
                            if ("model".equals(child.getType()) && assetsTypeCollect != null) {
                                child.setInstanceNum(assetsSubTypeCollect.get(child.getModelGroupId() + "") != null ? assetsSubTypeCollect.get(child.getModelGroupId() + "") + "" : "0");
                            }
                            modelGroupIdSet.add(child.getModelGroupIdStr());
                            //获取当前类目的子类目
                            getModelTypeChild(child, mwModelManageTypeDtoList, modelGroupIdSet, assetsTypeCollect, assetsSubTypeCollect, num);
                            childList.add(child);

                        }
                );
        //先按照modelSort排序，在按名称排序
        List<MwModelManageTypeDto> childList1 = childList.stream().sorted(Comparator.comparing(MwModelManageTypeDto::getModelSort)
                .thenComparing(MwModelManageTypeDto::getModelGroupName)).collect(Collectors.toList());
        mwModelManageTypeDto.addChild(childList1);
    }

    private void sort(List<MwModelManageTypeDto> modelDTO) {
        Collections.sort(modelDTO, new Comparator<MwModelManageTypeDto>() {
            @Override
            public int compare(MwModelManageTypeDto o1, MwModelManageTypeDto o2) {
                if (o1.getModelSort() > o2.getModelSort()) {
                    return 1;
                }
                if (o1.getModelSort() < o2.getModelSort()) {
                    return -1;
                }
                return 0;
            }
        });
    }


    private void getModelTypeChild(MwModelInstanceRelationDto modelInstanceRelationDto, List<MwModelInstanceRelationDto> modelInstanceRelationDtos,
                                   Set<Integer> set) {
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
        Map properties = new HashMap();
        Map field = new HashMap();
        Map value = new HashMap();
        Map fields = new HashMap();
        Map type = new HashMap();
        value.put("type", "text");
        value.put("normalizer", "my_analyzer");
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
        List<Map<String, Object>> listMap;
        //获取模型下的属性类型
        Map<String, Map> timeOutPropertiesMap = new HashMap();
        //获取所有设置到期时间的模型信息
        List<String> modelIndexs = new ArrayList<>();
        List<ModelInfo> allModelInfo = mwModelManageDao.selectAllModelInfo();
        Map<String, PropertyInfo> propertyInfoMap = new HashMap<>();

        List<PropertyInfo> expirePropertyList = new ArrayList<>();
        try {
            for (ModelInfo modelInfo : allModelInfo) {
                if (null != modelInfo.getPropertyInfos()) {
                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                        if (propertyInfo.getPropertiesTypeId() == DATE.getCode() && propertyInfo.getExpireRemind() == true) {
                            modelIndexs.add(modelInfo.getModelIndex());
                            propertyInfoMap = modelInfo.getPropertyInfoMapByIndexId();
                            expirePropertyList.add(propertyInfo);
                        }
                    }
                }
            }
            log.info("进入过期提醒1");
            QueryInstanceModelParam qParam = new QueryInstanceModelParam();
            //忽略权限
            qParam.setSkipDataPermission(true);
            qParam.setModelIndexs(modelIndexs);
            EmailFrom emailFrom = selectEmailFrom();
            List<MWUser> emails = mwModelInstanceDao.selectEmailAll();
            if (modelIndexs.size() != 0) {
                //滚动查询所有modelIndex的数据
                listMap = getInstanceInfoByModelIndexs(qParam);
                List<MWUser> users = mwModelInstanceDao.selectAllUserList();
                for (PropertyInfo propertyInfo : expirePropertyList) {
                    if (propertyInfo.getPropertiesTypeId() == DATE.getCode() && propertyInfo.getExpireRemind() == true) {
                        String k = propertyInfo.getIndexId();
                        for (Map<String, Object> maps : listMap) {
                            if (maps.get(k) != null && maps.get(USER_IDS) != null && maps.get(USER_IDS) instanceof List) {
                                List<Integer> userIdList = (List) JSONArray.parse(JSONObject.toJSONString(maps.get(USER_IDS)));
                                HashSet<Integer> setUser = new HashSet<>(userIdList);
                                String date = maps.get(k).toString();
                                Object obj = (Object) maps;
                                Integer timeNum = propertyInfo.getBeforeExpireTime();
                                String timeType = propertyInfo.getTimeUnit();

                                //是否到期需要提示
                                Boolean isTimeOut = isTimeOut(date, timeNum, timeType);
                                if (isFlag) {
                                    //西藏邮储时间只保留年月日
                                    date = DateUtils.formatDate(DateUtils.parse(date));
                                }
                                if (isTimeOut) {
                                    String[] userTo = selectAccepts(setUser, emails);
                                    String text = maps.get(MwModelViewCommonService.INSTANCE_NAME_KEY).toString() + "在" + date + "到期，请您尽快查验";
                                    mwMessageService.sendTimeOutMessage(text, users, true, obj);
                                    emailSendUtil.sendTextEmail(userTo, emailFrom, "资产到期提醒", text, false);
                                    log.info("到期提醒发送成功::");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("过期提醒失败", e);
            return Reply.fail(500, "过期提醒失败");
        }
        return Reply.ok();
    }


    //    @Override
    public Reply getTimeOutInfoSendEmail() {
        List<Map<String, Object>> listMap;

        List<Map<String, Object>> timeOutListMap = new ArrayList<>();
        //获取所有设置到期时间的模型信息
        List<String> modelIndexs = new ArrayList<>();
        List<ModelInfo> allModelInfo = mwModelManageDao.selectAllModelInfo();

        List<PropertyInfo> expirePropertyList = new ArrayList<>();
        try {
            for (ModelInfo modelInfo : allModelInfo) {
                if (null != modelInfo.getPropertyInfos()) {
                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                        if (propertyInfo.getPropertiesTypeId() == DATE.getCode() && propertyInfo.getExpireRemind() == true) {
                            modelIndexs.add(modelInfo.getModelIndex());
                            expirePropertyList.add(propertyInfo);
                        }
                    }
                }
            }
            QueryInstanceModelParam qParam = new QueryInstanceModelParam();
            //忽略权限
            qParam.setSkipDataPermission(true);
            qParam.setModelIndexs(modelIndexs);
            EmailFrom emailFrom = selectEmailFrom();
            List<MWUser> emails = mwModelInstanceDao.selectEmailAll();
            if (modelIndexs.size() != 0) {
                //滚动查询所有modelIndex的数据
                listMap = getInstanceInfoByModelIndexs(qParam);
                for (PropertyInfo propertyInfo : expirePropertyList) {
                    if (propertyInfo.getPropertiesTypeId() == DATE.getCode() && propertyInfo.getExpireRemind() == true) {
                        String k = propertyInfo.getIndexId();
                        for (Map<String, Object> maps : listMap) {
                            if (maps.get(k) != null && maps.get(USER_IDS) != null) {
                                String date = maps.get(k).toString();
                                Integer timeNum = propertyInfo.getBeforeExpireTime();
                                String timeType = propertyInfo.getTimeUnit();
                                //是否到期需要提示
                                Boolean isTimeOut = isTimeOut(date, timeNum, timeType);
                                if (isTimeOut) {
                                    timeOutListMap.add(maps);
                                }
                            }
                        }
                    }
                }
            }

            //根据Map中的负责人来分组
            Map<Integer, List<Map<String, Object>>> groupedData = timeOutListMap.stream().flatMap(map -> {
                Object obj = map.get(USER_IDS);
                if (obj instanceof List) {
                    List<Integer> userIds = (List<Integer>) obj;
                    return userIds.stream().map(userId -> new AbstractMap.SimpleEntry<>(userId, map));
                } else {
                    return Stream.empty();
                }
            }).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

            //数据导出成excel
            groupedData.forEach((k, v) -> {
                ModelExportSendEmailParam params = new ModelExportSendEmailParam();
                params.setFieldPath(filePath);
                Integer userId = k;
                List<Map<String, Object>> mapList = v;
                if (CollectionUtils.isNotEmpty(mapList)) {
                    params.setModelId(intValueConvert(mapList.get(0).get(MODEL_ID_KEY)));
                }
                String email = "";
                for (MWUser userInfo : emails) {
                    if (userInfo.getUserId() != null && userInfo.getUserId().intValue() == userId.intValue()) {
                        email = userInfo.getEmail();
                    }
                }
                params.setListMap(mapList);
                String filePaths = exportForExcelBySendEmail(params);
                if (!Strings.isNullOrEmpty(email)) {
                    emailSendUtil.sendReportEmail(email, "软件许可到期提醒", emailFrom, filePaths);
                }
            });
        } catch (Exception e) {
            log.error("过期提醒失败", e);
            return Reply.fail(500, "过期提醒失败");
        }
        return Reply.ok();
    }


    @Override
    public TimeTaskRresult getTimeOutInfoByTimeTask() {
        log.info("过期提醒方法开始:::");
        getTimeOutInfoSendEmail();
        log.info("过期提醒方法结束:::");
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        taskRresult.setSuccess(true).setResultContext("过期提醒成功");
        return taskRresult;
    }

    /**
     * 获取外部关联数据
     *
     * @return
     */
    @Override
    public Reply getSelectDataInfo(List<QueryRelationInstanceInfo> paramList) {
        List<String> modelIndexs = new ArrayList<>();
        List<GetRelationDataParam> listParam = new ArrayList<>();
        try {
            for (QueryRelationInstanceInfo param : paramList) {
                modelIndexs.add(param.getModelIndex());
            }
            List<Integer> instanceIdList = new ArrayList<>();
            //获取所有基础设施下的实例Id
            if (CollectionUtils.isNotEmpty(modelIndexs)) {
                instanceIdList = mwModelViewDao.getInstanceIdByBase(modelIndexs);
            }
            QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
            params.setModelIndexs(modelIndexs);
            params.setInstanceIds(instanceIdList);
            //根据节点数据，查询es信息
            List<Map<String, Object>> selectList = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);
            for (QueryRelationInstanceInfo param : paramList) {
                if (!Strings.isNullOrEmpty(param.getPropertiesIndex()) && !Strings.isNullOrEmpty(param.getModelIndex())
                        && !Strings.isNullOrEmpty(param.getProp())) {
                    List<KeyValueDataParam> list = new ArrayList<>();
                    GetRelationDataParam relationDataParam = new GetRelationDataParam();
                    for (Map<String, Object> map : selectList) {
                        String label = "";
                        String value = "";
                        KeyValueDataParam m = new KeyValueDataParam();
                        if (map != null && param.getModelIndex().equals(map.get(MODEL_INDEX))) {
                            if (map.get(param.getPropertiesIndex()) != null) {
                                label = map.get(param.getPropertiesIndex()).toString();
                                m.setLabel(label);
                                value = map.get(INSTANCE_ID_KEY).toString();
                                m.setValue(value);
                                if (map.get("relationSiteRoom") != null && !"".equals(map.get("relationSiteRoom").toString())) {
                                    m.setRelationId(map.get("relationSiteRoom").toString());
                                }
                                list.add(m);
                            }
                        }
                    }
                    relationDataParam.setRelationKey(param.getModelIndex() + "_" + param.getPropertiesIndex() + "@" + param.getProp());
                    List<KeyValueDataParam> listSort = list.stream().sorted(Comparator.comparing(s -> s.getLabel())).collect(Collectors.toList());
                    relationDataParam.setDataVal(listSort);
                    listParam.add(relationDataParam);
                }
            }
            return Reply.ok(listParam);
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
        if (num < timeNum) {
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

    public UpdateRequest getUpdateRequestToES(MwModelInstanceParam param) {
        UpdateRequest updateRequest = new UpdateRequest(param.getModelIndex(), param.getEsId());
        updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
        Map<String, Object> jsonMap = new HashMap<>();
        Integer type = param.getPropertiesType();
        if (typeList.contains(type)) {//type类型为10、11、12、13,16都为数组类型
            if (param.getPropertiesVal() != null && param.getPropertiesVal() != "") {
                jsonMap.put(param.getPropertiesIndex(), JSONArray.parse(JSONObject.toJSONString(param.getPropertiesVal())));
            }
        } else if (type == ModelPropertiesType.STRUCE.getCode()) {//type类型6 为结构体类型
            if (param.getPropertiesVal() != null && param.getPropertiesVal() != "") {
                jsonMap.put(param.getPropertiesIndex(), JSONArray.parse(JSONObject.toJSONString(param.getPropertiesVal())));
            }
        } else if (type == ModelPropertiesType.DATE.getCode()) {//type类型8 为时间类型
            if (param.getPropertiesVal() != null && param.getPropertiesVal() != "") {
                jsonMap.put(param.getPropertiesIndex(), param.getPropertiesVal());
            }
        } else if (type == ModelPropertiesType.SWITCH.getCode()) {//type类型17 布尔类型
            if (param.getPropertiesVal() != null && param.getPropertiesVal() != "") {
                jsonMap.put(param.getPropertiesIndex(), Boolean.valueOf(JSONObject.toJSONString(param.getPropertiesVal())));
            }
        } else {
            jsonMap.put(param.getPropertiesIndex(), param.getPropertiesVal());
        }
        updateRequest.doc(jsonMap);
        return updateRequest;
    }

    /**
     * 清除指定字段值
     * 字符串类型 为空
     * 数字类型为 0
     * 布尔类型为 false
     */
    @Override
    public Reply cleanFieldValueToEs(List<MwModelInstanceParam> paramList) {
        BulkRequest request = new BulkRequest();
        try {
            for (MwModelInstanceParam param : paramList) {
                UpdateRequest updateRequest = new UpdateRequest(param.getModelIndex(), param.getEsId());
                updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                Map<String, Object> jsonMap = new HashMap<>();
                Integer type = param.getPropertiesType();
                if (typeList.contains(type) || type == ModelPropertiesType.STRUCE.getCode()) {//type类型为10、11、12、13,16都为数组类型
                    jsonMap.put(param.getPropertiesIndex(), new ArrayList<>());
                } else if (type == ModelPropertiesType.SWITCH.getCode()) {//type类型17 布尔类型
                    jsonMap.put(param.getPropertiesIndex(), false);
                } else {
                    jsonMap.put(param.getPropertiesIndex(), "");
                }
                updateRequest.doc(jsonMap);
                request.add(updateRequest.upsert());
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            RestStatus status = bulkResponse.status();
            if (status.getStatus() == 200) {
                return Reply.ok();
            } else {
                return Reply.fail(500, "修改es字段数据失败");
            }
        } catch (Throwable e) {
            return Reply.fail(500, "修改数据失败");
        }
    }


    /**
     * 修改机房布局数据
     */
//    @Override
    public Reply batchUpdateRoomLayout(QueryBatchSelectDataParam qparam) {
        try {
            //修改机房布局时，多条同一个机房的数据要一起修改好后在去es中修改数据。
            //后面修改到相同的机房数据时，一定要把前面占用的坐标加入才行。
            if (qparam != null && qparam.getLayoutDataList() != null && qparam.getLayoutDataList().size() > 0) {
                List<AddAndUpdateModelInstanceParam> updateInfoList = new ArrayList<>();

                List<Integer> relationInstanceIdList = qparam.getLayoutDataList().stream().filter(s -> s.getBeforeRoomId() != null).map(QuerySelectDataListParam::getBeforeRoomId).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());

                List<Integer> instanceIdList = qparam.getLayoutDataList().stream().map(QuerySelectDataListParam::getInstanceId).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(relationInstanceIdList)) {
                    instanceIdList.addAll(relationInstanceIdList);
                }
                List<String> modelIndexList = qparam.getLayoutDataList().stream().map(QuerySelectDataListParam::getModelIndex).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());
                QueryESWhetherExistField queryRoomParam = new QueryESWhetherExistField();
                queryRoomParam.setExistFields(Arrays.asList(ROWNUM.getField(), COLNUM.getField(), LAYOUTDATA.getField()));
                queryRoomParam.setModelIndexs(modelIndexList);
                queryRoomParam.setInstanceIds(instanceIdList);
                //获取所有机房数据，instanceId为key
                Map<String, List<Map<String, Object>>> roomModelInfoMap = getInstanceInfoByExistsField(queryRoomParam);
                Map<String, Object> ms = new HashMap();
                for (QuerySelectDataListParam param : qparam.getLayoutDataList()) {
                    //修改前的原坐标
                    List<Integer> beforeCoordinate = param.getBeforeCoordinate();
                    //修改后的新坐标
                    List<Integer> nowCoordinate = param.getCoordinate();
                    List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();
                    List<QueryLayoutDataParam> listLayoutDataParam = new ArrayList<>();
                    param.getInstanceId();
                    param.getModelIndex();
                    String esId = "";
                    List<List> lists = new ArrayList();
                    if (roomModelInfoMap != null) {
                        //修改前的机房Id
                        String beforeRoomId = strValueConvert(param.getBeforeRoomId());
                        if (beforeRoomId != "" && roomModelInfoMap.containsKey(beforeRoomId)) {
                            //修改之前的机房布局
                            int beforeType = 0;
                            valueConvert(roomModelInfoMap, beforeRoomId, beforeCoordinate, ms, beforeType);
                        }
                        //当前的机房Id
                        String currentRoomId = strValueConvert(param.getCurrentRoomId());
                        if (currentRoomId != "" && roomModelInfoMap.containsKey(currentRoomId)) {
                            //修改现在的机房布局
                            int beforeType = 1;
                            valueConvert(roomModelInfoMap, currentRoomId, nowCoordinate, ms, beforeType);
                        }
                    }
                }
                ms.forEach((k, v) ->
                {
                    AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                    instanceParam.setModelIndex(Joiner.on(",").join(modelIndexList));
                    instanceParam.setEsId(k);
                    AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                    List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
                    propertiesParam.setPropertiesType(16);
                    propertiesParam.setPropertiesIndexId(LAYOUTDATA.getField());
                    propertiesParam.setPropertiesValue(JSON.toJSONString(v));
                    propertiesParamList.add(propertiesParam);
                    instanceParam.setPropertiesList(propertiesParamList);
                    updateInfoList.add(instanceParam);
                });
                //修改es字段数据
                Reply reply = batchUpdateModelInstance(updateInfoList);
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    return Reply.fail(500, reply.getMsg());
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateRoomLayout param{}, case by {}", qparam, e);
            return Reply.fail(500, "更新机房布局数据失败");
        }
    }

    private void valueConvert(Map<String, List<Map<String, Object>>> roomModelInfoMap, String roomId, List<Integer> coordinate, Map<String, Object> ms, int type) {
        List<List> lists = new ArrayList<>();
        List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();
        List<QueryLayoutDataParam> listLayoutDataParam = new ArrayList<>();
        String esId = "";
        Map<String, Object> map = roomModelInfoMap.get(roomId).get(0);
        if (map != null && map.get("esId") != null) {
            esId = map.get("esId").toString();
        }
        if (map != null && map.get(LAYOUTDATA.getField()) != null) {
            Object obj = map.get(LAYOUTDATA.getField());
            lists = JSONArray.parseArray(JSONArray.toJSONString(obj), List.class);
            for (List listArr : lists) {
                listLayoutDataParam = JSONArray.parseArray(JSONObject.toJSONString(listArr), QueryLayoutDataParam.class);
                layoutData.add(listLayoutDataParam);
            }
        }
        if (CollectionUtils.isNotEmpty(layoutData)) {
            if (CollectionUtils.isNotEmpty(coordinate)) {
                Integer xVal = coordinate.get(0);
                Integer yVal = coordinate.get(1);
                if (xVal >= 0 && yVal >= 0) {
                    if (type == 0) {//修改之前的机房布局
                        layoutData.get(xVal).get(yVal).setIsSelected(false);
                    } else {//当前的机房布局
                        layoutData.get(xVal).get(yVal).setIsSelected(true);
                    }
                    layoutData.get(xVal).get(yVal).setIsBan(false);
                }
            }
        }
        Map<String, Object> layoutDataMap = new HashMap();
        List<Map<String, Object>> list = new ArrayList<>();
        //坐标修改后，重新塞入map中，后面数据调用时，前面修改的坐标就不会缺失。
        layoutDataMap.put("esId", esId);
        layoutDataMap.put(LAYOUTDATA.getField(), layoutData);
        list.add(layoutDataMap);
        roomModelInfoMap.put(roomId, list);
        ms.put(esId, layoutData);
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
                QueryESWhetherExistField queryRoomParam = new QueryESWhetherExistField();
                queryRoomParam.setExistFields(Arrays.asList(ROWNUM.getField(), COLNUM.getField(), LAYOUTDATA.getField()));
                //获取所有机房数据，instanceId为key
                Map<String, List<Map<String, Object>>> roomModelInfoMap = getInstanceInfoByExistsField(queryRoomParam);

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
                for (Map.Entry<Integer, List<QuerySelectDataListParam>> entry : m.entrySet()) {
                    Integer k = entry.getKey();
                    List<QuerySelectDataListParam> v = entry.getValue();
                    if (v != null && v.size() > 0) {
                        QuerySelectDataListParam param = v.get(0);
                        List<Map<String, Object>> roomLayout = roomModelInfoMap.get(param.getInstanceId() + "");
                        List<List<QueryLayoutDataParam>> layoutData = new ArrayList<>();
                        List<List<Integer>> coordinateLists = coordinateMap.get(k);
                        List<List<Integer>> beforecoordinateLists = beforeCoordinateMap.get(k);

                        String esId = "";
                        List lists = new ArrayList();
                        for (Map<String, Object> map : roomLayout) {
                            if (map != null && map.get("esId") != null) {
                                esId = map.get("esId").toString();
                            }
                            if (map != null && map.get(LAYOUTDATA.getField()) != null) {
                                Object obj = map.get(LAYOUTDATA.getField());
                                lists = JSONArray.parseArray(JSONArray.toJSONString(obj), List.class);
                            }
                        }

                        //// JSONArray.parseArray(JSONObject.toJSONString((ArrayList) lists.get(8)), QueryLayoutDataParam.class).get(4).setIsBan(false);

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
                        updateModelInstanceByLayout(param.getModelIndex(), esId, 16, LAYOUTDATA.getField(), layoutData);
                    }
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateRoomLayout param{}, case by {}", qparam, e);
            return Reply.fail(500, "更新机房布局数据失败");
        }
    }

    /**
     * 批量修改机柜布局数据
     */
//    @Override
    public Reply batchUpdateCabinetLayout(QueryCabinetLayoutListParam params) {
        try {
            if (params.getCabinetLayoutList() != null && params.getCabinetLayoutList().size() > 0) {
                Map<Integer, List<QueryCabinetLayoutParam>> m = new HashMap();
                //上次保存的机柜布局数据
                Map<Integer, List> lastDataMap = new HashMap();
                //本次保存的机柜布局数据
                Map<Integer, List> currentDataMap = new HashMap();
                List<QueryCabinetLayoutParam> list = new ArrayList<>();
                //修改前的位置
                List<CabinetLayoutDataParam> lastDataList = new ArrayList<>();
                //修改后的位置
                List<CabinetLayoutDataParam> currentDataList = new ArrayList<>();
                //获取布局数据中所有的关联资产实例Id
                List<Integer> instanceIdList = params.getCabinetLayoutList().stream().map(QueryCabinetLayoutParam::getInstanceId).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());
                //获取刀片布局中所属刀箱的id
                List<Integer> chassisIdList = params.getCabinetLayoutList().stream().filter(s -> !Strings.isNullOrEmpty(s.getChassisInstanceId())).map(s -> intValueConvert(s.getChassisInstanceId())).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());


                List<String> modelIndexList = params.getCabinetLayoutList().stream().map(QueryCabinetLayoutParam::getModelIndex).collect(Collectors.toList())
                        .stream().distinct().collect(Collectors.toList());
                QueryESWhetherExistField queryCabinetParam = new QueryESWhetherExistField();
                queryCabinetParam.setInstanceIds(instanceIdList);
                queryCabinetParam.setModelIndexs(modelIndexList);
                queryCabinetParam.setExistFields(Arrays.asList(UNUM.getField(), LAYOUTDATA.getField()));
                //获取所有机柜数据，
                List<Map<String, Object>> cabinetModelInfo = getInstanceInfoByES(queryCabinetParam);
                Map<String, String> chassisESIdCollect = new HashMap<>();
                Map<String, String> chassisIndexCollect = new HashMap();
                if (CollectionUtils.isNotEmpty(chassisIdList)) {
                    queryCabinetParam = new QueryESWhetherExistField();
                    queryCabinetParam.setInstanceIds(chassisIdList);
                    //获取所属刀箱布局数据
                    List<Map<String, Object>> chassisInfo = getInstanceInfoByES(queryCabinetParam);
                    for (Map<String, Object> s : chassisInfo) {
                        chassisESIdCollect.put(strValueConvert(s.get(INSTANCE_ID_KEY)), strValueConvert(s.get(ESID)));
                        chassisIndexCollect.put(strValueConvert(s.get(ESID)), strValueConvert(s.get(MODEL_INDEX)));
                    }
                    cabinetModelInfo.addAll(chassisInfo);
                }

                //以instanceId为key，转为Map
                Map<String, List<Map<String, Object>>> cabinetModelInfoMap = cabinetModelInfo.stream().collect(Collectors.groupingBy(s -> s.get(INSTANCE_ID_KEY) != null ?
                        s.get(INSTANCE_ID_KEY).toString() : ""));

                Map<String, List<CabinetLayoutDataParam>> ms = new HashMap();
                Map<String, CabinetLayoutDataParam> chassisMap = new HashMap();

                List<AddAndUpdateModelInstanceParam> updateInfoList = new ArrayList<>();
                //相同所属机柜的实例，对修改的位置进行整合，一次性修改布局数据
                Set<String> assetsIds = new HashSet<>();
                //是否刀片视图
                boolean isBladeView = false;
                List<QueryCurrentBladeLayoutParam> currentBladeLayoutList = new ArrayList<>();
                for (QueryCabinetLayoutParam param : params.getCabinetLayoutList()) {
                    QueryCurrentBladeLayoutParam currentBladeLayoutParam = new QueryCurrentBladeLayoutParam();
                    //机柜下属设备Id不为空，获取机柜下属设备的assetsId，通过es查询所有布局中存在该设备的机柜，
                    // 修改布局时，先统一全部清除该设备的占用，防止脏数据
                    String assetsId = "";
                    assetsId = param.getCurrentInstanceId();
                    //修改后布局数据
                    if (param.getCurrentData() != null) {
                        //刀片布局
                        if (BLADE_VIEW.equals(strValueConvert(param.getCurrentData().getType())) && CollectionUtils.isNotEmpty(param.getCurrentData().getDaoData())) {
                            isBladeView = true;
                            List<List<QueryBladeInstanceParam>> bladeInfo = param.getCurrentData().getDaoData();
                            //获取刀箱实例Id
                            String chassisInstanceId = "";
                            if (param.getCurrentData().getInfo() != null) {
                                chassisInstanceId = param.getCurrentData().getInfo().getAssetsId();
                                assetsIds.add(chassisInstanceId);
                            }
//                            Set<String> ids = new HashSet<>();
                            //获取当前刀片实例Id
//                            for (List<QueryBladeInstanceParam> list1 : bladeInfo) {
//                                for (QueryBladeInstanceParam listParam : list1) {
//                                    if (listParam.isCurrentFlag()) {
//                                        assetsId = listParam.getInstanceId();
//                                        assetsIds.add(assetsId);
//
//                                    }
//                                }
//                            }
                            //根据刀片实例Id，将非当前选中的刀片数据清除，只保留前端页面选择的数据
                            for (List<QueryBladeInstanceParam> list1 : bladeInfo) {
                                for (QueryBladeInstanceParam listParam : list1) {
                                    if (!listParam.isCurrentFlag() && assetsId.equals(listParam.getInstanceId())) {
                                        listParam.setInstanceId("");
                                        listParam.setInstanceName("");
                                    }
                                }
                            }
                            //当前刀片布局数据转换
                            currentBladeLayoutParam.setChassisInstanceId(chassisInstanceId);
                            currentBladeLayoutParam.setInstanceId(assetsId);
                            if (!Strings.isNullOrEmpty(param.getCurrentInstanceId())) {//单前刀片实例Id
                                currentBladeLayoutParam.setInstanceId(param.getCurrentInstanceId());
                            }
                            currentBladeLayoutParam.setDaoData(bladeInfo);
                            currentBladeLayoutList.add(currentBladeLayoutParam);
                        }

                        //非刀片布局，获取实例Id
//                        if (!BLADE_VIEW.equals(strValueConvert(param.getCurrentData().getType())) && param.getCurrentData().getInfo() != null && !Strings.isNullOrEmpty(param.getCurrentData().getInfo().getAssetsId())) {
//                            assetsId = param.getCurrentData().getInfo().getAssetsId();
//                            assetsIds.add(assetsId);
//                        }

                    }

                    //修改前布局数据
                    if (param.getLastData() != null) {
                        //刀片布局
                        if (BLADE_VIEW.equals(strValueConvert(param.getLastData().getType())) && CollectionUtils.isNotEmpty(param.getLastData().getDaoData())) {
                            isBladeView = true;
                            if (!Strings.isNullOrEmpty(param.getCurrentInstanceId())) {
                                assetsId = param.getCurrentInstanceId();
                                assetsIds.add(assetsId);
                            }
                            //获取刀箱实例Id
                            String chassisInstanceId = "";
                            if (param.getLastData().getInfo() != null) {
                                chassisInstanceId = param.getLastData().getInfo().getAssetsId();
                                assetsIds.add(assetsId);
                            }
                            List<List<QueryBladeInstanceParam>> bladeInfo = param.getLastData().getDaoData();
                            for (List<QueryBladeInstanceParam> list1 : bladeInfo) {
                                for (QueryBladeInstanceParam listParam : list1) {
                                    if (assetsId.equals(listParam.getInstanceId())) {
                                        listParam.setInstanceId("");
                                        listParam.setInstanceName("");
                                    }
                                }
                            }
                            //当前刀片布局数据转换
                            currentBladeLayoutParam.setChassisInstanceId(chassisInstanceId);
                            currentBladeLayoutParam.setInstanceId(assetsId);
                            currentBladeLayoutParam.setDaoData(bladeInfo);
                            currentBladeLayoutList.add(currentBladeLayoutParam);
                        }
                        //非刀片布局，获取实例Id
//                        if (!BLADE_VIEW.equals(strValueConvert(param.getLastData().getType())) && param.getLastData().getInfo() != null && !Strings.isNullOrEmpty(param.getLastData().getInfo().getAssetsId())) {
//                            assetsId = param.getLastData().getInfo().getAssetsId();
//                            assetsIds.add(assetsId);
//                        }else{
//                            assetsIds.add(param.getCurrentInstanceId());
//                        }
                    }

                    assetsIds.add(assetsId);
                }
                //所有机柜modelIndex
                List<String> cabinetModelIndexs = mwModelManageDao.getAllCabinetModelIndex();
                QueryEsParam esParam = new QueryEsParam();
                esParam.setModelIndexs(cabinetModelIndexs);
                List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
                QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
                propertyIndexParam.setPropertiesIndexId(queryLayoutField);
                if (isBladeView) {//刀片视图
                    propertyIndexParam.setPropertiesIndexId(queryLayoutBladeField);
                }
                propertyIndexParam.setPropertiesValueList(new ArrayList(assetsIds));
                paramLists.add(propertyIndexParam);
                esParam.setParamLists(paramLists);
                //通过es查询所有布局中存在该设备的机柜
                List<Map<String, Object>> listMap = mwModelViewService.getAllInstanceInfoByModelIndexs(esParam);
                List<CabinetLayoutDataParam> cabinetLayoutInfos = new ArrayList<>();
                Map<String, Map<String, List<CabinetLayoutDataParam>>> mapByEsId = new HashedMap();
                Map<String, List<CabinetLayoutDataParam>> cabinetLayoutInfoByEsId = new HashedMap();
                for (Map<String, Object> map : listMap) {
                    if (map != null && map.get("layoutData") != null) {
                        String esId = map.get(ESID) != null ? map.get(ESID).toString() : "";
                        Object obj = map.get("layoutData");
                        cabinetLayoutInfos = JSONArray.parseArray(JSONArray.toJSONString(obj), CabinetLayoutDataParam.class);
                        Map<String, List<CabinetLayoutDataParam>> usedLayoutMap = cabinetLayoutInfos.stream().filter(s -> s.getInfo() != null && !Strings.isNullOrEmpty(s.getInfo().getAssetsId())).
                                collect(Collectors.groupingBy(s -> s.getInfo().getAssetsId(), Collectors.mapping(s -> s, Collectors.toList())));
                        mapByEsId.put(esId, usedLayoutMap);
                        cabinetLayoutInfoByEsId.put(esId, cabinetLayoutInfos);
                    }
                }

                //相同所属机柜的实例，对修改的位置进行整合，一次性修改布局数据
                for (QueryCabinetLayoutParam param : params.getCabinetLayoutList()) {
                    String assetsId = param.getCurrentInstanceId();
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

                if (isBladeView) {//刀片布局
                    mapByEsId.forEach((k, v) -> {
                        String esId = k;
                        Map<String, List<CabinetLayoutDataParam>> maps = v;
                        if (CollectionUtils.isNotEmpty(currentBladeLayoutList)) {
                            //q清除非本次修改，新增的旧数据（根据assetsId和当前状态CurrentFlag）
                            for (QueryCurrentBladeLayoutParam currentBladeLayoutParam : currentBladeLayoutList) {
                                for (List<QueryBladeInstanceParam> list1 : currentBladeLayoutParam.getDaoData()) {
                                    for (QueryBladeInstanceParam listParam : list1) {
                                        if (assetsIds.contains(listParam.getInstanceId()) && !listParam.isCurrentFlag()) {
                                            listParam.setInstanceId("");
                                            listParam.setInstanceName("");
                                        }
                                    }
                                }
                            }
                            for (QueryCurrentBladeLayoutParam currentBladeLayoutParam : currentBladeLayoutList) {

                                //刀箱实例Id
                                String chassisInstanceId = currentBladeLayoutParam.getChassisInstanceId();
                                //当前选择的刀片实例Id
                                String instanceId = currentBladeLayoutParam.getInstanceId();
                                //刀片布局数据

                                List<CabinetLayoutDataParam> cabinetLayoutDataParams = cabinetLayoutInfoByEsId.get(esId);
                                Map<String, List<CabinetLayoutDataParam>> collect = cabinetLayoutDataParams.stream().filter(s -> s.getInfo() != null && !Strings.isNullOrEmpty(s.getInfo().getAssetsId())).
                                        collect(Collectors.groupingBy(s -> s.getInfo().getAssetsId(), Collectors.mapping(s -> s, Collectors.toList())));


                                collect.forEach((key, val) -> {
                                    List<CabinetLayoutDataParam> layoutDataParamList = val;
                                    String chassisInstanceIdByEs = key;
                                    //如果是同一个刀箱下的刀片布局
                                    if (chassisInstanceId.equals(chassisInstanceIdByEs)) {
                                        //同刀箱下的，布局直接替换成前端传过来的当前布局数据（currentData）
                                        for (CabinetLayoutDataParam layoutDataParam : layoutDataParamList) {
                                            List<List<QueryBladeInstanceParam>> bladeInfo = currentBladeLayoutParam.getDaoData();
                                            layoutDataParam.setDaoData(bladeInfo);
                                        }
                                    } else {
                                        //不同刀箱下的，将该刀片Id数据清除
                                        for (CabinetLayoutDataParam layoutDataParam : layoutDataParamList) {
                                            for (List<QueryBladeInstanceParam> list1 : layoutDataParam.getDaoData()) {
                                                for (QueryBladeInstanceParam listParam : list1) {
                                                    if (instanceId.equals(listParam.getInstanceId())) {
                                                        listParam.setInstanceId("");
                                                        listParam.setInstanceName("");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    ms.put(esId, cabinetLayoutDataParams);
                                });
                            }
                        }

                    });


                } else {
                    //机柜删除布局
                    mapByEsId.forEach((k, v) -> {
                        String esId = k;
                        Map<String, List<CabinetLayoutDataParam>> maps = v;
                        List<CabinetLayoutDataParam> currentCabinetLayout = new ArrayList<>();
                        //获取当前esId下的机柜布局
                        if (cabinetLayoutInfoByEsId != null && cabinetLayoutInfoByEsId.containsKey(esId)) {
                            currentCabinetLayout = cabinetLayoutInfoByEsId.get(esId);
                        }
                        if (CollectionUtils.isNotEmpty(currentCabinetLayout)) {
                            //相同所属机柜的实例，对修改的位置进行整合，一次性修改布局数据
                            for (QueryCabinetLayoutParam param : params.getCabinetLayoutList()) {
                                String assetsId = param.getCurrentInstanceId();
//                                if (param.getLastData() != null && param.getLastData().getInfo() != null && !Strings.isNullOrEmpty(param.getLastData().getInfo().getAssetsId())) {
//                                    assetsId = param.getLastData().getInfo().getAssetsId();
//                                }
//                                if (Strings.isNullOrEmpty(assetsId) && param.getCurrentData() != null && param.getCurrentData().getInfo() != null && !Strings.isNullOrEmpty(param.getCurrentData().getInfo().getAssetsId())) {
//                                    assetsId = param.getCurrentData().getInfo().getAssetsId();
//                                }
                                if (maps != null && maps.containsKey(assetsId)) {
                                    List<CabinetLayoutDataParam> lastDataListsAll = maps.get(assetsId);
                                    Integer lastStart = null;
                                    Integer lastEnd = null;
                                    if (lastDataListsAll != null && lastDataListsAll.size() > 0 && lastDataListsAll.get(0) != null) {
                                        for (CabinetLayoutDataParam cabinetParam : lastDataListsAll) {
                                            if (cabinetParam.getStart() != null && cabinetParam.getEnd() != null) {
                                                lastStart = cabinetParam.getStart();
                                                lastEnd = cabinetParam.getEnd();
                                                int index = 0;
                                                int indexNum = 0;
                                                Boolean isFlag = true;
                                                Iterator<CabinetLayoutDataParam> layoutInfo = currentCabinetLayout.iterator();
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
                                                        currentCabinetLayout.add(indexNum + y, c);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ms.put(esId, currentCabinetLayout);
                        }

                    });
                }


                boolean finalIsBladeView = isBladeView;
                m.forEach((k, v) -> {
                    List<QueryCabinetLayoutParam> listInfo = v;
                    if (v != null && v.size() > 0) {
                        List<CabinetLayoutDataParam> currentDataLists = currentDataMap.get(k);
                        //获取机房布局数据
                        Map<String, Object> map = cabinetModelInfoMap.get(k + "").get(0);
                        List<CabinetLayoutDataParam> cabinetLayoutInfo = new ArrayList<>();
                        String esId = "";
                        //对刀箱服务器布局进行修改
                        for (QueryCabinetLayoutParam layoutParam : listInfo) {
                            if (!Strings.isNullOrEmpty(layoutParam.getCurrentInstanceId()) && !Strings.isNullOrEmpty(layoutParam.getChassisInstanceId())) {
                                String chassisESId = chassisESIdCollect.get(layoutParam.getChassisInstanceId());
                                if (layoutParam.getLastData() != null) {
                                    chassisMap.put(chassisESId, layoutParam.getLastData());
                                }
                                if (layoutParam.getCurrentData() != null) {
                                    chassisMap.put(chassisESId, layoutParam.getCurrentData());
                                }
                            }
                        }

                        if (map != null && map.get("esId") != null) {
                            esId = map.get("esId").toString();
                            Object obj = map.get("layoutData");
                            cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(obj), CabinetLayoutDataParam.class);
                            if (ms != null && ms.containsKey(esId)) {
                                cabinetLayoutInfo = ms.get(esId);
                            }
                        }
                        Integer currentStart = null;
                        Integer currentEnd = null;
//                        if(!finalIsBladeView){
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
                                    cabinetLayoutInfo.add(indexNum, cabinetParam);
                                }
                            }
                        }
//                        }
                        ms.put(esId, cabinetLayoutInfo);
                    }
                });

                ms.forEach((k, v) ->
                {
                    AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                    instanceParam.setModelIndex(Joiner.on(",").join(modelIndexList));
                    instanceParam.setEsId(k);
                    AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                    List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
                    propertiesParam.setPropertiesType(16);
                    propertiesParam.setPropertiesIndexId(LAYOUTDATA.getField());
                    //类型转换一下，去除刀片布局中的currentFlag字段(前端需求)
                    List<SaveCabinetLayoutDataParam> listData = JSONArray.parseArray(JSON.toJSONString(v), SaveCabinetLayoutDataParam.class);
                    propertiesParam.setPropertiesValue(JSON.toJSONString(listData));
                    propertiesParamList.add(propertiesParam);
                    instanceParam.setPropertiesList(propertiesParamList);
                    updateInfoList.add(instanceParam);
                });
                //更新刀箱实例中的布局数据
                chassisMap.forEach((k, v) ->
                {
                    AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                    if (chassisIndexCollect != null && chassisIndexCollect.containsKey(k)) {
                        String modelIndex = chassisIndexCollect.get(k);
                        instanceParam.setModelIndex(modelIndex);
                        instanceParam.setEsId(k);
                        AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
                        propertiesParam.setPropertiesType(16);
                        propertiesParam.setPropertiesIndexId(POSITIONBYCABINET.getField());
                        //类型转换一下，去除刀片布局中的currentFlag字段(前端需求)
                        SaveCabinetLayoutDataParam listData = JSONObject.parseObject(JSONObject.toJSONString(v), SaveCabinetLayoutDataParam.class);
                        propertiesParam.setPropertiesValue(JSON.toJSONString(listData));
                        propertiesParamList.add(propertiesParam);
                        instanceParam.setPropertiesList(propertiesParamList);
                        updateInfoList.add(instanceParam);
                    }
                });
                //修改es字段数据
                Reply reply = batchUpdateModelInstance(updateInfoList);
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    return Reply.fail(500, reply.getMsg());
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to getRoomAndCabinetLayout param{}, case by {}", params, e);
            return Reply.fail(500, "获取机房机柜布局数据失败");
        }
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
                                    cabinetLayoutInfo.add(indexNum, cabinetParam);
                                }
                            }
                        }
                        updateModelInstanceByLayout(param.getModelIndex(), esId, 16, "layoutData", cabinetLayoutInfo);
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
     * 批量更新ES数据
     *
     * @param instanceParams
     */
    @Override
    public Reply editorData(List<AddAndUpdateModelInstanceParam> instanceParams) {
        try {
            QueryBatchSelectDataParam updateRoomLayout = new QueryBatchSelectDataParam();
            QueryCabinetLayoutListParam updateCabinetParam = new QueryCabinetLayoutListParam();
            List<QueryCabinetLayoutParam> layoutDataCabinetList = new ArrayList<>();
            List<QuerySelectDataListParam> layoutDataRoomList = new ArrayList<>();
            List<UpdateRelationIdParam> upInstanceRelationList = new ArrayList<>();

            List<String> modelIndexs = new ArrayList<>();
            List<Integer> instanceIds = new ArrayList<>();

            Map<Integer, QuerySelectDataListParam> map = new HashMap<>();
            for (AddAndUpdateModelInstanceParam m : instanceParams) {
                List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
                List<Integer> coordinate = new ArrayList<>();
                Integer relationRoomInstanceId = 0;
                Integer relationCabintInstanceId = 0;
                for (AddModelInstancePropertiesParam propertiesParam : m.getPropertiesList()) {
                    Integer type = propertiesParam.getPropertiesType();
                    if (m.getModelViewType() != null && m.getModelViewType().intValue() > 0) {
                        if (type == ModelPropertiesType.SINGLE_RELATION.getCode() && propertiesParam.getPropertiesIndexId().indexOf("relationSite") != -1) {
                            if (propertiesParam.getPropertiesValue() != null && m.getModelViewType() == 2) {
                                relationRoomInstanceId = Integer.valueOf(propertiesParam.getPropertiesValue());
                                UpdateRelationIdParam updateRelationIdParam = new UpdateRelationIdParam();
                                updateRelationIdParam.setInstanceId(m.getInstanceId());
                                updateRelationIdParam.setInstanceName(m.getInstanceName());
                                updateRelationIdParam.setRelationInstanceId(relationRoomInstanceId);
                                upInstanceRelationList.add(updateRelationIdParam);
                            }
                            if (propertiesParam.getPropertiesValue() != null && m.getModelViewType() == 3) {
                                relationCabintInstanceId = Integer.valueOf(propertiesParam.getPropertiesValue());
                            }
                        }
                        if (m.getModelViewType() == 2 && POSITIONBYROOM.getField().equals(propertiesParam.getPropertiesIndexId())) {
                            coordinate.addAll((List) JSONArray.parse(propertiesParam.getPropertiesValue()));
                        }
                        if (type == ModelPropertiesType.LAYOUTDATA.getCode() && POSITIONBYCABINET.getField().equals(propertiesParam.getPropertiesIndexId())) {
                            if (propertiesParam.getPropertiesValue() != null && !Strings.isNullOrEmpty(m.getInstanceName())) {
                                CabinetLayoutDataParam cdParam = JSONObject.parseObject(propertiesParam.getPropertiesValue(), CabinetLayoutDataParam.class);
                                QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                assetsListParam.setAssetsId(m.getInstanceId() + "");
                                assetsListParam.setAssetsName(m.getInstanceName());

                                //刀片视图
                                if (BLADE_VIEW.equals(cdParam.getType()) && CollectionUtils.isNotEmpty(cdParam.getDaoData())) {
                                    String instanceId = strValueConvert(m.getInstanceId());
                                    String instanceName = strValueConvert(m.getInstanceName());
                                    for (List<QueryBladeInstanceParam> list1 : cdParam.getDaoData()) {
                                        for (QueryBladeInstanceParam listParam : list1) {
                                            if (listParam.getInstanceName().equals(instanceName)) {
                                                listParam.setInstanceId(instanceId);
                                            }
                                        }
                                    }
                                } else {
                                    cdParam.setInfo(assetsListParam);
                                }
                                //刀片布局数据中instanceId数值处理
                                editorConvertValueByBladeLayout(cdParam, instanceParams);
                                SaveCabinetLayoutDataParam saveParam = new SaveCabinetLayoutDataParam();
                                //刀片布局数据类型转换，去除currentFlag字段
                                saveParam = JSONObject.parseObject(JSONObject.toJSONString(cdParam), SaveCabinetLayoutDataParam.class);
                                propertiesParam.setPropertiesValue(JSONObject.toJSONString(saveParam));
                                cdParamList.add(cdParam);
                            }
                        }
                    }
                }
                if (m.getModelViewType() != null && m.getModelViewType() == 2) {
                    QuerySelectDataListParam querySelectDataListParam = new QuerySelectDataListParam();
                    //外部关联的实例Id
                    if (relationRoomInstanceId != null) {
                        querySelectDataListParam.setInstanceId(relationRoomInstanceId);
                    }
                    //外部关联的modelIndex
                    querySelectDataListParam.setModelIndex(m.getRelationModelIndex());
                    querySelectDataListParam.setCoordinate(coordinate);
                    if (CollectionUtils.isNotEmpty(m.getRoomCoordinate())) {
                        querySelectDataListParam.setBeforeCoordinate(m.getRoomCoordinate());
                    }
                    if (CollectionUtils.isNotEmpty(coordinate) || CollectionUtils.isNotEmpty(m.getRoomCoordinate())) {
//                        layoutDataRoomList.add(querySelectDataListParam);
                        map.put(m.getInstanceId(), querySelectDataListParam);
                        //获取机柜的Id和index，为查询机柜数据获取所属机房信息准备。
                        modelIndexs.add(m.getModelIndex());
                        instanceIds.add(m.getInstanceId());
                    }
                }
                if (m.getModelViewType() != null && m.getModelViewType() == 3) {
                    //机柜下属设备实例，设置设备位置，对应修改机柜布局
                    QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                    clParam.setCurrentInstanceId(strValueConvert(m.getInstanceId()));
                    //外部关联的实例Id
                    if (relationCabintInstanceId != null) {
                        clParam.setInstanceId(relationCabintInstanceId);
                    }
                    //外部关联的modelIndex
                    clParam.setModelIndex(m.getRelationModelIndex());
                    if (cdParamList != null && cdParamList.size() > 0) {//获取修改后的布局数据
                        clParam.setCurrentData(cdParamList.get(0));
                    }
                    if (m.getCabinetCoordinate() != null) {//获取修改前的布局数据
                        clParam.setLastData(m.getCabinetCoordinate());
                    }
                    if ((clParam.getCurrentData() != null && clParam.getCurrentData().getStart() != null && clParam.getCurrentData().getEnd() != null)
                            || (clParam.getLastData() != null && clParam.getLastData().getEnd() != null && clParam.getLastData().getStart() != null)) {
                        //不是默认视图
                        CabinetLayoutDataParam caDaraParam = clParam.getLastData();
                        if (clParam.getCurrentData() != null) {
                            caDaraParam = clParam.getCurrentData();
                        }
                        if (CHASSIS_VIEW.equals(caDaraParam.getType()) || BLADE_VIEW.equals(caDaraParam.getType())) {
                            if (caDaraParam.getInfo() != null) {
                                clParam.setChassisInstanceId(caDaraParam.getInfo().getAssetsId());
                            }
                        }
                        layoutDataCabinetList.add(clParam);
                    }

                }
            }
            if (CollectionUtils.isNotEmpty(instanceIds)) {
                QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
                params.setModelIndexs(modelIndexs);
                params.setInstanceIds(instanceIds);
                //根据节点数据，查询es信息
                List<Map<String, Object>> relationInstanceMaps = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);
                for (Map<String, Object> ms : relationInstanceMaps) {
                    //机房修改前的位置数据
                    List<Integer> beforeCoordinate = new ArrayList<>();
                    Integer beforeRoomId = 0;
                    Integer instanceId = 0;
                    if (ms != null && ms.get(POSITIONBYROOM.getField()) != null) {
                        Object obj = ms.get(POSITIONBYROOM.getField());
                        beforeCoordinate = (List<Integer>) JSONArray.parse(obj.toString());
                    }
                    if (ms != null && ms.get(RELATIONSITEROOM.getField()) != null) {
                        beforeRoomId = intValueConvert(ms.get(RELATIONSITEROOM.getField()));
                    }
                    if (ms != null && ms.get(INSTANCE_ID_KEY) != null) {
                        instanceId = intValueConvert(ms.get(INSTANCE_ID_KEY));
                    }
                    Integer finalInstanceId = instanceId;
                    Integer finalBeforeRoomId = beforeRoomId;
                    List<Integer> finalBeforeCoordinate = beforeCoordinate;
                    map.forEach((k, v) -> {
                        if (finalInstanceId.equals(k)) {
                            QuerySelectDataListParam dataParam = v;
                            dataParam.setBeforeRoomId(finalBeforeRoomId);
                            dataParam.setBeforeCoordinate(finalBeforeCoordinate);
                            layoutDataRoomList.add(dataParam);
                        }
                    });
                }
            }
            //修改mysql数据库中机柜关联的机房Id
            if (CollectionUtils.isNotEmpty(upInstanceRelationList)) {
                mwModelInstanceDao.updateCabinetRelationId(upInstanceRelationList);
            }

            //批量修改机房布局
            if (CollectionUtils.isNotEmpty(layoutDataRoomList)) {
                updateRoomLayout.setLayoutDataList(layoutDataRoomList);
                batchUpdateRoomLayout(updateRoomLayout);
            }
            //批量修改机柜布局
            if (CollectionUtils.isNotEmpty(layoutDataCabinetList)) {
                updateCabinetParam.setCabinetLayoutList(layoutDataCabinetList);
                batchUpdateCabinetLayout(updateCabinetParam);
            }
            batchUpdateModelInstance(instanceParams);
            batchUpdateZabbixHostName(instanceParams);
        } catch (Throwable e) {
            log.error("修改数据失败");
        }
        return Reply.ok();
    }

    public void batchUpdateZabbixHostName(List<AddAndUpdateModelInstanceParam> instanceParams) {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (AddAndUpdateModelInstanceParam param : instanceParams) {
            multiGetRequest.add(new MultiGetRequest.Item(param.getModelIndex(), param.getModelIndex() + param.getInstanceId()));
        }
        try {
            List<MwSyncZabbixAssetsParam> paramList = new ArrayList<>();
            MultiGetResponse multiGetItemResponses = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
            MultiGetItemResponse[] responses = multiGetItemResponses.getResponses();
            for (MultiGetItemResponse response : responses) {
                GetResponse resp = response.getResponse();
                if (resp.isExists()) {
                    Map<String, Object> map = resp.getSourceAsMap();
                    MwSyncZabbixAssetsParam param = new MwSyncZabbixAssetsParam();
                    if (map != null) {
                        param.setHostId(strValueConvert(map.get(ASSETS_ID)));
                        param.setMonitorServerId(intValueConvert(map.get(MONITOR_SERVER_ID)));
                        param.setInstanceName(strValueConvert(map.get(INSTANCE_NAME_KEY)));
                        paramList.add(param);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(paramList)) {
                batchUpdateSyncZabbixName(paramList);
            }
        } catch (Exception e) {
            log.error("batchUpdateZabbixHostName失败", e);
        }

    }


    /**
     * 批量更新ES数据
     *
     * @param instanceParams
     */
    public Reply batchUpdateModelInstance(List<AddAndUpdateModelInstanceParam> instanceParams) {
        BulkRequest request = new BulkRequest();
        try {
            for (AddAndUpdateModelInstanceParam m : instanceParams) {
                //TODO 可以将AddAndUpdateModelInstanceParam 转为资产的实体，方便查询
                String esId = m.getEsId();
                if (Strings.isNullOrEmpty(esId)) {
                    esId = m.getModelIndex() + m.getInstanceId();
                }
                UpdateRequest updateRequest = new UpdateRequest(m.getModelIndex(), esId);
                updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                HashMap<String, Object> jsonMap = new HashMap<>();
                for (AddModelInstancePropertiesParam propertiesParam : m.getPropertiesList()) {
                    Integer type = propertiesParam.getPropertiesType();
                    //TODO 后期去掉属性类型，直接使用Object接收和提交  类型只对页面限制输入，后端数据接收提交不区分Type（时间类型不确定）
                    ModelPropertiesType modelType = ModelPropertiesType.getTypeByCode(type);
                    if (null != modelType && propertiesParam.getPropertiesValue() != null) {
                        jsonMap.put(propertiesParam.getPropertiesIndexId(), modelType.convertToEsData(propertiesParam.getPropertiesValue()));
                    }

                }
                updateRequest.doc(jsonMap);
                request.add(updateRequest.upsert());
            }
//            Cancellable bulkResponse = restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT);
            restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        //新增失败时，删除插入数据库的实例数据
                        log.error("异步执行批量添加模型insertModelInstanceProperties属性到es中失败");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    //新增失败时，删除插入数据库的实例数据
                    log.error("异步执行批量添加模型属性到es中失败");
                }
            });
            return Reply.ok();
        } catch (Throwable e) {
            return Reply.fail(500, "修改数据失败");
        }
    }


    /**
     * 更新机房布局数据
     *
     * @param modelIndex
     * @param esId
     * @param layoutData
     */
    private void updateModelInstanceByLayout(String modelIndex, String esId, Integer propertiesType, String
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
        updateModelInstanceInfo(instanceParam);
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
            long time1 = System.currentTimeMillis();
            String modelIndex = param.getModelIndex();
            Integer instanceId = param.getInstanceId();
            QueryModelInstanceParam params = new QueryModelInstanceParam();
            params.setModelIndex(modelIndex);
            params.setInstanceIdList(Arrays.asList(instanceId));
            //指定返回机房机柜布局数据
            params.setFieldList(Arrays.asList("layoutData"));
            List<Map<String, Object>> modelLayout = getModelInstanceDataByInstanceId(params);
            long time2 = System.currentTimeMillis();
            log.info("查询机房布局接口耗时:" + (time2 - time1) + "ms");
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
                if (param.getInstanceIdList() != null && param.getInstanceIdList().size() > 0) {
                    queryBuilder.must(QueryBuilders.termsQuery(INSTANCE_ID_KEY, param.getInstanceIdList()));
                }
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.from((param.getPageNumber() - 1) * param.getPageSize());
                searchSourceBuilder.size(param.getPageSize());
                //返回指定字段数据
                if (CollectionUtils.isNotEmpty(param.getFieldList())) {
                    String[] includes = param.getFieldList().toArray(new String[param.getFieldList().size()]);
                    FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                    searchSourceBuilder.fetchSource(sourceContext);
                }
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

    /**
     * 修改es字段数据
     *
     * @param instanceParam
     * @return
     */
    public Reply updateModelInstanceInfo(AddAndUpdateModelInstanceParam instanceParam) {
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
        } catch (Exception e) {
            log.error("fail to updateModelInstanceInfo param{}, case by {}", instanceParam, e);
            return Reply.fail(500, "更新数据失败");
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
            long time1 = System.currentTimeMillis();
            List<Map<String, Object>> list = getRelationModelInfo(param);
            for (Map<String, Object> map : list) {
                String position = JSONObject.toJSONString(map.get(POSITIONBYROOM.getField()));
                mapInfo.put(position, map);
            }
            long time2 = System.currentTimeMillis();
            log.info("查询机房下所有机柜位置耗时：" + (time2 - time1) + "ms");
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
            List<ModelAssociatedView> views = new ArrayList<>();
            Session session = connectionPool.getSession();
            if (param.getOwmRelationsParam() != null) {
                QueryInstanceRelationsParam qparam = param.getOwmRelationsParam();
                List<EdgeParam> edgesLastInfo = new ArrayList<>();
                List<ComboParam> combosLastInfo = new ArrayList<>();
                if (param.getLastData() != null) {
                    edgesLastInfo = param.getLastData().getEdges();
                    combosLastInfo = param.getLastData().getCombos();
                }

                //查询模型对应的分组
                ModelRelationGroupSelParam selParam = new ModelRelationGroupSelParam();
                selParam.setOwnModelId(qparam.getModelId());
                List<ModelRelationGroupDTO> groupDTOS = mwModelRelationsDao.selectModelRelationGroup(selParam);

                //查询模型的连接关系
                ModelAsset modelAsset = session.load(ModelAsset.class, qparam.getModelId(), 1);

                for (ModelRelationGroupDTO modelRelationGroupDTO : groupDTOS) {
                    //设置本端和对端关系信息,记录对端分组Id
                    List<ModelAssociatedView> list = ModelAssociatedView.genViewList(modelAsset, modelRelationGroupDTO.getId());
                    if (list.size() > 0) {
                        for (ModelAssociatedView modelAssociatedView : list) {
                            modelAssociatedView.setGroupId(modelRelationGroupDTO.getId());
                            modelAssociatedView.setGroupName(modelRelationGroupDTO.getRelationGroupName());
                        }
                        views.addAll(list);
                    }
                }

                //默认所有模型只有一个上级
                List<String> sourceList = new ArrayList<>();
                //由于前端新增实例数据时，获取不了 关联的模型Id和实例id
                //根据参数relationInstanceList中的新增实例的modelId，去lastData的edges中获取source
                for (EdgeParam edge : edgesLastInfo) {
                    //循环获取
                    String target = edge.getTarget();
                    String[] targetStr = target.split("_");
                    if (targetStr.length > 1 && targetStr[0].equals(qparam.getModelId() + "")) {
                        String source = edge.getSource();
                        String lastModelId = source.split("_")[0];
                        sourceList.add(lastModelId);
                    }
                }
                //对combos数据去重
                List<ComboParam> modelIdDistinctList = combosLastInfo.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getId()))), ArrayList::new));
                List<Integer> modelIds = new ArrayList<>();
                //获取所有已存在的模型id
                for (ComboParam m : modelIdDistinctList) {
                    modelIds.add(m.getId());
                }

                //现阶段，暂定：拓扑实例中，所有的模型只能出现一次，避免出现一个模型有多个上级的情况
                Iterator<ModelAssociatedView> it = views.iterator();
                //去除已出现的模型数据
                while (it.hasNext()) {
                    ModelAssociatedView view = it.next();
                    if (modelIds.contains(view.getOppositeModelId())) {
                        it.remove();
                    }
                }
            }
            return Reply.ok(views);
        } catch (Exception e) {
            log.error("fail to getModelRelationInfo param{}, case by {}", e);
            return Reply.fail(500, "根据模型id获取所有模型关系关联数据");
        }
    }


    /**
     * 根据当前实例拓扑查询能够选择连接的实例
     *
     * @return
     */
    @Override
    public Reply instanceRelationBrowse(QueryInstanceRelationToPoParam param) {
        Integer ownModelId = param.getOwmRelationsParam().getModelId();
        Integer ownInstanceId = param.getOwmRelationsParam().getInstanceId();

        NodeParam ownNodeParam = new NodeParam(ownModelId, ownInstanceId);

        //模型映射关系
        Map<Integer, ComboParam> comboParamMap = new HashMap<>();
        if (null != param.getLastData().getCombos()) {
            for (ComboParam comboParam : param.getLastData().getCombos()) {
                comboParamMap.put(comboParam.getId(), comboParam);
            }

        }

        //在当前图形节点中去掉起始节点
        List<NodeParam> nodeParamList = param.getLastData().getNodes();
        Map<String, NodeParam> nodeParamMap = new HashMap<>();
        if (null != nodeParamList) {
            for (NodeParam nodeParam : nodeParamList) {
                nodeParamMap.put(nodeParam.getId(), nodeParam);
            }
        }
        nodeParamList.remove(ownNodeParam);

        //遍历所有节点构造连线
        //过滤已经存在的连线
        List<EdgeParam> existEdges = param.getLastData().getEdges();
        List<EdgeParam> newEdgeParamList = new ArrayList<>();
        for (NodeParam endNodeParam : nodeParamList) {
            EdgeParam edgeParam = new EdgeParam(ownNodeParam, endNodeParam);
            if (!existEdges.contains(edgeParam)) {
                newEdgeParamList.add(edgeParam);
            }
        }

        //根据能够选择的连线构造显示的数据格式
        Map<String, List<EdgeParam>> listMap = new HashMap<>();
        for (EdgeParam edgeParam : newEdgeParamList) {
            List<EdgeParam> list = listMap.get(edgeParam.getSource());
            if (null == list) {
                list = new ArrayList<>();
                listMap.put(edgeParam.getSource(), list);
            }
            list.add(edgeParam);
        }

        List<IntanceTopoSelView> ret = new ArrayList<>();

        //把可以连接的边,分解成模型和实例
        List<EdgeParam> modelList = listMap.get(ownNodeParam.getId());
        if (null != modelList) {
            for (EdgeParam edgeParam : modelList) {
                NodeParam nodeParam = nodeParamMap.get(edgeParam.getTarget());
                if (null != nodeParam) {
                    IntanceTopoSelView intanceTopoSelView = new IntanceTopoSelView();
                    ComboParam comboParam = comboParamMap.get(nodeParam.getComboId());
                    intanceTopoSelView.extractFrom(comboParam);

                    IntanceTopoSelView modelView = null;
                    for (IntanceTopoSelView view : ret) {
                        if (view.getId().equals(intanceTopoSelView.getId())) {
                            modelView = view;
                            break;
                        }
                    }

                    if (null == modelView) {
                        ret.add(intanceTopoSelView);
                    } else {
                        intanceTopoSelView = modelView;
                    }

                    intanceTopoSelView.extractChild(nodeParam);
                }
            }
        }

        return Reply.ok(ret);
    }

    @Override
    public Reply instanceRelationLink(QueryInstanceRelationToPoParam param) {
        InstanceTopoView instanceTopoView = new InstanceTopoView();
        if (null != param.getLinkInstanceParams()) {
            NodeParam start = param.getLastData().getNodes().get(0);
            Integer fromModelId = param.getOwmRelationsParam().getModelId();
            Integer fromInstanceId = param.getOwmRelationsParam().getInstanceId();
            NodeParam src = new NodeParam(fromModelId, fromInstanceId);

            //构造新增的边
            List<EdgeParam> newEdges = param.getLastData().getEdges();
            for (List<Integer> toList : param.getLinkInstanceParams()) {
                Integer toModelId = toList.get(0);
                Integer toInstanceId = toList.get(1);
                NodeParam dest = new NodeParam(toModelId, toInstanceId);
                EdgeParam edgeParam = new EdgeParam(src, dest);
                newEdges.add(edgeParam);
            }

            //调整实例拓扑图排列
            LastData lastData = ModelUtils.sortLastData(start, newEdges
                    , param.getLastData().getNodes(), param.getLastData().getCombos());

            instanceTopoView.setLastData(lastData);
        }
        return Reply.ok(instanceTopoView);
    }


    @Override
    public Reply getInstanceListByModelId(QueryInstanceModelParam params) {
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        MwModelInfoDTO m = mwModelInstanceDao.getModelIndexInfo(params.getModelId());
        String modelIndex = "";
        if (m != null) {
            modelIndex = m.getModelIndex();
        }
        param.setModelIndex(modelIndex);
        param.setModelId(params.getModelId());
        Reply reply = selectInstanceInfo(param);
//        Reply reply = selectModelInstance(params);
        List instanceList = (List) reply.getData();
        //去除实例拓扑中已选择的实例
        Iterator<Map> its = instanceList.iterator();
        while (its.hasNext()) {
            Map s = its.next();
            if (params.getInstanceIds().contains(Integer.valueOf(s.get(INSTANCE_ID_KEY).toString()))) {
                its.remove();
            }
        }
        return reply;
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
        //查询机房下机柜的位置信息
        if (param.getInstanceId() != null) {
            queryBuilder.must(QueryBuilders.termQuery(RELATIONSITEROOM.getField(), param.getInstanceId()));
        }
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
     * @param addUpdModelInstanceContexts 实例更新上下文List
     * @param isLicense                   是否许可控制
     * @param isPower                     是否用户权限控制
     */
    @Transactional
    public void saveData(List addUpdModelInstanceContexts, Boolean isLicense, Boolean
            isPower) throws Exception {
        List<AddAndUpdateModelInstanceParam> instanceInfoList = null;
        Object dataObject = new Object();
        if (CollectionUtils.isNotEmpty(addUpdModelInstanceContexts) && addUpdModelInstanceContexts.size() > 0) {
            dataObject = addUpdModelInstanceContexts.get(0);
        }
        Map<Integer, AddUpdateTangAssetsParam> tangAssetsParamMap = new HashMap<>();

        if (dataObject instanceof AddUpdModelInstanceContext) {
            instanceInfoList = new ArrayList<>();
            for (int i = 0; i < addUpdModelInstanceContexts.size(); i++) {
                AddUpdModelInstanceContext addUpdModelInstanceContext = (AddUpdModelInstanceContext) addUpdModelInstanceContexts.get(i);
                instanceInfoList.add(addUpdModelInstanceContext.getAddAndUpdateModelInstanceParam());
                tangAssetsParamMap.put(i, addUpdModelInstanceContext.getAddUpdateTangAssetsParam());
            }
        } else {
            instanceInfoList = (List<AddAndUpdateModelInstanceParam>) addUpdModelInstanceContexts;
        }

        if (CollectionUtils.isNotEmpty(addUpdModelInstanceContexts)) {
            //判断新增实例数量是否达到上限
            if (isLicense != null && isLicense) {
                Integer count = selectCountInstances();
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("model_manage", count, instanceInfoList.size());
                if (responseBase.getRtnCode() != 200) {
                    throw new Exception("该模块新增数量已达许可数量上限！");
                }
            }
            //数据库为oracle,先调用序列将实体类InstanceId赋值，再批量插入
            if (DATACHECK.equals(DATEBASEORACLE)) {
                synchronized (this) {
                    mwModelInstanceDao.increaseInstanceIdSeq(instanceInfoList.size() + 1);
                    Integer lastInstanceId = mwModelInstanceDao.getInstanceIdSeq();
                    mwModelInstanceDao.increaseInstanceIdSeq(1);
                    Integer start = lastInstanceId - instanceInfoList.size() + 1;
                    for (int i = 0; i < instanceInfoList.size(); i++) {
                        AddAndUpdateModelInstanceParam param = instanceInfoList.get(i);
                        param.setInstanceId(start + i);
                    }
                }
            }
            //先向数据库中插入instance名称
            mwModelInstanceDao.insertInstanceName(instanceInfoList);

            //获取资产添加信息
            for (int i = 0; i < instanceInfoList.size(); i++) {
                AddAndUpdateModelInstanceParam aParam = instanceInfoList.get(i);
                AddUpdateTangAssetsParam tangAssetsParam = tangAssetsParamMap.get(i);
                if (null != tangAssetsParam) {
                    AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                    addModelInstancePropertiesParam.extractFrom(tangAssetsParam, aParam.getInstanceId());
                    aParam.getPropertiesList().add(addModelInstancePropertiesParam);
                }
            }

            //在es数据库插入数据
            BulkRequest bulkRequest = new BulkRequest();
            //获取所有模型的groupNodes
            List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
            Map<String, String> groupNodeMap = new HashMap();
            for (MwModelManageDTO manageDTO : groupNodeList) {
                groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
            }
            QueryBatchSelectDataParam updateRoomLayout = new QueryBatchSelectDataParam();
            List<QuerySelectDataListParam> layoutDataRoomList = new ArrayList<>();
            QueryCabinetLayoutListParam updateCabinetParam = new QueryCabinetLayoutListParam();
            List<QueryCabinetLayoutParam> layoutDataCabinetList = new ArrayList<>();
            List<ModelPermControlParam> permList = new ArrayList<>();

            //检查是否存在未定义字段
            Map<String, List<AddAndUpdateModelInstanceParam>> mapByMdelIndex = instanceInfoList.stream().collect(Collectors.groupingBy(AddAndUpdateModelInstanceParam::getModelIndex));
            mapByMdelIndex.forEach((k, v) -> {
                String modelIndex = k;
                List<AddAndUpdateModelInstanceParam> instanceParams = v;
                List<String> notExistFields = null;
                List<AddModelInstancePropertiesParam> allPropertiesList = new ArrayList<>();
                for (AddAndUpdateModelInstanceParam instanceParam : instanceParams) {
                    //先设置es字段mapping
                    //先根据getPropertiesIndexId过滤去重
                    List<AddModelInstancePropertiesParam> propertiesList = instanceParam.getPropertiesList().stream().filter(data -> data.getPropertiesIndexId() != null).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(AddModelInstancePropertiesParam::getPropertiesIndexId))), ArrayList::new));
                    allPropertiesList.addAll(propertiesList);
                }
                Map<String, AddModelInstancePropertiesParam> fieldTypeMap = allPropertiesList.stream()
                        .collect(Collectors.toMap(AddModelInstancePropertiesParam::getPropertiesIndexId, AddModelInstancePropertiesParam -> AddModelInstancePropertiesParam, (
                                value1, value2) -> {
                            return value2;
                        }));
                try {
                    notExistFields = MwModelUtils.getNotExistFields(modelIndex, fieldTypeMap.keySet(), restHighLevelClient);
                } catch (Exception e) {
                }
                //新增未定义字段
                if (null != notExistFields && notExistFields.size() > 0) {
                    MwModelUtils.batchSetESMapping(modelIndex, notExistFields, fieldTypeMap, restHighLevelClient);
                }
            });
            List<UpdateRelationIdParam> upInstanceRelationList = new ArrayList<>();
            List<AddAndUpdateModelInstanceParam> finalInstanceInfoList = instanceInfoList;
            instanceInfoList.forEach(param -> {
                SimpleDateFormat UTC2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                UTC2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                HashMap<String, Object> hashMap = new HashMap<>();

                List<Integer> userId = new ArrayList<>();
                List<List<Integer>> orgId = new ArrayList<>();
                List<Integer> groupId = new ArrayList<>();
                //机房布局坐标
                List<Integer> coordinate = new ArrayList<>();
                List<CabinetLayoutDataParam> cdParamList = new ArrayList<>();
                Integer reationRoomInstanceId = 0;
                Integer reationCabinetInstanceId = 0;
                //机柜U位数
                Integer UNum = 0;
                //机房布局行数
                Integer rowNum = 0;
                //机房布局列数
                Integer colNum = 0;
                QuerySelectDataListParam querySelectDataListParam = new QuerySelectDataListParam();

                for (AddModelInstancePropertiesParam properties : param.getPropertiesList()) {
                    if (properties.getPropertiesType() != null && (!Strings.isNullOrEmpty(properties.getPropertiesValue()))) {
                        if (properties.getPropertiesType() != null) {
                            Integer type = properties.getPropertiesType();
                            ModelPropertiesType modelType = ModelPropertiesType.getTypeByCode(type);
                            if (null != modelType && !Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                hashMap.put(properties.getPropertiesIndexId(), modelType.convertToEsData(properties.getPropertiesValue()));
                            }
                            if (type == ORG.getCode()) {//type类型 11 机构/部门
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    List<List<Integer>> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                    orgId.addAll(list);
                                }
                            } else if (type == USER.getCode()) {//type类型12 负责人
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    List<Integer> list = (List) JSONArray.parse(properties.getPropertiesValue());
                                    userId.addAll(list);
                                }
                            } else if (type == GROUP.getCode()) {//type类型13 用户组
                                if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                    groupId.addAll((List<? extends Integer>) JSONArray.parse(properties.getPropertiesValue()));
                                }
                            }
                            // 0表示普通视图，1表示机房，2表示机柜，3表示机柜下属设备
                            if (param.getModelViewType() != null && param.getModelViewType() > 0) {
                                if (UNUM.getField().equals(properties.getPropertiesIndexId())) {
                                    UNum = Integer.valueOf(properties.getPropertiesValue());
                                }
                                if (ROWNUM.getField().equals(properties.getPropertiesIndexId())) {
                                    rowNum = Integer.valueOf(properties.getPropertiesValue());
                                }
                                if (COLNUM.getField().equals(properties.getPropertiesIndexId())) {
                                    colNum = Integer.valueOf(properties.getPropertiesValue());
                                }
                                //modelViewType为1 机房实例（机房实例导入时，没有布局数据，需要自动生成）
                                if (param.getModelViewType() == 1) {
                                    List<List<QueryLayoutDataParam>> layoutDatas = new ArrayList<>();
                                    for (int x = 0; x < rowNum; x++) {
                                        List<QueryLayoutDataParam> layoutData = new ArrayList<>();
                                        for (int y = 0; y < colNum; y++) {
                                            QueryLayoutDataParam layoutDataParam = new QueryLayoutDataParam();
                                            layoutDataParam.setIsSelected(false);
                                            layoutDataParam.setIsBan(false);
                                            layoutData.add(layoutDataParam);
                                        }
                                        layoutDatas.add(layoutData);
                                    }
                                    hashMap.put(LAYOUTDATA.getField(), layoutDatas);
                                }
                                //ModelViewType=2,机柜实例，设置机柜所在机房的坐标
                                if (param.getModelViewType() == 2 && POSITIONBYROOM.getField().equals(properties.getPropertiesIndexId())) {
                                    coordinate.addAll((List) JSONArray.parse(properties.getPropertiesValue()));
                                }
                                if (type == ModelPropertiesType.SINGLE_RELATION.getCode() && properties.getPropertiesIndexId().indexOf("relationSite") != -1) {
                                    //ModelViewType() == 2 表示机柜实例，修改所属机房，mysql中实例的relationInstanceId也要修改
                                    //ModelViewType() == 1 机房实例，修改所属楼宇
                                    UpdateRelationIdParam updateRelationIdParam = new UpdateRelationIdParam();
                                    updateRelationIdParam.setInstanceId(param.getInstanceId());
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue()) && param.getModelViewType() == 1) {
                                        Integer reationFloorInstanceId = Integer.valueOf(properties.getPropertiesValue());
                                        updateRelationIdParam.setRelationInstanceId(reationFloorInstanceId);
                                    }
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue()) && param.getModelViewType() == 2) {
                                        reationRoomInstanceId = Integer.valueOf(properties.getPropertiesValue());
                                        updateRelationIdParam.setRelationInstanceId(reationRoomInstanceId);
                                    }
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue()) && param.getModelViewType() == 3) {
                                        reationCabinetInstanceId = Integer.valueOf(properties.getPropertiesValue());
                                        updateRelationIdParam.setRelationInstanceId(reationCabinetInstanceId);
                                    }
                                    upInstanceRelationList.add(updateRelationIdParam);
                                }
                                if (type == ModelPropertiesType.LAYOUTDATA.getCode() && POSITIONBYCABINET.getField().equals(properties.getPropertiesIndexId())) {
                                    if (!Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        CabinetLayoutDataParam cdParam = JSONObject.parseObject(properties.getPropertiesValue(), CabinetLayoutDataParam.class);
                                        QueryAssetsListParam assetsListParam = new QueryAssetsListParam();
                                        assetsListParam.setAssetsId(param.getInstanceId() + "");
                                        assetsListParam.setAssetsName(param.getInstanceName());
                                        //非刀片视图
                                        if (Strings.isNullOrEmpty(cdParam.getType()) || !BLADE_VIEW.equals(cdParam.getType()) || CollectionUtils.isEmpty(cdParam.getDaoData())) {
                                            cdParam.setInfo(assetsListParam);
                                        }
                                        //刀片布局数据中instanceId数值处理
                                        convertValueByBladeLayout(cdParam, finalInstanceInfoList);
                                        SaveCabinetLayoutDataParam saveParam = new SaveCabinetLayoutDataParam();
                                        saveParam = JSONObject.parseObject(JSONObject.toJSONString(cdParam), SaveCabinetLayoutDataParam.class);
                                        //刀片布局数据类型转换，去除currentFlag字段
                                        hashMap.put(properties.getPropertiesIndexId(), saveParam);

                                        cdParamList.add(cdParam);
                                    }
                                }
                            }
                        }
                    }
                }

                if (param.getModelViewType() != null) {
                    //新增机房布局（机房实例导入时，没有布局数据，需要自动生成）
                    //视图类型为0，且有布局和行数字段的，为机房实例数据。
                    if (param.getModelViewType() == 1) {
                        //type类型为16，且字段id为机房布局
//                        if (Strings.isNullOrEmpty(map.get(LAYOUTDATA.getField()).get(0).getPropertiesValue())) {
                        List<List<QueryLayoutDataParam>> layoutDatas = new ArrayList<>();
                        for (int x = 0; x < rowNum; x++) {
                            List<QueryLayoutDataParam> layoutData = new ArrayList<>();
                            for (int y = 0; y < colNum; y++) {
                                QueryLayoutDataParam layoutDataParam = new QueryLayoutDataParam();
                                layoutDataParam.setIsSelected(false);
                                layoutDataParam.setIsBan(false);
                                layoutData.add(layoutDataParam);
                            }
                            layoutDatas.add(layoutData);
                        }
                        hashMap.put(LAYOUTDATA.getField(), layoutDatas);
//                        }
                    }
                    if (param.getModelViewType() == 2) {//TODO  getModelViewType改为2
                        //批量导入机柜实例时，没有设置机柜布局数据，需要手动生成。
                        //①生成机柜布局数据
                        List<CabinetLayoutDataParam> cabinetLayoutDataParamList = new ArrayList<>();
                        for (int x = 0; x < UNum; x++) {
                            CabinetLayoutDataParam cabinetLayoutDataParam = new CabinetLayoutDataParam();
                            cabinetLayoutDataParam.setStart(x);
                            cabinetLayoutDataParam.setEnd(x);
                            cabinetLayoutDataParam.setIsUsed(false);
                            cabinetLayoutDataParam.setInfo(new QueryAssetsListParam());
                            cabinetLayoutDataParamList.add(cabinetLayoutDataParam);
                        }
                        hashMap.put(LAYOUTDATA.getField(), cabinetLayoutDataParamList);
                        //②修改对应的机房布局数据(批量修改)
                        //机柜实例，设置机柜位置，对应修改机房布局

                        querySelectDataListParam = new QuerySelectDataListParam();
                        //外部关联的实例Id
                        if (reationRoomInstanceId != null) {
                            querySelectDataListParam.setInstanceId(reationRoomInstanceId);
                            querySelectDataListParam.setCurrentRoomId(reationRoomInstanceId);
                        }
                        //外部关联的modelIndex
                        querySelectDataListParam.setModelIndex(param.getRelationModelIndex());
                        querySelectDataListParam.setCoordinate(coordinate);
                        layoutDataRoomList.add(querySelectDataListParam);
                    }
                    if (param.getModelViewType() == 3) {//TODO getModelViewType改为3
                        //机柜下属设备实例，设置设备位置，对应修改机柜布局
                        QueryCabinetLayoutParam clParam = new QueryCabinetLayoutParam();
                        clParam.setCurrentInstanceId(strValueConvert(param.getInstanceId()));
                        //外部关联的实例Id
                        if (reationCabinetInstanceId != null) {
                            clParam.setInstanceId(reationCabinetInstanceId);
                        }
                        //外部关联的modelIndex
                        clParam.setModelIndex(param.getRelationModelIndex());
                        if (cdParamList != null && cdParamList.size() > 0) {
                            CabinetLayoutDataParam cDataParam = cdParamList.get(0);
                            clParam.setCurrentData(cDataParam);
                            //不是默认视图
                            if (CHASSIS_VIEW.equals(cDataParam.getType()) || BLADE_VIEW.equals(cDataParam.getType())) {
                                if (cDataParam != null && cDataParam.getInfo() != null) {
                                    clParam.setChassisInstanceId(cDataParam.getInfo().getAssetsId());
                                }
                            }
                        }
                        layoutDataCabinetList.add(clParam);
                    }
                }
                //西藏邮储环境，添加资产模型权限控制
                //设置负责人，用户组，机构/部门
                ModelPermControlParam controlParam = new ModelPermControlParam();
                controlParam.setUserIds(userId);
                controlParam.setOrgIds(orgId);
                controlParam.setGroupIds(groupId);
                controlParam.setId(String.valueOf(param.getInstanceId()));
                if (!Strings.isNullOrEmpty(param.getInstanceType())) {
                    controlParam.setType(param.getInstanceType());
                } else {
                    controlParam.setType(DataType.INSTANCE_MANAGE.getName());
                }
                //设置基础属性字段。
                settingBaseByEsField(hashMap, param, groupNodeMap);
                permList.add(controlParam);
                JSONObject json = (JSONObject) JSONObject.toJSON(hashMap);
                IndexRequest newRequest = new IndexRequest(param.getModelIndex()).id(param.getModelIndex() + param.getInstanceId()).source(json);
                bulkRequest.add(newRequest);
            });

            //修改mysql机柜实例的关联机房Id
            if (CollectionUtils.isNotEmpty(upInstanceRelationList)) {
                mwModelInstanceDao.updateCabinetRelationId(upInstanceRelationList);
            }

            //TODO 批量更新机柜机房布局
            updateRoomLayout.setLayoutDataList(layoutDataRoomList);
            batchUpdateRoomLayout(updateRoomLayout);

            updateCabinetParam.setCabinetLayoutList(layoutDataCabinetList);
            batchUpdateCabinetLayout(updateCabinetParam);
            //批量插入负责人机构
            batchInsertPermList(permList);

            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            List<Integer> instanceIds = new ArrayList<>();
            final List<AddAndUpdateModelInstanceParam> updInstanceInfoList = instanceInfoList;
            restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        //新增失败时，删除插入数据库的实例数据
                        for (AddAndUpdateModelInstanceParam param : updInstanceInfoList) {
                            instanceIds.add(param.getInstanceId());
                        }
                        mwModelManageDao.deleteModelInstances(instanceIds);
                        log.error("异步执行批量添加模型insertModelInstanceProperties属性到es中失败");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    //新增失败时，删除插入数据库的实例数据
                    mwModelManageDao.deleteModelInstances(instanceIds);
                    log.error("异步执行批量添加模型属性到es中失败");
                }
            });
        }
    }

    private void convertValueByBladeLayout(CabinetLayoutDataParam param, List<AddAndUpdateModelInstanceParam> instanceInfoList) {
        Map<String, String> collect = instanceInfoList.stream().collect(Collectors.toMap(s -> s.getInstanceName(), s -> strValueConvert(s.getInstanceId())));
        //刀片布局数据
        if (param != null && BLADE_VIEW.equals(param.getType()) && CollectionUtils.isNotEmpty(param.getDaoData())) {
            List<List<QueryBladeInstanceParam>> daoDataList = param.getDaoData();
            for (List<QueryBladeInstanceParam> list : daoDataList) {
                for (QueryBladeInstanceParam instanceParam : list) {
                    if (collect != null && collect.containsKey(instanceParam.getInstanceName())) {
                        instanceParam.setInstanceId(collect.get(instanceParam.getInstanceName()));
                    }
                }
            }
        }
    }

    private void editorConvertValueByBladeLayout(CabinetLayoutDataParam param, List<AddAndUpdateModelInstanceParam> instanceInfoList) {
        Map<String, String> collect = instanceInfoList.stream().collect(Collectors.toMap(s -> s.getInstanceName(), s -> strValueConvert(s.getInstanceId())));

        Set<String> instanceIds = instanceInfoList.stream().map(s -> strValueConvert(s.getInstanceId())).collect(Collectors.toSet());

        //刀片布局数据
        if (param != null && BLADE_VIEW.equals(param.getType()) && CollectionUtils.isNotEmpty(param.getDaoData())) {
            List<List<QueryBladeInstanceParam>> daoDataList = param.getDaoData();
            //清除非本次修改，新增的旧数据（根据assetsId和当前状态CurrentFlag）
            for (List<QueryBladeInstanceParam> list1 : daoDataList) {
                for (QueryBladeInstanceParam listParam : list1) {
                    if (instanceIds.contains(listParam.getInstanceId()) && !listParam.isCurrentFlag()) {
                        listParam.setInstanceId("");
                        listParam.setInstanceName("");
                    }
                }
            }
        }
    }

    private void settingBaseByEsField(HashMap<String, Object> hashMap, AddAndUpdateModelInstanceParam param, Map<String, String> groupNodeMap) {
        //设置基础属性字段。
        MwModelInfoDTO modelInfoDTO = mwModelInstanceDao.getModelNameAndGroupName(param.getModelId());
        hashMap.put("modelId", param.getModelId());
        hashMap.put(INSTANCE_ID_KEY, param.getInstanceId());
        hashMap.put("modelIndex", param.getModelIndex());
        //设置修改人修改时间。
        hashMap.put("modifier", iLoginCacheInfo.getLoginName());
        hashMap.put("modificationDate", DateUtils.formatDateTime(new Date()));
        //设置创建人创建时间。
        hashMap.put("creator", iLoginCacheInfo.getLoginName());
        hashMap.put("createDate", DateUtils.formatDateTime(new Date()));
        if (param.getRelationInstanceId() != null) {
            hashMap.put("relationInstanceId", param.getRelationInstanceId());
        }
        String groupNodes = groupNodeMap.get(param.getModelIndex());
        hashMap.put("groupNodes", groupNodes);
        hashMap.put("isSync", param.isSync());
        String groupNodeId = "";
        //截取groupNodes最后一位作为模型的父节点id
        if (groupNodes.split(",").length > 1) {
            groupNodeId = groupNodes.split(",")[groupNodes.split(",").length - 1];
        }
        hashMap.put("assetsTypeSubId", param.getModelId() + "");
        hashMap.put("assetsTypeId", groupNodeId);
        hashMap.put("assetsTypeSubName", modelInfoDTO.getModelName());
        hashMap.put("assetsTypeName", modelInfoDTO.getModelGroupName());
        //同步数据加入es中
        if (CollectionUtils.isNotEmpty(param.getSyncParams())) {
            String profileName = "";
            for (MwModelMacrosValInfoParam macrosParam : param.getSyncParams()) {
                profileName = macrosParam.getAuthName();
                hashMap.put(macrosParam.getMacro(), macrosParam.getMacroVal());
            }
            hashMap.put("profileName", profileName);
        }
    }


    private void batchInsertPermList(List<ModelPermControlParam> permList) {
        List<OrgMapper> orgMapper = new ArrayList<>();
        List<GroupMapper> groupMapper = new ArrayList<>();
        List<UserMapper> userMapper = new ArrayList<>();
        List<DataPermissionDto> permissionMapper = new ArrayList<>();
        List<String> typeIds = new ArrayList<>();
        String type = DataType.INSTANCE_MANAGE.getName();
        for (ModelPermControlParam param : permList) {
            String typeId = param.getId();
            if (!Strings.isNullOrEmpty(param.getType())) {
                type = param.getType();
            }
            typeIds.add(typeId);
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(type);     //类型
            dto.setTypeId(typeId);  //数据主键
            dto.setDescription(DataType.valueOf(type).getDesc()); //描述
            List<Integer> userIdList = param.getUserIds();
            List<List<Integer>> orgIdList = param.getOrgIds();
            List<Integer> groupIdList = param.getGroupIds();
            String finalType = type;
            if (CollectionUtils.isNotEmpty(orgIdList)) {
                orgIdList.forEach(
                        orgId -> orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(finalType).build())
                );
            }
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                dto.setIsGroup(1);
                groupIdList.forEach(
                        groupId -> groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(finalType).build())
                );
            } else {
                dto.setIsGroup(0);
            }


            if (CollectionUtils.isNotEmpty(userIdList)) {
                dto.setIsUser(1);
                userIdList.forEach(userIds -> {
                            log.info("userMapper.add,userid:{}", userIds);
                            userMapper.add(UserMapper.builder().typeId(typeId).userId(userIds).type(finalType).build());
                        }
                );
            } else {
                dto.setIsUser(0);
            }
            permissionMapper.add(dto);
        }
        //数据超过1000个，进行切割循环插入
        List<List<String>> typeIdLists = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(typeIds)) {
            typeIdLists = Lists.partition(typeIds, insBatchFetchNum);
        }
        for (List<String> typeIdList : typeIdLists) {
            if (CollectionUtils.isNotEmpty(typeIdList)) {
                DeleteDto deleteDto = DeleteDto.builder()
                        .typeIds(typeIdList)
                        .type(type)
                        .build();
                mwCommonService.deleteMapperAndPerms(deleteDto);
            }
        }


        List<List<GroupMapper>> groupMappers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupMapper)) {
            groupMappers = Lists.partition(groupMapper, insBatchFetchNum);
        }
        for (List<GroupMapper> groupMapperList : groupMappers) {
            if (CollectionUtils.isNotEmpty(groupMapperList)) {
                mwCommonService.insertGroupMapper(groupMapperList);
            }
        }


        List<List<UserMapper>> userMappers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userMapper)) {
            userMappers = Lists.partition(userMapper, insBatchFetchNum);
        }
        for (List<UserMapper> userMapperList : userMappers) {
            if (CollectionUtils.isNotEmpty(userMapperList)) {
                mwCommonService.insertUserMapper(userMapperList);
            }
        }

        List<List<OrgMapper>> orgMappers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orgMapper)) {
            orgMappers = Lists.partition(orgMapper, insBatchFetchNum);
        }
        for (List<OrgMapper> orgMapperList : orgMappers) {
            if (CollectionUtils.isNotEmpty(orgMapperList)) {
                mwCommonService.insertOrgMapper(orgMapperList);
            }
        }

        List<List<DataPermissionDto>> permissionMappers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(permissionMapper)) {
            permissionMappers = Lists.partition(permissionMapper, insBatchFetchNum);
        }
        for (List<DataPermissionDto> permissionMapperList : permissionMappers) {
            if (CollectionUtils.isNotEmpty(permissionMapperList)) {
                mwCommonService.insertPermissionMapper(permissionMapperList);
            }
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
                if (param.getInstanceIds() != null && param.getInstanceIds().size() > 0) {
                    queryBuilder.must(QueryBuilders.termsQuery(INSTANCE_ID_KEY, param.getInstanceIds()));
                }
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
        List<MwModelInstanceCommonParam> list = mwModelInstanceDao.selectModelInstanceInfoById(modelId);
        return list;
    }


    /**
     * 通过modelIndexs和instanceIds批量删除es实例数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply batchDeleteInstanceInfo(DeleteModelInstanceParam param) {
        try {
            if (CollectionUtils.isEmpty(param.getModelIndexs())) {
                param.setModelIndexs(Strings.isNullOrEmpty(param.getModelIndex()) ? null : Arrays.asList(param.getModelIndex()));
            }
            if (CollectionUtils.isNotEmpty(param.getModelIndexs())) {
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(String.join(",", param.getModelIndexs()));
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                List<Integer> instanceIds = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                    queryBuilder.must(QueryBuilders.termsQuery(INSTANCE_ID_KEY, param.getInstanceIds()));
                }
                if (CollectionUtils.isEmpty(param.getRelationInstanceIds())) {
                    param.setRelationInstanceIds(param.getRelationInstanceId() == null ? null : Arrays.asList(param.getRelationInstanceId()));
                }
                //实例同步数据删除，要根据依附的实例id作为标准删除
                if (CollectionUtils.isNotEmpty(param.getRelationInstanceIds())) {
                    queryBuilder.must(QueryBuilders.termsQuery(RELATION_INSTANCE_ID, param.getRelationInstanceIds()));
                }
                deleteByQueryRequest.setQuery(queryBuilder);
                restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

                if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                    mwModelInstanceDao.deleteBatchInstanceById(param.getInstanceIds());
                    instanceIds.addAll(param.getInstanceIds());
                }

                //查询所有的关联数据Id
                List<Integer> instanceIdByQuery = mwModelInstanceDao.getInstanceIdsByRelationIds(param);
                instanceIds.addAll(instanceIdByQuery);

                //根据relationid 和modelIndex 删除所关联的实例数据
                if (CollectionUtils.isNotEmpty(param.getRelationInstanceIds())) {
                    mwModelInstanceDao.deleteBatchInstanceByRelationIds(param);
                }

                //调用许可
                Integer count = selectCountInstances();
                licenseManagement.getLicenseManagemengt("model_manage", count, 0);

                //删除权限数据
                //根据实例Id删除（instanceId唯一），无需区别资产类型
                if (CollectionUtils.isNotEmpty(instanceIds)) {
                    List<String> typeIds = instanceIds.stream().map(String::valueOf).collect(Collectors.toList());
                    String virtualType = DataType.MODEL_VIRTUAL.getName();
                    String instanceType = DataType.INSTANCE_MANAGE.getName();
                    batchDeletePermInfo(typeIds, virtualType);
                    batchDeletePermInfo(typeIds, instanceType);
                }
                return Reply.ok();
            } else {
                return Reply.fail(500, "批量删除失败");
            }
        } catch (Exception e) {
            log.error("batchDeleteInstanceInfo {}", e);
            return Reply.fail(500, "批量删除失败");
        }
    }

    @Override
    public List<Map<String, Object>> getInstanceInfoByProperties(QueryInstanceModelParam param) throws Exception {
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (StringUtils.isNotEmpty(param.getModelIndex())) {
            //条件组合查询
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                queryBuilder.must(QueryBuilders.termsQuery(INSTANCE_ID_KEY, param.getInstanceIds()));
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
            searchSourceBuilder.size(pageSize);
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

    /**
     * 批量删除权限信息
     */
    protected void batchDeletePermInfo(List<String> typeIds, String type) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeIds(typeIds)
                .type(type)
                .build();
        if (CollectionUtils.isNotEmpty(deleteDto.getTypeIds())) {
            mwCommonService.deleteMapperAndPerms(deleteDto);
        }
    }

    public List<QueryInstanceParam> getAllInstanceNameById(List<Integer> instanceIds) {
        List<QueryInstanceParam> listInstanceInfo = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(instanceIds)) {
            List<List<Integer>> instanceIdGroups = new ArrayList<>();
            instanceIdGroups = Lists.partition(instanceIds, insBatchFetchNum);
            if (null != instanceIdGroups) {
                for (List<Integer> instanceIdList : instanceIdGroups) {
                    listInstanceInfo.addAll(mwModelInstanceDao.getInstanceNameByIds(instanceIdList));
                }
            }
        }
        return listInstanceInfo;
    }

    /**
     * 根据机柜id获取机房的关联id
     *
     * @param instanceIds
     */
    public List<QueryInstanceParam> getCabinetInfoByRelationCabinedId(List<Integer> instanceIds) {
        List<QueryInstanceParam> instanceParams = new ArrayList<>();
        try {
            QueryInstanceModelParam param = new QueryInstanceModelParam();
            List<String> modelIndexs = mwModelInstanceDao.getModelIndexByInstanceIds(instanceIds);
            param.setInstanceIds(instanceIds);
            param.setModelIndexs(modelIndexs);
            param.setFieldList(Arrays.asList(INSTANCE_ID_KEY, INSTANCE_NAME_KEY, RELATIONSITEROOM.getField()));
            List<Map<String, Object>> listMap = getInstanceInfoByModelId(param);
            instanceParams = MwModelUtils.convertEsData(QueryInstanceParam.class, listMap);
        } catch (Exception e) {
            log.info("根据机柜id获取机房的关联数据失败");
        }
        return instanceParams;
    }


    /**
     * scroll滚动查询 获取索引下所有的数据
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> getAllInstanceInfoByModelIndex(QueryModelInstanceParam param) throws IOException {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        List<Map<String, Object>> listMap = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设定每次返回多少条数据
        searchSourceBuilder.size(scrollSize);
        List<Integer> instanceIdList = mwModelManageDao.selectInstanceIdsByModelId(param.getModelId());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("modelIndex", param.getModelIndex()));
        if (instanceIdList.size() > 0) {
            QueryBuilder queryBuilder1 = QueryBuilders.termsQuery(INSTANCE_ID_KEY, instanceIdList);
            queryBuilder.must(queryBuilder1);
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

    /**
     * es数据刷新（重新新建索引、设置Mapping）
     * 只在es层面操作
     */
    @Override
    public void esDataRefresh(ModelParam modelParams) {
        //获取mysql中所有的模型索引
        try {
            List<ModelParam> modelInfoList = mwModelManageDao.getModelInfoDisParent();
            for (ModelParam modelParam : modelInfoList) {
                //获取mapping文件
                GetMappingsRequest request = new GetMappingsRequest();
                request.indices(modelParam.getModelIndex());
                GetMappingsResponse getMappingsResponse = restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
                MappingMetadata mappings = getMappingsResponse.mappings().get(modelParam.getModelIndex());
                Map<String, Object> mapping = (Map<String, Object>) mappings.getSourceAsMap();
                updateEsMappingData(mapping);

                //循环获取索引下的数据
                QueryModelInstanceParam param = new QueryModelInstanceParam();
                param.setModelIndex(modelParam.getModelIndex());
                param.setModelId(modelParam.getModelId());
                List<Map<String, Object>> list = getAllInstanceInfoByModelIndex(param);
                //删除该索引，并重新创建索引
                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(modelParam.getModelIndex());
                restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                //重新创建索引
                mwModelManageService.createEsIndex(modelParam.getModelIndex());

                //同步数据
                BulkRequest bulkRequest = new BulkRequest();
                if (CollectionUtils.isNotEmpty(list)) {
                    //设置mapping
                    PutMappingRequest putMappingRequest = new PutMappingRequest(modelParam.getModelIndex());
                    putMappingRequest.source(JSONObject.toJSONString(mapping), XContentType.JSON);
                    restHighLevelClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
                    for (Map<String, Object> map : list) {
                        IndexRequest indexRequest = new IndexRequest(modelParam.getModelIndex());
                        indexRequest.id(map.get("esId").toString());
                        indexRequest.source(map);
                        bulkRequest.add(indexRequest);
                    }
                    BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    if (bulkResponse.hasFailures()) {
                        log.error(modelParam.getModelId() + ":同步数据失败");
                        return;
                    } else {
                        log.error(modelParam.getModelId() + ":同步数据成功");
                    }
                }
            }
        } catch (IOException e) {
            log.error("同步数据失败", e);
        }
    }

    private void updateEsMappingData(Map<String, Object> mapping) {
        if (mapping != null && mapping.size() > 0 && mapping.containsKey("properties")) {
            Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
            for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
                Map<String, Object> field = (Map<String, Object>) propertyEntry.getValue();
                //mapping的type类型为text的，设置分词器（不区分大小写）
                if (field.containsKey("type") && "text".equals(field.get("type"))) {
                    Map<String, Object> fields = (Map<String, Object>) field.get("fields");
                    if (fields == null) {
                        fields = new HashMap<>();
                    }
                    Map<String, Object> keyword = (Map<String, Object>) fields.get("keyword");
                    if (keyword == null) {
                        keyword = new HashMap<>();
                    }
                    //设置分词器normalizer为自定义的my_analyzer
                    keyword.put("normalizer", "my_analyzer");
                    fields.put("keyword", keyword);
                    field.put("fields", fields);
                }

                if("createDate".equals(propertyEntry.getKey()) || "modificationDate".equals(propertyEntry.getKey())){
                    if (field.containsKey("type") && "date".equals(field.get("type"))) {
                        field.put("type","text");
                        field.remove("format");
                        Map<String, Object> fields = (Map<String, Object>) field.get("fields");
                        if (fields == null) {
                            fields = new HashMap<>();
                        }
                        Map<String, Object> keyword = (Map<String, Object>) fields.get("keyword");
                        if (keyword == null) {
                            keyword = new HashMap<>();
                        }
                        //设置分词器normalizer为自定义的my_analyzer
                        keyword.put("normalizer", "my_analyzer");
                        keyword.put("type", "keyword");
                        fields.put("keyword", keyword);
                        field.put("fields", fields);
                    }
                }
            }
        }


    }

    /**
     * 批量刪除关联数据
     *
     * @param param
     */
    private void batchDeleteRelationInstances(DeleteModelInstanceParam param) {
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("mw_*");
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            List<Integer> instanceIds = new ArrayList<>();
            if (CollectionUtils.isEmpty(param.getRelationInstanceIds())) {
                param.setRelationInstanceIds(param.getRelationInstanceId() == null ? null : Arrays.asList(param.getRelationInstanceId()));
            }
            //实例同步数据删除，要根据依附的实例id作为标准删除
            if (CollectionUtils.isNotEmpty(param.getRelationInstanceIds())) {
                queryBuilder.must(QueryBuilders.termsQuery(RELATION_INSTANCE_ID, param.getRelationInstanceIds()));
            }
            deleteByQueryRequest.setQuery(queryBuilder);
            restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

            if (CollectionUtils.isNotEmpty(param.getInstanceIds())) {
                mwModelInstanceDao.deleteBatchInstanceById(param.getInstanceIds());
                instanceIds.addAll(param.getInstanceIds());
            }

            //查询所有的关联数据Id
            List<Integer> instanceIdByQuery = mwModelInstanceDao.getInstanceIdsByRelationIdAndModelId(param.getRelationInstanceIds());
            instanceIds.addAll(instanceIdByQuery);

            //根据relationid 和modelIndex 删除所关联的实例数据
            if (CollectionUtils.isNotEmpty(param.getRelationInstanceIds())) {
                mwModelInstanceDao.deleteInstanceIdsByRelationIdAndModelId(param.getRelationInstanceIds());
            }

            //调用许可
            Integer count = selectCountInstances();
            licenseManagement.getLicenseManagemengt("model_manage", count, 0);

            //删除权限数据
            //根据实例Id删除（instanceId唯一），无需区别资产类型
            if (CollectionUtils.isNotEmpty(instanceIds)) {
                List<String> typeIds = instanceIds.stream().map(String::valueOf).collect(Collectors.toList());
                String virtualType = DataType.MODEL_VIRTUAL.getName();
                String instanceType = DataType.INSTANCE_MANAGE.getName();
                batchDeletePermInfo(typeIds, virtualType);
                batchDeletePermInfo(typeIds, instanceType);
            }
        } catch (Exception e) {
            log.error("batchDeleteRelationInstances {}", e);
        }
    }

    public void deleteInstanceTopo(DeleteModelInstanceParam param) throws Exception {
        Session session = connectionPool.getSession();
        List<String> spaceList = new ArrayList<>();
        for (MwModelInstanceParam instanceParam : param.getParamList()) {
            String space = ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + instanceParam.getInstanceId();
            spaceList.add(space);
        }
        ModelAssetUtils.deleteInstanceTopoBySpaceList(session, spaceList);
        log.info("根据spaceList删除实例拓扑数据成功");
    }


    /**
     * 同步所有资产的关联关系数据
     */
    @Override
    public Reply syncAllInstanceLinkRelation() {
        try {
            List<MwModelInfoDTO> allModelList = mwModelManageDao.selectOrdinaryModel();
            Map<String, String> map = allModelList.stream().collect(Collectors.toMap(s -> s.getModelIndex(), s -> s.getModelId(), (
                    value1, value2) -> {
                return value2;
            }));
            //获取模型属性
            Map<Integer, List<PropertyInfo>> allPropertyLists = mwModelCommonService.getAllModelPropertyInfo();
            List<Integer> modelIds = new ArrayList<>();
            allPropertyLists.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    for (PropertyInfo property : v) {
                        //获取所有关系关联的属性信息
                        if (CONNECT_RELATION.getCode() == property.getPropertiesTypeId()) {
                            modelIds.add(k);
                        }
                    }
                }
            });
            //根据模型Id获取所有的实例数据
            List<MwModelInstanceCommonParam> mwModelInstanceCommonParams = mwModelInstanceDao.selectModelInstanceInfoByIds(modelIds);
            List<ModelConnectRelationParam> connectRelationList = new ArrayList<>();
            Set<String> relationModelIdSets = new HashSet<>();
            Set<String> relationModelIndexs = new HashSet<>();
            for (MwModelInstanceCommonParam instanceParam : mwModelInstanceCommonParams) {
                String modelIndex = instanceParam.getModelIndex();
                Integer modelId = instanceParam.getModelId();
                Integer instanceId = instanceParam.getModelInstanceId();
                List<PropertyInfo> propertyInfoList = new ArrayList<>();
                if (allPropertyLists != null && allPropertyLists.containsKey(modelId)) {
                    propertyInfoList = allPropertyLists.get(modelId);
                }
                for (PropertyInfo property : propertyInfoList) {
                    //获取所有关系关联的属性信息
                    if (CONNECT_RELATION.getCode() == property.getPropertiesTypeId()) {
                        ModelConnectRelationParam connectRelationParam = new ModelConnectRelationParam();
                        String propertiesIndex = property.getIndexId();
                        String relationModelIndex = property.getRelationModelIndex();
                        String relationPropertiesIndex = property.getRelationPropertiesIndex();
                        connectRelationParam.setModelIndex(modelIndex);
                        connectRelationParam.setModelId(modelId);
                        connectRelationParam.setInstanceId(instanceId);
                        connectRelationParam.setPropertiesIndex(propertiesIndex);
                        connectRelationParam.setRelationModelIndex(relationModelIndex);
                        relationModelIndexs.add(relationModelIndex);
                        connectRelationParam.setRelationModelId(0);
                        if (map != null && map.containsKey(relationModelIndex)) {
                            connectRelationParam.setRelationModelId(map.get(relationModelIndex) != null ? Integer.valueOf(map.get(relationModelIndex)) : 0);
                            relationModelIdSets.add(connectRelationParam.getRelationModelId() + "");
                        }
                        connectRelationParam.setRelationPropertiesIndex(relationPropertiesIndex);
                        connectRelationList.add(connectRelationParam);
                    }
                }
            }

            //根据ModelId和实例Id转为Map
            Map<Integer, ModelConnectRelationParam> relationParamMap = connectRelationList.stream().collect(Collectors.toMap(ModelConnectRelationParam::getInstanceId, s -> s, (
                    value1, value2) -> {
                return value2;
            }));
            //根据Neo4j数据库，查询指定关系关联的节点数据
            Session session = connectionPool.getSession();
            List<QueryToPoRelationInstanceInfo> queryList = new ArrayList<>();
            for (ModelConnectRelationParam connectRelationParam : connectRelationList) {
                QueryToPoRelationInstanceInfo queryInfo = new QueryToPoRelationInstanceInfo();
                queryInfo.setDeep("3");
                queryInfo.setOwnModelId(connectRelationParam.getModelId() + "");
                queryInfo.setOwnInstanceId(connectRelationParam.getInstanceId() + "");
                queryInfo.setRelationModelId(connectRelationParam.getRelationModelId() + "");
                queryInfo.setOwnModelIndex(connectRelationParam.getModelIndex());
                queryList.add(queryInfo);
            }

            List<MwModelToPoRelationInstanceParam> relationInstanceList = ModelAssetUtils.queryToPoInfoByInstanceList(session, queryList);
            List<ModelConnectRelationParam> insertEsData = new ArrayList<>();
            Set<Integer> relationIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(relationInstanceList)) {
                relationIds = relationInstanceList.stream().map(s -> s.getRelationInstanceId()).collect(Collectors.toSet());
                QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
                params.setModelIndexs(new ArrayList<>(relationModelIndexs));
                params.setInstanceIds(new ArrayList<>(relationIds));
                //根据节点数据，查询es信息
                List<Map<String, Object>> relationInstanceMaps = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);

                Map<Integer, Map<String, Object>> collect = relationInstanceMaps.stream().collect(Collectors.toMap(s -> intValueConvert(s.get(INSTANCE_ID_KEY)), s -> s, (
                        value1, value2) -> {
                    return value2;
                }));


                for (MwModelToPoRelationInstanceParam relationInstanceParam : relationInstanceList) {
                    Integer ownInstanceId = relationInstanceParam.getOwnInstanceId();
                    Integer realtionInstanceId = relationInstanceParam.getRelationInstanceId();
                    if (relationParamMap != null && relationParamMap.containsKey(ownInstanceId)) {
                        ModelConnectRelationParam relationInfoParam = relationParamMap.get(ownInstanceId);
                        String relationPropertiesIndex = relationInfoParam.getRelationPropertiesIndex();
                        String propertiesIndex = relationInfoParam.getPropertiesIndex();
                        Map<String, Object> m = collect.get(realtionInstanceId);
                        ModelConnectRelationParam connectParam = new ModelConnectRelationParam();
                        Object obj = m.get(relationPropertiesIndex);
                        connectParam.setPropertiesValue(obj);
                        connectParam.setPropertiesIndex(propertiesIndex);
                        connectParam.setInstanceId(ownInstanceId);
                        connectParam.setModelIndex(relationInstanceParam.getOwnModelIndex());
                        insertEsData.add(connectParam);
                    }
                }

                //更新es数据（关系关联数据插入es中
                BulkRequest request = new BulkRequest();
                for (ModelConnectRelationParam m : insertEsData) {
                    UpdateRequest updateRequest = new UpdateRequest(m.getModelIndex(), m.getModelIndex() + m.getInstanceId());
                    updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put(m.getPropertiesIndex(), m.getPropertiesValue());
                    updateRequest.doc(jsonMap);
                    request.add(updateRequest.upsert());
                }
                restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            }

        } catch (Exception e) {
            log.error("同步资产关联关系数据失败", e);
            return Reply.fail(500, "同步资产关联关系数据失败");
        }
        return Reply.ok();
    }


    /**
     * 关系关联属性值获取
     * 通过实例拓扑创建时，自动获取
     */
    @Override
    public Reply getInstanceIdByLinkRelation(QueryInstanceTopoInfoParam param) {
        try {
            List<MwModelInfoDTO> allModelList = mwModelManageDao.selectOrdinaryModel();
            Map<String, String> map = allModelList.stream().collect(Collectors.toMap(s -> s.getModelIndex(), s -> s.getModelId(), (
                    value1, value2) -> {
                return value2;
            }));
            //获取模型属性
            List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(Integer.valueOf(param.getOwnModelId()));
            String modelIndex = "";
            List<PropertyInfo> propertyInfos = new ArrayList<>();
            for (ModelInfo modelInfo : modelInfoList) {
                if (modelInfo.getModelId().intValue() == Integer.valueOf(param.getOwnModelId()).intValue()) {
                    modelIndex = modelInfo.getModelIndex();
                }
                propertyInfos.addAll(modelInfo.getPropertyInfos());
            }
            List<ModelConnectRelationParam> connectRelationList = new ArrayList<>();
            Set<String> relationModelIdSets = new HashSet<>();
            Set<String> relationModelIndexs = new HashSet<>();
            for (PropertyInfo property : propertyInfos) {
                //获取所有关系关联的属性信息
                if (CONNECT_RELATION.getCode() == property.getPropertiesTypeId()) {
                    ModelConnectRelationParam connectRelationParam = new ModelConnectRelationParam();
                    String propertiesIndex = property.getIndexId();
                    String relationModelIndex = property.getRelationModelIndex();
                    String relationPropertiesIndex = property.getRelationPropertiesIndex();
                    connectRelationParam.setModelIndex(modelIndex);
                    connectRelationParam.setModelId(Integer.valueOf(param.getOwnModelId()));
                    connectRelationParam.setInstanceId(Integer.valueOf(param.getOwnInstanceId()));
                    connectRelationParam.setPropertiesIndex(propertiesIndex);
                    connectRelationParam.setRelationModelIndex(relationModelIndex);
                    relationModelIndexs.add(relationModelIndex);
                    connectRelationParam.setRelationModelId(0);
                    if (map != null && map.containsKey(relationModelIndex)) {
                        connectRelationParam.setRelationModelId(map.get(relationModelIndex) != null ? Integer.valueOf(map.get(relationModelIndex)) : 0);
                        relationModelIdSets.add(connectRelationParam.getRelationModelId() + "");
                    }
                    connectRelationParam.setRelationPropertiesIndex(relationPropertiesIndex);
                    connectRelationList.add(connectRelationParam);
                }
            }
            //根据RelationModelId分组
            Map<Integer, List<ModelConnectRelationParam>> relationParamMap = connectRelationList.stream().collect(Collectors.groupingBy(ModelConnectRelationParam::getRelationModelId));

            //根据Neo4j数据库，查询指点关系关联的节点数据
            String ownModelInstanceId = param.getOwnModelId() + "_" + param.getOwnInstanceId();
            Session session = connectionPool.getSession();
            String deep = param.getDeep();
            List<String> topoInstanceIdList = ModelAssetUtils.queryTopoInfoByModelInstanceId(session, null, deep, ownModelInstanceId, relationModelIdSets);
            String value = "";
            List<ModelConnectRelationParam> insertEsData = new ArrayList<>();
            List<Integer> relationInstanceIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(topoInstanceIdList)) {
                for (String topoInstanceId : topoInstanceIdList) {
                    if (topoInstanceId.split("_").length > 1 && topoInstanceId.split("_")[1] != null) {
                        String relationInstanceId = topoInstanceId.split("_")[1];
                        relationInstanceIdList.add(Integer.valueOf(relationInstanceId));
                    }
                }
                QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
                params.setModelIndexs(new ArrayList<>(relationModelIndexs));
                params.setInstanceIds(relationInstanceIdList);
                //根据节点数据，查询es信息
                List<Map<String, Object>> relationInstanceMaps = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);
                for (Map<String, Object> esMap : relationInstanceMaps) {
                    Integer modelId = Integer.valueOf(esMap.get("modelId").toString());
                    if (relationParamMap != null && relationParamMap.containsKey(modelId)) {
                        //获取所有的关系关联属性
                        List<ModelConnectRelationParam> connectParams = relationParamMap.get(modelId);
                        for (ModelConnectRelationParam connectParam : connectParams) {
                            String relationproperIndex = connectParam.getRelationPropertiesIndex();
                            Object obj = esMap.get(relationproperIndex);
                            connectParam.setPropertiesValue(obj);
                            insertEsData.add(connectParam);
                        }
                    }
                }
                log.info("关系关联区域数据:");
                //更新es数据（关系关联数据插入es中）
                BulkRequest request = new BulkRequest();
                for (ModelConnectRelationParam m : insertEsData) {
                    UpdateRequest updateRequest = new UpdateRequest(m.getModelIndex(), m.getModelIndex() + m.getInstanceId());
                    updateRequest.timeout(new TimeValue(timeNum, TimeUnit.SECONDS));
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put(m.getRelationPropertiesIndex(), m.getPropertiesValue());
                    updateRequest.doc(jsonMap);
                    request.add(updateRequest.upsert());
                }
                restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            log.error("设置关系关联字段失败", e);
            return Reply.fail(500, "设置关系关联字段失败");
        }
        return Reply.ok();
    }

    private void setESAreaValue(List<Map<String, Object>> sourceAsMapList) {
        List<Integer> instanceIds = new ArrayList<>();
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            for (Map<String, Object> sourceAsMap : sourceAsMapList) {
                if (sourceAsMap != null && sourceAsMap.size() > 0
                        && sourceAsMap.get(RELATIONSITECABINET.getField()) != null && !"".equals(sourceAsMap.get(RELATIONSITECABINET.getField()))
                        && sourceAsMap.get(RELATIONSITEROOM.getField()) != null && !"".equals(sourceAsMap.get(RELATIONSITEROOM.getField()))) {
                    //获取所属机房，所属机柜的实例id
                    Integer cabinetInstanceId = Integer.valueOf(sourceAsMap.get(RELATIONSITECABINET.getField()).toString());
                    Integer roomInstanceId = Integer.valueOf(sourceAsMap.get(RELATIONSITEROOM.getField()).toString());
                    instanceIds.add(cabinetInstanceId);
                    instanceIds.add(roomInstanceId);
                    listMap.add(sourceAsMap);
                }
            }

            List<List<Integer>> instanceIdGroups = null;
            List<MwModelInstanceCommonParam> modelInstanceList = new ArrayList<>();
            if (null != instanceIds) {
                instanceIdGroups = Lists.partition(instanceIds, insBatchFetchNum);
                for (List<Integer> instanceIdList : instanceIdGroups) {
                    if (CollectionUtils.isNotEmpty(instanceIdList)) {
                        //所属机房，所属机柜的实例id查询对应实例名称
                        modelInstanceList.addAll(mwModelInstanceDao.getInstanNameAndRelationNameById(instanceIdList));
                    }
                }
            }
            Map<Integer, MwModelInstanceCommonParam> instanceMap = modelInstanceList.stream().collect(Collectors.toMap(s -> s.getModelInstanceId(), s -> s, (
                    value1, value2) -> {
                return value2;
            }));

            for (Map<String, Object> sourceAsMap : listMap) {
                //所属机房机柜数据不存在，则不设置告警区域字段
                if (sourceAsMap.get(RELATIONSITECABINET.getField()) != null && !"".equals(sourceAsMap.get(RELATIONSITECABINET.getField()))
                        && sourceAsMap.get(RELATIONSITEROOM.getField()) != null && !"".equals(sourceAsMap.get(RELATIONSITEROOM.getField()))) {
                    Integer cabinetInstanceId = Integer.valueOf(sourceAsMap.get(RELATIONSITECABINET.getField()).toString());
                    Integer roomInstanceId = Integer.valueOf(sourceAsMap.get(RELATIONSITEROOM.getField()).toString());
                    Object obj = sourceAsMap.get(POSITIONBYCABINET.getField());
                    CabinetLayoutDataParam cabinetCoordinate = new CabinetLayoutDataParam();
                    Integer startIndex = null;
                    Integer endIndex = null;
                    if (obj != null) {
                        if (obj instanceof List) {//防止有脏数据存在，
                            List<CabinetLayoutDataParam> cabinetCoordinateList = JSON.parseArray(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                            if (CollectionUtils.isNotEmpty(cabinetCoordinateList)) {
                                cabinetCoordinate = cabinetCoordinateList.get(0);
                            }
                        } else {
                            cabinetCoordinate = JSONObject.parseObject(JSONObject.toJSONString(obj), CabinetLayoutDataParam.class);
                        }
                        startIndex = cabinetCoordinate.getStart() + 1;
                        endIndex = cabinetCoordinate.getEnd() + 1;
                    }
                    String modelArea = "";
                    if (instanceMap.containsKey(cabinetInstanceId) && instanceMap.containsKey(roomInstanceId)
                            && startIndex != null && endIndex != null) {
                        MwModelInstanceCommonParam cabinetParam = instanceMap.get(cabinetInstanceId);
                        MwModelInstanceCommonParam roomParam = instanceMap.get(roomInstanceId);
                        modelArea = roomParam.getModelInstanceName() + "_" + cabinetParam.getModelInstanceName() + "(" + startIndex + "-" + endIndex + "U)";
                    }
                    sourceAsMap.put("modelArea", modelArea);
                }
            }
            editorDataToEs(listMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * es设置区域字段
     *
     * @return
     */
    @Override
    public Reply setModelAreaDataToEs() {
        List<MwModelInfoDTO> modelInfoDTOList = mwModelInstanceDao.getAllModelInfoByPids(modelSystemParentModelId);
        List<String> modelIndexs = modelInfoDTOList.stream().map(s -> s.getModelIndex()).collect(Collectors.toList());
        List<Map<String, Object>> listMap = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest(String.join(",", modelIndexs));
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有数据
            sourceBuilder.from(0);
            sourceBuilder.size(pageSize);
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<Integer> instanceIds = new ArrayList<>();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                sourceAsMap.put("esId", searchHit.getId());
                listMap.add(sourceAsMap);
            }
            setESAreaValue(listMap);
        } catch (Exception e) {
            log.error("更新告警区域数据失败", e);
            return Reply.fail(500, "更新告警区域数据失败");
        }
        return Reply.ok("数据更新成功");
    }

    private void editorDataToEs(List<Map<String, Object>> listMap) throws IOException {
        //同步数据
        BulkRequest bulkRequest = new BulkRequest();
        if (CollectionUtils.isNotEmpty(listMap)) {
            for (Map<String, Object> map : listMap) {
                IndexRequest indexRequest = new IndexRequest(map.get("modelIndex").toString());
                indexRequest.id(map.get("esId").toString());
                indexRequest.source(map);
                bulkRequest.add(indexRequest);
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error("同步数据失败");
            } else {
                log.error("同步数据成功");
            }
        }
    }


    /**
     * 批量新增数据
     *
     * @param batchInstanceList
     * @param types
     * @return
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Reply batchCreatModelInstance(Object batchInstanceList, Integer types) {
        BatchAddModelInstanceParam param = new BatchAddModelInstanceParam();
        List<Integer> instanceIdList = new ArrayList<>();

        try {
            //判断是否是流程审批类型
            if (types == 0) {
                param = (BatchAddModelInstanceParam) batchInstanceList;
            } else {
                param = JSONObject.parseObject(batchInstanceList.toString(), BatchAddModelInstanceParam.class);
            }
            if (CollectionUtils.isNotEmpty(param.getBatchInsertAssetsList())) {
                if (debug) {
                    log.info(param.toString());
                }
                Integer count = selectCountInstances();
                Integer addNum = param.getBatchInsertAssetsList().size();
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("model_manage", count, addNum);
                if (responseBase.getRtnCode() != 200) {
                    throw new Exception("该模块新增数量已达许可数量上限！");
                }
                MwModelInfoDTO dto = null;
                if (param.getModelId() != null) {
                    dto = mwModelInstanceDao.getModelIndexInfo(param.getModelId());
                    if (dto == null || Strings.isNullOrEmpty(dto.getModelIndex())) {
                        throw new Exception("请选择对应模型");
                    }
                    if (dto != null && dto.getModelIndex() != null) {
                        param.setModelIndex(dto.getModelIndex());
                    }
                    if (dto != null && dto.getModelName() != null) {
                        param.setModelName(dto.getModelName());
                    }
                }
                //数据转换
                List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
                for (BatchAddMwModelInstanceParam addParam : param.getBatchInsertAssetsList()) {
                    AddAndUpdateModelInstanceParam addModelInstanceParam = new AddAndUpdateModelInstanceParam();
                    addModelInstanceParam.setInstanceName(addParam.getInstanceName());
                    addModelInstanceParam.setModelId(param.getModelId());
                    addModelInstanceParam.setModelName(param.getModelName());
                    addModelInstanceParam.setModelIndex(param.getModelIndex());
                    addModelInstanceParam.setUserIds(param.getUserIds());
                    addModelInstanceParam.setGroupIds(param.getGroupIds());
                    addModelInstanceParam.setOrgIds(param.getOrgIds());
                    List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                    getPowerParamMethods(addParam, propertiesList);
                    addModelInstanceParam.setPropertiesList(propertiesList);
                    instanceInfoList.add(addModelInstanceParam);
                }

                //数据库为oracle,先调用序列将实体类InstanceId赋值，再批量插入
                if (DATACHECK.equals(DATEBASEORACLE)) {
                    synchronized (this) {
                        mwModelInstanceDao.increaseInstanceIdSeq(instanceInfoList.size() + 1);
                        Integer lastInstanceId = mwModelInstanceDao.getInstanceIdSeq();
                        mwModelInstanceDao.increaseInstanceIdSeq(1);
                        Integer start = lastInstanceId - instanceInfoList.size() + 1;
                        for (int i = 0; i < instanceInfoList.size(); i++) {
                            AddAndUpdateModelInstanceParam modelInstanceParam = instanceInfoList.get(i);
                            modelInstanceParam.setInstanceId(start + i);
                        }
                    }
                }
                synchronized (this) {
                    //先向数据库中插入instance名称
                    mwModelInstanceDao.insertInstanceName(instanceInfoList);
                }
                //设置权限
                BatchUpdatePowerParam powerParam = new BatchUpdatePowerParam();
                powerParam.setUserIds(param.getUserIds());
                powerParam.setGroupIds(param.getGroupIds());
                powerParam.setOrgIds(param.getOrgIds());
                powerParam.setType(DataType.INSTANCE_MANAGE.getName());
                List<MwModelInstanceParam> mwModelInstanceParamList = new ArrayList<>();

                for (AddAndUpdateModelInstanceParam addAndUpdateModelInstanceParam : instanceInfoList) {
                    MwModelInstanceParam mwModelInstanceParam = new MwModelInstanceParam();
                    mwModelInstanceParam.setInstanceId(addAndUpdateModelInstanceParam.getInstanceId());
                    instanceIdList.add(addAndUpdateModelInstanceParam.getInstanceId());
                    mwModelInstanceParamList.add(mwModelInstanceParam);
                    //将InstanceId的值赋值给BatchAddMwModelInstanceParam
                    for (BatchAddMwModelInstanceParam addParam : param.getBatchInsertAssetsList()) {
                        if (addParam.getInstanceName().equals(addAndUpdateModelInstanceParam.getInstanceName())) {
                            addParam.setModelInstanceId(addAndUpdateModelInstanceParam.getInstanceId());
                        }
                    }
                }
                powerParam.setInstanceParams(mwModelInstanceParamList);
                batchEditorPower(powerParam);

                batchAddInstanceToEs(instanceInfoList);
                String alertStr = "";
                if (param.isManage()) {
                    //是否纳管资产
                    alertStr = batchAddAssetsToZabbix(param);
                }
                //模型实例变更记录
                if (CollectionUtils.isNotEmpty(instanceInfoList)) {
                    List<SystemLogDTO> systemLogDTOList = new ArrayList<>();
                    for (AddAndUpdateModelInstanceParam modelInstanceParam : instanceInfoList) {
                        //新增记录
                        SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_INSTANCE.getName())
                                .objName(modelInstanceParam.getModelName() == null ? modelInstanceParam.getInstanceName() : param.getModelName() + "/" + modelInstanceParam.getInstanceName())
                                .operateDes(OperationTypeEnum.CREATE_INSTANCE.getName() + ":" + modelInstanceParam.getInstanceName()).operateDesBefore("").type("instance_" + modelInstanceParam.getInstanceId()).version(1).build();
                        //添加到系统操作日志
                        mwlogger.info(JSON.toJSONString(builder));
                        systemLogDTOList.add(builder);
                    }
                    //添加到模型管理日志
                    mwModelSysLogService.batchSaveInstaceChangeHistory(systemLogDTOList);
                }
                //重复数据告警提示
                if (!Strings.isNullOrEmpty(alertStr)) {
                    return Reply.warn(alertStr);
                }
            }
        } catch (Throwable throwable) {
            try {
                deleteEsInfoByQuery(Arrays.asList(param.getModelIndex()), instanceIdList);
            } catch (Exception e) {
                log.error("新增实例出错时删除es数据失败:{}", e);
            }
            log.error("fail to creatModelInstance with auParam={}, cause:{}", param, throwable);
            throw new RuntimeException("新增实例出错时删除es数据失败");
        }
        return Reply.ok();
    }

    private void batchAddInstanceToEs(List<AddAndUpdateModelInstanceParam> paramList) {
        BulkRequest bulkRequest = new BulkRequest();
        SimpleDateFormat UTC2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UTC2.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        List<Integer> instanceIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paramList)) {
            //获取所有模型的groupNodes
            List<MwModelManageDTO> groupNodeList = mwModelManageDao.getModelGroupNodes();
            Map<String, String> groupNodeMap = new HashMap();
            for (MwModelManageDTO manageDTO : groupNodeList) {
                groupNodeMap.put(manageDTO.getModelIndex(), manageDTO.getGroupNodes());
            }
            for (AddAndUpdateModelInstanceParam param : paramList) {
                instanceIdList.add(param.getInstanceId());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(USER_IDS, param.getUserIds());
                hashMap.put(ORG_IDS, param.getOrgIds());
                hashMap.put(GROUP_IDS, param.getGroupIds());
                param.getPropertiesList().forEach(properties -> {
                            if (properties.getPropertiesType() != null && (!Strings.isNullOrEmpty(properties.getPropertiesValue()))) {
                                if (properties.getPropertiesType() != null) {
                                    Integer type = properties.getPropertiesType();
                                    ModelPropertiesType modelType = ModelPropertiesType.getTypeByCode(type);
                                    if (null != modelType && !Strings.isNullOrEmpty(properties.getPropertiesValue())) {
                                        hashMap.put(properties.getPropertiesIndexId(), modelType.convertToEsData(properties.getPropertiesValue()));
                                    }

                                    if (MWMACROS_DTO.equals(properties.getPropertiesIndexId())) {
                                        //宏值转换
                                        macrosConvert(properties, hashMap);
                                    }
                                }
                            }
                        }
                );
                //基础字段设置
                settingBaseByEsField(hashMap, param, groupNodeMap);
                JSONObject json = (JSONObject) JSONObject.toJSON(hashMap);
                IndexRequest newRequest = new IndexRequest(param.getModelIndex()).id(param.getModelIndex() + param.getInstanceId()).source(json);
                bulkRequest.add(newRequest);
            }
        }

        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                if (bulkItemResponses.hasFailures()) {
                    //新增失败时，删除插入数据库的实例数据
                    if (CollectionUtils.isNotEmpty(instanceIdList)) {
                        mwModelManageDao.deleteModelInstances(instanceIdList);
                    }
                    log.error("异步执行批量添加模型insertModelInstanceProperties属性到es中失败");
                    throw new ModelManagerException("异步执行批量添加模型属性到es中失败");
                }
            }

            @Override
            public void onFailure(Exception e) {
                //新增失败时，删除插入数据库的实例数据
                if (CollectionUtils.isNotEmpty(instanceIdList)) {
                    mwModelManageDao.deleteModelInstances(instanceIdList);
                }
                log.error("异步执行批量添加模型属性到es中失败");
                throw new ModelManagerException("异步执行批量添加模型属性到es中失败");
            }
        });
    }

    public String batchAddAssetsToZabbix(BatchAddModelInstanceParam param) throws Exception {
        List<AddUpdateTangAssetsParam> addParams = new ArrayList<>();
        String alertStr = "";
        Integer monitorMode = 0;
        monitorMode = param.getManageParam().getMonitorMode();
        Map<String, List<Map<String, Object>>> groupListMap = batchInstanceAssetsCheckByMonitorModeAndIP(monitorMode);
        StringBuffer errorInfo = new StringBuffer("以下资产纳管失败:");
        boolean hasError = false;
        for (BatchAddMwModelInstanceParam instanceParam : param.getBatchInsertAssetsList()) {
            AddUpdateTangAssetsParam assetsParam = new AddUpdateTangAssetsParam();
            BeanUtils.copyProperties(param.getManageParam(), assetsParam);
            assetsParam.setHostName(instanceParam.getInstanceName());
            assetsParam.setInBandIp(instanceParam.getInBandIp());
            if (groupListMap != null && groupListMap.size() > 0) {
                List<Map<String, Object>> list = groupListMap.get(assetsParam.getMonitorMode() + "_" + assetsParam.getInBandIp());
                if (CollectionUtils.isNotEmpty(list)) {
                    errorInfo.append(assetsParam.getInBandIp()).append(";");
                    hasError = true;
                    continue;
                }
            }
            assetsParam.setAssetsTypeId(param.getModelGroupId());
            assetsParam.setAssetsTypeSubId(param.getModelId());
            assetsParam.setInstanceId(instanceParam.getModelInstanceId());
            assetsParam.setUserIds(param.getUserIds());
            assetsParam.setOrgIds(param.getOrgIds());
            assetsParam.setGroupIds(param.getGroupIds());
            assetsParam.setInstanceName(instanceParam.getInstanceName());
            assetsParam.setMonitorFlag(assetsParam.getMonitorFlag() != null ? assetsParam.getMonitorFlag() : false);
            assetsParam.setOperationMonitor(assetsParam.getOperationMonitor() != null ? assetsParam.getOperationMonitor() : false);
            assetsParam.setLogManage(assetsParam.getLogManage() != null ? assetsParam.getLogManage() : false);
            assetsParam.setAutoManage(assetsParam.getAutoManage() != null ? assetsParam.getAutoManage() : false);
            assetsParam.setPropManage(assetsParam.getPropManage() != null ? assetsParam.getPropManage() : false);
            addParams.add(assetsParam);
        }
        //批量创建zabbix主机
        mwModelAssetsByESService.batchAddModelAssetsByModelView(addParams);
        //有重复数据
        if (hasError) {
            log.warn(errorInfo.toString());
            alertStr = errorInfo.toString();
        }
        return alertStr;
    }


    public Map<String, List<Map<String, Object>>> batchInstanceAssetsCheckByMonitorModeAndIP(Integer monitorMode) throws IOException {
        QueryInstanceModelParam qParam = new QueryInstanceModelParam();
        qParam.setIsBaseData(true);
        mwModelViewServiceImpl.getInstanceListData(qParam);
        //获取对应的es实例数据
        qParam.setPageSize(pageSize);

        SearchRequest searchRequest = new SearchRequest(String.join(",", qParam.getModelIndexs()));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 创建一个bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 添加一个match查询来限制字段值
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("monitorMode", monitorMode + "");
        boolQueryBuilder.must(matchQueryBuilder);

        // 添加一个exists查询来限制必须存在的字段
        ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery("inBandIp");
        boolQueryBuilder.filter(existsQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> listMap = new ArrayList<>();

        //基础设备下的所有实例id
        List<ModelInstanceBaseInfoDTO> baseInstanceInfoList = mwModelViewDao.getModelIndexANDInstanceInfo(true);
        List<Integer> baseInstanceIds = baseInstanceInfoList.stream().map(s -> s.getInstanceId()).collect(Collectors.toList());

        // 处理查询结果
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            sourceAsMap.put(ESID, searchHit.getId());
            Integer instanceId = intValueConvert(sourceAsMap.get(INSTANCE_ID_KEY));
            if (baseInstanceIds.contains(instanceId)) {
                //mysql数据库中实例必须存在
                listMap.add(sourceAsMap);
            }
        }
        Map<String, List<Map<String, Object>>> groupListMap = listMap.stream().collect(Collectors.groupingBy(s -> s.get("monitorMode") + "_" + s.get("inBandIp")));
        return groupListMap;
    }

    private void macrosConvert(AddModelInstancePropertiesParam properties, HashMap<String, Object> hashMap) {
        List<ModelMacrosParam> macros = JSONArray.parseArray(properties.getPropertiesValue(), ModelMacrosParam.class);
        if (CollectionUtils.isNotEmpty(macros)) {
            for (ModelMacrosParam ma : macros) {
                String value = Strings.isNullOrEmpty(ma.getValue()) ? ma.getMacroVal() : ma.getValue();
                String macro = ma.getMacro();
                Integer macroType = ma.getMacroType() != null ? ma.getMacroType() : ma.getType();
                //type为1，表示密文类型，值得长度低于128位，默认为原始值，需要加密处理
                if (macroType == 1 && value.length() <= 128) {
                    value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                }
                if (macro.startsWith("{$") && macro.endsWith("}")) {
                    if (macro.indexOf("_") != -1) {
                        macro = macro.substring(macro.lastIndexOf("_") + 1, macro.length() - 1);
                    } else {
                        macro = macro.substring(macro.lastIndexOf("$") + 1, macro.length() - 1);
                    }
                }
                hashMap.put(macro, value);
            }
        }
    }

    /**
     * @param modelId
     * @return
     */
    @Override
    public Reply getModelInfoParamById(Integer modelId) {
        MwModelInfoParam dto = new MwModelInfoParam();
        try {
            dto = mwModelInstanceDao.getModelInfoParamById(modelId);
            if (!Strings.isNullOrEmpty(dto.getNodes())) {
                List<String> list = Arrays.asList(dto.getNodes().substring(1).split(","));
                List<Integer> listInts = list.stream().map(Integer::parseInt).collect(Collectors.toList());
                dto.setModelGroupIdList(listInts);
            } else {
                dto.setModelGroupIdList(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("getModelInfoParamById to fail", e);
        }
        return Reply.ok(dto);

    }


    /**
     * @param modelId
     * @return
     */
    @Override
    public Reply getModelPropertiesById(Integer modelId) {
        if (modelId == null || modelId.intValue() == 0) {
            return Reply.warn("请选择正确的模型数据");
        }
        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(modelId);
        List<ModelPropertiesTypeParam> list = new ArrayList<>();
        if (null != modelInfoList) {
            for (ModelInfo modelInfo : modelInfoList) {
                if (null != modelInfo.getPropertyInfos()) {
                    for (PropertyInfo propertyInfo : modelInfo.getPropertyInfos()) {
                        ModelPropertiesTypeParam param = new ModelPropertiesTypeParam();
                        param.setPropertyIndex(propertyInfo.getIndexId());
                        param.setPropertyType(propertyInfo.getPropertiesTypeId());
                        list.add(param);
                    }
                }
            }
        }
        return Reply.ok(list);
    }

    /**
     * web监测实例新增
     *
     * @param param
     */
    public void insertWebMonitorInstance(AddAndUpdateModelInstanceParam param) throws Exception {

        List<AddModelInstancePropertiesParam> propertyList = param.getPropertiesList();
        Map<String, Object> map = propertyList.stream().filter(s -> !Strings.isNullOrEmpty(s.getPropertiesIndexId())).collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> propertyTypeConvert(s.getPropertiesType(), s.getPropertiesValue()), (
                value1, value2) -> {
            return value2;
        }));

        AddAndUpdateModelWebMonitorParam webMonitorParam = JSONObject.parseObject(JSONObject.toJSONString(map), AddAndUpdateModelWebMonitorParam.class);
        //往zabbix中新增web监测服务
        AddAndUpdateModelWebMonitorParam insertParam = mwModelWebMonitorService.createWebSeverData(webMonitorParam);
        //将insertParam转为AddAndUpdateModelInstanceParam参数，插入es
        if (insertParam == null) {
            throw new Exception("新增web监测服务失败");
        }
        Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(insertParam));
        m.putAll(map);
        //通过匹配模型名称，获取对应的模型数据
        List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
        String modelIndex = "";
        Integer modelId = 0;
        String modelName = "";
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(webMonitorModeId);
        List<PropertyInfo> propertyInfos = new ArrayList<>();
        for (ModelInfo modelInfo : modelInfos) {
            if (modelInfo.getModelId().intValue() == webMonitorModeId.intValue()) {
                //获取模型本体信息
                modelIndex = modelInfo.getModelIndex();
                modelId = modelInfo.getModelId();
                modelName = modelInfo.getModelName();
            }
            propertyInfos.addAll(modelInfo.getPropertyInfos());
        }

        //通过匹配模型名称，获取对应的模型数据
        //虚拟化设备的type和模型的name相同，则该设备加入到模型中
        if (propertyInfos != null && propertyInfos.size() > 0) {
            for (PropertyInfo propertyInfo : propertyInfos) {
                AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                instanceParam.extractFromPropertyInfo(propertyInfo);
                //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
//                if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
//                    instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
//                    instanceParam.setPropertiesValue(param.getInstanceName());
//                    instanceParam.setPropertiesType(1);
//                }
                propertiesParamLists.add(instanceParam);
            }
        }
        //数据组装成AddAndUpdateModelInstanceParam
        param.setModelIndex(modelIndex);
        param.setModelId(modelId);
        param.setModelName(modelName);
        param.setInstanceType(DataType.INSTANCE_MANAGE.getName());
        param.setInstanceName(param.getInstanceName());
        param.setInstanceId(param.getInstanceId());
        param.setPropertiesList(propertiesParamLists);

    }


    /**
     * 批量新增web监测实例新增
     *
     * @param paramList
     */
    public List<AddAndUpdateModelInstanceParam> batchInsertWebMonitorInstance(List<AddAndUpdateModelInstanceParam> paramList) throws Exception {

        List<AddAndUpdateModelWebMonitorParam> webMonitorParamList = new ArrayList<>();
        for (AddAndUpdateModelInstanceParam param : paramList) {
            List<AddModelInstancePropertiesParam> propertyList = param.getPropertiesList();
            Map<String, Object> map = propertyList.stream().filter(s -> !Strings.isNullOrEmpty(s.getPropertiesIndexId())).collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> propertyTypeConvert(s.getPropertiesType(), s.getPropertiesValue()), (
                    value1, value2) -> {
                return value2;
            }));
            AddAndUpdateModelWebMonitorParam webMonitorParam = JSONObject.parseObject(JSONObject.toJSONString(map), AddAndUpdateModelWebMonitorParam.class);
            webMonitorParamList.add(webMonitorParam);
        }

        List<AddAndUpdateModelWebMonitorParam> insertParamList = new ArrayList<>();
        //往zabbix中新增web监测服务
        if (CollectionUtils.isNotEmpty(webMonitorParamList)) {
            insertParamList = mwModelWebMonitorService.batchCreateWebSeverData(webMonitorParamList);
        }

        //将insertParam转为AddAndUpdateModelInstanceParam参数，插入es
        if (CollectionUtils.isEmpty(insertParamList)) {
            throw new Exception("批量新增web监测服务失败");
        }
        List<AddAndUpdateModelInstanceParam> listParam = new ArrayList<>();
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(webMonitorModeId);
        for (AddAndUpdateModelWebMonitorParam insertParam : insertParamList) {
            AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
            Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(insertParam));
            //通过匹配模型名称，获取对应的模型数据
            List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
            String modelIndex = "";
            Integer modelId = 0;
            String modelName = "";

            List<PropertyInfo> propertyInfos = new ArrayList<>();
            for (ModelInfo modelInfo : modelInfos) {
                if (modelInfo.getModelId().intValue() == webMonitorModeId.intValue()) {
                    //获取模型本体信息
                    modelIndex = modelInfo.getModelIndex();
                    modelId = modelInfo.getModelId();
                    modelName = modelInfo.getModelName();
                }
                propertyInfos.addAll(modelInfo.getPropertyInfos());
            }

            //通过匹配模型名称，获取对应的模型数据
            //虚拟化设备的type和模型的name相同，则该设备加入到模型中
            if (propertyInfos != null && propertyInfos.size() > 0) {
                for (PropertyInfo propertyInfo : propertyInfos) {
                    AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                    instanceParam.extractFromPropertyInfo(propertyInfo);
                    //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                    instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
//                if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
//                    instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
//                    instanceParam.setPropertiesValue(param.getInstanceName());
//                    instanceParam.setPropertiesType(1);
//                }
                    propertiesParamLists.add(instanceParam);
                }
            }
            //数据组装成AddAndUpdateModelInstanceParam
            param.setModelIndex(modelIndex);
            param.setModelId(modelId);
            param.setModelName(modelName);
            param.setInstanceType(DataType.INSTANCE_MANAGE.getName());
            param.setInstanceName(param.getInstanceName());
            param.setInstanceId(param.getInstanceId());
            param.setPropertiesList(propertiesParamLists);
            listParam.add(param);
        }

        return listParam;

    }


    private Object propertyTypeConvert(Integer propertiesType, String value) {
        Object convertVal = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(value)) {
            try {
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertiesType);
                switch (type) {
                    case DATE:
                        convertVal = dateFormat.parse(value);
                        break;
                    case ORG:
                    case USER:
                    case GROUP:
                    case MULTIPLE_RELATION:
                        convertVal = JSONArray.parse(value);
                        break;
                    case INTEGER:
                        convertVal = Integer.parseInt(value);
                        break;
                    case SWITCH:
                        convertVal = Boolean.valueOf(value);
                        break;
                    case STRING:
                    case SINGLE_RELATION:
                    case IP:
                    case SINGLE_ENUM:
                        convertVal = value;
                        break;
                    default:
                }
            } catch (Exception e) {
                log.info("模型属性字段类型转换失败", e);
                return null;
            }
        }
        return convertVal;
    }

    /**
     * 查询所有监控项信息
     *
     * @return
     */
    @Override
    public Reply selectAllModelMonitorItem() {
        return Reply.ok(mwModelInstanceDao.selectAllMonitorItem());
    }

    @Override
    public Reply settingConfigPowerByIp(SettingConfigPowerParam param) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("settingConfigPower");
            stringBuilder.append("::");
            String key = stringBuilder.append("MwModel").toString();
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(param));
        } catch (Exception e) {
            log.error("根据Ip回显权限数据设置失败", e);
        }
        return Reply.ok();
    }

    @Override
    public Reply getSettingConfigPowerByIp() {
        SettingConfigPowerParam param = new SettingConfigPowerParam();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("settingConfigPower");
            stringBuilder.append("::");
            String key = stringBuilder.append("MwModel").toString();
            String hString = redisTemplate.opsForValue().get(key);
            if (null != hString && StringUtils.isNotEmpty(hString) && !"null".equals(hString) && !"[]".equals(hString)) {
                param = JSONObject.parseObject(hString, SettingConfigPowerParam.class);
            }
        } catch (Exception e) {
            log.error("根据Ip回显权限数据设置失败", e);
            return Reply.fail(500, "根据Ip回显权限数据设置失败");
        }
        return Reply.ok(param);
    }

    public EmailFrom selectEmailFrom() {
        return mwModelInstanceDao.selectEmailFrom(ruleName);
    }


    public String[] selectAccepts(HashSet<Integer> userIds, List<MWUser> userInfoList) {
        List<String> emails = new ArrayList<>();
        for (MWUser mwUser : userInfoList) {
            if (userIds.contains(mwUser.getUserId())) {
                emails.add(mwUser.getEmail());
            }
        }
        List<String> sendEmails = emails.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
        String[] tos = sendEmails.toArray(new String[sendEmails.size()]);
        log.info("用户邮箱：" + sendEmails);
        return tos;
    }

    public List<String> emailPattern(List<String> email) {
        String ruleEmail = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        Pattern emailPattern = Pattern.compile(ruleEmail);
        List<String> result = new ArrayList();
        if (CollectionUtils.isNotEmpty(email)) {
            for (String mail : email) {
                if (StringUtils.isEmpty(mail)) {
                    continue;
                }
                Matcher m = emailPattern.matcher(mail);
                if (!m.matches()) {
                    log.warn("error mail:" + mail);
                    continue;
                }
                result.add(mail);
            }
        }
        return result;

    }


    public String exportForExcelBySendEmail(ModelExportSendEmailParam param) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        String path = param.getFieldPath();
        String fileUrl = "";
        List<String> lableName = new ArrayList<>();
        List<String> lable = new ArrayList<>();
        List<String> relationIndexIds = new ArrayList<>();
        Map<String, Integer> map = new HashMap();
        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(param.getModelId());
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (ModelInfo modelInfo : modelInfoList) {
            if (null != modelInfo.getPropertyInfos()) {
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
        }
        for (PropertyInfo m : propertyInfoList) {
            map.put(m.getIndexId(), m.getPropertiesTypeId());
            if (m.getIsShow() != null && m.getIsShow()) {
                lableName.add(m.getPropertiesName());
                lable.add(m.getIndexId());
            }

        }
        Set<Integer> relatioinInstanceIds = new HashSet<>();
        Pattern pattern = Pattern.compile("^[+]?[\\d]*$");

        if (CollectionUtils.isNotEmpty(param.getListMap())) {
            listMap = param.getListMap();
            //循环获取所有外部关联的实例Id
            for (String relationIndexId : relationIndexIds) {
                for (Map<String, Object> m : param.getListMap()) {
                    if (m.get(relationIndexId) != null && !org.elasticsearch.common.Strings.isNullOrEmpty(m.get(relationIndexId).toString())) {
                        boolean isNum = pattern.matcher(m.get(relationIndexId).toString()).matches();
                        if (isNum) {
                            relatioinInstanceIds.add(Integer.valueOf(m.get(relationIndexId).toString()));
                        }
                    }
                }
            }
        }

        List<QueryInstanceParam> instanceParams = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(relatioinInstanceIds)) {
            List<List<Integer>> instanceIdGroups = new ArrayList<>();
            instanceIdGroups = Lists.partition(new ArrayList<>(relatioinInstanceIds), insBatchFetchNum);
            if (null != instanceIdGroups) {
                for (List<Integer> instanceIdList : instanceIdGroups) {
                    instanceParams.addAll(mwModelInstanceDao.getInstanceNameByIds(instanceIdList));
                }
            }
        }
        //将外部关联的实例Id作为key，name为value
        Map<String, String> relationMap = instanceParams.stream().collect(Collectors.toMap(s -> s.getModelInstanceId() != null ? s.getModelInstanceId().toString() : "0", s -> s.getInstanceName(), (
                value1, value2) -> {
            return value2;
        }));

        //监控服务关联类型字段值转换
        mwModelCommonServiceImpl.monitorServerRelationConvert(listMap);

        //获取所有用户信息
        List<MwModelViewTreeDTO> orgNameAllList = mwModelExportDao.getOrgNameAllByExport();
        Map orgMap = new HashMap();
        for (MwModelViewTreeDTO dto : orgNameAllList) {
            orgMap.put(dto.getId(), dto.getName());
        }
        //获取所有用户信息
        List<MwModelViewTreeDTO> userNameAllList = mwModelExportDao.getUserNameAllByExport();
        Map userMap = new HashMap();
        for (MwModelViewTreeDTO dto : userNameAllList) {
            userMap.put(dto.getId(), dto.getName());
        }
        //获取所有用户组信息
        List<MwModelViewTreeDTO> groupNameAllList = mwModelExportDao.getGroupNameAllByExport();
        Map groupMap = new HashMap();
        for (MwModelViewTreeDTO dto : groupNameAllList) {
            groupMap.put(dto.getId(), dto.getName());
        }

        //格式转换 List<Map<String, Object>> -> List<Map>
        List<Map> mapList = new ArrayList<>();
        for (Map<String, Object> m : listMap) {
            //对es的数据循环匹配获取属性type，查询用户组，机构，用户的名称
            //es中存储的是该项的id值
            if (!m.containsKey(MONITOR_FLAG)) {//监控状态没有字段的，设置一个默认值
                m.put(MONITOR_FLAG, false);
            }
            Map finalMap = map;
            m.forEach((k, v) -> {
                //机构/部门
                if (finalMap.get(k) != null) {
                    //外部关联(单选)类型
                    if (String.valueOf(SINGLE_RELATION.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof String) {
                            String code = v.toString();
                            label = relationMap.get(code);
                        }
                        m.put(k, label);
                    }
                    //外部关联(多选)类型
                    if (String.valueOf(MULTIPLE_RELATION.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof List) {
                            List<String> list = (List) v;
                            label = "";
                            for (String str : list) {
                                label += relationMap.get(str) + "/";
                            }
                            if (label.length() > 1) {
                                label = label.substring(0, label.length() - 1);
                            }
                        }
                        if (v instanceof String) {
                            label = (String) v;
                        }

                        m.put(k, label);
                    }
                    //多选枚举型
                    if (String.valueOf(ModelPropertiesType.MULTIPLE_ENUM.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof List) {
                            List list = (List) v;
                            label = Joiner.on(",").join(list);
                        }
                        if (v instanceof String) {
                            label = (String) v;
                        }
                        m.put(k, label);
                    }
                    //机构
                    if (String.valueOf(ModelPropertiesType.ORG.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof List) {
                            List<List> list = (List) v;
                            for (int i = 0, len = list.size(); i < len; i++) {
                                List<Integer> list1 = list.get(i);
                                if (CollectionUtils.isNotEmpty(list1) && orgMap != null) {
                                    Integer orgId = list1.get(list1.size() - 1);
                                    if (orgMap.get(orgId + "") != null) {
                                        label += orgMap.get(orgId + "") + "/";
                                    }
                                }
                            }
                            if (label.length() > 0) {
                                label = label.substring(0, label.length() - 1);
                            }
                        }
                        if (v instanceof String) {
                            label = (String) v;
                        }

                        m.put(k, label);
                    }
                    //负责人
                    if (String.valueOf(ModelPropertiesType.USER.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof List) {
                            List<Integer> list = (List) v;
                            for (Integer userId : list) {
                                if (userMap != null && userMap.get(userId + "") != null) {
                                    label += userMap.get(userId + "") + "/";
                                }
                            }
                            if (label.length() > 0) {
                                label = label.substring(0, label.length() - 1);
                            }
                        }
                        if (v instanceof String) {
                            label = (String) v;
                        }
                        m.put(k, label);
                    }
                    //用户组
                    if (String.valueOf(ModelPropertiesType.GROUP.getCode()).equals(finalMap.get(k).toString())) {
                        String label = "";
                        if (v instanceof List) {
                            List<Integer> list = (List) v;
                            for (Integer groupId : list) {
                                if (groupMap != null && groupMap.get(groupId + "") != null) {
                                    label += groupMap.get(groupId + "") + "/";
                                }
                            }
                            if (label.length() > 0) {
                                label = label.substring(0, label.length() - 1);
                            }
                        }
                        if (v instanceof String) {
                            label = (String) v;
                        }
                        m.put(k, label);
                    }
                    //机房位置 机房数据格式为List
                    if (String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()).equals(finalMap.get(k).toString()) && POSITIONBYROOM.getField().equals(k)) {
                        String index = "";
                        if (v instanceof List && ((List) v).size() > 0) {
                            List list = (List) v;
                            if (list.size() > 1) {
                                //行
                                Integer row = Integer.valueOf(list.get(0).toString());
                                //列
                                Integer col = Integer.valueOf(list.get(1).toString());
                                index = "第" + (row + 1) + "行第" + (col + 1) + "列";
                            }
                        }
                        m.put(k, index);
                    }
                    //机柜位置
                    if (String.valueOf(ModelPropertiesType.LAYOUTDATA.getCode()).equals(finalMap.get(k).toString()) && POSITIONBYCABINET.getField().equals(k)) {
                        String index = "";
                        //机柜数据格式为Map
                        if (v instanceof Map && ((Map) v).size() > 0) {
                            Map mapInfo = (Map) v;
                            Integer startIndex = Integer.valueOf(mapInfo.get("start").toString());
                            Integer endIndex = Integer.valueOf(mapInfo.get("end").toString());
                            if (endIndex > startIndex) {
                                index = "第" + (startIndex + 1) + "-" + (endIndex + 1) + "层";
                            } else {
                                index = "第" + (startIndex + 1) + "层";
                            }
                        }
                        m.put(k, index);
                    }
                }
            });
            mapList.add(m);
        }
        try {
            fileUrl = ExportExcelUtil.exportExcel("许可到期提醒", "许可到期提醒", lableName, lable, mapList, path, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            log.error("exportForExcel{}", e);
        }
        return fileUrl;
    }

    public void configEditorDataBase(AddAndUpdateModelInstanceParam param) {
        try {
            MwModelEditorDataBaseConfigParam configParam = new MwModelEditorDataBaseConfigParam();
            Map<String, String> map = param.getPropertiesList().stream().filter(s -> !Strings.isNullOrEmpty(s.getPropertiesIndexId()) && s.getPropertiesValue() != null).collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> s.getPropertiesValue(), (
                    value1, value2) -> {
                return value2;
            }));
            configParam = JSON.parseObject(JSON.toJSONString(map), MwModelEditorDataBaseConfigParam.class);
            // 1、首先远程连接ssh
            SSHRemoteCall.getInstance().sshRemoteCallLogin(dataBaseIp, dataBaseUserName, dataBasePassword, dataBasePort);
            // 打印信息
            log.info("连接ip地址: " + dataBaseIp + ",账号: " + dataBaseUserName + ",连接成功.....");

            // 2、下载文件
            // src 是linux服务器文件地址,dst 本地存放地址,采用默认的传输模式：OVERWRITE
            //test为文件名称哈
            String src = remoteFilePath + configFileName;//远程服务器上的文件路径和文件名
            String dst = localFilePath;//下载到本地的文件路径
            String localFileUrl = localFilePath + configFileName;
            File file = new File(localFileUrl);
            //判断是否存在指定文件夹，不存在新建
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            SSHRemoteCall.getInstance().fileDownload(src, dst);
            //3读取并写入文件
            SSHRemoteCall.getInstance().readWriteFiles(localFileUrl, configParam);

            // 4、上传文件
            String localFile = localFilePath + configFileName;// 本地文件名
            SSHRemoteCall.getInstance().uploadFile(localFile, src);

            // 5、关闭连接
            SSHRemoteCall.getInstance().closeSession();
        } catch (Exception e) {
            // 打印错误信息
            log.error("远程修改文件失败", e);
        }
    }


    public Map<String, Object> selectInfoByInstanceId(Integer instanceId) throws
            Exception {
        List<Map<String, Object>> listMap = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        if (instanceId != null) {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            QueryBuilder queryBuilder1 = QueryBuilders.termQuery("modelInstanceId", instanceId);
            queryBuilder.must(queryBuilder1);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(pageSize);
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
        }
        if (CollectionUtils.isNotEmpty(listMap)) {
            map = listMap.get(0);
        }
        return map;
    }

    public List<Map<String, Object>> selectInfosByModelId(Integer modelId) throws Exception {
        List<MwModelInstanceCommonParam> instanceInfo = mwModelInstanceDao.selectModelInstanceInfoById(modelId);
        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();
        for (MwModelInstanceCommonParam m : instanceInfo) {
            if (m != null && !Strings.isNullOrEmpty(m.getModelIndex())) {
                modelIndexSet.add(m.getModelIndex());
            }
            if (m != null && m.getModelInstanceId() != null) {
                instanceIdSet.add(m.getModelInstanceId());
            }
        }
        QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
        params.setModelIndexs(new ArrayList<>(modelIndexSet));
        params.setInstanceIds(new ArrayList<>(instanceIdSet));
        List<Map<String, Object>> listMap = mwModelViewServiceImpl.selectInstanceInfoByIdsAndModelIndexs(params);
        return listMap;
    }

    /**
     * 获取所有的基础数据实例信息
     */
    public List<Map<String, Object>> getAllInstanceInfoByBase() {
        //从es中获取所有数据
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        param.setPageSize(pageSize);
        mwModelViewServiceImpl.getInstanceListData(param);
        Map<String, Object> map = mwModelViewServiceImpl.getModelListInfoByBase(param);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if (map != null && map.get("data") != null) {
            listMap = (List<Map<String, Object>>) map.get("data");
        }
        return listMap;
    }


    /**
     * 将数据转换为实例新增类型的数据
     *
     * @param dataList
     * @param modelId
     * @return
     */
    @Override
    public List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId) {
        List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
        try {
            instanceInfoList = convertInstanceInfo(modelId, dataList, null);
        } catch (Exception e) {
            log.error("es插入数据类型转换失败", e);
        }
        return instanceInfoList;
    }

    /**
     * 将数据转换为实例新增类型的数据
     * relationId 关联依赖的实例id
     *
     * @param modelId
     * @param dataList
     * @param relationId
     * @return
     * @throws Exception
     */
    @Override
    public List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId, Integer relationId) {
        List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
        try {
            instanceInfoList = convertInstanceInfo(modelId, dataList, relationId);
        } catch (Exception e) {
            log.error("es插入数据类型转换失败", e);
        }
        return instanceInfoList;
    }


    private List<AddAndUpdateModelInstanceParam> convertInstanceInfo(Integer modelId, List dataList, Integer relationId) throws Exception {
        List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
        List<ModelInfo> modelInfos = mwModelCommonServiceImpl.getModelInfoAndParent(modelId);
        //获取到的rancherInfos数据
        for (Object info : dataList) {
            if (info != null) {
                Map<String, Object> m = new HashMap();
                ModelInfo ownModelInfo = new ModelInfo();
                ObjectMapper objectMapper = new ObjectMapper();
                m = objectMapper.readValue(JSONObject.toJSONString(info), Map.class);
                List<PropertyInfo> propertyInfos = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(modelId)) {
                        ownModelInfo = modelInfo;
                    }
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }
                List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
                if (m.get(INSTANCE_NAME_KEY) != null) {
                    if (CollectionUtils.isNotEmpty(propertyInfos)) {
                        for (PropertyInfo propertyInfo : propertyInfos) {
                            AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                            instanceParam.extractFromPropertyInfo(propertyInfo);
                            instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                            if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId()) && m.get(INSTANCE_NAME_KEY) != null) {
                                instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                                instanceParam.setPropertiesValue(m.get(INSTANCE_NAME_KEY).toString());
                                instanceParam.setPropertiesType(1);
                            }
                            propertiesParamLists.add(instanceParam);
                        }
                        AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                        instanceParam.setModelIndex(ownModelInfo.getModelIndex());
                        instanceParam.setModelId(ownModelInfo.getModelId());
                        instanceParam.setInstanceName(m.get(INSTANCE_NAME_KEY).toString());
                        instanceParam.setInstanceType(DataType.INSTANCE_MANAGE.getName());
                        instanceParam.setPropertiesList(propertiesParamLists);
                        instanceParam.setRelationInstanceId(relationId);
                        if ((!com.alibaba.nacos.shaded.com.google.common.base.Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                            instanceInfoList.add(instanceParam);
                        }
                    }
                }

            }
        }
        return instanceInfoList;
    }

}
