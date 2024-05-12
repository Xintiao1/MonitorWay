package cn.mw.monitor.webMonitor.service.impl;


import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.api.common.IpV6Util;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.model.RedisItemHistoryDto;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.dto.UpdateDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.NewUnits;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.webMonitor.api.param.webMonitor.*;
import cn.mw.monitor.webMonitor.dao.MwWebmonitorTableDao;
import cn.mw.monitor.webMonitor.dto.*;
import cn.mw.monitor.webMonitor.exception.TransformException;
import cn.mw.monitor.webMonitor.service.ExcelWebMonitorListener;
import cn.mw.monitor.webMonitor.service.MwWebMonitorService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.dto.MWStep;
import cn.mw.zbx.dto.MWWebDto;
import cn.mw.zbx.dto.MWWebValue;
import cn.mw.zbx.enums.ation.TriggerAlarmLevelEnum;
import cn.mw.zbx.manger.MWWebZabbixManger;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
/**
 * Created by baochengbin on 2020/3/12.
 */
@Service
@Slf4j
@Transactional
public class MwWebMonitorServiceImpl implements MwWebMonitorService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/webMonitor");

    private static final String WEB_TEST_FAIL = "web.test.fail";
    private static final String WEB_TEST_TIME = "web.test.time";

    @Resource
    private MwWebmonitorTableDao mwWebmonitorTableDao;


    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private MWWebZabbixManger mwWebZabbixManger;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MwWebMonitorService webMonitorService;

    @Autowired
    LicenseManagementService licenseManagement;

    @Autowired
    CheckCountService checkCountService;

    @Autowired
    private MWUserService userService;


    /**
     * 根据资产ID取机构信息
     *
     * @param assetsId 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(Integer assetsId) {
        try {
            MwWebMonitorDTO mwTangWebMonitor = mwWebmonitorTableDao.selectByPrimaryKey(assetsId);
            DataPermission dataPermission = mwCommonService.getDataPermissionDetail(DataType.WEB_MONITOR, assetsId + "");
            mwTangWebMonitor.setPrincipal(dataPermission.getPrincipal());
            mwTangWebMonitor.setDepartment(dataPermission.getDepartment());
            mwTangWebMonitor.setGroup(dataPermission.getGroups());

            MwWebMonitorByIdDTO mtDtos = CopyUtils.copy(MwWebMonitorByIdDTO.class, mwTangWebMonitor);
            mtDtos.setGroupIds(dataPermission.getGroupIds());
            mtDtos.setOrgIds(dataPermission.getOrgNodes());
            mtDtos.setPrincipal(dataPermission.getUserIds());

            logger.info("ACCESS_LOG[]WebMonitor[]Web监测管理[]根据自增序列ID取资产信息[]{}", assetsId);
            return Reply.ok(mtDtos);
        } catch (Exception e) {
            log.error("fail to selectById with d={}, cause:{}", assetsId, e.getMessage());
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301001, ErrorConstant.WEBMONITOR_MSG_301001);
        }
    }

    /**
     * 查询资产信息list
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply selectList(QueryWebMonitorParam qParam) {
        long time1 = System.currentTimeMillis();
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            List<String> typeIdList = userService.getAllTypeIdList(userInfo, DataType.WEB_MONITOR);
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<MwWebMonitorDTO> mwWebMonitores = new ArrayList<>();
            Map criteria = PropertyUtils.describe(qParam);
            criteria.put("listSet", Joiner.on(",").join(typeIdList));
            criteria.put("isSystem", userInfo.isSystemUser());
            mwWebMonitores = mwWebmonitorTableDao.selectList(criteria);

            //获取数据权限
            List<String> ids = new ArrayList<>();
            for (MwWebMonitorDTO monitor : mwWebMonitores) {
                ids.add(monitor.getId()+"");
            }
            List<DataPermission> dataPermissionList = mwCommonService.getDataAuthByIds(DataType.WEB_MONITOR, ids);
            Map<String, DataPermission> permissionMap = new HashMap<>();
            for (DataPermission permission : dataPermissionList) {
                permissionMap.put(permission.getId(), permission);
            }
            for (MwWebMonitorDTO monitor : mwWebMonitores) {
                DataPermission dataPermission = permissionMap.get(monitor.getId()+"");
                if (dataPermission != null){
                    monitor.setGroup(dataPermission.getGroups());
                    monitor.setDepartment(dataPermission.getDepartment());
                    monitor.setPrincipal(dataPermission.getPrincipal());
                }
            }

            PageInfo pageInfo = new PageInfo<>(mwWebMonitores);
            List<String> hostIds = new ArrayList<>();
            Map mapval = new HashMap();
            Map<Integer, List> serverIdMap = new HashMap();
            for (MwWebMonitorDTO mwWebMonitorDTO : mwWebMonitores) {
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
                        log.error("fail to selectList:多线程等待数据返回失败 param:{},cause:{}",qParam, e);
                    }
                });
            }
            executorService.shutdown();
            logger.info("关闭线程池");
            List<MwWebMonitorDTO> newmwWebMonitores = new ArrayList<>();
            for (MwWebMonitorDTO mwWebMonitorDTO : mwWebMonitores) {
                MWWebValue mwWebValue = webValues.get(mwWebMonitorDTO.getAssetsId() + "_" + mwWebMonitorDTO.getWebName());
                if (null != mwWebMonitorDTO.getTimeOut()) {
                    mwWebMonitorDTO.setFullTimeOut(mwWebMonitorDTO.getTimeOut() + "s");
                }
                if (mwWebValue != null) {
                    if (null != mwWebValue.getState()) {
                        mwWebMonitorDTO.setWebState(mwWebValue.getState());
                    }
                    if (null != mwWebValue.getBps()) {
                        BigDecimal bps = new BigDecimal(mwWebValue.getBps());
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
            pageInfo.setList(newmwWebMonitores);
            long time2 = System.currentTimeMillis();
            ////System.out.println("web监测修改版耗时：" + (time2 - time1) + "ms" + "webValues的大小为：" + webValues.size() + "；newmwWebMonitores 的大小：" + newmwWebMonitores.size());
            logger.info("web监测修改版耗时：" + (time2 - time1) + "ms" + "webValues的大小为：" + webValues.size() + "；newmwWebMonitores 的大小：" + newmwWebMonitores.size());
            logger.info("ASSETS_LOG[]assets[]Web监测管理[]查询Web监测信息[]{}[]", qParam);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            logger.error("fail to selectList with mtaDTO={}", qParam);
            logger.error("error: ", e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301001, ErrorConstant.WEBMONITOR_MSG_301001);
        }
    }


    /**
     * 更新资产信息
     *
     * @param uParam
     * @return
     */
    @Override
    public Reply updateWebMonitor(BatchUpdateParam uParam) {
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        List<Integer> ids = uParam.getIds();

        if (ids != null && ids.size() > 0) {
            return updateBatchWebMonitor(uParam);
        } else {
            return updateOneWebMonitor(uParam);
        }

    }

    /**
     * 单个更新资产信息
     *
     * @param uParam
     * @return
     */
