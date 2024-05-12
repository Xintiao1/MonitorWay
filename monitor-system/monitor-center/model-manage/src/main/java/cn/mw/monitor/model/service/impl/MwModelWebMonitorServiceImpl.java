package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.api.common.IpV6Util;
import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.model.dao.MWModelCommonDao;
import cn.mw.monitor.model.dao.MWModelZabbixMonitorDao;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dto.MwModelPowerDTO;
import cn.mw.monitor.model.dto.MwModelWEBProxyDTO;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.proxy.param.MwModelProxyDTO;
import cn.mw.monitor.model.service.MwModelExportService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelWebMonitorService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.dto.MWStep;
import cn.mw.zbx.dto.MWWebDto;
import cn.mw.zbx.dto.MWWebValue;
import cn.mw.zbx.enums.ation.TriggerAlarmLevelEnum;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author qzg
 * @date 2021/12/06
 */
@Service
@Slf4j
public class MwModelWebMonitorServiceImpl implements MwModelWebMonitorService {
    @Resource
    private MWModelCommonDao mwModelCommonDao;

    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Resource
    private MwModelExportService mwModelExportService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelInstanceServiceImplV1 mwModelInstanceServiceImpl;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private MwModelWebMonitorService mwModelWebMonitorService;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private MWWebZabbixManger mwWebZabbixManger;
    @Autowired
    private MwModelCommonServiceImpl mwModelCommonServiceImpl;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Resource
    private MWModelZabbixMonitorDao mwModelZabbixMonitorDao;
    @Autowired
    private MWUserService userService;
    private int pageSize = 10000;
    //web监测模型Id
    private static Integer webMonitorModeId = 72;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    private static final String WEB_TEST_FAIL = "web.test.fail";
    private static final String WEB_TEST_TIME = "web.test.time";
    private static final String WEB_TEST_RSPCODE = "web.test.rspcode";
    private static final String WEB_TEST_ERROR = "web.test.error";

    @Override
    public Reply selectWebSeverInfo(MwModelWEBProxyDTO param) {
        List<MwTangibleassetsTable> disList = new ArrayList<>();
        try {
            List<MwModelProxyDTO> proxyDTOS = new ArrayList<>();
            MWZabbixAPIResult result = mwtpServerAPI.proxyInfoget(intValueConvert(param.getMonitorServerId()));
            if (result != null && !result.isFail()) {
                proxyDTOS = JSONArray.parseArray(result.getData().toString(), MwModelProxyDTO.class);
            }
            MwModelTPServerParam serverInfo = mwModelZabbixMonitorDao.queryMonitorServerInfoById(param.getMonitorServerId());
            MwModelProxyDTO mwModelProxyDTO = new MwModelProxyDTO();
            mwModelProxyDTO.setProxyAddress(serverInfo.getMonitoringServerIp());
            proxyDTOS.add(mwModelProxyDTO);

            //获取所有服务器的资产
            QueryModelAssetsParam queryModelAssetsParam = new QueryModelAssetsParam();
            //查询服务器类型的数据
            queryModelAssetsParam.setAssetsTypeId(1);
            queryModelAssetsParam.setFilterQuery(true);
            List<MwTangibleassetsTable> list = mwModelViewCommonService.findModelAssets(MwTangibleassetsTable.class, queryModelAssetsParam);
            if (CollectionUtils.isNotEmpty(proxyDTOS) && CollectionUtils.isNotEmpty(list)) {
                for (MwTangibleassetsTable assets : list) {
                    for (MwModelProxyDTO proxyDTO : proxyDTOS) {
                        //根据引擎的ip过滤服务器数据
                        if (proxyDTO != null && !Strings.isNullOrEmpty(proxyDTO.getProxyAddress()) && (proxyDTO.getProxyAddress() + ",").indexOf(assets.getInBandIp() + ",") != -1) {
                            disList.add(assets);
                            break;
                        }
                    }

                }
            }
            disList.stream().distinct();
        } catch (Exception e) {
            log.error("获取web监测服务器数据失败", e);
            return Reply.fail(500, "获取web监测服务器数据失败");
        }
        return Reply.ok(disList);
    }

