package cn.mw.time;

import cn.mw.module.security.dto.EsDataSourceListDto;
import cn.mw.module.security.dto.EsDataSourceListInfoDto;
import cn.mw.module.security.dto.EsSysLogRuleDTO;
import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mw.module.security.service.EsSysLogAuditService;
import cn.mw.monitor.TPServer.dao.MwTPServerTableDao;
import cn.mw.monitor.TPServer.model.MwTPServerTable;
import cn.mw.monitor.accountmanage.dao.MwAlerthistory7daysTableDao;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.dao.MwAlertRuleDao;
import cn.mw.monitor.alert.param.AddAndUpdateAlertRuleParam;
import cn.mw.monitor.alert.param.MwRuleSelectEventParam;
import cn.mw.monitor.alert.param.MwRuleSelectListParam;
import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.security.dao.EsSysLogAuditDao;
import cn.mw.monitor.security.dao.EsSysLogRuleDao;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.tpserver.api.MWTPServerProxyListener;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.webMonitor.dao.MwWebmonitorTableDao;
import cn.mw.monitor.webMonitor.dto.HostDto;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.entity.ActionRule;
import cn.mw.monitor.weixin.message.MessageExecuter;
import cn.mw.monitor.alert.service.impl.MWAlertServiceImpl;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.weixin.service.WxPortalService;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixAPIResultCode;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static cn.mw.monitor.util.MWUtils.getUTCToCST;