//    @Override
    public Reply updateOneWebMonitor(BatchUpdateParam uParam) {
        try {
            String id = uParam.getHostId();
            HostDto hostDto = mwWebmonitorTableDao.getHostIdAndServerId(id);
            String hostId = hostDto.getAssetsId();
            Integer monitorServerId = hostDto.getMonitorServerId();
            String httpProxy = uParam.getHttpProxy();
            MwWebMonitorDTO mwTangWebMonitor = mwWebmonitorTableDao.selectByPrimaryKey(uParam.getId());
            boolean updateFlag = false;
            if (httpProxy.equals(mwTangWebMonitor.getHttpProxy())) {
                updateFlag = true;
            }
            List<MWStep> originalStepList = new ArrayList<>();
            originalStepList.add(MWStep.builder()
                    .name(mwTangWebMonitor.getWebName())
                    .no(1)
                    .required(mwTangWebMonitor.getString())
                    .status_codes(mwTangWebMonitor.getStatusCode())
                    .url(mwTangWebMonitor.getWebUrl())
                    .timeout(mwTangWebMonitor.getTimeOut().toString() + "s")
                    .followRedirects(mwTangWebMonitor.getFollowJump() ? 1 : 0)
                    .build());
            MWWebDto originalZibbixDto = MWWebDto.builder()
                    .steps(originalStepList)
                    .name(mwTangWebMonitor.getWebName())
                    .agent("Zabbix")
                    .hostId(hostId)
                    .delay(mwTangWebMonitor.getUpdateInterval().toString() + "s")
                    .status(mwTangWebMonitor.getEnable().equals("ACTIVE") ? 0 : 1)
                    .httpProxy(mwTangWebMonitor.getHttpProxy())
                    .retries(mwTangWebMonitor.getAttempts())
                    .build();

            List<String> httpTestIds = new ArrayList<>();
            httpTestIds.add(uParam.getHttpTestId().toString());
            List<MWStep> stepList = new ArrayList<>();
            stepList.add(MWStep.builder()
                    .name(uParam.getWebName())
                    .no(1)
                    .required(uParam.getString())
                    .status_codes(uParam.getStatusCode())
                    .url(uParam.getWebUrl())
                    .timeout(uParam.getTimeOut().toString() + "s")
                    .followRedirects(uParam.getFollowJump() ? 1 : 0)
                    .build());
            MWWebDto zibbixDto = MWWebDto.builder()
                    .steps(stepList)
                    .name(uParam.getWebName())
                    .agent("Zabbix")
                    .hostId(hostId)
                    .delay(uParam.getUpdateInterval().toString() + "s")
                    .status(uParam.getEnable().equals("ACTIVE") ? 0 : 1)
                    .httpProxy(uParam.getHttpProxy())
                    .retries(uParam.getAttempts())
                    .build();
            if (!zibbixDto.equals(originalZibbixDto)) {
                MWZabbixAPIResult result = null;

                if (updateFlag) {
                    zibbixDto.setHttptestids(uParam.getHttpTestId().toString());
                    result = mwtpServerAPI.HttpTestUpdate(monitorServerId, zibbixDto);
                } else {
                    mwtpServerAPI.HttpTestDelete(monitorServerId, httpTestIds);
                    result = mwtpServerAPI.HttpTestCreate(monitorServerId, zibbixDto);
                    JsonNode map = (JsonNode) result.getData();
                    uParam.setHttpTestId(map.get("httptestids").get(0).asInt());
                }

                if (result.code != 0) {
                    return Reply.fail(ErrorConstant.WEBMONITORCODE_301002, result.getData().toString());
                }
            }

//            uParam.setCreator(iLoginCacheInfo.getLoginName());
//            uParam.setModifier(iLoginCacheInfo.getLoginName());
            mwWebmonitorTableDao.update(uParam);
            //刪除用戶用户组机构关系
            DeleteDto deleteDto = DeleteDto.builder().typeId(String.valueOf(uParam.getId())).type(DataType.WEB_MONITOR.getName()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);

            //绑定用户用户组机构
            InsertDto report = InsertDto.builder()
                    .groupIds(uParam.getGroupIds())
                    .userIds(uParam.getPrincipal())
                    .orgIds(uParam.getOrgIds())
                    .typeId(String.valueOf(uParam.getId()))
                    .type(DataType.WEB_MONITOR.getName())
                    .desc(DataType.WEB_MONITOR.getDesc()).build();

            //添加负责人
            mwCommonService.addMapperAndPerm(report);

            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateWebMonitor with mtaDTO={}, cause:{}", uParam, e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301003, ErrorConstant.WEBMONITOR_MSG_301003);
        }
    }

    /**
     * 批量更新资产信息
     *
     * @param uParam
     * @return
     */