    @Override
    public List<AddAndUpdateModelWebMonitorParam> batchCreateWebSeverData(List<AddAndUpdateModelWebMonitorParam> list) {
        Map<Integer, List<AddAndUpdateModelWebMonitorParam>> map = list.stream().collect(Collectors.groupingBy(s -> s.getMonitorServerId()));

        map.forEach((k, v) -> {
            Integer monitorServerId = k;
            List<AddAndUpdateModelWebMonitorParam> paramList = v;
            List<MWWebDto> zibbixDtoList = new ArrayList<>();
            List<MwModelWebMonitorTriggerParam> triggerParams = new ArrayList<>();
            List<String> assetsName = new ArrayList<>();
            for (AddAndUpdateModelWebMonitorParam aParam : paramList) {
                String hostId = aParam.getAssetsId();
                aParam.setIsManage(false);
                if (!Strings.isNullOrEmpty(hostId) && monitorServerId != null && monitorServerId != 0) {
                    aParam.setIsManage(true);
                }
                List<String> webids1 = new ArrayList<>();
                List<MWStep> stepList = new ArrayList<>();
                stepList.add(MWStep.builder()
                        .name(aParam.getInstanceName())
                        .no(1)
                        .required(aParam.getString())
                        .status_codes(aParam.getStatusCode())
                        .url(aParam.getWebUrl())
                        .timeout(aParam.getTimeOut().toString() + "s")
                        .followRedirects(aParam.getFollowJump() ? 1 : 0)
                        .build());
                MWWebDto zibbixDto = MWWebDto.builder()
                        .steps(stepList)
                        .name(aParam.getInstanceName())
                        .agent("Zabbix")
                        .hostId(hostId)
                        .delay(aParam.getUpdateInterval().toString() + "s")
                        .status(aParam.isEnable() ? 0 : 1)
                        .httpProxy(aParam.getHttpProxy())
                        .retries(aParam.getAttempts())
                        .build();
                zibbixDtoList.add(zibbixDto);

                String hostName = aParam.getAssetsName();
                String webMonitorName = aParam.getInstanceName();
                String code = aParam.getStatusCode();
                String webFailDescription = getWebDescription(aParam.getInstanceName(), WEB_TEST_RSPCODE);
                MwModelWebMonitorTriggerParam triggerParam = new MwModelWebMonitorTriggerParam();
                triggerParam.setDescription(webFailDescription);
                triggerParam.setHostName(hostName);
                triggerParam.setWebName(webMonitorName);
                triggerParam.setCode(code);
                triggerParam.setKey(WEB_TEST_RSPCODE);
                triggerParam.setPriority(String.valueOf(TriggerAlarmLevelEnum.ERROR.getCode()));
                triggerParams.add(triggerParam);
                assetsName.add(aParam.getAssetsName());
            }
            List<String> webids1 = new ArrayList<>();
            //zabbix创建web监测数据
            MWZabbixAPIResult results = mwtpServerAPI.HttpTestBatchCreate(monitorServerId, zibbixDtoList);
            if (results.code != 0) {
                log.error("zabbix创建web监测数据失败:{},{}", results.getMessage(),results.getData());
                return;
            }
            JsonNode result = (JsonNode) results.getData();
            JsonNode httpTestIds = (JsonNode) result.get("httptestids");
            for (int x = 0; x < httpTestIds.size(); x++) {
                String httptestids = httpTestIds.get(x).asText();
                webids1.add(httptestids);
                paramList.get(x).setHttpTestId(Integer.valueOf(httptestids));
            }
            MWZabbixAPIResult triggerFailCreate = mwtpServerAPI.triggerBatchCreate(monitorServerId, triggerParams);
            if (triggerFailCreate.getCode() != 0) {
                if (CollectionUtils.isNotEmpty(webids1)) {
                    mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
                    log.error("zabbix创建web监测触发器失败:{},{}", triggerFailCreate.getMessage(),triggerFailCreate.getData());
                }
            }
        });
        return list;
    }


    @Override
    public MwModelImportWebListParam transform(List<MwModelImportWebMonitorParam> list) {
        MwModelImportWebListParam importWebListParam = new MwModelImportWebListParam();
        try {

            //获取厂别，领域的对应数据。
            List<MwModelInstanceCommonParam> systemAndClassifyInfo = mwModelInstanceDao.getSystemAndClassifyInfo();
            Map<String, Integer> collect = systemAndClassifyInfo.stream().collect(Collectors.toMap(s -> s.getModelInstanceName(), s -> s.getModelInstanceId(), (
                    value1, value2) -> {
                return value2;
            }));
            List<MwModelImportWebMonitorParam> successLists = new ArrayList<>();
            List<MwModelImportWebMonitorParam> errorLists = new ArrayList<>();
            //获取所有web导入的调用服务器Ip
            Set<String> inBandIpSet = list.stream().filter(s -> !Strings.isNullOrEmpty(s.getInBandIp())).map(s -> s.getInBandIp()).collect(Collectors.toSet());
            QueryEsParam queryEsParam = new QueryEsParam();
            List<QueryModelInstanceByPropertyIndexParam> queryList = new ArrayList<>();
            QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(IN_BAND_IP);
            qParam.setPropertiesValueList(new ArrayList(inBandIpSet));
            queryList.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(MONITOR_MODE);
            qParam.setPropertiesValue("1");
            queryList.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(ASSETTYPE_ID_KEY);
            qParam.setPropertiesValue("1");
            queryList.add(qParam);
            queryEsParam.setExistsList(Arrays.asList(IN_BAND_IP, MONITOR_SERVER_ID, ASSETS_ID));
            queryEsParam.setParamLists(queryList);
            //查询调用服务器的es数据信息
            List<Map<String, Object>> instanceListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
            //数据转换，以IP为key，
            Map<String, List<Map<String, Object>>> assetMapByIp = instanceListMap.stream().filter(s -> !strValueConvert(s.get(IN_BAND_IP)).equals("")).collect(Collectors.groupingBy(s -> strValueConvert(s.get(IN_BAND_IP))));

            List<MwModelPowerDTO> mwGroupPowerDTOS = mwModelExportService.selectGroupIdInfo();
            Map<String, String> groupPowerCollect = mwGroupPowerDTOS.stream().filter(s -> !Strings.isNullOrEmpty(s.getName()) && !Strings.isNullOrEmpty(s.getId())).collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));

