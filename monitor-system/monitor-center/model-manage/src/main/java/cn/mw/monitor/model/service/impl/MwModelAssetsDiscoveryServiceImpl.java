package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.assets.model.MonitorServer;
import cn.mw.monitor.assets.model.TangibleAssetState;
import cn.mw.monitor.assets.utils.ZabbixUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.model.dao.*;
import cn.mw.monitor.model.data.AddUpdModelInstanceContext;
import cn.mw.monitor.model.dto.ModelGroupHosts;
import cn.mw.monitor.model.dto.MwModelViewTreeDTO;
import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.model.param.ModelAssetMonitorState;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelAssestDiscoveryService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelManageService;
import cn.mw.monitor.model.service.MwModelSysLogService;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.assets.api.IMWBatchAssetsProcFinListener;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.assets.event.BatchDeleteAssetsEvent;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.Macros;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.event.AddModelAssetsEvent;
import cn.mw.monitor.service.model.listener.CheckMWModelAssetsListener;
import cn.mw.monitor.service.model.listener.MWModelAssetsListener;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelAssetsByESService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.scan.model.ScanResultFail;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.param.QueryScanResultParam;
import cn.mw.monitor.service.task.SingleThreadExeManage;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.snmp.service.scan.ScanResultType;
import cn.mw.monitor.util.AssetsUtils;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.mw.monitor.api.common.SpringUtils.getBean;
import static cn.mw.monitor.model.service.impl.MwModelViewServiceImpl.*;
import static cn.mw.monitor.service.assets.utils.RuleType.getInfoByName;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author qzg
 * @date 2022/7/12
 */
@Service
@Slf4j
public class MwModelAssetsDiscoveryServiceImpl extends ListenerService implements MwModelAssestDiscoveryService, MwModelAssetsByESService {

    private int pageSize = 10000;

    @Value("${es.duration.timeNum}")
    private int timeNum;

    @Value("${assets.checkNowFlag.batchFetchNum}")
    private int batchFetchNum;
    private static String assetsExist = "EXIST";
    private static String scanSuccessId = "scanSuccessId";
    private static String assetsNoExist = "NOTEXIST";
    static final String LOCALHOST_KEY = "localhost";
    static final String LOCALHOST_NAME = "本机";
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    @Resource
    private MwModelSysLogService mwModelSysLogService;
    @Resource
    private MWModelTemplateDao mwModelTemplateDao;
    @Resource
    private MwModelExportDao mwModelExportDao;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private LicenseManagementService licenseManagement;
    @Autowired
    private ILoginCacheInfo loginCacheInfo;
    @Resource
    private MwModelViewDao mwModelViewDao;
    @Resource
    private MwModelViewServiceImpl mwModelViewService;
    @Resource
    private MwModelManageDao mwModelManageDao;
    //操作日志记录
    private static final Logger mwlogger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwAssetsManager mwAssetsManager;
    @Autowired
    private ModelServer modelSever;
    @Autowired
    private MwModelManageService mwModelManageService;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Resource
    private MwModelAssetsDiscoveryDao mwModelAssetsDiscoveryDao;

    @Autowired
    private ZabbixUtils zabbixUtils;

    @Autowired
    private SingleThreadExeManage singleThreadExeManage;

    @Autowired
    public void addchecks(List<CheckMWModelAssetsListener> checklisteners) {
        for (CheckMWModelAssetsListener checkMWModelAssetsListener : checklisteners) {
            log.info("MwModelAssetsDiscoveryImpl addchecks:" + checkMWModelAssetsListener.getClass().getCanonicalName());
        }
        addCheckLists(checklisteners);
    }

    @Autowired
    public void addpost(List<MWModelAssetsListener> postlisteners) {
        for (MWModelAssetsListener mwModelAssetsListener : postlisteners) {
            log.info("MwModelAssetsDiscoveryImpl add post:" + mwModelAssetsListener.getClass().getCanonicalName());
        }
        addPostProcessorList(postlisteners);
    }

    @Autowired
    public void addFinish(List<IMWBatchAssetsProcFinListener> finishlisteners) {
        for (IMWBatchAssetsProcFinListener mwModelAssetsListener : finishlisteners) {
            log.info("MwModelAssetsDiscoveryImpl add finish:{}", mwModelAssetsListener.getClass().getCanonicalName());
        }
        addFinishProcessorList(finishlisteners);
    }

    @Override
    public Reply addModelAssetsByScanSuccess(ModelAddTangAssetsParam param) throws Throwable {
        Reply replys = null;
        String monitorServerName = "";
        String pollingEngineName = LOCALHOST_NAME;
        long time1 = System.currentTimeMillis();
        //查询扫描成功信息
        List<ScanResultSuccess> list = mwModelAssetsDiscoveryDao.selectSuccessListByIds(param.getScanSuccessId());
        if (param.getMonitorServerId() != null && param.getMonitorServerId() != 0) {
            monitorServerName = mwModelAssetsDiscoveryDao.getMonitorServerName(param.getMonitorServerId());
        }
        if (!Strings.isNullOrEmpty(param.getPollingEngine()) && !LOCALHOST_KEY.equals(param.getPollingEngine())) {
            pollingEngineName = mwModelAssetsDiscoveryDao.getPollingEngineName(param.getPollingEngine());
        }

        //判断是否批量纳管
        Boolean batchManage = false;//默认为批量新增
        if (param.getBatchManage() != null && param.getBatchManage()) {
            batchManage = true;
        }
        log.info("selectSuccessListByIds:" + JSONObject.toJSONString(list));
        List<Integer> monitorModesList = new ArrayList<Integer>();
        //资产是否存在   校验分为两种
        checkModelInstanceIsExists(list);
        if (!batchManage) {//批量新增资产，不纳管
            List<AddUpdateTangAssetsParam> listParam = new ArrayList<>();
            for (ScanResultSuccess scanResultSuccess : list) {
                //先解密信息
                scanResultSuccess.decrSnmpData();
                scanResultSuccess.setUserIds(param.getUserIds());
                scanResultSuccess.setOrgIds(param.getOrgIds());
                scanResultSuccess.setGroupIds(param.getGroupIds());
                AddUpdateTangAssetsParam aParam = new AddUpdateTangAssetsParam();
                tangAssetsParamSetting(scanResultSuccess, aParam);
                listParam.add(aParam);
            }
            if (CollectionUtils.isNotEmpty(listParam)) {
                log.info("进入批量插入es操作：" + listParam);
                //TODO批量插入到es中
                batchInsertAssetsInfoToES(listParam);
                return Reply.ok("批量新增资产成功！");
            }
        }

        for (ScanResultSuccess scanResultSuccess : list) {
            //先解密信息
            scanResultSuccess.decrSnmpData();
            if (!Strings.isNullOrEmpty(scanResultSuccess.getMonitorModeVal())) {
                monitorModesList.add(intValueConvert(scanResultSuccess.getMonitorModeVal()));
            }

            if (StringUtils.isNotEmpty(param.getPollingEngine())) {
                scanResultSuccess.setPollingEngine(param.getPollingEngine());
            }

            if (scanResultSuccess.getScanSuccessIdInAssets() > 0) {
                String tips = scanResultSuccess.getIpAddress() + "资产";
                String msg = Reply.replaceMsg(ErrorConstant.COMMON_MSG_200002, new String[]{tips});
                return Reply.fail(msg);
            }
        }

        long time2 = System.currentTimeMillis();
        //验证,过滤扫描结果
        BatchAddAssetsEvent batchAddAssetsEvent = new BatchAddAssetsEvent();

        batchAddAssetsEvent.setScanResultSuccessList(list);
        batchAddAssetsEvent.setIgnoreCodeCheck(param.isIgnoreCodeCheck());
        List<Reply> replyList = null;

        replyList = publishCheckEvent(batchAddAssetsEvent);
        long time3 = System.currentTimeMillis();
        String validateMsg = null;
        for (Reply reply : replyList) {
            //判断是否返回过滤之后的结果
            if (PaasConstant.RES_FILTER == reply.getRes()) {
                list = (List<ScanResultSuccess>) reply.getData();
            }
            if (PaasConstant.RES_ERROR == reply.getRes()) {
                validateMsg = reply.getMsg();
            }
        }
        Map<String, String> groupMap = new HashMap<String, String>();
        IdentityHashMap<String, MwModelTemplateDTO> assetsOidMap = new IdentityHashMap<>();
        List<MwModelTemplateDTO> modelTemplateList = new ArrayList<>();
        //资产纳管时，获取匹配的zabbix分组id和模板id
        //资产纳管时，获取匹配的templateId
        Map criteria = new HashMap();
        criteria.put("serverId", param.getMonitorServerId());
        if (null != monitorModesList && monitorModesList.size() > 0) {
            criteria.put("monitorModes", monitorModesList);
        }
        modelTemplateList = mwModelTemplateDao.selectListByModel(criteria);
        matchScanGroupIdAndTemplateId(param.getMonitorServerId(), groupMap, assetsOidMap, modelTemplateList);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("以下资产：");
        Boolean isFlag = false;
        long time4 = System.currentTimeMillis();
        List<AddUpdateTangAssetsParam> addParams = new ArrayList<>();

        //模版匹配
        for (ScanResultSuccess scanResultSuccess : list) {
            scanResultSuccess.setMonitorServerId(param.getMonitorServerId() == null ? 0 : param.getMonitorServerId());
            //MonitorServerId有值，则为纳管处理，匹配对应zabbix分组id
            if (param.getMonitorServerId() != null && param.getMonitorServerId() != 0 && batchManage) {//批量纳管进入，不纳管不进入
                String key = AssetsUtils.genGroupKey(scanResultSuccess.getAssetsTypeId(), param.getMonitorServerId());
                String groupId = groupMap.get(key);
                scanResultSuccess.setGroupId(groupId);
                MwModelTemplateInfo param2 = new MwModelTemplateInfo();
                param2.setServerId(param.getMonitorServerId());
                param2.setTemplateId(scanResultSuccess.getTemplateId());
                List<MwModelTemplateInfo> templateInfos = mwModelViewDao.getServerTemplateIdByName(param2);
                //根据模板名称和监控服务器Id，获取模板id
                if (templateInfos != null && templateInfos.size() > 0) {
                    scanResultSuccess.setTemplateId(templateInfos.get(0).getServerTemplateId());
                }else {
                    stringBuffer.append(scanResultSuccess.getIpAddress() + ";");
                    isFlag = true;
                    continue;
                }
            }
        }

        //转换zabbix添加参数
        for (ScanResultSuccess scanResultSuccess : list) {
            scanResultSuccess.setMonitorFlag(param.getMonitorFlag());
            AddUpdateTangAssetsParam param1 = zabbixUtils.tranform(scanResultSuccess);
            param1.setRandomName(param.isRandomName());
            addParams.add(param1);
        }

        //批量创建zabbix主机
        mwTangibleAssetsService.batchCreateAndGetZabbixHostId(addParams);

        long time5 = System.currentTimeMillis();
        stringBuffer.append("没有匹配到模型，纳管时已被忽略");
        //匹配扫描规则配置信息后,在zabbix中批量创建主机,并构造添加资产参数
        //创建资产
        StringBuffer errorInfo = new StringBuffer("以下资产创建assetsId失败:");
        //是否有错误
        boolean hasError = false;

        List<AddUpdateTangAssetsParam> listParam = new ArrayList<>();
        if (null != addParams) {
            String loginName = this.loginCacheInfo.getLoginName();
            List<String> assetsIdList = new ArrayList<>();

            for (AddUpdateTangAssetsParam addParam : addParams) {
                //如果assetsId 为空，直接跳出当前循环。
                if (Strings.isNullOrEmpty(addParam.getAssetsId())) {
                    hasError = true;
                    errorInfo.append(addParam.getInBandIp()).append("：" + addParam.getHostName() + "；");
                    continue;
                }
                assetsIdList.add(addParam.getAssetsId());
                //创建assetsId成功，则表示已经纳管
                addParam.setIsManage(true);
                addParam.setCreator(loginName);
                addParam.setModifier(loginName);
                addParam.setPrincipal(CollectionUtils.isNotEmpty(addParam.getUserIds()) ? addParam.getUserIds() : param.getUserIds());
                addParam.setOrgIds(CollectionUtils.isNotEmpty(addParam.getOrgIds()) ? addParam.getOrgIds() : param.getOrgIds());
                addParam.setGroupIds(CollectionUtils.isNotEmpty(addParam.getGroupIds()) ? addParam.getGroupIds() : param.getGroupIds());
                addParam.setAssetsLabel(param.getAssetsLabel() != null ? param.getAssetsLabel() : new ArrayList<>());
                addParam.setEnable(param.getEnable() != null ? param.getEnable() : "");
                addParam.setCheckNowFlag(param.getCheckNowFlag() != null ? param.getCheckNowFlag() : false);
                addParam.setUserIds(CollectionUtils.isNotEmpty(addParam.getUserIds()) ? addParam.getUserIds() : param.getUserIds());
                addParam.setMonitorFlag(param.getMonitorFlag() != null ? param.getMonitorFlag() : false);
                addParam.setOperationMonitor(param.getOperationMonitor() != null ? param.getOperationMonitor() : false);
                addParam.setLogManage(param.getLogManage() != null ? param.getLogManage() : false);
                addParam.setAutoManage(param.getAutoManage() != null ? param.getAutoManage() : false);
                addParam.setPropManage(param.getPropManage() != null ? param.getPropManage() : false);
                addParam.setPollingEngineName(pollingEngineName);
                if (LOCALHOST_NAME.equals(pollingEngineName)) {
                    addParam.setPollingEngine(LOCALHOST_KEY);
                }
                addParam.setMonitorServerName(monitorServerName);
                //许可校验
                //数量获取
                if (addParam.getTPServerHostName() == null || StringUtils.isEmpty(addParam.getTPServerHostName())) {
                    //设置第三方监控服务器中主机名称
                    addParam.setTPServerHostName(UuidUtil.getUid());
                }
                listParam.add(addParam);
            }
        }
        long time7 = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(listParam)) {
            log.info("进入批量插入es操作：" + listParam);
            //TODO批量插入到es中
            batchInsertAssetsInfoToES(listParam);
        }
        long time8 = System.currentTimeMillis();