//    @Override
    public Reply updateBatchWebMonitor(BatchUpdateParam uParam) {
        try {
            String httpProxy = uParam.getHttpProxy();
            List<MwWebMonitorDTO> mwTangWebMonitors = mwWebmonitorTableDao.selectByPrimaryKeys(uParam.getIds());
            for(MwWebMonitorDTO mwTangWebMonitor : mwTangWebMonitors) {
                String hostId = mwTangWebMonitor.getAssetsId();
                Integer monitorServerId = mwTangWebMonitor.getMonitorServerId();
//                boolean updateFlag = false;
//                if (httpProxy.equals(mwTangWebMonitor.getHttpProxy())) {
//                    updateFlag = true;
//                }
                List<MWStep> originalStepList = new ArrayList<>();
                originalStepList.add(MWStep.builder()
                        .name(mwTangWebMonitor.getWebName())
                        .no(1)
                        .required(mwTangWebMonitor.getString())
                        .status_codes(mwTangWebMonitor.getStatusCode())
                        .url(mwTangWebMonitor.getWebUrl())
                        .timeout(mwTangWebMonitor.getTimeOut().toString() + "s")
                        .followRedirects(mwTangWebMonitor.getFollowJump() ? 1 : 0)
                        .build());
                MWWebDto originalZibbixDto = MWWebDto.builder()
                        .steps(originalStepList)
                        .name(mwTangWebMonitor.getWebName())
                        .agent("Zabbix")
                        .hostId(hostId)
                        .delay(mwTangWebMonitor.getUpdateInterval().toString() + "s")
                        .status(mwTangWebMonitor.getEnable().equals("ACTIVE") ? 0 : 1)
                        .httpProxy(mwTangWebMonitor.getHttpProxy())
                        .retries(mwTangWebMonitor.getAttempts())
                        .build();
                if (uParam.isUpdateIntervalcheckbox()) { //更新间隔
                    mwTangWebMonitor.setUpdateInterval(uParam.getUpdateInterval());
                }
                if (uParam.isAttemptscheckbox()) {
                    mwTangWebMonitor.setAttempts(uParam.getAttempts());
                }
                if (uParam.isTimeOutcheckbox()) {
                    mwTangWebMonitor.setTimeOut(uParam.getTimeOut());
                }
                if (uParam.isStatusCodecheckbox()) {
                    mwTangWebMonitor.setStatusCode(uParam.getStatusCode());
                }
                if (uParam.isEnablecheckbox()) {
                    mwTangWebMonitor.setEnable(uParam.getEnable());
                }

                List<String> httpTestIds = new ArrayList<>();
                httpTestIds.add(mwTangWebMonitor.getHttpTestId());
                List<MWStep> stepList = new ArrayList<>();
                stepList.add(MWStep.builder()
                        .name(mwTangWebMonitor.getWebName())
                        .no(1)
                        .required(mwTangWebMonitor.getString())
                        .status_codes(mwTangWebMonitor.getStatusCode())
                        .url(mwTangWebMonitor.getWebUrl())
                        .timeout(mwTangWebMonitor.getTimeOut().toString() + "s")
                        .followRedirects(mwTangWebMonitor.getFollowJump() ? 1 : 0)
                        .build());
                MWWebDto zibbixDto = MWWebDto.builder()
                        .steps(stepList)
                        .name(mwTangWebMonitor.getWebName())
                        .agent("Zabbix")
                        .hostId(hostId)
                        .delay(mwTangWebMonitor.getUpdateInterval().toString() + "s")
                        .status(mwTangWebMonitor.getEnable().equals("ACTIVE") ? 0 : 1)
                        .httpProxy(mwTangWebMonitor.getHttpProxy())
                        .retries(mwTangWebMonitor.getAttempts())
                        .build();
                if (!zibbixDto.equals(originalZibbixDto)) {
                    MWZabbixAPIResult result = null;

//                    if (updateFlag) {
                        zibbixDto.setHttptestids(mwTangWebMonitor.getHttpTestId().toString());
                        result = mwtpServerAPI.HttpTestUpdate(monitorServerId, zibbixDto);
//                    } else {
//                        mwtpServerAPI.HttpTestDelete(monitorServerId, httpTestIds);
//                        result = mwtpServerAPI.HttpTestCreate(monitorServerId, zibbixDto);
//                        JsonNode map = (JsonNode) result.getData();
//                        uParam.setHttpTestId(map.get("httptestids").get(0).asInt());
//                    }

                    if (result.code != 0) {
                        return Reply.fail(ErrorConstant.WEBMONITORCODE_301002, result.getData().toString());
                    }
                }
            }

            mwWebmonitorTableDao.updateBatch(uParam);

            //用户，用户组，机构设置
            updateMapperAndPerms(uParam);

            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateWebMonitor with mtaDTO={}, cause:{}", uParam, e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301003, ErrorConstant.WEBMONITOR_MSG_301003);
        }
    }

    /**
     * 修改权限
     *
     * @param uParam
     */
    private void updateMapperAndPerms(BatchUpdateParam uParam) {
        UpdateDTO updateDTO = UpdateDTO.builder()
                .isGroup(uParam.isGroupIdscheckbox())
                .isUser(uParam.isPrincipalcheckbox())
                .isOrg(uParam.isOrgIdscheckbox())
                .groupIds(uParam.getGroupIds())  //用户组
                .userIds(uParam.getPrincipal()) //责任人
                .orgIds(uParam.getOrgIds()) //机构
                .typeIds(uParam.getIds().stream().map(String::valueOf).collect(Collectors.toList()))    //批量资产数据主键
                .type(DataType.WEB_MONITOR.getName())  //ASSETS
                .desc(DataType.WEB_MONITOR.getDesc()).build(); //资产
        mwCommonService.editorMapperAndPerms(updateDTO);
    }

    /**
     * 新增网站信息
     *
     * @param aParam
     * @return
     */
    @Override
    public Reply insertWebMonitor(AddUpdateWebMonitorParam aParam) {
        String id = aParam.getHostId();
        HostDto hostDto = mwWebmonitorTableDao.getHostIdAndServerId(id);
        String hostId = hostDto.getAssetsId();
        Integer monitorServerId = hostDto.getMonitorServerId();
        List<String> webids1 = new ArrayList<>();
        List<MWStep> stepList = new ArrayList<>();
        stepList.add(MWStep.builder()
                .name(aParam.getWebName())
                .no(1)
                .required(aParam.getString())
                .status_codes(aParam.getStatusCode())
                .url(aParam.getWebUrl())
                .timeout(aParam.getTimeOut().toString() + "s")
                .followRedirects(aParam.getFollowJump() ? 1 : 0)
                .build());
        MWWebDto zibbixDto = MWWebDto.builder()
                .steps(stepList)
                .name(aParam.getWebName())
                .agent("Zabbix")
                .hostId(hostId)
                .delay(aParam.getUpdateInterval().toString() + "s")
                .status(aParam.getEnable().equals("ACTIVE") ? 0 : 1)
                .httpProxy(aParam.getHttpProxy())
                .retries(aParam.getAttempts())
                .build();
        //zabbix创建web监测数据
        MWZabbixAPIResult result = mwtpServerAPI.HttpTestCreate(monitorServerId, zibbixDto);
        log.info("result:" + JSON.toJSONString(result));
        if (result.code != 0) {
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301002, result.getData().toString());
        }
        JsonNode map = (JsonNode) result.getData();
        webids1.add(map.get("httptestids").get(0).toString());
        //zabbix创建web监测触发器
        String webFailDescription = getWebDescription(aParam.getWebName(), WEB_TEST_FAIL);
        MWZabbixAPIResult hostResult = mwtpServerAPI.hostGetById(monitorServerId, hostId);
        if (hostResult.getCode() != 0) {
            mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301002, result.getData().toString());
        }
        JsonNode hostmap = (JsonNode) hostResult.getData();
        String hostName = hostmap.get(0).get("host").asText();

        String webFailExpression = getWebExpression(hostName, aParam.getWebName(), WEB_TEST_FAIL);
        String webFailRexExpression = getWebRexExpression(hostName, aParam.getWebName(), WEB_TEST_FAIL);
        String webTimeDescription = getWebDescription(aParam.getWebName(), WEB_TEST_TIME);
        String webTimeExpression = getWebExpression(hostName, aParam.getWebName(), WEB_TEST_TIME);

        MWZabbixAPIResult triggerFailCreate = mwtpServerAPI.triggerCreate(monitorServerId, webFailDescription, webFailExpression, webFailRexExpression, String.valueOf(TriggerAlarmLevelEnum.ERROR.getCode()));