            List<MwModelPowerDTO> mwOrgPowerDTOS = mwModelExportService.selectOrgIdInfo();
            Map<String, String> orgPowerCollect = mwOrgPowerDTOS.stream().filter(s -> !Strings.isNullOrEmpty(s.getName()) && !Strings.isNullOrEmpty(s.getId())).collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));

            List<MwModelPowerDTO> mwUserPowerDTOS = mwModelExportService.selectUserIdInfo();
            Map<String, String> userPowerCollect = mwUserPowerDTOS.stream().filter(s -> !Strings.isNullOrEmpty(s.getName()) && !Strings.isNullOrEmpty(s.getId())).collect(Collectors.toMap(s -> s.getName(), s -> s.getId(), (
                    value1, value2) -> {
                return value2;
            }));

            for (MwModelImportWebMonitorParam param : list) {
                List<String> errorMsg = new ArrayList<>();
                int errorNum = 0;
                if (Strings.isNullOrEmpty(param.getInstanceName())) {
                    errorMsg.add("web监测名称不可为空");
                    errorNum++;
                }
                if (Strings.isNullOrEmpty(param.getWebUrl())) {
                    errorMsg.add("web监测URL不可为空");
                    errorNum++;
                }

                if (Strings.isNullOrEmpty(param.getInBandIp())) {
                    errorMsg.add("调用服务器IP不可为空");
                    errorNum++;
                } else {
                    if (assetMapByIp != null && assetMapByIp.containsKey(param.getInBandIp())) {
                        List<Map<String, Object>> ckeckDTO = assetMapByIp.get(param.getInBandIp());
                        if ((CollectionUtils.isEmpty(ckeckDTO) || ckeckDTO.get(0) == null)) {
                            errorMsg.add("Ip地址调用服务器不存在");
                            errorNum++;
                        }
                        if (intValueConvert(ckeckDTO.get(0).get(ASSETS_ID)) == 0) {
                            errorMsg.add("Ip地址对应的资产服务器未纳管");
                            errorNum++;
                        }
                        param.setAssetsId(ckeckDTO.get(0).get(ASSETS_ID).toString());
                        param.setAssetsName(ckeckDTO.get(0).get(INSTANCE_NAME_KEY).toString());
                        param.setMonitorServerId(ckeckDTO.get(0).get(MONITOR_SERVER_ID).toString());
                        param.setIsManage(true);
                        param.setClient(101);
                    }
                }

                if (intValueConvert(param.getUpdateInterval()) == 0) {
                    param.setUpdateInterval(120);//默认值
                }

                if (intValueConvert(param.getAttempts()) == 0) {
                    param.setAttempts(1);//默认值
                }

                if (param.getEnable() == null) {
                    param.setEnable(true);//默认值
                }
                if (intValueConvert(param.getTimeOut()) == 0) {
                    param.setTimeOut(10);//默认值
                }

                if (intValueConvert(param.getStatusCode()) == 0) {
                    param.setStatusCode("200");//默认值
                }

                //负责人
                List<Integer> userIds = new ArrayList<>();
                if (Strings.isNullOrEmpty(param.getPrincipalName())) {
                    Integer userId = 106;
                    if (iLoginCacheInfo != null && iLoginCacheInfo.getLoginName() != null
                            && iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()) != null) {
                        userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
                    }
                    userIds.add(userId);
                } else {
                    String[] userArr = param.getPrincipalName().split("/");
                    for (String userName : userArr) {
                        if (userPowerCollect != null && userPowerCollect.containsKey(userName)) {
                            String userId = userPowerCollect.get(userName);
                            userIds.add(intValueConvert(userId));
                        }
                    }
                    param.setUserIds(userIds);
                    if (userIds == null|| userIds.size() != userArr.length) {
                        errorMsg.add("负责人名称错误或者不存在该用户");
                        errorNum++;
                        param.setUserIds(new ArrayList<>());
                    }
                }

                //机构
                List orgIdList = new ArrayList();
                if (!Strings.isNullOrEmpty(param.getOrgs())) {
                    String[] orgArr = param.getOrgs().split("/");
                    for (String orgName : orgArr) {
                        if (orgPowerCollect != null && orgPowerCollect.containsKey(orgName)) {
                            String orgId = orgPowerCollect.get(orgName);
                            String[] strArr = orgId.split(",");
                            List<String> listStr = Arrays.asList(strArr);
                            List<Integer> listInt = listStr.stream().map(Integer::parseInt).collect(Collectors.toList());
                            orgIdList.add(listInt);
                        } else {
                            errorMsg.add("机构名称错误或者不存在该机构");
                            errorNum++;
                        }
                    }
                    param.setOrgIds(orgIdList);
                }

                //用户组
                if (!Strings.isNullOrEmpty(param.getGroups())) {
                    List<Integer> groupIds = new ArrayList<>();
                    String[] groupArr = param.getGroups().split("/");
                    for (String groupName : groupArr) {
                        if (groupPowerCollect != null && groupPowerCollect.containsKey(groupName)) {
                            String groupId = groupPowerCollect.get(groupName);
                            groupIds.add(intValueConvert(groupId));
                        }
                    }
                    param.setGroupIds(groupIds);
                    if (groupIds == null || groupIds.size() != groupArr.length) {
                        errorMsg.add("用户组名称错误或者不存在该用户组");
                        errorNum++;
                        param.setGroupIds(new ArrayList<>());
                    }
                }

                //厂别
                if (!Strings.isNullOrEmpty(param.getModelSystem())) {
                    if (collect == null || !collect.containsKey(strValueConvert(param.getModelSystem()))) {
                        errorMsg.add("厂别名称错误或者不存在该厂别");
                        errorNum++;
                    } else {
                        param.setModelSystem(collect.get(strValueConvert(param.getModelSystem())) + "");
                    }
                }

                //领域
                if (!Strings.isNullOrEmpty(param.getModelClassify())) {
                    if (collect == null || !collect.containsKey(strValueConvert(param.getModelClassify()))) {
                        errorMsg.add("领域名称错误或者不存在该领域");
                        errorNum++;
                    } else {
                        param.setModelClassify(collect.get(strValueConvert(param.getModelClassify())) + "");
                    }
                }

                if (errorNum == 0) {
                    //正常数据
                    successLists.add(param);
                } else {
                    //异常数据
                    param.setErrorMsg(String.join(",", errorMsg));
                    errorLists.add(param);
                }
            }
            log.info("web监测导入正常数据::"+successLists);
            log.info("web监测导入异常数据::"+errorLists);
            importWebListParam.setSuccessList(successLists);
            importWebListParam.setErrorList(errorLists);
        } catch (Exception e) {
            log.error("web监测导入数据校验失败", e);
        }
        return importWebListParam;
    }


    @Override
    public AddAndUpdateModelWebMonitorParam createWebSeverData(AddAndUpdateModelWebMonitorParam aParam) {
        String hostId = aParam.getAssetsId();
        Integer monitorServerId = aParam.getMonitorServerId();
        aParam.setIsManage(false);
        if (!Strings.isNullOrEmpty(hostId) && monitorServerId != null && monitorServerId != 0) {
            aParam.setIsManage(true);
        }
        List<String> webids1 = new ArrayList<>();
        List<MWStep> stepList = new ArrayList<>();
        stepList.add(MWStep.builder()
                .name(aParam.getInstanceName())
                .no(1)
                .required(aParam.getString())
                .status_codes(aParam.getStatusCode())
                .url(aParam.getWebUrl())
                .timeout(aParam.getTimeOut().toString() + "s")
                .followRedirects(aParam.getFollowJump() ? 1 : 0)
                .build());
        MWWebDto zibbixDto = MWWebDto.builder()
                .steps(stepList)
                .name(aParam.getInstanceName())
                .agent("Zabbix")
                .hostId(hostId)
                .delay(aParam.getUpdateInterval().toString() + "s")
                .status(aParam.isEnable() ? 0 : 1)
                .httpProxy(aParam.getHttpProxy())
                .retries(aParam.getAttempts())
                .build();
        //zabbix创建web监测数据
        MWZabbixAPIResult result = mwtpServerAPI.HttpTestCreate(monitorServerId, zibbixDto);
        log.info("result:" + JSON.toJSONString(result));
        if (result.code != 0) {
            log.error("zabbix创建web监测数据失败:", result.getMessage());
            return null;
        }
        JsonNode map = (JsonNode) result.getData();
        webids1.add(map.get("httptestids").get(0).toString());
        //zabbix创建web监测触发器
        String webFailDescription = getWebDescription(aParam.getInstanceName(), WEB_TEST_RSPCODE);
        MWZabbixAPIResult hostResult = mwtpServerAPI.hostGetById(monitorServerId, hostId);
        if (hostResult.getCode() != 0) {
            mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
            log.error("查询主机信息失败:", hostResult.getMessage());
        }
        JsonNode hostmap = (JsonNode) hostResult.getData();
        String hostName = hostmap.get(0).get("host").asText();
        String webMonitorName = aParam.getInstanceName();
        String code = aParam.getStatusCode();
        //创建web.rspcode监测触发器
        MWZabbixAPIResult triggerFailCreate = mwtpServerAPI.triggerCreateByKey(monitorServerId, webFailDescription, hostName, webMonitorName, WEB_TEST_RSPCODE, code, String.valueOf(TriggerAlarmLevelEnum.ERROR.getCode()));
        if (triggerFailCreate.getCode() != 0) {
            mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
            log.error("zabbix创建web.rspcode监测触发器失败:", triggerFailCreate.getMessage());
        }
        //创建web.error监测触发器
        MWZabbixAPIResult triggerErrorCreate = mwtpServerAPI.triggerCreateByKey(monitorServerId, webFailDescription, hostName, webMonitorName, WEB_TEST_ERROR, code, String.valueOf(TriggerAlarmLevelEnum.ERROR.getCode()));
        if (triggerErrorCreate.getCode() != 0) {
            mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
            log.error("zabbix创建web.error监测触发器失败:", triggerErrorCreate.getMessage());
        }

        //添加zabbix返回数据到数据库
        aParam.setHttpTestId(map.get("httptestids").get(0).asInt());
        aParam.setAssetsId(aParam.getAssetsId());
        return aParam;
    }

    @Override
    public List<MwModelWebMonitorTable> queryWebSeverList(List<MwModelWebMonitorTable> params) {
        long time1 = System.currentTimeMillis();
        List<MwModelWebMonitorTable> newmwWebMonitores = new ArrayList<>();
        try {
            List<MwModelWebMonitorTable> mwWebMonitores = params;
            List<String> hostIds = new ArrayList<>();
            Map mapval = new HashMap();
            Map<Integer, List> serverIdMap = new HashMap();
            for (MwModelWebMonitorTable mwWebMonitorDTO : mwWebMonitores) {
                if (serverIdMap.containsKey(mwWebMonitorDTO.getMonitorServerId())) {
                    hostIds = serverIdMap.get(mwWebMonitorDTO.getMonitorServerId());
                    if (!mapval.containsKey(mwWebMonitorDTO.getAssetsId())) {
                        hostIds.add(mwWebMonitorDTO.getAssetsId());
                    }
                    serverIdMap.put(mwWebMonitorDTO.getMonitorServerId(), hostIds);
                } else {
                    hostIds = new ArrayList<>();
                    hostIds.add(mwWebMonitorDTO.getAssetsId());
                    serverIdMap.put(mwWebMonitorDTO.getMonitorServerId(), hostIds);
                }
                mapval.put(mwWebMonitorDTO.getAssetsId(), mwWebMonitorDTO.getAssetsId());
                if (IpV4Util.isValidIpv4Addr(mwWebMonitorDTO.getInBandIp())) {
                    mwWebMonitorDTO.setIpType("IPv4");
                } else if (IpV6Util.isIP(mwWebMonitorDTO.getInBandIp())) {
                    mwWebMonitorDTO.setIpType("IPv6");
                }
            }
            Map<String, MWWebValue> webValues = new HashMap<>();
            List<Future<Map<String, MWWebValue>>> lists = new ArrayList<>();
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(serverIdMap.entrySet().size(), serverIdMap.entrySet().size() + 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            for (Map.Entry<Integer, List> entrys : serverIdMap.entrySet()) {
                final Map.Entry<Integer, List> entry = entrys;
                Callable<Map<String, MWWebValue>> callable = new Callable<Map<String, MWWebValue>>() {
                    @Override
                    public Map<String, MWWebValue> call() throws Exception {
                        Integer k = entry.getKey();
                        List v = entry.getValue();
                        Integer serverId = k;
                        List<String> hostIdList = v;
                        Map<String, MWWebValue> webValue = mwWebZabbixManger.getWebValue(serverId, hostIdList);
                        return webValue;
                    }
                };
                Future<Map<String, MWWebValue>> submit = executorService.submit(callable);
                lists.add(submit);
            }
            if (lists.size() > 0) {
                lists.forEach(f -> {
                    try {
                        Map<String, MWWebValue> map = f.get(20, TimeUnit.SECONDS);
                        webValues.putAll(map);
                    } catch (Exception e) {
                        log.error("fail to selectList:多线程等待数据返回失败 param:{},cause:{}", e);
                    }
                });
            }
            executorService.shutdown();
            log.info("关闭线程池");

            for (MwModelWebMonitorTable mwWebMonitorDTO : mwWebMonitores) {
                mwWebMonitorDTO.setIsManage(false);
                if (!Strings.isNullOrEmpty(mwWebMonitorDTO.getAssetsId()) && mwWebMonitorDTO.getMonitorServerId() != null && mwWebMonitorDTO.getMonitorServerId() != 0) {
                    mwWebMonitorDTO.setIsManage(true);
                }
                MWWebValue mwWebValue = webValues.get(mwWebMonitorDTO.getAssetsId() + "_" + mwWebMonitorDTO.getInstanceName());
                if (null != mwWebMonitorDTO.getTimeOut()) {
                    mwWebMonitorDTO.setFullTimeOut(mwWebMonitorDTO.getTimeOut() + "s");
                }
                if (mwWebValue != null) {
                    if (null != mwWebValue.getState()) {
                        mwWebMonitorDTO.setWebState(mwWebValue.getState());
                    }
                    if (null != mwWebValue.getBps()) {
                        BigDecimal bps = new BigDecimal(mwWebValue.getBps());
                        mwWebMonitorDTO.setSortDownloadSpeed(bps.longValue());
                        if (bps.intValue() < 1024) {
                            mwWebMonitorDTO.setDownloadSpeed(bps + "bps");
                        } else if (1024 * 1024 > bps.intValue() && bps.intValue() > 1024) {
                            mwWebMonitorDTO.setDownloadSpeed(bps.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP) + "Kbps");
                        } else if (1024 * 1024 * 1024 > bps.intValue() && bps.intValue() > 1024 * 1024) {
                            mwWebMonitorDTO.setDownloadSpeed(bps.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "Mbps");
                        } else {
                            mwWebMonitorDTO.setDownloadSpeed(bps.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "Gbps");
                        }
                    }
                    if (null != mwWebValue.getResp()) {
                        mwWebMonitorDTO.setResponseTime(new BigDecimal(mwWebValue.getResp()).multiply(BigDecimal.valueOf(1000)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "ms");
                    }
                    if (null != mwWebValue.getRcode()) {
                        mwWebMonitorDTO.setMonitorCode(mwWebValue.getRcode());
                    }
                }
                newmwWebMonitores.add(mwWebMonitorDTO);
            }
            long time2 = System.currentTimeMillis();
            ////System.out.println("web监测修改版耗时：" + (time2 - time1) + "ms" + "webValues的大小为：" + webValues.size() + "；newmwWebMonitores 的大小：" + newmwWebMonitores.size());
            log.info("web监测修改版耗时：" + (time2 - time1) + "ms" + "webValues的大小为：" + webValues.size() + "；newmwWebMonitores 的大小：" + newmwWebMonitores.size());
        } catch (Exception e) {
            log.error("fail to selectList with mtaDTO={}", e);
        }
        return newmwWebMonitores;
    }

    /**
     * 批量更新资产信息
     *
     * @param params
     * @return
     */
    @Override
    public Reply updateWebMonitor(List<AddAndUpdateModelWebMonitorParam> params) {
        try {
            List<MWWebDto> WebDtoList = new ArrayList<>();
            Map<Integer, List<AddAndUpdateModelWebMonitorParam>> mapList = params.stream().collect(Collectors.groupingBy(s -> s.getMonitorServerId()));
            mapList.forEach((k, v) -> {
                Integer monitorServerId = k;
                for (AddAndUpdateModelWebMonitorParam mwTangWebMonitor : v) {
                    String hostId = mwTangWebMonitor.getAssetsId();
                    List<String> httpTestIds = new ArrayList<>();
                    httpTestIds.add(mwTangWebMonitor.getHttpTestId() + "");
                    List<MWStep> stepList = new ArrayList<>();
                    stepList.add(MWStep.builder()
                            .name(mwTangWebMonitor.getInstanceName())
                            .no(1)
                            .required(mwTangWebMonitor.getString())
                            .status_codes(mwTangWebMonitor.getStatusCode())
                            .url(mwTangWebMonitor.getWebUrl())
                            .timeout(mwTangWebMonitor.getTimeOut().toString() + "s")
                            .followRedirects(mwTangWebMonitor.getFollowJump() ? 1 : 0)
                            .build());
                    MWWebDto zibbixDto = MWWebDto.builder()
                            .steps(stepList)
                            .name(mwTangWebMonitor.getInstanceName())
                            .agent("Zabbix")
                            .hostId(hostId)
                            .delay(mwTangWebMonitor.getUpdateInterval().toString() + "s")
                            .status(mwTangWebMonitor.isEnable() ? 0 : 1)
                            .httpProxy(mwTangWebMonitor.getHttpProxy())
                            .retries(mwTangWebMonitor.getAttempts())
                            .build();
                    MWZabbixAPIResult result = null;
                    zibbixDto.setHttptestids(mwTangWebMonitor.getHttpTestId().toString());
                    result = mwtpServerAPI.HttpTestUpdate(monitorServerId, zibbixDto);
                    WebDtoList.add(zibbixDto);
                }
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.HttpTestBatchUpdate(monitorServerId, WebDtoList);
                if(mwZabbixAPIResult.isFail() && "No permissions to referred object or it does not exist!".equals(mwZabbixAPIResult.getData())){
                    if(CollectionUtils.isNotEmpty(params)){
                        AddAndUpdateModelWebMonitorParam webSeverData = createWebSeverData(params.get(0));
                        webSeverData.getHttpTestId();
                    }
                }
            });
            return Reply.ok("zabbix更新Web监测成功");
        } catch (Exception e) {
            log.error("fail to updateWebMonitor with cause:{}", e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301003, ErrorConstant.WEBMONITOR_MSG_301003);
        }
    }

    /**
     * 删除资产信息
     *
     * @param params
     * @return
     */
    @Override
    public Reply deleteWebMonitor(List<HttpParam> params) throws Exception {
        if (CollectionUtils.isNotEmpty(params)) {
            Map<Integer, List<HttpParam>> collect = params.stream().collect(Collectors.groupingBy(HttpParam::getMonitorServerId));
            for (Integer key : collect.keySet()) {
                List<HttpParam> listparam = collect.get(key);
                List<String> list = new ArrayList<>();
                for (HttpParam dto : listparam) {
                    list.add(dto.getHttpId());
                }
                String result = mwWebZabbixManger.HttpTestDelete(key, list);
                if ("删除失败".equals(result)) {
                    throw new RuntimeException("删除失败。请联系管理员！");
                }
            }
        }
        return Reply.ok("删除成功");
    }

    @Override
    public Reply excelTemplateExport(ExcelExportParam excelExportParam, HttpServletResponse response) {
        List<MwModelImportWebMonitorParam> list = new ArrayList<>();

        MwModelImportWebMonitorParam importWebMonitorParam = MwModelImportWebMonitorParam.builder()
                .instanceName("微信QQ")
                .webUrl("www.weixin.qq.com")
                .inBandIp("172.168.99.204")  //塞尔
                .updateInterval(120)
                .attempts(1)
                .enable(true)
                .timeOut(10)
                .statusCode("200")
                .monitorServerName("zabbix5.2.2")
                .principalName("负责人(多用户以/分隔)")
                .orgs("机构(多机构以/分隔)")
                .groups("用户组(多个组以/分隔)")
                .modelSystem("t3")
                .modelClassify("FAT")
                .build();
        list.add(importWebMonitorParam);
        Set<String> includeColumnFiledNames = new HashSet<>();
        if (excelExportParam.getFields().size() > 0) {
            includeColumnFiledNames = excelExportParam.getFields();
        } else {
            includeColumnFiledNames.add("instanceName");
            includeColumnFiledNames.add("webUrl");
            includeColumnFiledNames.add("inBandIp");
            includeColumnFiledNames.add("updateInterval");
            includeColumnFiledNames.add("attempts");

            includeColumnFiledNames.add("enable");
            includeColumnFiledNames.add("timeOut");
            includeColumnFiledNames.add("statusCode");

            includeColumnFiledNames.add("monitorServerName");
            includeColumnFiledNames.add("principalName");
            includeColumnFiledNames.add("orgs");
            includeColumnFiledNames.add("groups");
            includeColumnFiledNames.add("modelSystem");
            includeColumnFiledNames.add("modelClassify");
        }
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter(excelExportParam.getName(), response, MwModelImportWebMonitorParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            log.info("导出成功");
        } catch (IOException e) {
            log.error("导出失败{}", e);
            return Reply.fail(500, "导出失败");
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
        return Reply.ok("导出成功");
    }

    /**
     * web监测数据导入
     *
     * @param file
     * @return
     */
    @Override
    public void excelImportWebMonitor(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                EasyExcel.read(file.getInputStream(), MwModelImportWebMonitorParam.class, new ModelWebExcelMonitorListener(mwModelInstanceService, response, fileName, mwModelWebMonitorService)).sheet().doRead();
            } else {
                log.error("没有传入正确的excel文件名", file);
            }
        } catch (Exception e) {
            log.error("fail to excelImport with MultipartFile={}, cause:{}", file, e);
        }
//
//        try {
//            //获取导入数据
//            Reply reply = mwModelExportService.importWebMonitorData(file);
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return;
//            }
//            ModelWebMonitorParam webMonitorParam = (ModelWebMonitorParam) reply.getData();
//            List<MwModelImportWebMonitorParam> importWebList = new ArrayList<>();
//            String errorMessage = "";
//            if (webMonitorParam != null) {
//                importWebList = webMonitorParam.getImportWebList();
//                errorMessage = webMonitorParam.getErrorMessage();
//            }
//            List<AddAndUpdateModelInstanceParam> addAndUpdateModelInstanceParams = new ArrayList<>();
//            //数据转换为List<AddAndUpdateModelInstanceParam>，进行批量新增
//            if (CollectionUtils.isNotEmpty(importWebList)) {
//                addAndUpdateModelInstanceParams = mwModelInstanceService.getPropertiesInfoList(importWebList, webMonitorModeId);
//            }
//            List<AddAndUpdateModelInstanceParam> batchParamList = mwModelInstanceService.batchInsertWebMonitorInstance(addAndUpdateModelInstanceParams);
//            //批量插入数据
//            if (batchParamList != null && batchParamList.size() > 0) {
//                mwModelInstanceService.saveData(batchParamList, true, true);
//            }
//            if (Strings.isNullOrEmpty(errorMessage)) {
//                return Reply.ok("web监测导入数据成功");
//            } else {
//                return Reply.warn(errorMessage);
//            }
//        } catch (Exception e) {
//            log.error("web监测导入数据失败", e);
//        }

    }

    @Override
    public Reply updateState(MwModelUpdateWebMonitorStateParam updateWebMonitorStateParam) {
        try {
            if (updateWebMonitorStateParam.isEnable()) {
                List<Map<String, Object>> assetsDataList = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                        .assetsId(updateWebMonitorStateParam.getAssetsId()).instanceName(updateWebMonitorStateParam.getAssetsName()).monitorServerId(updateWebMonitorStateParam.getMonitorServerId()).build());
                if (CollectionUtils.isNotEmpty(assetsDataList)) {
                    for (Map<String, Object> m : assetsDataList) {
                        if (m.get(MONITOR_FLAG) != null) {
                            String instanceName = m.get(INSTANCE_NAME_KEY).toString();
                            Boolean monitorFlag = Boolean.valueOf(m.get(MONITOR_FLAG).toString());
                            if (!monitorFlag) {//资产处于关闭状态
                                return Reply.fail(ErrorConstant.WEBMONITORCODE_301005, instanceName + ":资产监控状态未启用,无法开启WEB监测");
                            }
                        }
                    }
                }
            }
            mwWebZabbixManger.HttpTestUpdate(updateWebMonitorStateParam.getMonitorServerId(), updateWebMonitorStateParam.getHttpTestId(), updateWebMonitorStateParam.isEnable() ? 0 : 1);
            List<AddAndUpdateModelInstanceParam> updateParams = new ArrayList<>();
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setEsId(updateWebMonitorStateParam.getModelIndex() + updateWebMonitorStateParam.getModelInstanceId());
            instanceParam.setModelIndex(updateWebMonitorStateParam.getModelIndex());
            List<AddModelInstancePropertiesParam> propertiesList = new ArrayList<>();
            AddModelInstancePropertiesParam propertiesParam = new AddModelInstancePropertiesParam();
            propertiesParam = new AddModelInstancePropertiesParam();
            propertiesParam.setPropertiesIndexId("enable");
            propertiesParam.setPropertiesValue(updateWebMonitorStateParam.isEnable() + "");
            propertiesParam.setPropertiesType(ModelPropertiesType.SWITCH.getCode());
            propertiesList.add(propertiesParam);
            instanceParam.setPropertiesList(propertiesList);
            updateParams.add(instanceParam);
            //修改监控状态
            mwModelInstanceService.batchUpdateModelInstance(updateParams);
        } catch (Exception e) {
            log.error("fail to updateState with updateWebMonitorStateParam={}, cause:{}", updateWebMonitorStateParam, e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301005, ErrorConstant.WEBMONITOR_MSG_301005);
        }
        return Reply.ok("更新成功");
    }


    private String getWebDescription(String webName, String key) {
        StringBuffer sb = new StringBuffer("[网站监测]");
        sb.append("[" + webName + "]");
        if (key.equals(WEB_TEST_TIME)) {
            sb.append("网站延时持续6分钟大于200ms");
        } else if (key.equals(WEB_TEST_ERROR)) {
            sb.append("网页告警");
        } else {
            sb.append("网站状态故障");
        }
        return sb.toString();
    }

    private String getWebRexExpression(String hostName, String webName, String key) {
        StringBuffer sb = new StringBuffer("{");
        sb.append(hostName);
        sb.append(":");
        sb.append(key);
        if (key.equals(WEB_TEST_TIME)) {
//            sb.append("[" + webName + "," + webName + "," + "resp]");
//            sb.append(".last(,360s)}>200");
        } else {
            sb.append("[" + webName + "]");
            sb.append(".last(#3)}=0");
        }
        return sb.toString();
    }

    private String getWebExpression(String hostName, String webName, String key) {
        StringBuffer sb = new StringBuffer("{");
        sb.append(hostName);
        sb.append(":");
        sb.append(key);
        if (key.equals(WEB_TEST_TIME)) {
            sb.append("[" + webName + "," + webName + "," + "resp]");
            sb.append(".last(,360s)}>200");
        } else {
            sb.append("[" + webName + "]");
            sb.append(".last(#3)}=1");
        }
        return sb.toString();
    }
}