        if (hasError) {
            log.warn(errorInfo.toString());
            return Reply.warn(errorInfo.toString());
        }
        if (isFlag) {
            log.warn(stringBuffer.toString());
            return Reply.warn(stringBuffer.toString());
        }

        //发布批量新增事件,放到后台执行
        MwModelAssetsDiscoveryServiceImpl service = getBean(MwModelAssetsDiscoveryServiceImpl.class);
        ModelInstanceAddTask modelBatchAddTask = new ModelInstanceAddTask(batchAddAssetsEvent, service);
        modelBatchAddTask.init(new Date(), loginCacheInfo.getLoginName(), OperationTypeEnum.SYNC_INTERFACE.getName(), OperationTypeEnum.SYNC_INTERFACE.getName());
        singleThreadExeManage.addNoLimitQueueTask(MwModelAssetsDiscoveryServiceImpl.class.getSimpleName()
                , modelBatchAddTask);
        log.info("批量纳管总耗时:" + (time8 - time1) + "ms;资产数量：" + list.size() + "个;");
        log.info("查询扫描成功列表耗时:" + (time2 - time1) + "ms;check验证耗时:" +
                (time3 - time2) + "ms;temp、group数组组装耗时:" + (time4 - time3) + "ms;创建zabbixId耗时:" + (time5 - time4) + "ms;"
                + "批量插入es耗时：" + (time8 - time7) + "ms");
        Reply reply = Reply.ok(batchAddAssetsEvent);
        //只要存在成功的结果信息,就不提示错误信息
        if (StringUtils.isNotEmpty(validateMsg) && list.size() < 1) {
            reply.setRes(PaasConstant.RES_WARN);
            reply.setMsg(validateMsg);
        }
        return reply;
    }


    /**
     * 资产视图批量纳管资产
     *
     * @param addParams
     * @return
     * @throws Throwable
     */
//    @Override
    public Reply batchAddModelAssetsByModelView(List<AddUpdateTangAssetsParam> addParams) throws Exception {

        if (CollectionUtils.isNotEmpty(addParams)) {
            Integer monitorMode = addParams.get(0).getMonitorMode();
            for (AddUpdateTangAssetsParam aParam : addParams) {
                //说明创建不关联zabbix的资产,启用状态设置成未启用
                if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
                    aParam.setEnable(TangibleAssetState.DISACTIVE.name());
                } else {//说明是关联zabbix资产,启用状态设置成启用
                    aParam.setEnable(TangibleAssetState.ACTIVE.name());
                }
                //设置创建时间和创建人
                aParam.setCreator(loginCacheInfo.getLoginName());
                aParam.setModifier(loginCacheInfo.getLoginName());

                //设置资产id为实例id
                aParam.setId(aParam.getInstanceId().toString());

                //判断是否为批量增加资产
                //数据重复校验 即将添加的数据的 资产监控方式(monitor_mode) 和带内ip(in_band_ip) 是否重复，二者有一即重复。
                if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
                    //设置第三方监控服务器中主机名称
                    aParam.setTPServerHostName(UuidUtil.getUid());
                }
                MwModelTemplateInfo param = new MwModelTemplateInfo();
                param.setServerId(aParam.getMonitorServerId());
                if (!Strings.isNullOrEmpty(aParam.getTemplateName())) {
                    param.setTemplateName(aParam.getTemplateName().split("@")[0]);
                }
                List<MwModelTemplateInfo> templateInfos = mwModelViewDao.getServerTemplateIdByName(param);
                //根据模板名称和监控服务器Id，获取模板id
                if (templateInfos != null && templateInfos.size() > 0) {
                    aParam.setTemplateId(templateInfos.get(0).getServerTemplateId());
                }
                //根据模型分组id和监控服务器Id，获取zabbix分组id
                String groupId = mwModelViewDao.getServerGroupId(aParam.getAssetsTypeId(), aParam.getMonitorServerId());
                aParam.setGroupId(groupId);

                //访问zabbix获取主机id, 生成资产id
                aParam.setAssetsName(aParam.getInstanceName());
            }
            mwTangibleAssetsService.batchCreateAndGetZabbixHostId(addParams);
        }

        log.info("batchCreateAndGetZabbixHostId::" + addParams);
        Reply replys = null;
        long time5 = System.currentTimeMillis();