//        MWZabbixAPIResult triggerTimeCreate = mwtpServerAPI.triggerCreate(monitorServerId, webTimeDescription, webTimeExpression, String.valueOf(TriggerAlarmLevelEnum.ERROR.getCode()));
//        if (triggerFailCreate.getCode() != 0 || triggerTimeCreate.getCode() != 0) {
        if (triggerFailCreate.getCode() != 0) {
            mwtpServerAPI.HttpTestDelete(monitorServerId, webids1);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301002, result.getData().toString());
        }
        //添加zabbix返回数据到数据库
        aParam.setHttpTestId(map.get("httptestids").get(0).asInt());
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());
        addWebMonitorSql(aParam);
        return Reply.ok("新增成功");
    }

    private String getWebDescription(String webName, String key) {
        StringBuffer sb = new StringBuffer("[网站监测]");
        sb.append("[" + webName + "]");
        if (key.equals(WEB_TEST_TIME)) {
            sb.append("网站延时持续6分钟大于200ms");
        } else {
            sb.append("网站状态故障");
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

    @Transactional
    public void addWebMonitorSql(AddUpdateWebMonitorParam aParam) {
        mwWebmonitorTableDao.insert(aParam);

        //绑定用户用户组机构
        InsertDto report = InsertDto.builder()
                .groupIds(aParam.getGroupIds())
                .userIds(aParam.getPrincipal())
                .orgIds(aParam.getOrgIds())
                .typeId(String.valueOf(aParam.getId()))
                .type(DataType.WEB_MONITOR.getName())
                .desc(DataType.WEB_MONITOR.getDesc()).build();

        //添加负责人
        mwCommonService.addMapperAndPerm(report);
    }

    /**
     * 删除资产信息
     *
     * @param deleteWebMonitorParam
     * @return
     */
    @Override
    @Transactional
    public Reply deleteWebMonitor(DeleteWebMonitorParam deleteWebMonitorParam) {
        // try {


        mwWebmonitorTableDao.delete(deleteWebMonitorParam.getIdList());
        //删除关联信息
        List<Integer> idList = deleteWebMonitorParam.getIdList();
        List<String> idLists = new ArrayList<>();
        for (Integer id : idList) {
            idLists.add(String.valueOf(id));
        }
        DeleteDto deleteDto = DeleteDto.builder().typeIds(idLists).type(DataType.WEB_MONITOR.getName()).build();
        mwCommonService.deleteMapperAndPerms(deleteDto);

        if (deleteWebMonitorParam.getHttpTestIds().size() > 0) {
            List<HttpParam> httpTestIds = deleteWebMonitorParam.getHttpTestIds();
            Map<Integer, List<HttpParam>> collect = httpTestIds.stream().collect(Collectors.groupingBy(HttpParam::getMonitorServerId));
            for (Integer key : collect.keySet()) {
                List<HttpParam> listparam = collect.get(key);
                List<String> list = new ArrayList<>();
                for (HttpParam dto : listparam) {
                    list.add(dto.getHttpId());
                }
                String result = mwWebZabbixManger.HttpTestDelete(key, list);
                if ("删除失败".equals(result)) {
                    throw new RuntimeException("删除失败。请联系管理员！");
                    //return Reply.fail(ErrorConstant.WEBMONITORCODE_301004, "删除失败。请联系管理员！");
                }
            }
        }
        return Reply.ok("删除成功");
//        } catch (Exception e) {
//            log.error("fail to deleteWebMonitor with ids={}, cause:{}", deleteWebMonitorParam, e);
//            return Reply.fail(ErrorConstant.WEBMONITORCODE_301004, ErrorConstant.WEBMONITOR_MSG_301004);
//        }
    }

    @Override
    public Reply updateState(UpdateWebMonitorStateParam updateWebMonitorStateParam) {
        try {
            if (updateWebMonitorStateParam.getEnable().equals("ACTIVE")) {
                int monitorFlag = mwWebmonitorTableDao.getMonitorFlagById(updateWebMonitorStateParam.getId());
                if (monitorFlag == 0) {//资产处于关闭状态
                    return Reply.fail(ErrorConstant.WEBMONITORCODE_301005, "资产监控状态未监控,无法启用WEB监测");
                }
            }
            mwWebZabbixManger.HttpTestUpdate(updateWebMonitorStateParam.getMonitorServerId(), updateWebMonitorStateParam.getHttpTestId(), updateWebMonitorStateParam.getEnable().equals("ACTIVE") ? 0 : 1);
            mwWebmonitorTableDao.updateUserState(updateWebMonitorStateParam);
        } catch (Exception e) {
            log.error("fail to updateState with updateWebMonitorStateParam={}, cause:{}", updateWebMonitorStateParam, e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301005, ErrorConstant.WEBMONITOR_MSG_301005);
        }
        return Reply.ok("更新成功");
    }

    @Override
    public Reply selectWebInfo(QueryWebHistoryParam qParam) {
        try {
            String key = null;
            if (1 == qParam.getTitleType()) {
                key = "web.test.in[" + qParam.getWebName() + "," + qParam.getWebName() + "," + "bps]";
            } else if (2 == qParam.getTitleType()) {
                key = "web.test.time[" + qParam.getWebName() + "," + qParam.getWebName() + "," + "resp]";
            }
            Assert.notNull(key, ErrorConstant.WEBMONITOR_MSG_301008);
            Assert.notNull(qParam.getAssetsId(), ErrorConstant.WEBMONITOR_MSG_301008);
            Assert.notNull(qParam.getMonitorServerId(), ErrorConstant.WEBMONITOR_MSG_301008);
            qParam.setKey(key);
            String hostId = qParam.getAssetsId();
            Integer monitorServerId = qParam.getMonitorServerId();
            List<String> hostids = new ArrayList<>();
            hostids.add(hostId);
            log.info("monitorServerId:" + monitorServerId);
            log.info("hostids:" + hostids.get(0));
            log.info("qParam.getKey():" + qParam.getKey());
            List<ItemApplication> itemApplicationList = mwWebZabbixManger.getItemApplicationList(monitorServerId, hostids, qParam.getKey());
            MwWebMonitorInfoDTO dto = new MwWebMonitorInfoDTO();
            String itemId = null;
            if (!CollectionUtils.isEmpty(itemApplicationList)) {
                itemId = itemApplicationList.get(0).getItemid();
                dto.setDelay(itemApplicationList.get(0).getDelay());
            }
            dto.setItemsList(itemApplicationList);
            List<MwHistoryDTO> HistoryDTOList = new ArrayList<>();

            if (itemId != "" && StringUtils.isNotEmpty(itemId)) {
                if (qParam.getDateType() == 1 || qParam.getDateType() == 2 || qParam.getDateType() == 5) {
                    Long startTime = 0L;
                    Calendar calendar = Calendar.getInstance();
                    dto.setLastUpdateTime(calendar.getTime());
                    Long endTime = calendar.getTimeInMillis() / 1000L;
                    if (qParam.getDateType() == 1) {
                        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
                        startTime = calendar.getTimeInMillis() / 1000L;
                    } else if (qParam.getDateType() == 2) {
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                        startTime = calendar.getTimeInMillis() / 1000L;
                    } else if (qParam.getDateType() == 5) {
                        if (null != qParam.getDateStart()) {
                            startTime = qParam.getDateStart().getTime() / 1000L;
                            endTime = qParam.getDateEnd().getTime() / 1000L;
                        } else {
                            return Reply.ok();
                        }
                    }
                    log.info("monitorServerId" + monitorServerId);
                    log.info("itemId" + itemId);
                    List<MWItemHistoryDto> mWItemHistoryDtoList = mwWebZabbixManger.HistoryGetByTime(monitorServerId, itemId, startTime, endTime);
                    if (mWItemHistoryDtoList.size() > 0) {
                        // Collections.reverse(mWItemHistoryDtoList);
                        List<MWItemHistoryDto> newValueList = new ArrayList<>();
                        mWItemHistoryDtoList.forEach(dto1 -> {
                            newValueList.add(dto1);
                        });
                        //降序排序
                        Collections.sort(newValueList);
                        MWItemHistoryDto mwItemHistoryDto = newValueList.get(0);
                        Map<String, String> maxMap = new HashMap<>();
                        if (1 == qParam.getTitleType()) {
                            maxMap = UnitsUtil.getValueAndUnits(mwItemHistoryDto.getValue(), NewUnits.BPS.getUnits());
                        } else if (2 == qParam.getTitleType()) {
                            maxMap = UnitsUtil.getValueAndUnits(mwItemHistoryDto.getValue(), NewUnits.MS.getUnits());
                        }
                        String lastUnits = maxMap.get("units");
                        dto.setUnit(lastUnits);
                        mWItemHistoryDtoList.forEach(
                                HistoryDto -> {
                                    Map<String, String> map = new HashMap<>();
                                    if (1 == qParam.getTitleType()) {
                                        map = UnitsUtil.getValueMap(HistoryDto.getValue(), lastUnits, NewUnits.BPS.getUnits());
                                    } else if (2 == qParam.getTitleType()) {
                                        map = UnitsUtil.getValueMap(HistoryDto.getValue(), lastUnits, NewUnits.MS.getUnits());
                                    }
                                    String newValue = map.get("value");
                                    HistoryDTOList.add(MwHistoryDTO.builder()
                                            .value(newValue)
                                            .date(new Timestamp(Long.valueOf(HistoryDto.getClock()) * 1000L))
                                            .build());
                                }
                        );
                    }
                } else {//3,4 周和月的数据从redis中取
                    Integer timeType = 0;
                    if (qParam.getDateType() == 3) {
                        timeType = 1;
                    } else if (qParam.getDateType() == 4) {
                        timeType = 2;
                    }
                    key = qParam.getTangibleId() + ":" + MWUtils.TIMETYPEVALUE.get(timeType) + itemId + key;
                    Set<String> range = redisTemplate.opsForZSet().range(key, 0, -1);
                    List<RedisItemHistoryDto> list = new ArrayList<>();
                    for (String str : range) {
                        if (null != str && StringUtils.isNotEmpty(str)) {
                            RedisItemHistoryDto redisItemHistoryDto = JSONObject.parseObject(str, RedisItemHistoryDto.class);
                            list.add(redisItemHistoryDto);
                        }
                    }
                    if (list.size() > 0) {
                        Double avgMax = list.get(0).getAvgMax();
                        Map<String, String> maxMap = new HashMap<>();
                        if (1 == qParam.getTitleType()) {
                            maxMap = UnitsUtil.getValueAndUnits(String.valueOf(avgMax), NewUnits.BPS.getUnits());
                        } else if (2 == qParam.getTitleType()) {
                            maxMap = UnitsUtil.getValueAndUnits(String.valueOf(avgMax), NewUnits.MS.getUnits());
                        }
                        String lastUnits = maxMap.get("units");
                        dto.setUnit(lastUnits);
                        list.forEach(redisValue -> {
                            Map<String, String> map = new HashMap<>();
                            if (1 == qParam.getTitleType()) {
                                map = UnitsUtil.getValueMap(redisValue.getAvgValue(), lastUnits, NewUnits.BPS.getUnits());
                            } else if (2 == qParam.getTitleType()) {
                                map = UnitsUtil.getValueMap(redisValue.getAvgValue(), lastUnits, NewUnits.MS.getUnits());
                            }
                            String newValue = map.get("value");
                            HistoryDTOList.add(MwHistoryDTO.builder()
                                    .value(newValue)
                                    .date(MWUtils.strToDate(redisValue.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"))
                                    .build());
                        });
                    }
                }
            }
            dto.setDataList(HistoryDTOList);
            if (1 == qParam.getTitleType()) {
                dto.setTitleName("下载速度");
            } else if (2 == qParam.getTitleType()) {
                dto.setTitleName("响应时间");
            }
            return Reply.ok(dto);
        } catch (Exception e) {
            log.error("fail to selectWebInfo with qParam={}, cause:{}", qParam, e);
            return Reply.fail(ErrorConstant.WEBMONITORCODE_301008, ErrorConstant.WEBMONITOR_MSG_301008);
        }
    }

    @Override
    public Reply getAssetsListByAssetsTypeId(MwCommonAssetsDto mwCommonAssetsDto) {
        mwCommonAssetsDto.setUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
        Map<String, Object> assetsByUserId = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
        Object assetsList = assetsByUserId.get("assetsList");
        List<MwTangibleassetsTable> list = new ArrayList<>();
        if (null != assetsByUserId) {
            list = (List<MwTangibleassetsTable>) assetsList;
        }
        return Reply.ok(list);

    }

    @Override
    public void excelImport(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                EasyExcel.read(file.getInputStream(), ImportWebMonitorParam.class, new ExcelWebMonitorListener(webMonitorService, response, fileName, licenseManagement, checkCountService)).sheet().doRead();
            } else {
                logger.error("没有传入正确的excel文件名", file);
            }
        } catch (Exception e) {
            logger.error("fail to excelImport with MultipartFile={}, cause:{}", file, e);
        }
    }

    @Override
    public void excelTemplateExport(ExcelExportParam excelExportParam, HttpServletResponse response) {
        List<ImportWebMonitorParam> list = new ArrayList<>();

        ImportWebMonitorParam importWebMonitorParam = ImportWebMonitorParam.builder()
                .webName("微信QQ")
                .webUrl("www.weixin.qq.com")
                .hostIp("172.168.99.204")  //塞尔
                .updateInterval(120)
                .attempts(1)
                .enable("ACTIVE")
                .timeOut(10)
                .statusCode("200")
                .monitorServer("zabbix5.2")
                .principalName("用户名（多用户以逗号分隔）")
                .orgs("某某机构（多机构以逗号分隔）")
                .groups("网络组（多个组以逗号分隔）")
                .build();
        list.add(importWebMonitorParam);
        Set<String> includeColumnFiledNames = new HashSet<>();
        if (excelExportParam.getFields().size() > 0) {
            includeColumnFiledNames = excelExportParam.getFields();
        } else {
            includeColumnFiledNames.add("webName");
            includeColumnFiledNames.add("webUrl");
            includeColumnFiledNames.add("hostIp");
            includeColumnFiledNames.add("updateInterval");
            includeColumnFiledNames.add("attempts");

            includeColumnFiledNames.add("enable");
            includeColumnFiledNames.add("timeOut");
            includeColumnFiledNames.add("statusCode");

            includeColumnFiledNames.add("monitorServer");
            includeColumnFiledNames.add("principalName");
            includeColumnFiledNames.add("orgs");
            includeColumnFiledNames.add("groups");
        }
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter(excelExportParam.getName(), response, ImportWebMonitorParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            logger.info("导出成功");
        } catch (IOException e) {
            logger.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }

    }

    @Override
    public AddUpdateWebMonitorParam transform(ImportWebMonitorParam iParam) throws TransformException {
        AddUpdateWebMonitorParam webMonitorParam = new AddUpdateWebMonitorParam();
        webMonitorParam.setWebName(iParam.getWebName().trim());
        webMonitorParam.setWebUrl(iParam.getWebUrl().trim());
        webMonitorParam.setUpdateInterval(iParam.getUpdateInterval());//更新间隔
        webMonitorParam.setAttempts(iParam.getAttempts());//尝试次数
        webMonitorParam.setEnable(iParam.getEnable());
        webMonitorParam.setTimeOut(iParam.getTimeOut());
        webMonitorParam.setStatusCode(iParam.getStatusCode());
        String monitorServer = iParam.getMonitorServer().trim();
        String ip = iParam.getHostIp().trim();
        List<String> errorMsg = new ArrayList<>();
        if (ip != null && monitorServer != null) {
            HostDto hostDto = mwWebmonitorTableDao.getAssetsId(ip, monitorServer);
            if (hostDto == null) {
                errorMsg.add("IP地址或者监控服务器名称不正确");
            } else {
                webMonitorParam.setClient(101);
                webMonitorParam.setHostId(hostDto.getHostId());
                webMonitorParam.setAssetsId(hostDto.getAssetsId());
                webMonitorParam.setMonitorServerId(hostDto.getMonitorServerId());
            }
        } else {
            errorMsg.add("IP地址或者监控服务器名称不能为空");
        }
        //转换用户
        String principalName = iParam.getPrincipalName().trim();
        if (principalName != null && StringUtils.isNotEmpty(principalName)) {
            principalName = principalName.replaceAll("，", ",");
            String[] splitPrincipal = principalName.split(",");
            List<Integer> principal = mwWebmonitorTableDao.selectUserIdsByUserNames(Arrays.asList(splitPrincipal));
            if (principal == null || principal.size() <= 0) {
                errorMsg.add("负责人名称错误或者不存在该用户");
            } else {
                webMonitorParam.setPrincipal(principal);
            }
        } else {
            errorMsg.add("负责人不能为空");
        }

        //转换用户组
        String groupName = iParam.getGroups().trim();
        if (groupName != null && StringUtils.isNotEmpty(groupName)) {
            groupName = groupName.replaceAll("，", ",");
            String[] splitGroup = groupName.split(",");
            List<Integer> groupIds = mwWebmonitorTableDao.selectGroupIdsByGroupNames(Arrays.asList(splitGroup));
            if (groupIds == null || groupIds.size() <= 0) {
                errorMsg.add("用户组名称错误或者不存在该用户组");
            } else {
                webMonitorParam.setGroupIds(groupIds);
            }
        } else {
            errorMsg.add("用户组不能为空");
        }

        //转换机构
        String orgName = iParam.getOrgs().trim();
        if (orgName != null && StringUtils.isNotEmpty(orgName)) {
            orgName = orgName.replaceAll("，", ",");
            String[] splitOrg = orgName.split(",");
            List<OrgDTO> orgIds = mwWebmonitorTableDao.selectOrgIdsByOrgNames(Arrays.asList(splitOrg));
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (orgIds == null || orgIds.size() <= 0) {
                errorMsg.add("机构名称错误或者不存在该机构");
            } else {
                orgIds.forEach(department -> {
                            List<Integer> orgList = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgList.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgList);
                        }
                );
                webMonitorParam.setOrgIds(orgNodes);
            }
        } else {
            errorMsg.add("机构不能为空");
        }
        if (errorMsg.size() > 0) {
            throw new TransformException(StringUtils.join(errorMsg, ";"));
        }

        return webMonitorParam;
    }

    /**
     * web监测模糊搜索所有字段联想
     *
     * @param param
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(QueryWebMonitorParam param) {

            //根据值模糊查询数据
            List<Map<String, String>> fuzzSeachAllFileds = mwWebmonitorTableDao.fuzzSearchAllFiled(param.getFuzzyQuery());
            Set<String> fuzzSeachData = new HashSet<>();
            if (!cn.mwpaas.common.utils.CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
                for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                    String web_name = fuzzSeachAllFiled.get("web_name");
                    String web_url = fuzzSeachAllFiled.get("web_url");

                    if (!Strings.isNullOrEmpty(web_name) && web_name.contains(param.getFuzzyQuery())) {
                        fuzzSeachData.add(web_name);
                    }
                    if (!Strings.isNullOrEmpty(web_url) && web_url.contains(param.getFuzzyQuery())) {
                        fuzzSeachData.add(web_url);
                    }
                }
            }
            Map<String, Set<String>> fuzzyQuery = new HashMap<>();
            fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
            return Reply.ok(fuzzyQuery);
    }

}