/**
 * @author xhy
 * @date 2020/4/17 17:07
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MWZbxTime {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MWAlertServiceImpl mwAlertService;

    @Resource
    private MwAlertActionDao mwAlertActionDao;

    @Resource
    EsSysLogAuditDao esSysLogAuditDao;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;
    @Resource
    private MwModelViewCommonService mwModelViewCommonService;

    @Resource
    EsSysLogRuleDao esSysLogRuleDao;

    @Resource
    private MwWeixinTemplateDao mwWeixinTemplateDao;

    @Resource
    private MwAlertRuleDao mwAlertRuleDao;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Autowired
    EsSysLogAuditService esSysLogAuditService;

    @Resource
    private MwTPServerTableDao mwTPServerTableDao;

    @Resource
    private MwWebmonitorTableDao mwWebmonitorTableDao;

    @Autowired
    private WxPortalService wxPortalService;

    @Autowired
    private MWTPServerAPI mWTPServerAPI;

    @Scheduled(cron = "0 */60  * * * ?") //许可时间倒计时
    public void licenseTimeDown() throws Exception {
        Date now = new Date();
        long nowTime = now.getTime();
        if (redisTemplate.hasKey("moduleStart")) {
            long time = Long.parseLong(redisTemplate.opsForValue().get("moduleStart"));
            if(nowTime > time){
                redisTemplate.opsForValue().set("moduleStart", String.valueOf(nowTime));
            }else{
                redisTemplate.opsForValue().set("moduleStart", String.valueOf(time + 1 * 60 * 60 * 1000));
            }
        }
        ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        for (String s : propMap.keySet()){
            long time = Long.parseLong(redisTemplate.opsForValue().get(s + "_date"));
            redisTemplate.opsForValue().set(s + "_date", String.valueOf(time - 1 * 60 * 60 * 1000));
        }
    }

    //@Scheduled(cron = "0 */3 * * * ?") //告警压缩
    public TimeTaskRresult alarmCompression() throws Exception {
        log.info(">>>>>>>alarmCompression>>>>>>>>>>");
        log.info(">>>>>>>告警压缩启动>>>>>>>>>>");
        //MwRuleSelectEffectParam mwRuleSelectEffectParam = new MwRuleSelectEffectParam();
        List<MwRuleSelectListParam> mwRuleSelectEffectParams = mwAlertActionDao.selectMwAlertAction(null);
        for(MwRuleSelectListParam mwRuleSelectEffectParam : mwRuleSelectEffectParams){
            if(!mwRuleSelectEffectParam.getAlarmCompressionSelect().equals("自定义")){
                continue;
            }
            List<MwRuleSelectEventParam> mwRuleSelectEventParams = mwAlertActionDao.selectMwAlertRuleSelectEvent(mwRuleSelectEffectParam.getActionId());
            if(mwRuleSelectEffectParam != null && mwRuleSelectEventParams != null && mwRuleSelectEventParams.size() > 2){
                for (MwRuleSelectEventParam s : mwRuleSelectEventParams) {
                    Date date = new Date();
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(date);
                    gc.add(GregorianCalendar.MINUTE, -mwRuleSelectEffectParam.getCustomTime());
                    Date gcDate = gc.getTime();
                    Date resultDate = s.getDate();
                    if(resultDate.getTime() <= gcDate.getTime()){
                        MessageExecuter messageExecuter = new MessageExecuter();
                        messageExecuter.execute(s.getText(),null,null,s.getSize());
                        mwAlertActionDao.deleteMwAlertRuleSelectEvent(s);
                    }
                }
            }
        }
        log.info(">>>>>>>alarmCompression>>>>>>>>>>");
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("告警压缩成功");
        return taskRresult;
    }



    private final List<String> fieldList = Arrays.asList("message", "@timestamp", "facility_label", "host", "severity_label");


    @MwPermit(moduleName = "log_security")
    @Transactional
    //@Scheduled(cron = "0 */10 * * * ?")
    public TimeTaskRresult sendSyslog() throws Exception {
        log.info(">>>>>>>sendSyslog>>>>start>>>>>>");
        log.info(">>>>>>>系统日志启动>>>>>>>>>>");
        esSysLogAuditService.initDataSourceState();
        EsDataSourceListDto dto = new EsDataSourceListDto();
        List<Map<String,Object>> assetsIpInfo = new ArrayList<>();
        if(modelAssetEnable){//获取资源中心下的资产数据
            QueryModelAssetsParam qparam = new QueryModelAssetsParam();
            qparam.setSkipDataPermission(true);
            assetsIpInfo = mwModelViewCommonService.getModelListInfoByPerm(qparam);

        }else{//获取mw_tangibleassets_table表中的数据
            assetsIpInfo = esSysLogAuditDao.getAllAssetsInfoByIp();
        }

        log.info(">>>>>>>sendSyslog>>>>assetsIpInfo>>>>>>：" + assetsIpInfo.size());
        Map<String, List> assetsIpInfoMap = new HashMap();
        for (Map<String,Object> m : assetsIpInfo) {
            String ip = "";
            String name = "";
            if (m.get("inBandIp") != null) {
                ip = m.get("inBandIp").toString();
            }
            if (m.get("instanceName") != null) {
                name = m.get("instanceName").toString();
            }
            if (assetsIpInfoMap.containsKey(ip)) {
                List<String> listName = assetsIpInfoMap.get(ip);
                listName.add(name);
                assetsIpInfoMap.put(ip, listName);
            } else {
                List<String> listName = new ArrayList<>();
                listName.add(name);
                assetsIpInfoMap.put(ip, listName);
            }
        }
        List<Map<String, Object>> listMap = new ArrayList<>();
        Date endTime = new Date();
        Date startTime = DateUtils.addMinutes(endTime, -10);
        if (dto != null && dto.getInfoList() != null && dto.getInfoList().size() > 0) {
            for (EsDataSourceListInfoDto p : dto.getInfoList()) {
                RestHighLevelClient client = p.getClient();
                String queryEsIndex = p.getQueryEsIndex();
                //数据源名称
                String dataSourceName = p.getDataSourceName();

                //条件组合查询
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                queryBuilder.must(QueryBuilders.rangeQuery("@timestamp").from(MWUtils.getUtcTime(DateUtils.formatDateTime(startTime))).to(MWUtils.getUtcTime(DateUtils.formatDateTime(endTime))));
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                //返回指定字段数据
                String[] includes = fieldList.toArray(new String[fieldList.size()]);
                FetchSourceContext sourceContext = new FetchSourceContext(true, includes, null);
                searchSourceBuilder.fetchSource(sourceContext);
                List<String> sortField = Arrays.asList("message", "facility_label", "severity_label");
                //设置超时时间
                searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.source(searchSourceBuilder);
                searchRequest.indices(queryEsIndex);
                SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit searchHit : search.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("esId", searchHit.getId());
                    sourceAsMap.put("dataSourceName", dataSourceName);
                    if (sourceAsMap.get("host") != null) {
                        String ip = sourceAsMap.get("host").toString();
                        List<String> assetsName = assetsIpInfoMap.get(ip);
                        List<Map> list = new ArrayList<>();
                        if (assetsName != null && assetsName.size() > 1) {
                            for (String name : assetsName) {
                                Map m = new HashMap();
                                m.put("hostName", name);
                                m.put("isOpen", false);
                                list.add(m);
                            }
                        }
                        if (assetsName != null && assetsName.size() > 0) {
                            sourceAsMap.put("hostName", assetsName.get(0));
                        } else {
                            sourceAsMap.put("hostName", ip);
                        }
                        sourceAsMap.put("hostNameList", list);
                    }
                    if (sourceAsMap.get("@timestamp") != null) {
                        String date = sourceAsMap.get("@timestamp").toString();
                        sourceAsMap.put("@timestamp", getUTCToCST(date));
                    }
                    sourceAsMap.put("isOpen", true);
                    listMap.add(sourceAsMap);
                }
            }
        }
        log.info(">>>>>>>sendSyslog>>>>listMap>>>>>>："+ listMap);
        setRuleByAllSysLog(listMap);
        log.info(">>>>>>>sendSyslog>>>>>end>>>>>");
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("告警系统日志成功");
        return taskRresult;
    }

    private void setRuleByAllSysLog(List<Map<String, Object>> listMap) throws Exception {
        //获取所有的规则列表数据
        List<EsSysLogRuleDTO> list = esSysLogRuleDao.getRulesInfosByAction();
        for (EsSysLogRuleDTO esSysLogRuleDTO : list) {
            if (esSysLogRuleDTO != null && esSysLogRuleDTO.getId() != null) {
                //获取标签信息
                List<EsSysLogTagDTO> listTag = esSysLogRuleDao.getRuleTags(esSysLogRuleDTO.getId());
                esSysLogRuleDTO.setTagDTOList(listTag);
            }
            if (esSysLogRuleDTO != null && esSysLogRuleDTO.getRuleId() != null) {
                //获取规则信息
                List<MwRuleSelectParam> listRule = esSysLogRuleDao.getAlertRules(esSysLogRuleDTO.getRuleId());
                esSysLogRuleDTO.setMwRuleSelectListParam(listRule);
            }
        }
        //循环日志列表
        for (Map<String, Object> m : listMap) {
            MessageContext messageContext = new MessageContext();
            messageContext.setKey((HashMap) m);
            //循环规则列表
            for (EsSysLogRuleDTO esSysLogRuleDTO : list) {
                if (esSysLogRuleDTO != null && esSysLogRuleDTO.getMwRuleSelectListParam() != null &&
                        esSysLogRuleDTO.getMwRuleSelectListParam().size() != 0) {
                    //获取规则信息
                    List<MwRuleSelectParam> ruleSelectList = esSysLogRuleDTO.getMwRuleSelectListParam();
                    log.info(">>>>>>>sendSyslog>>>>ruleSelectList>>>>>>："+ ruleSelectList);
                    Boolean resultBoolean = false;
                    List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                    for (MwRuleSelectParam s : ruleSelectList) {
                        if (s.getParentKey().equals("root")) {
                            ruleSelectParams.add(s);
                        }
                    }
                    for (MwRuleSelectParam s : ruleSelectParams) {
                        s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                    }
                    resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                    log.info("ruleSelectParams: " + ruleSelectParams);
                    log.info("messageContext:" + messageContext);
                    log.info("ruleSelectParams: " + ruleSelectParams);
                    log.info("result: " + resultBoolean);
                    if (resultBoolean) {
                        for (EsSysLogRuleDTO s : list) {
                            m.put("tagList", esSysLogRuleDTO.getTagDTOList());
                            List<ActionRule> rules = mwWeixinTemplateDao.selectRuleMapper(s.getRuleId());
                            HashSet<Integer> userIds = new HashSet<>();
                            userIds = mwAlertActionDao.selectActionUsersMapper(s.getRuleId());
                            List<Integer> groupIds = mwAlertActionDao.selectActionGroupsMapper(s.getRuleId());
                            //查询用户组中所有用户
                            if (null != groupIds && groupIds.size() > 0) {
                                for (Integer groupid : groupIds) {
                                    Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                                    if (selectGroupUser.getRes() == 0) {
                                        List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                                        if (null != groupUserData && groupUserData.size() > 0) {
                                            for (GroupUserDTO pri : groupUserData) {
                                                userIds.add(pri.getUserId());
                                            }
                                        }
                                    }
                                }
                            }
                            ExecutorService pool = Executors.newFixedThreadPool(10);
                            if (rules != null && rules.size() > 0) {
                                for (ActionRule rule : rules) {
                                    AddAndUpdateAlertRuleParam alertRuleParam = mwAlertRuleDao.selectRuleById(rule.getRuleId());
                                    log.info("alert enable：" + alertRuleParam.getEnable());
                                    if (!alertRuleParam.getEnable()) {
                                        log.info("该规则告警按钮已关闭ruleId：" + rule.getRuleId());
                                        continue;
                                    }
                                    log.info("dealMessage 发送方式：" + rule.getActionType());
                                    if (rule.getActionType() == 1) {//发送方式有微信服务号
                                        //dealWxMessage(map, userIds, severity, assets);
                                        pool.submit(new WxSendSysLogImpl(m, userIds, rule.getRuleId()));
                                    } else if (rule.getActionType() == 3) {//发送方式有邮件
                                        //dealEmailMessage(map, actionId, severity, userIds, rule.getRuleId(), assets);
                                       pool.submit(new EmailSendSysLogImpl(m, userIds, rule.getRuleId()));
                                    } else if (rule.getActionType() == 7) {//发送方式有钉钉群消息
                                        //dealDXMessage(map, userIds, severity, assets, rule.getRuleId());
                                       pool.submit(new DingdingQunSendSysLogImpl(m, userIds, rule.getRuleId()));

                                    }else if(rule.getActionType() == 5){
                                        pool.submit(new QyWxSendSysLogImpl(m, userIds, rule.getRuleId()));
                                    }
                                }
                            } else {
                                log.info("发送方式不存在");
                                return;
                            }
                            pool.shutdown();
                            try {
                                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException e) {
                                log.error("线程错误：" + e);
                            }
                        }


                        break;
                    }
                }
            }
        }

    }

    private List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList) {
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for (MwRuleSelectParam s : rootList) {
            if (s.getParentKey().equals(key)) {
                childList.add(s);
            }
        }
        for (MwRuleSelectParam s : childList) {
            s.setConstituentElements(getChild(s.getKey(), rootList));
        }
        if (childList.size() == 0) {
            return null;
        }
        return childList;
    }


    @Transactional
    //@Scheduled(cron = "0 */15 * * * ?")
    public TimeTaskRresult sendZabbixService() throws Exception {
        log.info(">>>>>>>zaiibx监控启动>>>>>>>>>>");
        List<MwTPServerTable> mwTPServerTables = new ArrayList<>();
        mwTPServerTables = mwTPServerTableDao.selectAll();
        if(CollectionUtils.isNotEmpty(mwTPServerTables)){
            for(MwTPServerTable s : mwTPServerTables){
                HostDto hostDto = mwWebmonitorTableDao.getAssetsIp(s.getMonitoringServerIp());
                if(hostDto == null) continue;
                /*ProxyServerInfo proxyServerInfo = new ProxyServerInfo(s.getMonitoringServerType()
                        , s.getMonitoringServerVersion(), s.getMonitoringServerUser()
                        , s.getMonitoringServerPassword(), s.getMonitoringServerUrl());
                boolean isok = mwtpServerProxyListener.check(proxyServerInfo);*/
                boolean isok = true;
                MWZabbixAPIResult result = mWTPServerAPI.getScript(s.getId());
                if (result.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
                    JsonNode data = (JsonNode) result.getData();
                    Iterator ite = data.iterator();
                    MWZabbixAPIResult hostIdResult = mWTPServerAPI.getHostInfosById(s.getId(),null,null);
                    while(ite.hasNext()){
                        JsonNode iteData = (JsonNode)ite.next();
                        String name = iteData.get("name").asText();
                        if(name.contains("telnet")){
                            String scriptid = iteData.get("scriptid").asText();
                            JsonNode hostIdNode = (JsonNode) hostIdResult.getData();
                            String hostId = hostIdNode.get(0).get("hostid").asText();
                            MWZabbixAPIResult executeScript =  mWTPServerAPI.executeScript(s.getId(),scriptid,hostId);
                            if (executeScript != null && executeScript.getCode() != MWZabbixAPIResultCode.SUCCESS.code()) {
                                isok = false;
                            }
                        }
                    }
                }
                if(!isok){
                    String msg = "告警标题:title,\n" +
                            "HOSTID:hostid,\n" +
                            "HOSTNAME:hostname,\n" +
                            "HOSTIP:hostip,\n" +
                            "告警信息:服务器访问错误,\n" +
                            "告警等级:,High\n" +
                            "告警时间:AlertTime,\n" +
                            "问题详情:服务器访问错误,\n" +
                            "当前状态: PROBLEM,\n" +
                            "事件ID: 1111";
                    msg = msg.replaceAll("hostid", hostDto.getAssetsId())
                            .replaceAll("hostip", s.getMonitoringServerIp())
                            .replaceAll("hostname", s.getMonitoringServerName())
                            .replaceAll("AlertTime", new Date().toString());
                    List<String> msgs = new ArrayList<>();
                    msgs.add(msg);
                    wxPortalService.dealMessage(msgs,null,null);
                }
            }
        }
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        taskRresult.setResultType(0);
        taskRresult.setResultContext("告警zaiibx监控成功");
        return taskRresult;
    }


}