//        stringBuffer.append("没有匹配到模型，纳管时已被忽略");
        //匹配扫描规则配置信息后,在zabbix中批量创建主机,并构造添加资产参数
        //创建资产
        StringBuffer errorInfo = new StringBuffer("以下资产创建assetsId失败:");
        //是否有错误
        boolean hasError = false;
        List<AddUpdateTangAssetsParam> listParam = new ArrayList<>();
        if (null != addParams) {
            String loginName = this.loginCacheInfo.getLoginName();
            List<String> assetsIdList = new ArrayList<>();
            for (AddUpdateTangAssetsParam addParam : addParams) {
                //如果assetsId 为空，直接跳出当前循环。
                if (Strings.isNullOrEmpty(addParam.getAssetsId())) {
                    hasError = true;
                    errorInfo.append(addParam.getInBandIp()).append("：" + addParam.getHostName() + "；");
                    continue;
                }
                assetsIdList.add(addParam.getAssetsId());
                //创建assetsId成功，则表示已经纳管
                addParam.setIsManage(true);
                addParam.setCreator(loginName);
                addParam.setModifier(loginName);
                listParam.add(addParam);
            }
        }
        long time7 = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(listParam)) {
            log.info("进入批量插入es操作：" + listParam);
            //TODO批量插入到es中
            batchEditorssetsInfoToES(listParam);
        }
        long time8 = System.currentTimeMillis();

        if (hasError) {
            log.warn(errorInfo.toString());
            return Reply.warn(errorInfo.toString());
        }


        //发布批量新增事件,放到后台执行
        //验证,过滤扫描结果
        BatchAddAssetsEvent batchAddAssetsEvent = new BatchAddAssetsEvent();
        MwModelAssetsDiscoveryServiceImpl service = getBean(MwModelAssetsDiscoveryServiceImpl.class);
        ModelInstanceAddTask modelBatchAddTask = new ModelInstanceAddTask(batchAddAssetsEvent, service);
        modelBatchAddTask.init(new Date(), loginCacheInfo.getLoginName(), OperationTypeEnum.SYNC_INTERFACE.getName(), OperationTypeEnum.SYNC_INTERFACE.getName());
        singleThreadExeManage.addNoLimitQueueTask(MwModelAssetsDiscoveryServiceImpl.class.getSimpleName()
                , modelBatchAddTask);
        return Reply.ok();
    }

    /**
     * 批量处理 立即执行操作
     */
    public Boolean batchOpenNowItems(int monitorServerId, List<String> hostIds) {
        List<String> itemIds = new ArrayList<>();
        //所有自动发现规则的监控项id
        MWZabbixAPIResult resultDRules = mwtpServerAPI.getDRuleByHostIdList(monitorServerId, hostIds);
        if (!resultDRules.isFail()) {
            JsonNode jsonNodes = (JsonNode) resultDRules.getData();
            if (jsonNodes.size() > 0) {
                for (JsonNode node : jsonNodes) {
                    String itemId = node.get("itemid").asText();
                    itemIds.add(itemId);
                }
            }
        }
        //所有的监控项id
        MWZabbixAPIResult resultItems = mwtpServerAPI.itemGetbyHostIdList(monitorServerId, hostIds);
        if (!resultItems.isFail()) {
            JsonNode jsonNodes = (JsonNode) resultItems.getData();
            if (jsonNodes.size() > 0) {
                for (JsonNode node : jsonNodes) {
                    String itemId = node.get("itemid").asText();
                    itemIds.add(itemId);
                }
            }
        }
        boolean isFlag = false;
        if (CollectionUtils.isNotEmpty(itemIds)) {
            //如果itemIds数量太多,需要分组
            List<List<String>> itemIdsGroups = Lists.partition(itemIds, batchFetchNum);
            for (List<String> itemIdSubList : itemIdsGroups) {
                //将所有的监控项进行是否立即执行 "6"是立即执行；"1"是诊断信息
                MWZabbixAPIResult result = mwtpServerAPI.taskItems(monitorServerId, "6", itemIdSubList);
                if (result != null && !result.isFail()) {
                    JsonNode jsonNode = (JsonNode) result.getData();
                    if (jsonNode.size() > 0) {
                        isFlag = true;
                    }
                } else {
                    isFlag = false;
                }
            }
        }
        return isFlag;
    }


    @Override
    public Reply addModelAssetsByInsert(ModelAddTangAssetsParam param) throws Throwable {


        return null;
    }

    /**
     * 资产扫描成功 数据添加到es
     *
     * @param aParam
     * @param isbatch
     * @return
     */
    @Override
    public Reply doInsertAssetsByES(AddUpdateTangAssetsParam aParam, boolean isbatch) {
        try {
            String errorMessage = "";
            insertAssetsInfoToES(aParam, isbatch, errorMessage);
            if (errorMessage.length() > 0) {
                return Reply.fail(500, "新增失败:" + errorMessage);
            }
            return Reply.ok("新增成功");
        } catch (Throwable throwable) {
            log.error("doInsertAssetsByES {}", throwable);
            return Reply.fail(500, "新增失败");
        }
    }

    /**
     * 批量纳管x修改数据到es
     *
     * @param paramList
     * @throws Throwable
     */
    public void batchEditorssetsInfoToES(List<AddUpdateTangAssetsParam> paramList) throws Exception {
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (AddUpdateTangAssetsParam aParam : paramList) {
                //根据资产id和服务器id判断该实例是否已经纳管
                if (!Strings.isNullOrEmpty(aParam.getAssetsId()) && aParam.getMonitorServerId() != 0) {
                    aParam.setIsManage(true);
                } else {
                    aParam.setIsManage(false);
                }
                Map<String, Object> m = ListMapObjUtils.beanToMap(aParam);
                //宏值获取
                List<Macros> macros = aParam.getMwMacrosDTO();
                if (CollectionUtils.isNotEmpty(macros)) {
                    for (Macros ma : macros) {
                        String value = ma.getValue();
                        //type为1，表示密文类型，值得长度低于32位，默认为原始值，需要加密处理
                        if (ma.getType() == 1 && value.length() <= 32) {
                            value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                        }
                        m.put(ma.getMacro(), value);
                    }
                }
                String checkNowMsg = "";
                //是否启动立即执行
                if (aParam.isCheckNowFlag() && !Strings.isNullOrEmpty(aParam.getAssetsId())) {
                    Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());
                    if (!aBoolean) {
                        checkNowMsg = "立即执行的操作未成功";
                    }
                }

                //保存数据
                //根据资产类型、资产子类型自动匹配模型
                //获取模型及父模型属性信息
                List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(aParam.getAssetsTypeSubId());

                AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
                List<PropertyInfo> propertyInfoList = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(aParam.getAssetsTypeSubId())) {
                        param.setModelIndex(modelInfo.getModelIndex());
                        param.setModelId(modelInfo.getModelId());
                        param.setModelName(modelInfo.getModelName());
                        param.setEsId(modelInfo.getModelIndex() + "" + aParam.getInstanceId());
                    }

                    if (null != modelInfo.getPropertyInfos()) {
                        propertyInfoList.addAll(modelInfo.getPropertyInfos());
                    }
                }

                param.setInstanceName(aParam.getInstanceName() == null ? aParam.getAssetsName() : aParam.getInstanceName());
                List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();

                for (PropertyInfo propertyInfo : propertyInfoList) {
                    AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                    addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
                    propertiesParamList.add(addModelInstancePropertiesParam);
                }

                m.forEach((k, v) -> {
                    for (AddModelInstancePropertiesParam p : propertiesParamList) {
                        if (p.getPropertiesIndexId().equals(k)) {
                            p.setPropertiesValue(v != null ? v.toString() : null);
                        }
                        //宏值赋值，只需要字段包含上就行
                        if (k.startsWith("{$") && k.endsWith("}")) {
                            if (k.indexOf(p.getPropertiesIndexId()) != -1) {
                                p.setPropertiesValue(v != null ? v.toString() : null);
                            }
                        }
                    }
                });
                param.setPropertiesList(propertiesParamList);
                //模型实例单个新增、使用修改方法（基础属性信息保存已经调用新增方法了、此处只要修改就行）
                param.setInstanceId(aParam.getInstanceId());

                modelSever.updateModelInstance(param, 0);

                //缓存相关资产信息
                MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
                mwTangibleassetsDTO.extractFrom(aParam);
                mwModelViewCommonService.addCacheAssetInfo(mwTangibleassetsDTO);

                //只有snmp协议添加的资产才有机器识别码
                if (MwModelUtils.isSNMPType(aParam.getMonitorMode())) {
                    aParam.setId(String.valueOf(param.getInstanceId()));
                    mwTangibleAssetsService.insertDeviceInfo(aParam);
                }

                //发布新增事件,放到后台执行
                MwModelAssetsDiscoveryServiceImpl service = getBean(MwModelAssetsDiscoveryServiceImpl.class);
                ModelInstanceAddTask modelInstanceAddTask = new ModelInstanceAddTask(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build(), service);
                modelInstanceAddTask.init(new Date(), loginCacheInfo.getLoginName(), OperationTypeEnum.SYNC_INTERFACE.getName(), OperationTypeEnum.SYNC_INTERFACE.getName());
                singleThreadExeManage.addNoLimitQueueTask(MwModelAssetsDiscoveryServiceImpl.class.getSimpleName()
                        , modelInstanceAddTask);

            }
        }
    }


    /**
     * 批量纳管保存数据到es
     *
     * @param paramList
     * @throws Throwable
     */
    public void batchInsertAssetsInfoToES(List<AddUpdateTangAssetsParam> paramList) throws Exception {
        //获取基础设施下的模型及其属性
        List<ModelInfo> modelInfoLists = mwModelManageDao.selectAllModelListWithParent();
        //将模型id作为key值，转为map
        Map<Integer, List<ModelInfo>> modelInfoListMap = modelInfoLists.stream().collect(Collectors.groupingBy(ModelInfo::getModelId));
        //将模型id作为key值，本体模型和所有父模型做为value，转为map
        Map<Integer, List<ModelInfo>> modelAndParentInfoListMap = new HashMap<>();
        for (ModelInfo modelInfo : modelInfoLists) {
            List<ModelInfo> modelInfos = new ArrayList<>();
            if (!Strings.isNullOrEmpty(modelInfo.getPids())) {
                String pids = modelInfo.getPids().substring(0, modelInfo.getPids().length() - 1);
                List<String> pidList = Arrays.asList(pids.split(","));
                for (String pid : pidList) {
                    if (!Strings.isNullOrEmpty(pid) && modelInfoListMap != null && modelInfoListMap.get(intValueConvert(pid)) != null) {
                        //获取每个模型的父模型集合
                        modelInfos.addAll(modelInfoListMap.get(intValueConvert(pid)));
                    }
                }
                //将本体模型也加入
                modelInfos.add(modelInfo);
                // //将模型id作为key值，本体模型和所有父模型list做为value
                modelAndParentInfoListMap.put(modelInfo.getModelId(), modelInfos);
            }
        }

        List<AddUpdModelInstanceContext> addUpdModelInstanceContexts = new ArrayList<>();
        for (AddUpdateTangAssetsParam aParam : paramList) {
            Map m = ListMapObjUtils.beanToMap(aParam);
            //宏值获取
            List<Macros> macros = aParam.getMwMacrosDTO();
            if (CollectionUtils.isNotEmpty(macros)) {
                for (Macros ma : macros) {
                    String value = ma.getValue();
                    //type为1，表示密文类型，需要加密处理
                    if (ma.getType() == 1) {
                        value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                    }
                    m.put(ma.getMacro(), value);
                }
            }
            //保存数据
            //根据资产类型、资产子类型自动匹配模型
            if (modelAndParentInfoListMap != null && modelAndParentInfoListMap.get(aParam.getAssetsTypeSubId()) != null) {
                List<ModelInfo> modelInfoList = new ArrayList<>(modelAndParentInfoListMap.get(aParam.getAssetsTypeSubId()));
                ModelInfo curModelInfo = null;
                List<PropertyInfo> allProperties = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfoList) {
                    if (aParam.getAssetsTypeSubId().equals(modelInfo.getModelId())) {
                        curModelInfo = modelInfo;
                    }
                    if (null != modelInfo.getPropertyInfos()) {
                        allProperties.addAll(modelInfo.getPropertyInfos());
                    }
                }
                AddUpdModelInstanceContext addUpdModelInstanceContext = new AddUpdModelInstanceContext();
                AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
                addUpdModelInstanceContext.setAddAndUpdateModelInstanceParam(param);
                addUpdModelInstanceContext.setAddUpdateTangAssetsParam(aParam);

                param.setModelIndex(curModelInfo.getModelIndex());
                param.setModelId(curModelInfo.getModelId());
                param.setModelName(curModelInfo.getModelName());
                param.setInstanceName(aParam.getInstanceName() == null ? aParam.getAssetsName() : aParam.getInstanceName());

                List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
                for (PropertyInfo propertyInfo : allProperties) {
                    AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
                    addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
                    propertiesParamList.add(addModelInstancePropertiesParam);
                }

                m.forEach((key, value) -> {
                    for (AddModelInstancePropertiesParam p : propertiesParamList) {
                        if (p.getPropertiesIndexId().equals(key)) {
                            p.setPropertiesValue(value != null ? value.toString() : null);
                        }
                        if (p.getPropertiesIndexId().equals("createDate") || p.getPropertiesIndexId().equals("modificationDate")) {
                            p.setPropertiesValue(DateUtils.formatDateTime(new Date()));
                        }
                    }
                });

                param.setPropertiesList(propertiesParamList);
                param.setManageParam(aParam);
                //为了下面方便插入snmp类型的机器码
                param.setMonitorMode(aParam.getMonitorMode());
                param.setDeviceCode(aParam.getDeviceCode());
                param.setAssetsId(aParam.getAssetsId());
                param.setInBandIp(aParam.getInBandIp());

                addUpdModelInstanceContexts.add(addUpdModelInstanceContext);
            } else {
                continue;
            }
        }

        log.info("插入数据saveData条数：" + addUpdModelInstanceContexts.size());
        //返回成功的instanceId，才执行下一步操作
        mwModelInstanceService.saveData(addUpdModelInstanceContexts, true, true);
        List<AddUpdateTangAssetsParam> assetsParams = new ArrayList<>();
        List<SystemLogDTO> logDTOList = new ArrayList<>();
        for (AddUpdModelInstanceContext addUpdModelInstanceContext : addUpdModelInstanceContexts) {
            AddAndUpdateModelInstanceParam instanceParam = addUpdModelInstanceContext.getAddAndUpdateModelInstanceParam();
            //新增历史变更记录
            SystemLogDTO builder = SystemLogDTO.builder().userName(loginCacheInfo.getLoginName()).modelName(OperationTypeEnum.BATCH_MANAGE_INSTANCE.getName())
                    .objName(instanceParam.getModelName() == null ? instanceParam.getInstanceName() : instanceParam.getModelName() + "/" + instanceParam.getInstanceName())
                    .operateDes(OperationTypeEnum.BATCH_MANAGE_INSTANCE.getName() + ":" + instanceParam.getInstanceName()).operateDesBefore("").type("instance_" + instanceParam.getInstanceId()).version(1).build();
            //添加到系统操作日志
            mwlogger.info(JSON.toJSONString(builder));
            //添加到模型管理日志
            logDTOList.add(builder);

            //TODO 可以将instanceParam.getPropertiesList转为Map，在实体类转换为AddUpdateTangAssetsParam；
            AddUpdateTangAssetsParam aParam = new AddUpdateTangAssetsParam();
            aParam.setId(instanceParam.getInstanceId() + "");
            aParam.setInBandIp(instanceParam.getInBandIp());
            aParam.setAssetsId(instanceParam.getAssetsId());
            MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
            mwTangibleassetsDTO.extractFrom(aParam);
            mwModelViewCommonService.addCacheAssetInfo(mwTangibleassetsDTO);
            //只有批量纳管且snmp协议添加的资产才有机器识别码
            if (!Strings.isNullOrEmpty(instanceParam.getAssetsId()) && instanceParam.getMonitorMode() != null && MwModelUtils.isSNMPType(instanceParam.getMonitorMode())
                    && instanceParam.getInstanceId() != null) {
                aParam.setInstanceId(instanceParam.getInstanceId());
                if (!Strings.isNullOrEmpty(instanceParam.getDeviceCode())) {
                    aParam.setDeviceCode(instanceParam.getDeviceCode());
                    assetsParams.add(aParam);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            mwModelSysLogService.batchSaveInstaceChangeHistory(logDTOList);
        }
        //批量插入deviceInfo信息。
        if (CollectionUtils.isNotEmpty(assetsParams)) {
            log.info("批量插入deviceInfo信息");
            mwModelAssetsDiscoveryDao.batchInsertDeviceInfo(assetsParams);
        }
    }


    public void insertAssetsInfoToES(AddUpdateTangAssetsParam aParam, boolean isbatch, String errorMessage) throws Throwable {
        //说明创建不关联zabbix的资产,启用状态设置成未启用
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            aParam.setEnable(TangibleAssetState.DISACTIVE.name());
        } else {//说明是关联zabbix资产,启用状态设置成启用
            aParam.setEnable(TangibleAssetState.ACTIVE.name());
        }
        if (Strings.isNullOrEmpty(aParam.getInstanceName())) {
            aParam.setInstanceName(aParam.getHostName());
        }
        log.info("doInsertAssets params:" + aParam.toString() + ";isbatch:" + isbatch);
        List<Reply> faillist;
        //设置创建时间和创建人
        aParam.setCreator(loginCacheInfo.getLoginName());
        aParam.setModifier(loginCacheInfo.getLoginName());

        //判断是否为批量增加资产
        if (!isbatch) {
            //数据重复校验 即将添加的数据的 资产id(assets_id) 和带内ip(in_band_ip) 是否重复，二者有一即重复。
            faillist = publishCheckEvent(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build());
            if (faillist.size() > 0) {
                errorMessage += faillist.get(0).getMsg();
//                    throw new Throwable(faillist.get(0).getMsg());
            }
            if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
                //设置第三方监控服务器中主机名称
                aParam.setTPServerHostName(UuidUtil.getUid());
            }
            if (Strings.isNullOrEmpty(aParam.getAssetsId())) {
                //访问zabbix获取主机id, 生成资产id
                aParam.setAssetsName(aParam.getInstanceName());
                String zabbixHostId = mwTangibleAssetsService.createAndGetZabbixHostId(aParam);
                //设置资产id
                aParam.setAssetsId(zabbixHostId);
            }
        }
        //判断是否成功创建资产
        if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() > 0 && Strings.isNullOrEmpty(aParam.getAssetsId())) {
            errorMessage += aParam.getHostName() + "fail to create zabbixHostid";
//                return Reply.fail(ErrorConstant.TANGASSETSCODE_210104, ErrorConstant.TANGASSETS_MSG_210104 + "fail to create zabbixHostid");
        }

        //设置伪删除和创建修改时间
        aParam.setDeleteFlag(false);
        Date date = new Date();
        aParam.setCreateDate(date);
        aParam.setModificationDate(date);
        //更加资产id和服务器id判断该实例是否已经纳管
        if (!Strings.isNullOrEmpty(aParam.getAssetsId()) && aParam.getMonitorServerId() != 0) {
            aParam.setIsManage(true);
        } else {
            aParam.setIsManage(false);
        }
        Map m = ListMapObjUtils.beanToMap(aParam);
        //宏值获取
        List<Macros> macros = aParam.getMwMacrosDTO();
        if (CollectionUtils.isNotEmpty(macros)) {
            for (Macros ma : macros) {
                String value = ma.getValue();
                //type为1，表示密文类型，需要加密处理
                if (ma.getType() == 1) {
                    value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                }
                m.put(ma.getMacro(), value);
            }
        }
        String checkNowMsg = "";
        //是否启动立即执行
        if (aParam.isCheckNowFlag() && !Strings.isNullOrEmpty(aParam.getAssetsId())) {
            Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());
            if (!aBoolean) {
                checkNowMsg = "立即执行的操作未成功";
            }
        }

        //保存数据
        //根据资产类型、资产子类型自动匹配模型
        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(aParam.getAssetsTypeSubId());
        ModelInfo curModelInfo = null;
        List<PropertyInfo> allProperties = new ArrayList<>();

        for (ModelInfo modelInfo : modelInfoList) {
            if (aParam.getAssetsTypeSubId().equals(modelInfo.getModelId())) {
                curModelInfo = modelInfo;
            }
            if (null != modelInfo.getPropertyInfos()) {
                allProperties.addAll(modelInfo.getPropertyInfos());
            }
        }

        AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
        param.setModelIndex(curModelInfo.getModelIndex());
        param.setModelId(curModelInfo.getModelId());
        param.setModelName(curModelInfo.getModelName());
        param.setInstanceName(aParam.getInstanceName() == null ? aParam.getAssetsName() : aParam.getInstanceName());

        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
        for (PropertyInfo propertyInfo : allProperties) {
            AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
            addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
            propertiesParamList.add(addModelInstancePropertiesParam);
        }
        m.forEach((key, value) -> {
            for (AddModelInstancePropertiesParam p : propertiesParamList) {
                if (p.getPropertiesIndexId().equals(key)) {
                    p.setPropertiesValue(value != null ? value.toString() : null);
                }
                if (p.getPropertiesIndexId().equals("createDate") || p.getPropertiesIndexId().equals("modificationDate")) {
                    p.setPropertiesValue(DateUtils.formatDateTime(new Date()));
                }
            }
        });
        param.setPropertiesList(propertiesParamList);
        param.setManageParam(aParam);

        if (isbatch) {
            //批量纳管、使用实例新增方法
            modelSever.creatModelInstance(param, 0);
            aParam.setId(String.valueOf(param.getInstanceId()));
            //只有snmp协议添加的资产才有机器识别码
            if (MwModelUtils.isSNMPType(aParam.getMonitorMode()) && param.getInstanceId() != null) {
                aParam.setInstanceId(param.getInstanceId());
                aParam.setId(String.valueOf(param.getInstanceId()));
                if (!Strings.isNullOrEmpty(aParam.getDeviceCode())) {
                    mwTangibleAssetsService.insertDeviceInfo(aParam);
                }
            }
        } else {
            //模型实例单个新增、使用修改方法（基础属性信息保存已经调用新增方法了、此处只要修改就行)
            //旧的资产管理在纳管的时候,会走这个逻辑
            param.setInstanceId(aParam.getInstanceId());
            param.setEsId(curModelInfo.getModelIndex() + "" + aParam.getInstanceId());
            modelSever.updateModelInstance(param, 0);
        }
        //缓存相关资产信息
        MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
        mwTangibleassetsDTO.extractFrom(aParam);
        mwModelViewCommonService.addCacheAssetInfo(mwTangibleassetsDTO);
    }


    /**
     * 模型视图实例新增
     *
     * @param aParam
     * @param isbatch
     * @return
     * @throws Throwable
     */
    @Override
    public Reply doInsertAssetsToESByView(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable {
        //说明创建不关联zabbix的资产,启用状态设置成未启用
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            aParam.setEnable(TangibleAssetState.DISACTIVE.name());
        } else {//说明是关联zabbix资产,启用状态设置成启用
            aParam.setEnable(TangibleAssetState.ACTIVE.name());
        }
        log.info("doInsertAssets params:" + aParam.toString() + ";isbatch" + isbatch);
        List<Reply> faillist;
        //设置创建时间和创建人
        aParam.setCreator(loginCacheInfo.getLoginName());
        aParam.setModifier(loginCacheInfo.getLoginName());
        if (!Strings.isNullOrEmpty(aParam.getPollingEngine()) && LOCALHOST_KEY.equals(aParam.getPollingEngine())) {
            aParam.setPollingEngineName(LOCALHOST_NAME);
        }

        //设置资产id为实例id
        aParam.setId(aParam.getInstanceId().toString());
        //判断是否为批量增加资产
        if (!isbatch) {
            long time1 = System.currentTimeMillis();
            //数据重复校验 即将添加的数据的 资产监控方式(monitor_mode) 和带内ip(in_band_ip) 是否重复，二者有一即重复。
            faillist = publishCheckEvent(AddModelAssetsEvent.builder().addModelAssetsParam(aParam).build());
            if (faillist.size() > 0) {
                throw new Throwable(faillist.get(0).getMsg());
            }
            if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
                //设置第三方监控服务器中主机名称
                aParam.setTPServerHostName(UuidUtil.getUid());
            }
            long time2 = System.currentTimeMillis();
            log.info("数据重复校验耗时::" + (time2 - time1) + "ms");
            MwModelTemplateInfo param = new MwModelTemplateInfo();
            param.setServerId(aParam.getMonitorServerId());
            if (!Strings.isNullOrEmpty(aParam.getTemplateName())) {
                param.setTemplateName(aParam.getTemplateName().split("@")[0]);
            }
            List<MwModelTemplateInfo> templateInfos = mwModelViewDao.getServerTemplateIdByName(param);
            //根据模板名称和监控服务器Id，获取模板id
            if (templateInfos != null && templateInfos.size() > 0) {
                aParam.setTemplateId(templateInfos.get(0).getServerTemplateId());
            }
            //根据模型分组id和监控服务器Id，获取zabbix分组id
            String groupId = mwModelViewDao.getServerGroupId(aParam.getAssetsTypeId(), aParam.getMonitorServerId());
            aParam.setGroupId(groupId);
            long time3 = System.currentTimeMillis();
            //访问zabbix获取主机id, 生成资产id
            aParam.setAssetsName(aParam.getInstanceName());
            String zabbixHostId = "";
            if (aParam.getMonitorMode() == 5) {//IPMI新增使用带外资产新增方式
                zabbixHostId = mwTangibleAssetsService.createAndGetOutAssetsZabbixHostId(aParam);
            } else {
                zabbixHostId = mwTangibleAssetsService.createAndGetZabbixHostId(aParam);
            }
            long time4 = System.currentTimeMillis();
            log.info("zabbixHostId:{}", zabbixHostId);
            log.info("创建资产hostId耗时::" + (time4 - time3) + "ms");
            //设置资产id
            aParam.setAssetsId(zabbixHostId);
        }
        //判断是否成功创建资产
        if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() > 0 && Strings.isNullOrEmpty(aParam.getAssetsId())) {
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210104, ErrorConstant.TANGASSETS_MSG_210104 + "fail to create zabbixHostid");
        }
        //更加资产id和服务器id判断该实例是否意见纳管
        if (!Strings.isNullOrEmpty(aParam.getAssetsId()) && aParam.getMonitorServerId() != 0) {
            aParam.setIsManage(true);
        } else {
            aParam.setIsManage(false);
        }
        Map<String, Object> m = ListMapObjUtils.beanToMap(aParam);
        //宏值获取
        List<Macros> macros = aParam.getMwMacrosDTO();
        if (CollectionUtils.isNotEmpty(macros)) {
            for (Macros ma : macros) {
                String value = ma.getValue();
                //type为1，表示密文类型，值得长度低于32位，默认为原始值，需要加密处理
                if (ma.getType() == 1 && value.length() <= 32) {
                    value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                }
                ma.setValue(value);
                m.put(ma.getMacro(), value);
            }
        }

        String jsonStr = JSON.toJSONString(macros);


        String checkNowMsg = "";
        long time5 = System.currentTimeMillis();
        //是否启动立即执行
        if (aParam.isCheckNowFlag() && !Strings.isNullOrEmpty(aParam.getAssetsId())) {
            Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());

            if (!aBoolean) {
                checkNowMsg = "立即执行的操作未成功";
            }
        }
        long time6 = System.currentTimeMillis();
        log.info("资产立即执行耗时::" + (time6 - time5) + "ms");

        //保存数据
        //根据资产类型、资产子类型自动匹配模型
        //获取模型及父模型属性信息
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListWithParent(aParam.getAssetsTypeSubId());

        AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
        List<PropertyInfo> propertyInfoList = new ArrayList<>();
        for (ModelInfo modelInfo : modelInfos) {
            if (modelInfo.getModelId().equals(aParam.getAssetsTypeSubId())) {
                param.setModelIndex(modelInfo.getModelIndex());
                param.setModelId(modelInfo.getModelId());
                param.setModelName(modelInfo.getModelName());
                param.setEsId(modelInfo.getModelIndex() + "" + aParam.getInstanceId());
            }

            if (null != modelInfo.getPropertyInfos()) {
                propertyInfoList.addAll(modelInfo.getPropertyInfos());
            }
        }

        param.setInstanceName(aParam.getInstanceName() == null ? aParam.getAssetsName() : aParam.getInstanceName());
        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();

        for (PropertyInfo propertyInfo : propertyInfoList) {
            AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
            addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
            propertiesParamList.add(addModelInstancePropertiesParam);
        }

        m.forEach((k, v) -> {
            for (AddModelInstancePropertiesParam p : propertiesParamList) {
                if (p.getPropertiesIndexId().equals(k)) {
                    p.setPropertiesValue(v != null ? v.toString() : null);
                }
                //宏值赋值，只需要字段包含上就行
                if (k.startsWith("{$") && k.endsWith("}")) {
                    if (k.indexOf(p.getPropertiesIndexId()) != -1) {
                        p.setPropertiesValue(v != null ? v.toString() : null);
                    }
                }
                //宏值赋值，只需要字段包含上就行
                if (MWMACROS_DTO.equals(p.getPropertiesIndexId())) {//mwMacrosDTO字段，
                    p.setPropertiesValue(jsonStr);
                }
            }
        });
        param.setPropertiesList(propertiesParamList);
        //模型实例单个新增、使用修改方法（基础属性信息保存已经调用新增方法了、此处只要修改就行）
        param.setInstanceId(aParam.getInstanceId());

        mwModelInstanceService.batchUpdateModelInstance(Arrays.asList(param));
        long time7 = System.currentTimeMillis();
        log.info("资产添加入库耗时::" + (time7 - time6) + "ms");

        //缓存相关资产信息
        MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
        mwTangibleassetsDTO.extractFrom(aParam);
        mwModelViewCommonService.addCacheAssetInfo(mwTangibleassetsDTO);
        //deviceCod不为空，且不等于"null",37a6259cc0c1dae299a7866489dff0bd是“null”的md5密文
        boolean isInsertCode = !Strings.isNullOrEmpty(aParam.getDeviceCode()) && !"37a6259cc0c1dae299a7866489dff0bd".equals(aParam.getDeviceCode());
        //只有snmp协议添加的资产才有机器识别码
        if (MwModelUtils.isSNMPType(aParam.getMonitorMode()) && isInsertCode) {
            aParam.setId(String.valueOf(param.getInstanceId()));
            mwTangibleAssetsService.insertDeviceInfo(aParam);
        }
        //发布新增事件,放到后台执行
        MwModelAssetsDiscoveryServiceImpl service = getBean(MwModelAssetsDiscoveryServiceImpl.class);
        ModelInstanceAddTask modelInstanceAddTask = new ModelInstanceAddTask(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build(), service);
        modelInstanceAddTask.init(new Date(), loginCacheInfo.getLoginName(), OperationTypeEnum.SYNC_INTERFACE.getName(), OperationTypeEnum.SYNC_INTERFACE.getName());
        singleThreadExeManage.addNoLimitQueueTask(MwModelAssetsDiscoveryServiceImpl.class.getSimpleName()
                , modelInstanceAddTask);
        long time8 = System.currentTimeMillis();
        log.info("资产入库后相关处理耗时::" + (time8 - time7) + "ms");
        return Reply.ok("新增成功" + checkNowMsg);
    }

    /**
     * 查看已发现主机
     *
     * @param queryScanResultParam
     * @return
     * @throws Exception
     */
    @Override
    public Reply scanResultSearch(QueryScanResultParam queryScanResultParam) throws Exception {

        //若传instanceIds数据，表示在资产视图列表界面进行纳管，需要获取原数据的实例名称和权限信息
//        if(CollectionUtils.isNotEmpty(queryScanResultParam.getInstanceIds())){
//            QueryRelationInstanceModelParam params = new QueryRelationInstanceModelParam();
//            params.setInstanceIds(queryScanResultParam.getInstanceIds());
//            //根据节点数据，查询es信息
//            List<Map<String, Object>> selectList = mwModelViewService.selectInstanceInfoByIdsAndModelIndexs(params);
//        }
        ScanResultType scanResultType = null;
        if (StringUtils.isEmpty(queryScanResultParam.getResulttype())) {
            scanResultType = ScanResultType.DEFAULT;
        } else {
            try {
                scanResultType = ScanResultType.valueOf(queryScanResultParam.getResulttype());
            } catch (Exception e) {
                return Reply.fail(e.getMessage());
            }
        }
        try {
            switch (scanResultType) {
                case SUCCESS:
                    PageInfo pageInfo = new PageInfo<List>();
                    PageList pageList = new PageList();
                    List<ScanResultSuccess> list = mwModelAssetsDiscoveryDao.selectScanSuccessList(queryScanResultParam);
                    //资产是否存在校验
                    checkModelInstanceIsExists(list);

                    if (!Strings.isNullOrEmpty(queryScanResultParam.getIsExist())) {
                        //获取已添加的
                        if (assetsExist.equals(queryScanResultParam.getIsExist())) {
                            list = list.stream().filter(s -> s.getScanSuccessIdInAssets() > 0).collect(Collectors.toList());
                        }
                        //获取未添加的
                        if (assetsNoExist.equals(queryScanResultParam.getIsExist())) {
                            list = list.stream().filter(s -> s.getScanSuccessIdInAssets() == 0).collect(Collectors.toList());
                        }
                    }
                    pageInfo.setTotal(list.size());
                    //当监控方式是中间件，数据库，应用的时候不进行ip校验
                    //其他的按照监控方式和IP地址
                    list.forEach(scanResultSuccess -> {
                        scanResultSuccess.decrSnmpData();
                    });
                    if (null != list && list.size() > 0) {
                        ScanResultSuccess scanResultSuccess = list.get(0);
                        log.info("scanResultSuccess id:{};ip:{}", scanResultSuccess.getId(), scanResultSuccess.getIpAddress());
                    } else {
                        log.info("scanResultSuccess no data queryScanResultParam:{}", queryScanResultParam.toString());
                    }
                    //排序，未纳管的排在前面
                    list = list.stream().sorted(Comparator.comparing(s -> s.getScanSuccessIdInAssets())).collect(Collectors.toList());
                    log.info("ACCESS_LOG[]scan[]资产发现[]查询扫描成功信息[]{}[]");
                    list = pageList.getList(list, queryScanResultParam.getPageNumber(), queryScanResultParam.getPageSize());
                    pageInfo.setList(list);
                    return Reply.ok(pageInfo);
                case ERROR:
                    PageHelper.startPage(queryScanResultParam.getPageNumber(), queryScanResultParam.getPageSize());
                    Map criteria2 = PropertyUtils.describe(queryScanResultParam);
                    List<ScanResultFail> faillist = mwModelAssetsDiscoveryDao.selectFailList(criteria2);
                    log.info("ACCESS_LOG[]scan[]资产发现[]查询扫描失败信息[]{}[]");
                    PageInfo pagefailInfo = new PageInfo<>(faillist);
                    pagefailInfo.setList(faillist);
                    return Reply.ok(pagefailInfo);
                default:
            }
        } catch (Exception e) {
            log.error("scanResultSearch{}", e);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            //DataPermUtil.remove();
        }

        return Reply.ok();
    }

    private void checkModelInstanceIsExists(List<ScanResultSuccess> paramList) {
        //对扫描资产成功接口进行check校验
        QueryInstanceModelParam param = new QueryInstanceModelParam();
        Set<String> modelIndexSet = new HashSet<>();
        for (ScanResultSuccess scan : paramList) {
            if (!Strings.isNullOrEmpty(scan.getModelIndex())) {
                modelIndexSet.add(scan.getModelIndex());
            }
        }
        List<String> modelIndexs = new ArrayList<>(modelIndexSet);
        param.setModelIndexs(modelIndexs);
        List<Integer> instanceIds = mwModelAssetsDiscoveryDao.getInstanceByModelIndex(modelIndexs);
        param.setInstanceIds(instanceIds);
        param.setFieldList(Arrays.asList(MwModelViewCommonService.INSTANCE_NAME_KEY, "inBandIp", "orgIds", "userIds", "groupIds", "manufacturer", "specifications", "monitorMode", "assetsTypeSubId", "assetsId", "hostName"));
        param.setPageSize(pageSize);
        Map<String, Object> maps = mwModelViewService.getModelListInfoByBase(param);
        List<Map<String, Object>> list = new ArrayList<>();
        if (maps != null && maps.get("data") != null) {
            list = (List<Map<String, Object>>) maps.get("data");
        }
        Map<String, Map<String, Object>> map = new HashMap();
        Map<String, Map<String, Object>> map2 = new HashMap();
        for (Map<String, Object> ms : list) {
            //ip地址、监控方式作为key值
            map.put(ms.get(IN_BAND_IP) + "_" + ms.get(MONITOR_MODE), ms);
            map2.put(strValueConvert(ms.get(IN_BAND_IP)), ms);//资产视图页面，未纳管的资产没有监控方式，只根据ip匹配
        }
        log.info("进入 checkModelInstanceIsExists:" + map);

        if (map != null && map.size() > 0) {
            for (ScanResultSuccess scan : paramList) {
                Map<String, Object> m = new HashMap<>();
                List orgIds = new ArrayList();
                List userIds = new ArrayList();
                List groupIds = new ArrayList();
                String instanceName = "";
                Map<String, Object> instanceInfo = new HashMap<>();
                if (map2.containsKey(scan.getIpAddress())) {
                    instanceInfo = map2.get(scan.getIpAddress());
                }
                if (instanceInfo != null && instanceInfo.get(MwModelViewCommonService.INSTANCE_NAME_KEY) != null) {
                    instanceName = instanceInfo.get(MwModelViewCommonService.INSTANCE_NAME_KEY).toString();
                }
                if (instanceInfo != null && instanceInfo.get("orgIds") != null) {
                    orgIds = JSONArray.parseArray(JSONArray.toJSONString(instanceInfo.get("orgIds")));
                }
                if (instanceInfo != null && instanceInfo.get("userIds") != null) {
                    userIds = JSONArray.parseArray(JSONArray.toJSONString(instanceInfo.get("userIds")));
                }
                if (instanceInfo != null && instanceInfo.get("groupIds") != null) {
                    groupIds = JSONArray.parseArray(JSONArray.toJSONString(instanceInfo.get("groupIds")));
                }
                scan.setGroupIds(groupIds);
                scan.setOrgIds(orgIds);
                scan.setUserIds(userIds);
                scan.setInstanceName(instanceName);
                //匹配上，则说明该资产重复
                String ipAddress = scan.getIpAddress() != null ? scan.getIpAddress() : "";
                String hostName = scan.getHostName() != null ? scan.getHostName() : "";
                String monitorModeVal = scan.getMonitorModeVal() != null ? scan.getMonitorModeVal() : "";
                m = map.get(ipAddress + "_" + monitorModeVal);
                if (m != null && m.size() > 0) {
                    //批量纳管check逻辑
                    scan.setScanSuccessIdInAssets(1);
                } else {
                    scan.setScanSuccessIdInAssets(0);
                }
            }
        }
        log.info("checkModelInstanceIsExists: 结束" + paramList);
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
            if (StringUtils.isNotEmpty(param.getModelIndex()) || (param.getModelIndexs() != null && param.getModelIndexs().size() > 0)) {
                //条件组合查询
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                BoolQueryBuilder queryBuilder0 = QueryBuilders.boolQuery();
                if (param.getModelIndexs() != null && param.getModelIndexs().size() > 0) {
                    for (String modelIndex : param.getModelIndexs()) {
                        queryBuilder0 = queryBuilder0.should(QueryBuilders.termQuery("modelIndex", modelIndex));
                    }
                }
                queryBuilder.must(queryBuilder0);
                BoolQueryBuilder queryBuilder1 = QueryBuilders.boolQuery();
                if (param.getInstanceIdList() != null && param.getInstanceIdList().size() > 0) {
                    for (Integer instanceId : param.getInstanceIdList()) {
                        queryBuilder1 = queryBuilder1.should(QueryBuilders.termQuery(MwModelViewCommonService.INSTANCE_ID_KEY, instanceId));
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
                if (!Strings.isNullOrEmpty(param.getModelIndex())) {
                    searchRequest.indices(param.getModelIndex());
                }
                if (param.getModelIndexs() != null && param.getModelIndexs().size() > 0) {
                    searchRequest.indices(String.join(",", param.getModelIndexs()));
                }
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


    @Override
    public Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam) {
        if (null != aParam.getMonitorModeName() && StringUtils.isNotEmpty(aParam.getMonitorModeName())) {
            RuleType ruleType = getInfoByName(aParam.getMonitorModeName());
            aParam.setMonitorMode(ruleType.getMonitorMode());
        }
        //根据添加模式判断是否需要查询所有模板
        List<MwModelTemplateDTO> data = mwModelAssetsDiscoveryDao.getByServerIdAllTemplate(aParam);
        return Reply.ok(data);
    }

    @Override
    public Reply modelAssetsToManage(QueryInstanceModelParam params) throws Exception {
        Reply reply = null;


        List<Integer> monitorModesList = new ArrayList<Integer>();
        //从es中获取数据信息
        List<Map<String, Object>> list = modelSever.getInstanceInfoByModelId(params);
        List<Integer> scanSuccessIds = new ArrayList<>();
        for (Map<String, Object> m : list) {
            monitorModesList.add(intValueConvert(m.get(MONITOR_MODE)));
            scanSuccessIds.add(intValueConvert(m.get(scanSuccessId)));
        }
        List<ScanResultSuccess> scanSuccessList = mwModelAssetsDiscoveryDao.selectSuccessListByIds(scanSuccessIds);
        Map<Integer, ScanResultSuccess> scanSuccessMap = scanSuccessList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s));
        //获取所有监控服务器信息
        List<MwModelViewTreeDTO> serverNameList = mwModelExportDao.getServerNameByExport();
        Map<String, String> serverMap = new HashMap();
        for (MwModelViewTreeDTO dto : serverNameList) {
            serverMap.put(dto.getId(), dto.getName());
        }
        //获取所有监控方式信息
        List<MwModelViewTreeDTO> monitorModeList = mwModelExportDao.getMonitorModeByExport();
        Map<String, String> monitorModeMap = new HashMap();
        for (MwModelViewTreeDTO dto : monitorModeList) {
            monitorModeMap.put(dto.getId(), dto.getName());
        }
        //获取所有轮询引擎信息
        List<MwModelViewTreeDTO> proxyInfoList = mwModelExportDao.getAllProxyInfoByExport();
        Map<String, String> proxyInfoListMap = new HashMap();
        for (MwModelViewTreeDTO dto : proxyInfoList) {
            proxyInfoListMap.put(dto.getId(), dto.getName());
        }

        List<Map<String, Object>> listMap = new ArrayList<>();

        Map<String, String> groupMap = new HashMap<String, String>();
        IdentityHashMap<String, MwModelTemplateDTO> assetsOidMap = new IdentityHashMap<>();
        List<MwModelTemplateDTO> modelTemplateList = new ArrayList<>();
        Map criteria = new HashMap();
        criteria.put("serverId", params.getMonitorServerId());
        if (null != monitorModesList && monitorModesList.size() > 0) {
            criteria.put("monitorMode", monitorModesList);
        }
        modelTemplateList = mwModelTemplateDao.selectListByModel(criteria);
        //资产纳管时，获取匹配的zabbix分组id和模板id
        matchScanGroupIdAndTemplateId(params.getMonitorServerId(), groupMap, assetsOidMap, modelTemplateList);


        Map<String, MwModelTemplateDTO> collect = modelTemplateList.stream().collect(Collectors.toMap(s -> strValueConvert(s.getId()), s -> s));

        for (Map<String, Object> m : list) {
            AddUpdateTangAssetsParam aParam = JSONObject.parseObject(JSON.toJSONString(m), AddUpdateTangAssetsParam.class);
            if (scanSuccessMap != null && scanSuccessMap.containsKey(aParam.getScanSuccessId())) {
                ScanResultSuccess scanResultSuccess = scanSuccessMap.get(aParam.getScanSuccessId());
                scanResultSuccess.decrSnmpData();
                aParam = zabbixUtils.tranform(scanResultSuccess);
            }
            aParam.setMonitorServerId(params.getMonitorServerId());
            String pollingEngineName = LOCALHOST_NAME;
            if (!Strings.isNullOrEmpty(aParam.getPollingEngine()) && !LOCALHOST_KEY.equals(aParam.getPollingEngine()) && proxyInfoListMap != null) {
                pollingEngineName = proxyInfoListMap.get(aParam.getPollingEngine());
            } else {
                aParam.setPollingEngine(LOCALHOST_KEY);
            }
            String monitorServerName = "";
            if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() != 0 && serverMap != null) {
                monitorServerName = serverMap.get(strValueConvert(aParam.getMonitorServerId()));
            }
            String monitorModeName = "";
            if (aParam.getMonitorMode() != null && aParam.getMonitorMode() != 0 && monitorModeMap != null) {
                monitorModeName = monitorModeMap.get(strValueConvert(aParam.getMonitorMode()));
            }
            aParam.setMonitorFlag(params.getMonitorFlag());
            aParam.setMonitorServerId(params.getMonitorServerId());
            //MonitorServerId有值，则为纳管处理，匹配对应zabbix分组id
            String groupId = "";
            if (params.getMonitorServerId() != null && params.getMonitorServerId() != 0) {
                String key = AssetsUtils.genGroupKey(aParam.getAssetsTypeId(), params.getMonitorServerId());
                groupId = groupMap.get(key);
                aParam.setGroupId(groupId);
                if (!Strings.isNullOrEmpty(aParam.getTemplateId()) && collect != null && collect.containsKey(aParam.getTemplateId())) {
                    MwModelTemplateDTO mwModelTemplateDTO = collect.get(aParam.getTemplateId());
                    if (CollectionUtils.isNotEmpty(mwModelTemplateDTO.getTemplate())) {
                        List<MwModelZabbixTemplateParam> template = mwModelTemplateDTO.getTemplate();
                        for (MwModelZabbixTemplateParam zabbixTemplateParam : template) {
                            if (zabbixTemplateParam != null && zabbixTemplateParam.getServerId().equals(params.getMonitorServerId())) {
                                aParam.setTemplateId(strValueConvert(zabbixTemplateParam.getTemplateId()));
                            }
                        }
                    }
                } else {
                    aParam.setTemplateId("");
                }
            }
            //访问zabbix获取主机id, 生成资产id
            aParam.setAssetsName(aParam.getInstanceName());

            String zabbixHostId = mwTangibleAssetsService.createAndGetZabbixHostId(aParam);
            String esId = m.get(ESID).toString();
            String modelIndex = strValueConvert(m.get(MODEL_INDEX));
            Map<String, Object> map = new HashMap<>();
            map.put(ESID, esId);
            map.put(MODEL_INDEX, modelIndex);
            map.put(POLLINGENGINENAME, pollingEngineName);
            map.put(POLLINGENGINE, aParam.getPollingEngine());
            map.put(ASSETS_ID, zabbixHostId);
            if (Strings.isNullOrEmpty(zabbixHostId)) {
                m.put(IS_MANAGE, false);
            } else {
                m.put(IS_MANAGE, true);
            }
            map.put(MONITOR_FLAG, params.getMonitorFlag() == null ? false : params.getMonitorFlag());
            map.put(MONITOR_SERVER_ID, params.getMonitorServerId());
            map.put(MONITORSERVERNAME, monitorServerName);
            map.put(MONITORMODENAME, monitorModeName);
            map.put(HOST_GROUP_ID, groupId);
            listMap.add(map);
        }
        //更新es数据，将assetsId添加上。
        if (listMap != null && listMap.size() > 0) {
            reply = updateModelInstanceInfoByEs(listMap);
        }
        if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            return reply.fail(500, "资产纳管失败");
        } else {
            return reply.ok();
        }
    }


    @Override
    public Reply updateListStatus(BatchUpdatePowerParam param) {
        BulkRequest request = new BulkRequest();
        try {
            //监控状态就修改
            ModelAssetMonitorState tas = ModelAssetMonitorState.valueOf(param.getMonitorFlag() != null ? param.getMonitorFlag().toString().toUpperCase() : "FALSE");
            List<AddAndUpdateModelInstanceParam> updateParams = new ArrayList<>();
            for (MwModelInstanceParam mParam : param.getInstanceParams()) {
                mwModelInstanceService.batchUpdateHostState(mParam.getMonitorServerId(), Arrays.asList(mParam.getAssetsId()), tas.getZabbixStatus());
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setInstanceId(mParam.getInstanceId());
                instanceParam.setEsId(mParam.getEsId());
                instanceParam.setModelIndex(mParam.getModelIndex());
                List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
                AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
                propertiesParam.setPropertiesIndexId(MONITOR_FLAG);
                propertiesParam.setPropertiesValue(JSONObject.toJSONString(param.getMonitorFlag()));
                propertiesParam.setPropertiesType(ModelPropertiesType.SWITCH.getCode());
                propertiesList.add(propertiesParam);
                instanceParam.setPropertiesList(propertiesList);
                updateParams.add(instanceParam);
            }
            //修改监控状态
            Reply reply = mwModelInstanceService.batchUpdateModelInstance(updateParams);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return Reply.fail(500, "修改数据失败");
            }
            return Reply.ok();
        } catch (Throwable e) {
            return Reply.fail(500, "修改数据失败");
        }
    }

    /**
     * 修改es中指定字段数据
     *
     * @param mapList
     */
    private Reply updateModelInstanceInfoByEs(List<Map<String, Object>> mapList) {
        try {
            BulkRequest request = new BulkRequest();
            for (Map<String, Object> m : mapList) {
                UpdateRequest updateRequest = new UpdateRequest(m.get(MODEL_INDEX).toString(), m.get(ESID).toString());
                updateRequest.doc(m);
                request.add(updateRequest.upsert());
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            RestStatus status = bulkResponse.status();
            if (status.getStatus() == 200) {
                return Reply.ok();
            } else {
                return Reply.fail(500, "修改es字段数据失败");
            }
        } catch (Exception e) {
            log.error("修改es字段数据失败", e);
            return Reply.fail(500, "修改es字段数据失败");
        }
    }


    /**
     * 删除资产的关联信息
     */
    @Transactional
    public Reply deleteAssetsRelationInfo(List<DeleteTangAssetsID> ids) {
        if (ids != null && ids.size() > 0) {
            //校验删除资产是否被线路引用，如果被线路引用，则必须先删除线路数据
            List<String> linkNames = mwTangibleAssetsService.deleteAssetsCheckLinkRelation(ids);
            if (CollectionUtils.isNotEmpty(linkNames)) {
                StringBuilder builder = new StringBuilder();
                for (String linkName : linkNames) {
                    builder.append(linkName + ",");
                }
                builder.deleteCharAt(builder.length() - 1);
                return Reply.fail("该资产与线路[" + builder.toString() + "]有关联，请先删除线路数据再进行资产删除");
            }
            List<String> tids = new ArrayList<String>();
            List<String> zabbixIds = new ArrayList<String>();
            List<String> VMzabbixIds = new ArrayList<String>();
            Collections.sort(ids);

            //根据服务器id分组
            int monitorServerId = -1;
            List<MonitorServer> monitorServers = new ArrayList<MonitorServer>();
            MonitorServer ms = null;
            for (DeleteTangAssetsID deleteTangAssetsID : ids) {
                //当监控方式为虚拟化时，创建的虚拟化资产自动发现的那些虚拟机，主机太多删除不掉时需要做的处理
                if (deleteTangAssetsID.getMonitorMode() != null && deleteTangAssetsID.getMonitorMode() == 7) {
                    //删除虚拟化对应缓存
                    redisTemplate.delete(redisTemplate.keys("virtualization::" + "*"));
                    //主机对应的自动发现的规则名
                    MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(deleteTangAssetsID.getMonitorServerId(), deleteTangAssetsID.getAssetsId());
                    JsonNode resultData = (JsonNode) dRuleByHostId.getData();
                    if (resultData.size() > 0) {
                        for (JsonNode resultDatum : resultData) {
                            String name = resultDatum.get("name").asText();
//                                    根据规则名获取主机组信息
                            MWZabbixAPIResult groupHostByName = mwtpServerAPI.getGroupHostByName(deleteTangAssetsID.getMonitorServerId(), name);
                            JsonNode groupHost = (JsonNode) groupHostByName.getData();
                            if (groupHost.size() > 0) {
                                groupHost.forEach(group -> {
                                    List<ModelGroupHosts> groupHostDTOS = JSONArray.parseArray(group.get("hosts").toString(), ModelGroupHosts.class);
                                    if (groupHostDTOS != null && groupHostDTOS.size() > 0) {
                                        List<String> list = groupHostDTOS.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                                        zabbixIds.addAll(list);
                                        VMzabbixIds.addAll(list);
                                    }
                                });
                            }
                        }
                    }
                }
                tids.add(deleteTangAssetsID.getId());
                zabbixIds.add(deleteTangAssetsID.getAssetsId());
                if (monitorServerId != deleteTangAssetsID.getMonitorServerId()) {
                    ms = new MonitorServer();
                    ms.setMonitorServerId(deleteTangAssetsID.getMonitorServerId());
                    monitorServerId = deleteTangAssetsID.getMonitorServerId();
                    monitorServers.add(ms);
                }
                ms.getZabbixIds().add(deleteTangAssetsID.getAssetsId());
                if (VMzabbixIds.size() > 0) {
                    ms.getZabbixIds().addAll(VMzabbixIds);
                }
            }
            mwTangibleAssetsService.deleteDeviceInfo(tids);
            for (String id : tids) {
                mwModelViewCommonService.removeCacheAssetInfo(id);
            }
            mwModelViewCommonService.updateMonitorServerSet();
            //删除关联信息
            mwTangibleAssetsService.batchDeleteAssetsAgentByAssetsId(tids);
            mwTangibleAssetsService.batchDeleteAssetsPortByAssetsId(tids);
            mwTangibleAssetsService.batchDeleteAssetsSnmpv12ByAssetsId(tids);
            mwTangibleAssetsService.batchDeleteAssetsSnmpv3ByAssetsId(tids);
            //删除告警映射关系
            mwTangibleAssetsService.deleteAssetsActionMapper(zabbixIds);

            //删除redis缓存
            deleteRedisActive(tids);

            //删除zabbix 配置
            boolean isFail = false;
            List<Integer> failServers = new ArrayList<Integer>();
            for (MonitorServer monitorServer : monitorServers) {
                List<String> delZabbixIds = monitorServer.getZabbixIds();
                //去除不存在的id
                List<String> lists = new ArrayList<>();
                delZabbixIds.forEach(id -> {
                    if (id.length() != "1603259561318843dd528c36e42d0b2c".length()) {
                        lists.add(id);
                    }
                });
                if (monitorServer.getMonitorServerId() != null && monitorServer.getMonitorServerId() != 0) {
                    if (lists.size() > 10) {//当zabbix一次删除过多的时候，删除不掉资产
                        for (int i = 0; i < lists.size(); i = i + 10) {
                            List<String> list = lists.subList(i, (i + 10) > lists.size() ? lists.size() : (i + 10));
                            MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), list);
                            if (result.isFail()) {
//                            isFail = true;
                                failServers.add(monitorServer.getMonitorServerId());
                                log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                            }
                        }
                    } else {
                        MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), lists);
                        if (result.isFail()) {
//                        isFail = true;
                            failServers.add(monitorServer.getMonitorServerId());
                            log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                        }
                    }
                }
            }

            if (isFail) {
                return Reply.fail("主机删除失败:[" + failServers + "]");
            }
            //删除有型资产关联数据
            BatchDeleteAssetsEvent batchDeleteAssetsEvent = new BatchDeleteAssetsEvent();
            batchDeleteAssetsEvent.setDeleteTangAssetsIDList(ids);
            try {
                publishFinishEvent(batchDeleteAssetsEvent);
            } catch (Throwable throwable) {
                log.error("deleteAssets publishFinishEvent", throwable);
            }
        }
        return Reply.ok("删除成功");
    }

    public void deleteRedisActive(List<String> ids) {
        Set<String> deleteIds = new HashSet<>();
        ids.forEach(id -> deleteIds.add(id));
        if (deleteIds.size() > 0) {
            redisTemplate.delete(deleteIds);
        }
    }

    /**
     * 取消资产纳管，删除zabbix中监控资产
     */
    public Reply deleteAssetsToZabbix(List<CancelZabbixAssetsParam> ids) {
        int monitorServerId = -1;
        List<MonitorServer> monitorServers = new ArrayList<MonitorServer>();
        MonitorServer ms = null;
        List<String> zabbixIds = new ArrayList<String>();
        List<String> VMzabbixIds = new ArrayList<String>();
        //当监控方式为虚拟化时，创建的虚拟化资产自动发现的那些虚拟机，主机太多删除不掉时需要做的处理
        for (CancelZabbixAssetsParam param : ids) {
            //当监控方式为虚拟化时，创建的虚拟化资产自动发现的那些虚拟机，主机太多删除不掉时需要做的处理
            if (param.getMonitorMode() != null && param.getMonitorMode() == 7) {
                //删除虚拟化对应缓存
                redisTemplate.delete(redisTemplate.keys("virtualization::" + "*"));
                //主机对应的自动发现的规则名
                MWZabbixAPIResult dRuleByHostId = mwtpServerAPI.getDRuleByHostId(param.getMonitorServerId(), param.getAssetsId());
                JsonNode resultData = (JsonNode) dRuleByHostId.getData();
                if (resultData.size() > 0) {
                    for (JsonNode resultDatum : resultData) {
                        String name = resultDatum.get("name").asText();
//                                    根据规则名获取主机组信息
                        MWZabbixAPIResult groupHostByName = mwtpServerAPI.getGroupHostByName(param.getMonitorServerId(), name);
                        JsonNode groupHost = (JsonNode) groupHostByName.getData();
                        if (groupHost.size() > 0) {
                            groupHost.forEach(group -> {
                                List<ModelGroupHosts> groupHostDTOS = JSONArray.parseArray(group.get("hosts").toString(), ModelGroupHosts.class);
                                if (groupHostDTOS != null && groupHostDTOS.size() > 0) {
                                    List<String> list = groupHostDTOS.stream().map(host -> host.getHostid()).collect(Collectors.toList());
                                    zabbixIds.addAll(list);
                                    VMzabbixIds.addAll(list);
                                }
                            });
                        }
                    }
                }
            }
            zabbixIds.add(param.getAssetsId());
            if (monitorServerId != param.getMonitorServerId()) {
                ms = new MonitorServer();
                ms.setMonitorServerId(param.getMonitorServerId());
                monitorServerId = param.getMonitorServerId();
                monitorServers.add(ms);
            }
            ms.getZabbixIds().add(param.getAssetsId());
            if (VMzabbixIds.size() > 0) {
                ms.getZabbixIds().addAll(VMzabbixIds);
            }
        }
        //删除zabbix 配置
        boolean isFail = false;
        List<Integer> failServers = new ArrayList<Integer>();
        for (MonitorServer monitorServer : monitorServers) {
            List<String> delZabbixIds = monitorServer.getZabbixIds();
            //去除不存在的id
            List<String> lists = new ArrayList<>();
            delZabbixIds.forEach(id -> {
                if (id.length() != "1603259561318843dd528c36e42d0b2c".length()) {
                    lists.add(id);
                }
            });
            if (monitorServer.getMonitorServerId() != null && monitorServer.getMonitorServerId() != 0) {
                if (lists.size() > 10) {//当zabbix一次删除过多的时候，删除不掉资产
                    for (int i = 0; i < lists.size(); i = i + 10) {
                        List<String> list = lists.subList(i, (i + 10) > lists.size() ? lists.size() : (i + 10));
                        MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), list);
                        if (result.isFail()) {
                            isFail = true;
                            failServers.add(monitorServer.getMonitorServerId());
                            log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                        }
                    }
                } else {
                    MWZabbixAPIResult result = mwtpServerAPI.hostDelete(monitorServer.getMonitorServerId(), lists);
                    if (result.isFail()) {
                        isFail = true;
                        failServers.add(monitorServer.getMonitorServerId());
                        log.error("[]ERROR_LOG[][]删除主机失败[][]msg:[]{}", result.getData());
                    }
                }
            }
        }
        if (isFail) {
            return Reply.fail("纳管取消失败");
        } else {
            return Reply.ok("纳管取消成功");
        }
    }

    private MwModelTemplateDTO matchAssetsInfo(RuleType rt, String sysObjectid, String sysDesc, String hostname
            , Map<String, MwModelTemplateDTO> assetsOidMap, List<MwModelTemplateDTO> modelTemplateList) {
        List<MwModelTemplateDTO> filterList = new ArrayList<>();
        MwModelTemplateDTO mwAssetsTemplateDTO = null;
        log.info("try sysObjectid:[" + sysObjectid + "]");
        if (null != sysObjectid) {
            for (Map.Entry<String, MwModelTemplateDTO> entry : assetsOidMap.entrySet()) {
                if (sysObjectid.equals(entry.getKey()) && null != entry.getValue()) {
                    if (sysDesc == null || StringUtils.isEmpty(sysDesc)) {//当系统信息为空时，只匹配oid 返回匹配到的第一个相同的oid
                        return entry.getValue();
                    }
                    log.info("try entry.getValue:[" + entry.getValue() + "]");
                    filterList.add(entry.getValue());
                }
            }
        }
        if (null != sysObjectid) {
            modelTemplateList = filterList;
            log.info("modelTemplateList::!" + modelTemplateList);
        }
        log.info("try sysDesc:[" + sysDesc + "]");
        boolean isFlag1 = false;
        for (MwModelTemplateDTO item : modelTemplateList) {
            if (item.getMonitorMode() != null && item.getMonitorMode() != rt.getMonitorMode()) {
                log.info("try item.getDescription():[" + item.getMonitorMode() + "]");
                continue;
            }
            if (null != sysDesc) {
                log.info("try item.getDescription():[" + item.getDescription() + "]");
                boolean isFind = false;
                String description = "";
                if (!Strings.isNullOrEmpty(item.getDescription())) {
                    //使用正则进行数据匹配
                    description = item.getDescription().replace("\r\n", "").replace("\u200B", "");
                }
                sysDesc = sysDesc.replace("\r\n", "").replace("\u200B", "");//正则匹配去除换行符
                log.info("desc match server desc111:[{}],template desc:[{}]", sysDesc, description);
                if (StringUtils.isNotBlank(description)) {
                    if (sysDesc.contains(description)) {
                        mwAssetsTemplateDTO = item;
                        log.info("desc match server desc:[{}],template desc:[{}]", sysDesc, item.getDescription());
                        return mwAssetsTemplateDTO;
                    }
                }
                //模板只匹配到OID，没有特征信息的
                if (Strings.isNullOrEmpty(description)) {
                    isFlag1 = true;//设置标识开关，匹配的模板是否都没有特征信息
                    log.info("description is empty:", isFlag1);
                }
                if (isFind) {
                    break;
                }
            }
        }
        if (isFlag1 && CollectionUtils.isNotEmpty(modelTemplateList)) {
            log.info("description::modelTemplateList:", modelTemplateList);
            return modelTemplateList.get(0);
        }
        return mwAssetsTemplateDTO;
    }

    /**
     * //资产纳管时，获取匹配的zabbix分组id和模板id
     *
     * @param monitorServerId
     * @param groupMap
     * @param assetsOidMap
     * @param modelTemplateList
     */
    private void matchScanGroupIdAndTemplateId(Integer monitorServerId, Map<String, String> groupMap, IdentityHashMap<String, MwModelTemplateDTO> assetsOidMap, List<MwModelTemplateDTO> modelTemplateList) {
        List<MwModelAssetsGroupTable> mwModelAssetsGroupTables = new ArrayList<>();
        //资产纳管时，获取匹配的zabbix分组id  groupId
        if (monitorServerId != null && monitorServerId.intValue() != 0) {
            Reply stsReply = mwModelManageService.selectGroupServerMap(null);
            mwModelAssetsGroupTables = (List<MwModelAssetsGroupTable>) stsReply.getData();
            if (null != mwModelAssetsGroupTables && mwModelAssetsGroupTables.size() > 0) {
                mwModelAssetsGroupTables.forEach(value -> {
                    log.info("assetsTypeId:" + value.getAssetsSubtypeId() + ";getMonitorServerId:" + monitorServerId);
                    String key = AssetsUtils.genGroupKey(value.getAssetsSubtypeId(), value.getMonitorServerId());
                    groupMap.put(key, value.getGroupId());
                });
            }
        }
        //可能存在相同oid 不同特征信息，被覆盖的情况
        modelTemplateList.forEach(value -> {
            if (null != value.getSystemObjid()) {
                assetsOidMap.put(value.getSystemObjid().trim(), value);
            }
        });
    }

    private void tangAssetsParamSetting(ScanResultSuccess scanResultSuccess, AddUpdateTangAssetsParam aParam) {
        aParam.setAssetsName(scanResultSuccess.getHostName());
        aParam.setPollingEngine(scanResultSuccess.getPollingEngine());
        aParam.setHostName(scanResultSuccess.getHostName());
        aParam.setAssetsTypeId(scanResultSuccess.getAssetsTypeId());
        aParam.setAssetsTypeSubId(scanResultSuccess.getAssetsSubTypeId());
        aParam.setEnable(TangibleAssetState.ACTIVE.name());
        aParam.setManufacturer(scanResultSuccess.getBrand());
        aParam.setDescription(scanResultSuccess.getDescription());
        aParam.setSpecifications(scanResultSuccess.getSpecifications());
        aParam.setHostGroupId(scanResultSuccess.getGroupId());
        aParam.setTemplateId(scanResultSuccess.getTemplateId());
        aParam.setSettingFlag(true);
        aParam.setInBandIp(scanResultSuccess.getIpAddress());
        aParam.setScanSuccessId(scanResultSuccess.getId());
        aParam.setMonitorServerId(scanResultSuccess.getMonitorServerId());
        aParam.setDeviceCode(scanResultSuccess.getDeviceCode());
        aParam.setUserIds(scanResultSuccess.getUserIds());
        aParam.setOrgIds(scanResultSuccess.getOrgIds());
        aParam.setGroupIds(scanResultSuccess.getGroupIds());
        aParam.setInstanceName(scanResultSuccess.getHostName());
        RuleType rt = RuleType.valueOf(scanResultSuccess.getMonitorMode());
        String monitorPort = null;
        switch (rt) {
            case SNMPv1v2:
                MwSnmpv1AssetsDTO snmpV1AssetsDTO = new MwSnmpv1AssetsDTO();
                snmpV1AssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                snmpV1AssetsDTO.setCommunity(scanResultSuccess.getCommunity());
                aParam.setSnmpV1AssetsDTO(snmpV1AssetsDTO);
                break;
            case SNMPv3:
                MwSnmpAssetsDTO snmpAssetsDTO = new MwSnmpAssetsDTO();
                snmpAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                snmpAssetsDTO.setSecName(scanResultSuccess.getSecurityName());
                snmpAssetsDTO.setContextName(scanResultSuccess.getContextName());

                aParam.setSnmpLev(scanResultSuccess.getSecurityLevel().getCode());

                snmpAssetsDTO.setSecLevelName(scanResultSuccess.getSecurityLevel().name());
                snmpAssetsDTO.setAuthAlg(scanResultSuccess.getAuthProtocol().getDropDownMapCode());
                snmpAssetsDTO.setAuthAlgName(scanResultSuccess.getAuthProtocol().name());
                snmpAssetsDTO.setAuthValue(scanResultSuccess.getAuthToken());
                snmpAssetsDTO.setPrivAlg(scanResultSuccess.getPrivProtocol().getDropDownMapCode());
                snmpAssetsDTO.setPrivAlgName(scanResultSuccess.getPrivProtocol().name());
                snmpAssetsDTO.setPriValue(scanResultSuccess.getPrivToken());
                aParam.setSnmpAssetsDTO(snmpAssetsDTO);
                break;
            case ZabbixAgent:
                MwAgentAssetsDTO agentAssetsDTO = new MwAgentAssetsDTO();
                agentAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                aParam.setAgentAssetsDTO(agentAssetsDTO);
                break;
            case Port:
                MwPortAssetsDTO portAssetsDTO = new MwPortAssetsDTO();
                portAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                aParam.setPortAssetsDTO(portAssetsDTO);
                aParam.setMonitorPort(Integer.parseInt(scanResultSuccess.getMonitorPort()));
                monitorPort = scanResultSuccess.getMonitorPort();
                break;
            default:
                log.warn("no match RuleType!" + scanResultSuccess.getMonitorMode());
        }
        aParam.setMonitorMode(rt.getMonitorMode());
        aParam.setVersion(scanResultSuccess.getMonitorMode());
        aParam.setMonitorFlag(false);

    }


    /**
     * 公共接口新增资产
     */
    @Override
    public Reply insertAssetsByCommon(AddUpdateTangAssetsParam aParam) throws Throwable {

        //查询更新许可，单个资产新增，每次+1；
        Integer count = mwModelInstanceService.selectCountInstances();
        ResponseBase responseBase = licenseManagement.getLicenseManagemengt("model_manage", count, 1);
        if (responseBase.getRtnCode() != 200) {
            throw new Exception("该模块新增数量已达许可数量上限！");
        }

        List<Integer> monitorModesList = new ArrayList<>();
        monitorModesList.add(intValueConvert(aParam.getMonitorMode()));
        String description = aParam.getDescription() != null ? aParam.getDescription() : "";
        String sysObjId = aParam.getSysObjId() != null ? aParam.getSysObjId() : "";
        Map<String, String> groupMap = new HashMap<String, String>();
        IdentityHashMap<String, MwModelTemplateDTO> assetsOidMap = new IdentityHashMap<>();
        List<MwModelTemplateDTO> modelTemplateList = new ArrayList<>();
        //资产纳管时，获取匹配的zabbix分组id和模板id
        //资产纳管时，获取匹配的templateId
        Map criteria = new HashMap();
        criteria.put("serverId", aParam.getMonitorServerId());
        if (null != monitorModesList && monitorModesList.size() > 0) {
            criteria.put("monitorModes", monitorModesList);
        }
        modelTemplateList = mwModelTemplateDao.selectListByModel(criteria);
        matchScanGroupIdAndTemplateId(aParam.getMonitorServerId(), groupMap, assetsOidMap, modelTemplateList);

        //说明创建不关联zabbix的资产,启用状态设置成未启用
        if (aParam.getMonitorServerId() == null || aParam.getMonitorServerId() == 0) {
            aParam.setEnable(TangibleAssetState.DISACTIVE.name());
        } else {//说明是关联zabbix资产,启用状态设置成启用
            aParam.setEnable(TangibleAssetState.ACTIVE.name());
            RuleType rt = RuleType.getInfoByMonitorMode(intValueConvert(aParam.getMonitorMode()));
            MwModelTemplateDTO mwAssetsTemplateDTO = matchAssetsInfo(rt, sysObjId, description, aParam.getHostName(), assetsOidMap, modelTemplateList);
            String templateId = "";
            if (mwAssetsTemplateDTO != null && CollectionUtils.isNotEmpty(mwAssetsTemplateDTO.getTemplate())) {
                templateId = String.valueOf(mwAssetsTemplateDTO.getTemplate().get(0).getTemplateId());
                aParam.setTemplateId(templateId);
                aParam.setAssetsTypeSubId(intValueConvert(mwAssetsTemplateDTO.getModelId()));
                aParam.setAssetsTypeId(intValueConvert(mwAssetsTemplateDTO.getModelGroupId()));
            } else {
                return Reply.fail(500, "获取资产模板id失败");
            }
            String key = AssetsUtils.genGroupKey(aParam.getAssetsTypeId(), aParam.getMonitorServerId());
            String groupId = groupMap.get(key);
            aParam.setGroupId(groupId);

        }
        if (com.google.common.base.Strings.isNullOrEmpty(aParam.getInstanceName())) {
            aParam.setInstanceName(aParam.getHostName());
        }
        List<Reply> faillist;
        //设置创建时间和创建人
        aParam.setCreator(loginCacheInfo.getLoginName());
        aParam.setModifier(loginCacheInfo.getLoginName());

        //判断是否为批量增加资产
        String errorMessage = "";
        //数据重复校验 即将添加的数据的 资产id(assets_id) 和带内ip(in_band_ip) 是否重复，二者有一即重复。
        faillist = publishCheckEvent(AddTangibleassetsEvent.builder().addTangAssetsParam(aParam).build());
        if (faillist.size() > 0) {
            errorMessage += faillist.get(0).getMsg();
        }
        if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
            //设置第三方监控服务器中主机名称
            aParam.setTPServerHostName(UuidUtil.getUid());
        }
        if (com.google.common.base.Strings.isNullOrEmpty(aParam.getAssetsId())) {
            //访问zabbix获取主机id, 生成资产id
            aParam.setAssetsName(aParam.getInstanceName());
            String zabbixHostId = mwTangibleAssetsService.createAndGetZabbixHostId(aParam);
            //设置资产id
            aParam.setAssetsId(zabbixHostId);
        }
        //判断是否成功创建资产
        if (aParam.getMonitorServerId() != null && aParam.getMonitorServerId() > 0 && com.google.common.base.Strings.isNullOrEmpty(aParam.getAssetsId())) {
            errorMessage += aParam.getHostName() + "fail to create zabbixHostid";
//                return Reply.fail(ErrorConstant.TANGASSETSCODE_210104, ErrorConstant.TANGASSETS_MSG_210104 + "fail to create zabbixHostid");
        }

        //设置伪删除和创建修改时间
        aParam.setDeleteFlag(false);
        Date date = new Date();
        aParam.setCreateDate(date);
        aParam.setModificationDate(date);
        //更加资产id和服务器id判断该实例是否已经纳管
        if (!com.google.common.base.Strings.isNullOrEmpty(aParam.getAssetsId()) && aParam.getMonitorServerId() != 0) {
            aParam.setIsManage(true);
        } else {
            aParam.setIsManage(false);
        }
        Map m = ListMapObjUtils.beanToMap(aParam);
        //宏值获取
        List<Macros> macros = aParam.getMwMacrosDTO();
        if (CollectionUtils.isNotEmpty(macros)) {
            for (Macros ma : macros) {
                String value = ma.getValue();
                //type为1，表示密文类型，需要加密处理
                if (ma.getType() == 1) {
                    value = RSAUtils.encryptData(ma.getValue(), RSAUtils.RSA_PUBLIC_KEY);
                }
                m.put(ma.getMacro(), value);
            }
        }
        String checkNowMsg = "";
        //是否启动立即执行
        if (aParam.isCheckNowFlag() && !com.google.common.base.Strings.isNullOrEmpty(aParam.getAssetsId())) {
            Boolean aBoolean = mwAssetsManager.checkNowItems(aParam.getMonitorServerId(), aParam.getAssetsId());
            if (!aBoolean) {
                checkNowMsg = "立即执行的操作未成功";
            }
        }

        //保存数据
        //根据资产类型、资产子类型自动匹配模型
        List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListWithParent(aParam.getAssetsTypeSubId());
        ModelInfo curModelInfo = null;
        List<PropertyInfo> allProperties = new ArrayList<>();

        for (ModelInfo modelInfo : modelInfoList) {
            if (aParam.getAssetsTypeSubId().equals(modelInfo.getModelId())) {
                curModelInfo = modelInfo;
            }
            if (null != modelInfo.getPropertyInfos()) {
                allProperties.addAll(modelInfo.getPropertyInfos());
            }
        }

        AddAndUpdateModelInstanceParam param = new AddAndUpdateModelInstanceParam();
        param.setModelIndex(curModelInfo.getModelIndex());
        param.setModelId(curModelInfo.getModelId());
        param.setModelName(curModelInfo.getModelName());
        param.setInstanceName(aParam.getInstanceName() == null ? aParam.getAssetsName() : aParam.getInstanceName());

        List<AddModelInstancePropertiesParam> propertiesParamList = new ArrayList<>();
        for (PropertyInfo propertyInfo : allProperties) {
            AddModelInstancePropertiesParam addModelInstancePropertiesParam = new AddModelInstancePropertiesParam();
            addModelInstancePropertiesParam.extractFromPropertyInfo(propertyInfo);
            propertiesParamList.add(addModelInstancePropertiesParam);
        }
        m.forEach((key, value) -> {
            for (AddModelInstancePropertiesParam p : propertiesParamList) {
                if (p.getPropertiesIndexId().equals(key)) {
                    p.setPropertiesValue(value != null ? value.toString() : null);
                }
                if (p.getPropertiesIndexId().equals("createDate") || p.getPropertiesIndexId().equals("modificationDate")) {
                    p.setPropertiesValue(DateUtils.formatDateTime(new Date()));
                }
            }
        });
        param.setPropertiesList(propertiesParamList);
        param.setManageParam(aParam);

        //批量纳管、使用实例新增方法
        modelSever.creatModelInstance(param, 0);
        aParam.setId(String.valueOf(param.getInstanceId()));
        //只有snmp协议添加的资产才有机器识别码
        if (MwModelUtils.isSNMPType(aParam.getMonitorMode()) && param.getInstanceId() != null) {
            aParam.setInstanceId(param.getInstanceId());
            aParam.setId(String.valueOf(param.getInstanceId()));
            if (!com.google.common.base.Strings.isNullOrEmpty(aParam.getDeviceCode())) {
                mwTangibleAssetsService.insertDeviceInfo(aParam);
            }
        }

        //缓存相关资产信息
        MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
        mwTangibleassetsDTO.extractFrom(aParam);
        mwModelViewCommonService.addCacheAssetInfo(mwTangibleassetsDTO);
        if (errorMessage.length() > 0) {
            return Reply.fail(500, "新增失败:" + errorMessage);
        }
        return Reply.ok();
    }

}
