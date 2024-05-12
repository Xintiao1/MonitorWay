package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.accountmanage.entity.AlertRecordTableDTO;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.dto.MWAlertHistoryDto;
import cn.mw.monitor.alert.param.ReportDateUtil;
import cn.mw.monitor.alert.param.TriggerInfoParam;
import cn.mw.monitor.alert.service.associa.AssociatedSubject;
import cn.mw.monitor.alert.service.manager.MWAlertManager;
import cn.mw.monitor.alert.service.manager.ObtainAlertLevel;
import cn.mw.monitor.alert.service.manager.ObtainAlertLevelFactory;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.common.util.ZabbixTriggerUtils;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.*;
import cn.mw.monitor.service.alert.param.AssetsStatusQueryParam;
import cn.mw.monitor.service.assets.api.MwInspectModeService;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.knowledgeBase.api.MwKnowledgeService;
import cn.mw.monitor.service.link.api.LinkLifeCycleListener;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWUserLifeCycle;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.service.zbx.model.AlertDTO;
import cn.mw.monitor.service.zbx.model.HostProblem;
import cn.mw.monitor.service.zbx.model.HostProblemType;
import cn.mw.monitor.service.zbx.model.Problem;
import cn.mw.monitor.service.zbx.param.*;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.GzipTool;
import cn.mw.monitor.util.ListSortUtil;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.util.lucene.LuceneUtils;
import cn.mw.monitor.weixinapi.NotifyAlertMessage;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixAPIResultCode;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/3/27 14:11
 */
@Service
@Slf4j(topic = "MWAlertController")
public class MWAlertServiceImpl implements MWAlertService , NotifyAlertMessage
        , MWUserLifeCycle ,LinkLifeCycleListener , InitializingBean {

    @Value("${alert.debug}")
    private boolean debug;

    @Value("${alert.level}")
    private String alertLevel;

    @Value("${alert.isOpenVir}")
    private boolean isVir;

    @Value("${alert.cacheTime}")
    private long currentCacheExpireTime;

    @Value("${record.date}")
    private int recordDate;

    @Value("${zabbix.hostids.group}")
    private Integer hostIdsNum;

    private Map<String ,Date> currentPageCacheTimeMap = new ConcurrentHashMap<>();

    private static String NOW_ALERT_KEY = "now_alert";

    private Date receiveAlertMessageTime;

    private Date linkChangeTime;

    private static final String currentPageCacheKey = "alert:currentPageCache";

    private static final String currentPageMesSep = "&SEP";

    private static final String currentPageCacheKeyPrefix = "alert:currentPageNew";

    private static final String currentHostProblemCacheKey = "alert:currentHostProblem";

    private Date currentProblemCacheTime;

    private static final Map<String ,Object> redisLockMap = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    @Value("${model.assets.enable}")
    private Boolean modelAssetEnable;

    @Value("${hostid.num}")
    private Integer hostidGroupCount;

    @Value("${zabbix.hisAlertCount}")
    private Integer hisAlertCount;

    @Value("${alert.domain}")
    private String domain;

    @Autowired
    private MWAlertManager mwalertManager;


    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private MWAlertAssetsDao assetsDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwKnowledgeService mwKnowledgeService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String saveAlertGetNow = "saveAlertGetNow";

    private static final String getList = "get_alert_list";

    private static final String saveAlertGetHist = "saveAlertGetHist";

    private static final String TAI_SHENG_KEY = "tai";

    private static final Integer admin = 106;

    @Autowired
    private MWNetWorkLinkCommonService mWNetWorkLinkCommonService;

    @Autowired
    private MwInspectModeService mwInspectModeService;

    //获得当前用户的所有hostid
    public List<String> getHostIdsByUserId(Integer userId) {
        return assetsDao.getHostIdsByUserId(userId);
    }

    //获得所有的用户uid
    public List<Integer> getUserIds() {
        return assetsDao.getUserIds();
    }


    private String genRedisKey(String methodName, String objectName, Integer uid) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName).append(":").append(objectName)
                .append("_").append(uid);
        return sb.toString();
    }

    private void saveToRedis(String key, String value) {
        if (redisUtils.hasKey(key)) {
            redisUtils.del(key);
        }
        redisUtils.set(key, value, 60 * 10);
    }

    @Override
    public Reply getCurrAlertPage(AlertParam dto) {
        try {
            //获取当前告警
            if(mwInspectModeService.getInspectModeInfo()) return Reply.ok();
            List<ZbxAlertDto> list = new ArrayList<>();
            long time = System.currentTimeMillis();
            list = getCurrAlertList(dto);
            list = getFilterSelect(list, dto);
            PageList pageList = new PageList();
            if(CollectionUtils.isEmpty(list)) list = new ArrayList<>();
            List newList = pageList.getList(list, dto.getPageNumber(), dto.getPageSize());
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(dto.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("[]ERROR_LOG[][]fail to getCurrAlertPage with FAIL {}", e);
            return Reply.fail(ErrorConstant.ALARM_NOW_CODE_300001, ErrorConstant.ALARM_NOW_MSG_300001);
        }
    }

    public List<AlertDTO> getZipCurrAlertPageFromRedis(AlertParam dto){
        List<AlertDTO> alertDTOS = new ArrayList<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Object redisLock = getRedisLock(loginName);

        String key = currentPageCacheKey + RedisUtils.SEP + loginName;
        Date currentPageCacheTime = currentPageCacheTimeMap.get(key);

        if(null != receiveAlertMessageTime && null != currentPageCacheTime){
            long interval = DateUtils.between(currentPageCacheTime ,receiveAlertMessageTime ,DateUnitEnum.SECOND);
            if(interval > 0){
                redisUtils.del(key);
            }
        }

        if (!redisUtils.hasKey(key)) {
            synchronized (redisLock) {
                if (!redisUtils.hasKey(key)) {
                    Date start = new Date();
                    Reply reply = getCurrAlertPage(dto);
                    if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                        PageInfo<ZbxAlertDto> pageInfo = (PageInfo<ZbxAlertDto>) reply.getData();
                        if (pageInfo.getList().size() > 0) {
                            for (ZbxAlertDto zbxAlertDto : pageInfo.getList()) {
                                AlertDTO alertDTO = new AlertDTO();
                                alertDTO.extractFrom(zbxAlertDto);
                                alertDTOS.add(alertDTO);
                            }
                            String dataStr = JSON.toJSONString(alertDTOS);
                            String zipString = GzipTool.gzip(dataStr);
                            redisUtils.set(key, zipString, currentCacheExpireTime);
                            currentPageCacheTime = new Date();
                            currentPageCacheTimeMap.put(key ,currentPageCacheTime);
                        }
                    }
                    Date end = new Date();
                    long cost = DateUtils.between(start ,end , DateUnitEnum.SECOND);
                }else{
                    alertDTOS = doZipCurrInfoFromRedis(key ,AlertDTO.class);
                }
            }
        }else{
            alertDTOS = doZipCurrInfoFromRedis(key ,AlertDTO.class);
        }

        return alertDTOS;
    }

    @Override
    public List<HostProblem> getZipCurrHostProblemFromRedis() {
        List<HostProblem> ret = null;
        String loginName = iLoginCacheInfo.getLoginName();
        Object redisLock = getRedisLock(loginName);

        if(null != receiveAlertMessageTime && null != currentProblemCacheTime){
            long interval = DateUtils.between(currentProblemCacheTime ,receiveAlertMessageTime ,DateUnitEnum.SECOND);
            if(interval > 0){
                redisUtils.del(currentHostProblemCacheKey);
            }
        }

        if(null != linkChangeTime && null != linkChangeTime){
            long interval = DateUtils.between(currentProblemCacheTime ,linkChangeTime ,DateUnitEnum.SECOND);
            if(interval > 0){
                redisUtils.del(currentHostProblemCacheKey);
            }
        }

        if (!redisUtils.hasKey(currentHostProblemCacheKey)) {
            synchronized (redisLock) {
                if (!redisUtils.hasKey(currentHostProblemCacheKey)) {
                    Date start = new Date();

                    ret = getHostProblems();

                    if(null != ret && ret.size() > 0){
                        String dataStr = JSON.toJSONString(ret);
                        String zipString = GzipTool.gzip(dataStr);
                        redisUtils.set(currentHostProblemCacheKey, zipString, currentCacheExpireTime);
                        currentProblemCacheTime = new Date();
                    }
                    Date end = new Date();
                    long cost = DateUtils.between(start ,end , DateUnitEnum.SECOND);
                }else{
                    ret = doZipCurrInfoFromRedis(currentHostProblemCacheKey ,HostProblem.class);
                }
            }
        }else{
            ret = doZipCurrInfoFromRedis(currentHostProblemCacheKey ,HostProblem.class);
        }

        return ret;
    }

    private Object getRedisLock(String loginName){
        Object redisLock = redisLockMap.get(loginName);
        if(null == redisLock){
            synchronized (redisLockMap){
                redisLock = redisLockMap.get(loginName);
                if(null == redisLock){
                    redisLock = new Object();
                    redisLockMap.put(loginName ,redisLock);
                }
            }
        }
        return redisLock;
    }

    private <T> List<T> doZipCurrInfoFromRedis(String key ,Class<T> clazz){
        List<T> data = null;
        String gipStr = (String)redisUtils.get(key);
        String unGipStr = GzipTool.gunzip(gipStr);
        if(StringUtils.isNotEmpty(unGipStr)){
            data = JSON.parseArray(unGipStr ,clazz);
        }
        return data;
    }

    private List<HostProblem> getHostProblems(){
        List<HostProblemType> hostProblemTypes = new ArrayList<>();
        hostProblemTypes.add(HostProblemType.warning);
        hostProblemTypes.add(HostProblemType.average);
        hostProblemTypes.add(HostProblemType.high);
        hostProblemTypes.add(HostProblemType.disaster);

        List<HostProblem> ret = null;
        Reply reply = mwModelViewCommonService.findAllMonitorServerId();
        if(null != reply && PaasConstant.RES_SUCCESS == reply.getRes()){
            Set<Integer> monitorServerSet = (Set<Integer>)reply.getData();
            ret = new ArrayList<>();
            for(Integer serverId : monitorServerSet){
                if(serverId == null || serverId == 0)continue;
                MWZabbixAPIResult eventGetResult = mwtpServerAPI.hostGetBySeverity(serverId, hostProblemTypes);
                if(!eventGetResult.isFail()){
                    JsonNode data = (JsonNode) eventGetResult.getData();
                    Iterator ite = data.iterator();
                    while(ite.hasNext()){
                        JsonNode node = (JsonNode)ite.next();
                        HostProblem hostProblem = new HostProblem();
                        hostProblem.setMonitorServerId(serverId);
                        hostProblem.setHostId(node.get(AlertEnum.HOSTID.toString().toLowerCase()).asText());
                        hostProblem.setHostName(node.get(AlertEnum.NAME.toString()).asText());
                        hostProblem.setHost(node.get(AlertEnum.HOST.toString()).asText());

                        JsonNode triggers = node.get(AlertEnum.TRIGGERS.toString());
                        Iterator iteTrigger = triggers.iterator();
                        while(iteTrigger.hasNext()){
                            JsonNode trigger = (JsonNode)iteTrigger.next();
                            String status = trigger.get(AlertEnum.STATUS.toString()).asText();

                            if(status.equals(ZabbixTriggerUtils.ENABLE)){
                                Problem problem = new Problem();
                                HostProblemType type = HostProblemType.getType(trigger.get(AlertEnum.PRIORITY.toString()).asText());
                                if(type.isHasProblem()){
                                    problem.setType(type);
                                    problem.setId(trigger.get(AlertEnum.TRIGGERID.toString()).asText());
                                    problem.setDesc(trigger.get(AlertEnum.DESCRIPTIONEN.toString()).asText());
                                    hostProblem.addProblem(problem);
                                }
                            }
                        }
                        ret.add(hostProblem);
                    }
                }
            }
        }
        return ret;
    }

    public List<ZbxAlertDto> getCurrAlertList(AlertParam dto){
        List<ZbxAlertDto> list1 = new ArrayList<>();
        try{
            long assetsTime = System.currentTimeMillis();
            Integer userId = dto.getUserId();
            List<ZbxAlertDto> list = new ArrayList<>();
            if(userId == null){
                userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            }
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
            if(isVir){
                MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
                mwCommonAssetsDto.setUserId(userId);
                mwCommonAssetsDto.setIsQueryAssetsState(false);
                mwCommonAssetsDto.setAlertQuery(false);
                mwCommonAssetsDto.setMonitorFlag(true);
                mwTangibleassetsDTOS = mwAssetsManager.getAllAssetsByUserId(mwCommonAssetsDto);
            }else{
                QueryTangAssetsParam qparam = new QueryTangAssetsParam();
                qparam.setUserId(userId);
                qparam.setIsQueryAssetsState(false);
                if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                    qparam.setAlertQuery(true);
                }
                qparam.setMonitorFlag(true);
                mwTangibleassetsDTOS = mwAssetsManager.getAssetsTable(qparam);
            }
            if(CollectionUtils.isEmpty(mwTangibleassetsDTOS)) return  list1;
            Map<Integer, List<String>> map = mwAssetsManager.getAssetsByServerId(mwTangibleassetsDTOS);
            String key = genRedisKey(saveAlertGetNow, getList, admin);
            if(redisTemplate.hasKey(key) && dto.getIsRedis()){
                String redislist = redisTemplate.opsForValue().get(key);
                list = JSONArray.parseArray(redislist, ZbxAlertDto.class);
                List<ZbxAlertDto> result = new ArrayList<>();
                for (Integer serverId : map.keySet()) {
                    List<String> hostIds = map.get(serverId);
                    if (null != hostIds && hostIds.size() > 0) {
                        AlertParam alertParamUser = new AlertParam();
                        alertParamUser.setHostids(hostIds);
                        alertParamUser.setMonitorServerId(serverId);
                        List<ZbxAlertDto> alertList = getZabbixAlert(alertParamUser,list);
                        if(CollectionUtils.isNotEmpty(alertList)){
                            result.addAll(alertList);
                        }
                    }
                }
                return result;
            }
            long time = System.currentTimeMillis();
            List<Map> monitorMapList = assetsDao.getMonitorServerName();
            Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
            List<AlertReasonEditorParam> reasonEditorParams = assetsDao.selectListAlertSolutionTable();
            List<AlertConfirmUserParam> confirmUserParams = assetsDao.selectConfirmUserList();
            for (Integer serverId : map.keySet()) {
                List<String> hostIds = map.get(serverId);
                log.info("hostid分组:" + hostidGroupCount);
                if (hostIds.size() > 0) {
                    List<List<String>> partition = Lists.partition(hostIds, hostidGroupCount);
                    for(List<String> partitionHostIds : partition){
                        dto.setHostids(partitionHostIds);
                        dto.setMonitorServerId(serverId);
                        List<ZbxAlertDto> partitionList = mwalertManager.getCurrentAltertList(dto);
                        if(CollectionUtils.isNotEmpty(partitionList)){
                            list.addAll(partitionList);
                        }
                    }
                    if(list != null &&  list.size() > 0){
                        //MWZabbixAPIResult eventGetResult = mwtpServerAPI.eventGetByHistory(serverId, dto);
                        for (ZbxAlertDto ad : list) {
                            if (StringUtils.isNotEmpty(ad.getRclock())) {
                                ad.setRclock(ad.getRclock());//恢复时间
                            } else {
                                ad.setRclock(AlertEnum.NOTRECOVERED.toString());//恢复时间
                            }
                            ad.setAlertTimes(0);

                            for (MwTangibleassetsTable mt : mwTangibleassetsDTOS){
                                if(ad.getHostid().equals(mt.getAssetsId()) && serverId.equals(mt.getMonitorServerId())){
                                    ad.setIp(mt.getInBandIp());
                                    ad.setAlertType(mt.getAssetsTypeName());
                                    ad.setObjectName(mt.getAssetsName());
                                    BeanUtils.copyProperties(mt,ad);
                                    break;
                                }
                            }
                            if(CollectionUtils.isNotEmpty(reasonEditorParams)){
                                for (AlertReasonEditorParam reasonEditorParam : reasonEditorParams){
                                    if(ad.getEventid().equals(reasonEditorParam.getEventId()) && serverId.equals(reasonEditorParam.getMonitorServerId())){
                                        ad.setTriggerReason(reasonEditorParam.getTriggerReason());
                                        ad.setSolution(reasonEditorParam.getSolution());
                                        break;
                                    }
                                }
                            }
                            if(CollectionUtils.isNotEmpty(confirmUserParams)){
                                List<String> userNames = new ArrayList<>();
                                for (AlertConfirmUserParam cofirm : confirmUserParams){
                                    if(ad.getEventid().equals(cofirm.getEventId()) && serverId.equals(cofirm.getMonitorServerId())){
                                        userNames.add(cofirm.getUserName());
                                    }

                                }
                                ad.setUserName(userNames.toString());
                                ad.setDealUser(userNames);
                            }
                            ad.setMonitorServerName(monitorMap.get(serverId));
                            if(alertLevel.equals(AlertEnum.GuangZhouBank.toString()))
                                ad.setLinkAlert(getSplitLink(ad.getName(),ad.getHostid(),ad.getMonitorServerId(),ad.getClock(),ad.getIp()));
                            list1.add(ad);
                        }
                    }

                }
            }
            return list1;
        }catch (Exception e){
            log.error("当前告警查询失败:{}",e);
            return list1;
        }
    }

    private String getSplitLink(String title, String hostId, Integer serverId, String time, String hostIp){
        String result = null;
        MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
        mwTangibleassetsDTO.setAssetsId(hostId);
        mwTangibleassetsDTO.setMonitorServerId(serverId);
        mwTangibleassetsDTO.setInBandIp(hostIp);
        StringBuffer sbLink = getAssociatedAlarmLink(mwTangibleassetsDTO);
        String associatedLink = sbLink.toString();
        String linkTargetIp = hostIp;
        if((title.contains(AlertEnum.LINK.toString()) || title.contains(AlertEnum.ICMP.toString())) && associatedLink != null && !associatedLink.equals("") && associatedLink.contains(AlertEnum.LINKNAME.toString())) {
            if(title.contains(AlertEnum.LINK.toString())){
                linkTargetIp = title.substring(title.indexOf(AlertAssetsEnum.RightBracket.toString() + AlertAssetsEnum.LeftBracket.toString()) + 2, title.lastIndexOf(AlertAssetsEnum.RightBracket.toString()));
            }
            List<String> linkNames = new ArrayList<>();
            String associatedModule = associatedLink.substring(associatedLink.indexOf(AlertEnum.LINKNAME.toString() + AlertAssetsEnum.COLON.toString() + AlertAssetsEnum.LeftBracket.toString()) + 6, associatedLink.lastIndexOf(AlertAssetsEnum.RightBracket.toString()));
            if (associatedModule.contains(AlertAssetsEnum.ELLIPSIS.toString())) {
                associatedModule = associatedModule.substring(0, associatedModule.lastIndexOf(AlertAssetsEnum.Comma.toString() + AlertAssetsEnum.ELLIPSIS.toString()));
            }
            linkNames = Arrays.asList(associatedModule.split(","));
            List<AddAndUpdateParam> links = new ArrayList<>();
            AddAndUpdateParam param = new AddAndUpdateParam();
            param.setLinkNames(linkNames);
            param.setLinkTargetIp(linkTargetIp);
            links = mWNetWorkLinkCommonService.getLinkByAssetsIdAndIp(param);
            if (CollectionUtils.isNotEmpty(links)) {
                AddAndUpdateParam link = links.get(0);
                String port = "";
                String ip = "";
                if (link.getValuePort().equals(AlertEnum.ROOT.toString().toUpperCase())) {
                    port = link.getRootPort();
                    ip = link.getRootIpAddress();
                } else {
                    port = link.getTargetPort();
                    ip = link.getTargetIpAddress();
                }
                //取运营商、线路类型、线路编号
                List<MwAssetsLabelDTO> assetsLabels = link.getAssetsLabel();
                String operator = getLaeblValue(assetsLabels, AlertEnum.OPERATOR.toString());
                String linkType = getLaeblValue(assetsLabels, AlertEnum.LINKTYPE.toString());
                String linkNum = getLaeblValue(assetsLabels, AlertEnum.LINKNUM.toString());
                String domain = getLaeblValue(assetsLabels, AlertEnum.Domain.toString());
                StringBuffer sb = new StringBuffer();
                sb.append(AlertAssetsEnum.LeftBracketZH.toString()).append(link.getScanType()).append(AlertAssetsEnum.RightBracketZH.toString()).append(" ")
                        .append(link.getLinkName()).append(AlertAssetsEnum.Dash.toString()).append(domain).append(AlertAssetsEnum.Dash.toString());
                sb.append(AlertAssetsEnum.Dash.toString()).append(AlertEnum.LINKDISCONNECTION.toString());
                sb.append('\n').append(AlertAssetsEnum.LEFTPARENTHESES.toString()).append(port).append(AlertAssetsEnum.LEFTPARENTHESES.toString()).append(ip).append(AlertAssetsEnum.RIGETPARENTHESES.toString()).append(AlertAssetsEnum.RIGETPARENTHESES.toString()).append(AlertAssetsEnum.Comma.toString())
                        .append(AlertEnum.OPERATOR.toString()).append(AlertAssetsEnum.COLON.toString()).append(operator).append(AlertAssetsEnum.Comma.toString())
                        .append(AlertEnum.LINKTYPE.toString()).append(AlertAssetsEnum.COLON.toString()).append(linkType).append(AlertAssetsEnum.Comma.toString())
                        .append(AlertEnum.LINKNUM.toString()).append(AlertAssetsEnum.COLON.toString()).append(linkNum).append(AlertAssetsEnum.Comma.toString())
                        .append(AlertEnum.ALERTHAPPEN.toString()).append(AlertAssetsEnum.COLON.toString()).append(time);
                result = sb.toString();
            }
        }
        return result;
    }

    public String getLaeblValue(List<MwAssetsLabelDTO> assetsLabels,String condition){
        log.info("广州银行标签数据：" + assetsLabels);
        log.info("广州银行condition数据：" + condition);
        String labelValue = "";
        if(CollectionUtils.isNotEmpty(assetsLabels)){
            for(MwAssetsLabelDTO label : assetsLabels){
                if(label.getLabelName().equals(condition)){
                    if (label.getInputFormat().equals(AlertAssetsEnum.One.toString())) {
                        labelValue = label.getTagboard();
                        break;
                    }
                    if (label.getInputFormat().equals(AlertAssetsEnum.Three.toString())) {
                        labelValue = label.getDropValue();
                        break;
                    }
                }
            }
        }
        return labelValue;
    }

    private List<ZbxAlertDto> getZabbixAlert(AlertParam param,List<ZbxAlertDto> alertDtoList){
        if(CollectionUtils.isEmpty(alertDtoList)){
            return null;
        }
        List<ZbxAlertDto> result = new ArrayList<>();
        Map<Integer,List<ZbxAlertDto>> map = alertDtoList.stream().filter(ZbxAlertDto -> ZbxAlertDto.getMonitorServerId() != null).collect(Collectors.groupingBy(ZbxAlertDto::getMonitorServerId));
        List<ZbxAlertDto> temp = map.get(param.getMonitorServerId());
        if(CollectionUtils.isNotEmpty(temp)){
            result = temp.stream().filter(new Predicate<ZbxAlertDto>() {
                @Override
                public boolean test(ZbxAlertDto zbxAlertDto) {
                    if(param.getHostids().contains(zbxAlertDto.getHostid())){
                        return true;
                    }
                    return false;
                }
            }).collect(Collectors.toList());
        }
        return result;
    }


    //当前告警模糊查询
    @Override
    public Reply nowFuzzSeachAllFiledData(AlertParam alertParam){
        List<ZbxAlertDto> list = new ArrayList<>();
        list = getCurrAlertList(alertParam);
        List<String> result = getFuzzyQueryData(list,alertParam);
        Map<String,List<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery",result);
        return Reply.ok(fuzzyQuery);
    }

    //历史告警模糊查询
    @Override
    public Reply histFuzzSeachAllFiledData(AlertParam alertParam){
        List<ZbxAlertDto> list = getHistAlertList(alertParam);
        List<String> result = getFuzzyQueryData(list,alertParam);
        Map<String,List<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery",result);
        return Reply.ok(fuzzyQuery);
    }

    public List<String> getFuzzyQueryData(List<ZbxAlertDto> list, AlertParam alertParam){
        HashSet<String> result = new HashSet<String>();
        if(list.size() > 0 && list != null){
            for (ZbxAlertDto alertDto : list) {
                if (StringUtils.isNotEmpty(alertDto.getName()) && alertDto.getName().toUpperCase().contains(alertParam.getFuzzyQuery().toUpperCase())) {
                    result.add(alertDto.getName());
                }
                if(StringUtils.isNotEmpty(alertDto.getEventid()) && alertDto.getEventid().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getEventid());
                }
                if(StringUtils.isNotEmpty(alertDto.getObjectName()) && alertDto.getObjectName().toUpperCase().contains(alertParam.getFuzzyQuery().toUpperCase())){
                    result.add(alertDto.getObjectName());
                }
                if(StringUtils.isNotEmpty(alertDto.getAlertType()) && alertDto.getAlertType().toUpperCase().contains(alertParam.getFuzzyQuery().toUpperCase())){
                    result.add(alertDto.getAlertType());
                }
                if(StringUtils.isNotEmpty(alertDto.getIp()) && alertDto.getIp().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getIp());
                }
                if(StringUtils.isNotEmpty(alertDto.getClock()) && alertDto.getClock().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getClock());
                }
                if(StringUtils.isNotEmpty(alertDto.getLongTime()) && alertDto.getLongTime().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getLongTime());
                }
                if(StringUtils.isNotEmpty(alertDto.getAcknowledged()) && alertDto.getAcknowledged().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getAcknowledged());
                }
                if(StringUtils.isNotEmpty(alertDto.getSeverity()) && alertDto.getSeverity().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getSeverity());
                }
                if(StringUtils.isNotEmpty(alertDto.getMonitorServerName()) && alertDto.getMonitorServerName().toUpperCase().contains(alertParam.getFuzzyQuery().toUpperCase())){
                    result.add(alertDto.getMonitorServerName());
                }
                if(StringUtils.isNotEmpty(alertDto.getRclock()) && alertDto.getRclock().contains(alertParam.getFuzzyQuery())){
                    result.add(alertDto.getRclock());
                }
            }
        }
        List<String> ts = new ArrayList<>(result);
        Collections.sort(ts, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return (o1.compareToIgnoreCase(o2) == 0 ? o1.compareTo(o2) : o1.compareToIgnoreCase(o2));
            }
        });
        /*TreeSet ts = new TreeSet(result);
        ts.comparator();*/
        return ts;
    }

    @Override
    public Reply getHistAlertPage(AlertParam alertParam) {
        try {
            if(mwInspectModeService.getInspectModeInfo()) return Reply.ok();
            log.info("getHistAlertList start:" + new Date());
            List<ZbxAlertDto> list = getHistAlertList(alertParam);
            log.info("getHistAlertList end:" + new Date());
            log.info("getFilterSelect start:" + new Date());
            list = getFilterSelect(list, alertParam);
            log.info("getFilterSelect end:" + new Date());
            PageList pageList = new PageList();
            if(CollectionUtils.isEmpty(list)) list = new ArrayList<>();
            List newList = pageList.getList(list, alertParam.getPageNumber(), alertParam.getPageSize());
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(alertParam.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            log.info("ACCESS_LOG[][]AlertServiceImpl-getHistAlertPage[][]获取历史告警信息[].");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("[]ERROR_LOG[][]fail to getHistAlertPage with FAIL {}", e);
            return Reply.fail(ErrorConstant.ALARM_HIST_CODE_300002, ErrorConstant.ALARM_HIST_MSG_300002);
        }
    }
    public List<ZbxAlertDto> getHistAlertList(AlertParam alertParam){
        List<ZbxAlertDto> list = getHistAlertListOld(alertParam);
        if(alertLevel.equals(AlertEnum.HUAXING.toString())){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(new Date());
            cd.add(Calendar.DATE,-7);
            Date date = cd.getTime();
            assetsDao.deleteHuaXingAlert(sdf.format(date));
            assetsDao.deleteHuaxingBuAlert(sdf.format(date));
            int huaXingAlertCount = assetsDao.getHuaXingAlertCount();
            int huaXingAlertNum = huaXingAlertCount % 1000 == 0 ? huaXingAlertCount/1000 : (huaXingAlertCount/1000 + 1);
            for(int i=0; i<huaXingAlertNum;i++){
                int startNum = i * 1000 + 1;
                int endNum = (i + 1) * 1000;
                List<ZbxAlertDto> huaxingAlert = assetsDao.getHuaXingAlert(startNum,endNum);
                list.addAll(huaxingAlert);
            }
            int buCount = assetsDao.getHuaxingBuAlertCount();
            int buNum = buCount % 1000 == 0 ? buCount/1000 : (buCount/1000 + 1);
            for(int i=0; i<buNum;i++){
                int startNum = i * 1000 + 1;
                int endNum = (i + 1) * 1000;
                List<ZbxAlertDto> huaxingBuAlert = assetsDao.getHuaxingBuAlert(startNum,endNum);
                list.addAll(huaxingBuAlert);
            }
        }
        return list;
    }

    public List<ZbxAlertDto> getHistAlertListOld(AlertParam alertParam){
        List<ZbxAlertDto> list = new ArrayList<>();
        Integer userId = alertParam.getUserId();
        if(userId == null){
            userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        }
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        if(isVir){
            MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
            mwCommonAssetsDto.setUserId(userId);
            mwCommonAssetsDto.setMonitorFlag(true);
            mwTangibleassetsDTOS = mwAssetsManager.getAllAssetsByUserId(mwCommonAssetsDto);
        }else{
            QueryTangAssetsParam qparam = new QueryTangAssetsParam();
            qparam.setUserId(userId);
            qparam.setIsQueryAssetsState(false);
            if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                qparam.setAlertQuery(true);
            }
            qparam.setMonitorFlag(true);
            mwTangibleassetsDTOS = mwAssetsManager.getAssetsTable(qparam);
        }
        if(CollectionUtils.isEmpty(mwTangibleassetsDTOS)) return list;
        Map<Integer, List<String>> map = mwAssetsManager.getAssetsByServerId(mwTangibleassetsDTOS);
        String key = genRedisKey(saveAlertGetHist, getList, admin);
        if(redisTemplate.hasKey(key) && alertParam.getIsRedis()){
            log.info("历史告警redis进程");
            String redislist = redisTemplate.opsForValue().get(key);
            list = JSONArray.parseArray(redislist, ZbxAlertDto.class);
            List<ZbxAlertDto> result = new ArrayList<>();
            for (Integer serverId : map.keySet()) {
                List<String> hostIds = map.get(serverId);
                if (null != hostIds && hostIds.size() > 0) {
                    AlertParam alertParamUser = new AlertParam();
                    alertParamUser.setHostids(hostIds);
                    alertParamUser.setMonitorServerId(serverId);
                    List<ZbxAlertDto> alertList = getZabbixAlert(alertParamUser,list);
                    if(CollectionUtils.isNotEmpty(alertList)){
                        result.addAll(alertList);
                    }
                }
            }
            return result;
        }
        List<Map> monitorMapList = assetsDao.getMonitorServerName();
        Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
        List<AlertReasonEditorParam> reasonEditorParams = assetsDao.selectListAlertSolutionTable();
        List<AlertConfirmUserParam> confirmUserParams = assetsDao.selectConfirmUserList();
        for (Integer serverId : map.keySet()) {
            List<String> hostIds = map.get(serverId);
            if (hostIds.size() > 0 && hostIds != null) {
                List<List<String>> partition = Lists.partition(hostIds, hostidGroupCount);
                List<ZbxAlertDto> alertHist = new ArrayList<>();
                for(List<String> partitionHostIds : partition){
                    alertParam.setHostids(partitionHostIds);
                    alertParam.setMonitorServerId(serverId);
                    alertParam.setHisAlertCount(hisAlertCount);
                    List<ZbxAlertDto> partitionHist = getAlertHist(alertParam);
                    if(CollectionUtils.isNotEmpty(partitionHist)){
                        alertHist.addAll(partitionHist);
                    }
                }

                //统计同一笔告警出现次数
                if (null != alertHist && alertHist.size() > 0) {
                    for (ZbxAlertDto zbxAlertDto : alertHist) {
                        zbxAlertDto.setAlertTimes(0);
                        for(ZbxAlertDto s : alertHist){
                            if(s.getHostid().equals(zbxAlertDto.getHostid()) && s.getName().equals(zbxAlertDto.getName())){
                                zbxAlertDto.setAlertTimes(zbxAlertDto.getAlertTimes() + 1);
                            }
                        }
                        for (MwTangibleassetsTable mt : mwTangibleassetsDTOS){
                            if(zbxAlertDto.getHostid().equals(mt.getAssetsId()) && serverId.equals(mt.getMonitorServerId())){
                                zbxAlertDto.setIp(mt.getInBandIp());
                                zbxAlertDto.setAlertType(mt.getAssetsTypeName());
                                zbxAlertDto.setObjectName(mt.getAssetsName());
                                BeanUtils.copyProperties(mt,zbxAlertDto);
                                break;
                            }
                        }
                        if(CollectionUtils.isNotEmpty(reasonEditorParams)){
                            for (AlertReasonEditorParam reasonEditorParam : reasonEditorParams){
                                if(zbxAlertDto.getEventid().equals(reasonEditorParam.getEventId()) && serverId.equals(reasonEditorParam.getMonitorServerId())){
                                    zbxAlertDto.setTriggerReason(reasonEditorParam.getTriggerReason());
                                    zbxAlertDto.setSolution(reasonEditorParam.getSolution());
                                }
                                break;
                            }
                        }
                        if(CollectionUtils.isNotEmpty(confirmUserParams)){
                            List<String> userNames = new ArrayList<>();
                            for (AlertConfirmUserParam confirm : confirmUserParams){
                                if(zbxAlertDto.getEventid().equals(confirm.getEventId()) && serverId.equals(confirm.getMonitorServerId())){
                                    userNames.add(confirm.getUserName());
                                }

                            }
                            zbxAlertDto.setUserName(userNames.toString());
                            zbxAlertDto.setDealUser(userNames);
                        }
                        zbxAlertDto.setMonitorServerName(monitorMap.get(serverId));
                        list.add(zbxAlertDto);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void export(AlertParam alertParam, HttpServletResponse response) throws ParseException {
        List<ZbxAlertDto> alertHistorylist = new ArrayList<>();
        if(alertParam.getIsNowAlert()!=null && alertParam.getIsNowAlert()){
            alertHistorylist = getCurrAlertList(alertParam);
        }else {
            alertHistorylist = getHistAlertList(alertParam);
        }
        alertHistorylist = getFilterSelect(alertHistorylist,alertParam);
        if(CollectionUtils.isNotEmpty(alertHistorylist) ){
            int count = assetsDao.selectCountRecordTable(alertParam.getClockStart(), alertParam.getClockEnd());
            int num = count % 1000 == 0 ? count/1000 : (count/1000 + 1);
            List<AlertRecordTableDTO> sendInfos = new ArrayList<>();
            for(int i=0; i<num;i++){
                int startNum = i * 1000 + 1;
                int endNum = (i + 1) * 1000;
                List<AlertRecordTableDTO> temp = assetsDao.getSendInfoList(alertParam.getClockStart(), alertParam.getClockEnd(),startNum,endNum);
                sendInfos.addAll(temp);
            }

            if(alertLevel.equals(AlertEnum.HUAXING.toString()) && CollectionUtils.isNotEmpty(sendInfos)){
                List<Integer> ids = new ArrayList<>();
                for (AlertRecordTableDTO temp : sendInfos){
                    ids.add(temp.getId());
                }

                List<AlertRecordTableDTO> recordTableDTOS = new ArrayList<>();
                List<List<Integer>> disSplitList = splitList(ids,300);
                for(List<Integer> idsList : disSplitList){
                    List<AlertRecordTableDTO> temp = assetsDao.getAlertRecordUserIds(idsList);;
                    recordTableDTOS.addAll(temp);
                }
                for(AlertRecordTableDTO dto : sendInfos){
                    dto.setUserName(getUserName(dto.getId(),recordTableDTOS));
                }
            }

            for(ZbxAlertDto dto : alertHistorylist){
                if(StringUtils.isEmpty(dto.getEventid())) continue;;
                StringBuffer sb = new StringBuffer();
                RecordParam recordParam = new RecordParam();
                recordParam.setPageNumber(1);
                recordParam.setPageSize(1000);
                recordParam.setEventid(dto.getEventid());
                log.info("导出告警消息：" + sendInfos);
                if(CollectionUtils.isEmpty(sendInfos)) continue;
                for(AlertRecordTableDTO recordTableDTO : sendInfos){
                    if(StringUtils.isNotEmpty(dto.getEventid()) && dto.getEventid().equals(recordTableDTO.getEventId()) && dto.getHostid().equals(recordTableDTO.getHostid())){
                        if(StringUtils.isNotEmpty(recordTableDTO.getUserName())){
                            sb.append(recordTableDTO.getMethod()).append("-").append(recordTableDTO.getUserName()).append("\n");
                        }
                    }
                }
                dto.setNotifyUser(sb.toString());
            }

        }
        //List<MwAlerthistory7daysParam> list = mwAlerthistory7daysTableDao.getAlertHistory(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
        Class dtoclass = null;
        ExcelWriter excelWriter = null;
        dtoclass = ZbxAlertDto.class;
        try {
            Set<String> fields = new HashSet<>();
            //fields.add(AlertEnum.EXCELALERTPARAM.toString());
            fields.add(AlertEnum.EVENTIDEN.toString());
            fields.add(AlertEnum.OBJECTID.toString());
            fields.add(AlertEnum.NAME.toString());
            fields.add(AlertEnum.SEVERITY.toString());
            fields.add(AlertEnum.OBJECTNAME.toString());
            fields.add(AlertEnum.ALERTTYPE.toString());
            fields.add(AlertEnum.IP.toString());
            fields.add(AlertEnum.CLOCK.toString());
            fields.add(AlertEnum.LONGTIME.toString());
            fields.add(AlertEnum.ACKNOWLEDGED.toString());
            fields.add(AlertEnum.RCLOCK.toString());
            fields.add(AlertEnum.TRIGGERREASON.toString());
            fields.add(AlertEnum.SOLUTION.toString());
            fields.add(AlertEnum.USERNAME.toString());
            fields.add(AlertEnum.ALERTTIMES.toString());
            fields.add(AlertEnum.MESSAGE.toString());
            fields.add("modelSystem");
            fields.add(AlertEnum.MODELCLASSIFYEN.toString());
            fields.add(AlertEnum.NOTIFYUSER.toString());
            //excelAlertParam.setFields(fields);
            /*if (excelAlertParam.getFields() != null && excelAlertParam.getFields().size() > 0 && dtoclass != null) {
                includeColumnFiledNames = excelAlertParam.getFields();
            }*/
            //创建easyExcel写出对象
           // excelWriter = ReportUtil.getExcelWriter(uParam, response, dtoclass);
            //excelWriter = ReportUtil.getExcelWriter(uParam, response, dtoclass);
            String fileName = AlertEnum.HISTORY.toString(); //导出文件名
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 头的策略
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);//设置头居中
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            WriteFont contentWriteFont = new WriteFont();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short) 12);
            contentWriteCellStyle.setWriteFont(contentWriteFont);
            // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
            HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            //创建easyExcel写出对象
            excelWriter = EasyExcel.write(response.getOutputStream(), dtoclass).registerWriteHandler(horizontalCellStyleStrategy).build();

            //计算sheet分页
            WriteSheet sheet = EasyExcel.writerSheet(0, AlertEnum.SHEET.toString())
                    .includeColumnFiledNames(fields)
                    .build();
            excelWriter.write(alertHistorylist, sheet);

        } catch (Exception e) {
            log.error("导出失败：{}",e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    public static <T> List<List<T>> splitList(List<T> list, int chunkSize) {
        List<List<T>> subLists = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, list.size());
            subLists.add(list.subList(i, endIndex));
        }
        return subLists;
    }
    /**
     * 根据objectId和zabbix版本号monitorServerId
     * 获取告警历史
     *
     * @return
     */
    @Override
    public Reply getAlertHistory(Integer monitorServerId, String objectid) {
        try {
            if (objectid != null && StringUtils.isNotEmpty(objectid)) {
                List<MWHistDto> list = mwalertManager.getAlarmHistEvent(monitorServerId, objectid);
                return Reply.ok(list);
            } else {
                return Reply.fail(ErrorConstant.ALARM_SHOW_CODE_300003, ErrorConstant.ALARM_SHOW_MSG_300003);
            }

        } catch (Exception e) {
            log.error("ERROR_LOG[][][]获得当前告警的历史纪录[][]objectid:[]case{}.", objectid, e);
            return Reply.fail(ErrorConstant.ALARM_SHOW_CODE_300003, ErrorConstant.ALARM_SHOW_MSG_300003);
        }

    }

    @Autowired
    private MWOrgService orgService;

    /**
     * 获取告警通知
     *
     * @param eventid
     * @return
     */
    @Override
    public Reply getNoticeList(Integer monitorServerId, String eventid) {
        try {
            HashMap list = mwalertManager.getActionAlertByEventid(monitorServerId, eventid);
            if(list.containsKey(AlertEnum.MESSAGE.toString())){
                String msg = toJsonString(list.get(AlertEnum.MESSAGE.toString()).toString());
                msg = converUnicodeToChar(msg);
                String[] strs = msg.split(",");
                String regex = ":";
                HashMap<String, String> map = new HashMap<>();
                for (String s : strs) {
                    String s1 = s.substring(0, s.indexOf(regex) + 1).replaceAll(":", "");
                    String s2 = s.substring(s.indexOf(regex) + 1);
                    map.put(s1, s2);
                }
                QueryTangAssetsParam qparam = new QueryTangAssetsParam();
                qparam.setIsQueryAssetsState(false);
                qparam.setAlertQuery(true);
                qparam.setAssetsId(map.get(AlertEnum.HOSTID.toString()));
                qparam.setInBandIp(map.get(AlertEnum.HOSTIP.toString()));
                qparam.setUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
                List<MwTangibleassetsTable> assetsList = mwAssetsManager.getAssetsTable(qparam);
                if(CollectionUtils.isNotEmpty(assetsList)){
                    MwTangibleassetsTable assets = assetsList.get(0);
                    list.put(AlertEnum.DOMAINEN.toString(), assets.getRelationArea());
                    if(!modelAssetEnable){
                        if(domain.equals(AlertEnum.TAG.toString())){
                            MwTangibleassetsDTO labelDtos = mwAssetsManager.getAssetsAndOrgs(assets.getId());
                            if(CollectionUtils.isNotEmpty(labelDtos.getAssetsLabel())){
                                for(MwAssetsLabelDTO labelDTO : labelDtos.getAssetsLabel()){
                                    if(labelDTO.getLabelName().equals(AlertEnum.Domain.toString())){
                                        if (labelDTO.getInputFormat().equals(AlertAssetsEnum.One.toString())) {
                                            list.put(AlertEnum.DOMAINEN.toString(), labelDTO.getTagboard());
                                            break;
                                        }
                                        if (labelDTO.getInputFormat().equals(AlertAssetsEnum.Three.toString())) {
                                            list.put(AlertEnum.DOMAINEN.toString(), labelDTO.getDropValue());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(domain.equals(AlertEnum.ORGEN.toString())){
                            List<OrgDTO> orgDTOList = new ArrayList<>();
                            Map tempMap = new HashMap();
                            List listQuery = new ArrayList();
                            listQuery.add(assets.getId());
                            tempMap.put("ids", listQuery);
                            Reply reply = mwTangibleAssetsService.selectListWithExtend(tempMap);
                            if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                                List<MwTangibleassetsDTO> datas = (List) reply.getData();
                                MwTangibleassetsDTO temp = new MwTangibleassetsDTO();
                                temp = datas.get(0);
                                orgDTOList = temp.getDepartment();
                            }
                            if (CollectionUtils.isNotEmpty(orgDTOList)) {
                                log.info("labelDTOID:" + orgDTOList.size());
                                String orgName = "";
                                for (OrgDTO s : orgDTOList) {
                                    orgName = orgName + s.getOrgName() + "/";
                                }
                                orgName = orgName.substring(0, orgName.length() - 1);
                                list.put(AlertEnum.DOMAINEN.toString(), orgName);
                            }
                        }
                    }else {
                        List<MWOrg> orgDTOS = new ArrayList<>();
                        if(CollectionUtils.isNotEmpty(assets.getModelViewOrgIds())){
                            for(List<Integer> oIds : assets.getModelViewOrgIds()){
                                if(CollectionUtils.isNotEmpty(oIds)){
                                    for (Integer orgId : oIds){
                                        Reply res = orgService.selectByOrgId(orgId);
                                        if (null != res && PaasConstant.RES_SUCCESS == res.getRes()) {
                                            MWOrg temp = (MWOrg) res.getData();
                                            orgDTOS.add(temp);
                                        }

                                    }
                                }

                            }
                        }
                        if (CollectionUtils.isNotEmpty(orgDTOS)) {
                            log.info("labelDTOID:" + orgDTOS.size());
                            String orgName = "";
                            for (MWOrg s : orgDTOS) {
                                orgName = orgName + s.getOrgName() + "/";
                            }
                            orgName = orgName.substring(0, orgName.length() - 1);
                            list.put(AlertEnum.DOMAINEN.toString(), orgName);
                        }
                    }
                }else{
                    list.put(AlertEnum.ASSETSNAMEEN.toString(),map.get(AlertEnum.HOSTNAME.toString()));
                    list.put(AlertEnum.IP.toString(), map.get(AlertEnum.HOSTIP.toString()));
                }
            }

            AlertReasonEditorParam param = new AlertReasonEditorParam();
            param.setEventId(eventid);
            param.setMonitorServerId(monitorServerId);
            AlertReasonEditorParam result = assetsDao.selectAlertSolutionTable(param);
            if(result != null){
                list.put(AlertEnum.SOLUTION.toString(),result.getSolution());
                list.put(AlertEnum.TRIGGERREASON.toString(),result.getTriggerReason());
            }

            return Reply.ok(list);
        } catch (Exception e) {

            log.error("ERROR_LOG[][][]获得当前告警的历史纪录[][]objectid:[]{}case{}", eventid, e);
            return Reply.fail(ErrorConstant.ALARM_SHOW_CODE_300003, ErrorConstant.ALARM_SHOW_MSG_300003);
        }
    }

    public String converUnicodeToChar(String str) {
        str = str.replaceAll("\\\\r", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\\\\f", "")
                .replaceAll("\\\\b", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll(" ", "")
                .replaceAll("\\\\\"", "\"");

        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);

        //迭代，将str的unicode都转为字符
        while (matcher.find()) {
            String unicodeFull = matcher.group(1);
            String unicodeNum = matcher.group(2);

            char singleChar = (char) Integer.parseInt(unicodeNum, 16);
            str = str.replace(unicodeFull, singleChar + "");
        }
        return str;
    }

    //将json格式多余的双引号去掉
    private static String toJsonString(String s) {
        char[] tempArr = s.toCharArray();
        int tempLength = tempArr.length;
        for (int i = 0; i < tempLength; i++) {
            if (tempArr[i] == ':' && tempArr[i + 1] == '"') {
                for (int j = i + 2; j < tempLength; j++) {
                    if (tempArr[j] == '"') {
                        if (tempArr[j + 1] != ',' && tempArr[j + 1] != '}') {
                            tempArr[j] = '”'; // 将value中的 双引号替换为中文双引号
                        } else if (tempArr[j + 1] == ',' || tempArr[j + 1] == '}') {
                            break;
                        }
                    }
                }
            }
        }
        return new String(tempArr);
    }

    public List<ZbxAlertDto> getAlertHist(AlertParam alertParam) {
        //   MWAlertParamDto dtoA = CopyUtils.copy(MWAlertParamDto.class, alertParam);
        List<ZbxAlertDto> list = mwalertManager.getHistoryAlertList(alertParam);
        return list;
    }

    /**
     * 确认事件
     *
     * @param eventid
     * @return
     */
    @Override
    public Reply confirm(Integer monitorServerId, Integer uid, String eventid, String type) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!alertLevel.equals(AlertEnum.SHANYING.toString())){
                if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                    return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
                }
            }
            MWZabbixAPIResult result = mwtpServerAPI.eventacknowledge(monitorServerId, eventid, type);
            log.info("确认事件 result:" + result);
            if (result.isFail()) {
                log.error("处理告警zabbix中返回的错误信息{}", result.getData());
                if (type.equals("cl")) {
                    return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "当前告警不能手动关闭，如需关闭请修改触发器设置");
                }
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, String.valueOf(result.getData()));
            }
            AlertConfirmUserParam param = new AlertConfirmUserParam();
            param.setEventId(eventid);
            param.setMonitorServerId(monitorServerId);
            param.setUserId(userId);
            param.setType(type);
            assetsDao.insertConfirmUserTable(param);
            /*AlertParam alertParam = new AlertParam();
            alertParam.setUserId(uid);
            alertParam.setMonitorServerId(monitorServerId);
            List<ZbxAlertDto> list = mwalertManager.getCurrentAltertList(alertParam);
            AlertParam alertHistParam = new AlertParam();
            alertHistParam.setIsSeverDay("1");
            alertHistParam.setDays("7");
            alertHistParam.setMonitorServerId(monitorServerId);
            alertHistParam.setUserId(uid);
            //List<ZbxAlertDto> list1 = mwAlertService.getAlertHist(alertHistParam);
            String key = genRedisKey("saveAlertGetNow", "get_list", uid);
            //String key1 = genRedisKey("saveAlertGetHist", "get_list", uid);
            saveToRedis(key, JSON.toJSONString(list));
            //saveToRedis(key1, JSON.toJSONString(list1));*/
            return Reply.ok();
        } catch (Exception e) {
            log.error("ERROR_LOG[][][]告警事件确认[][]eventid:[]{},type:[]{},case{}.", eventid, type, e);
            return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, ErrorConstant.ALARM_HANDLER_MSG_300004);
        }
    }

    /**
     * 批量确认事件
     *
     * @param param
     * @return
     */
    @Override
    public Reply confirmList(List<ConfirmDto> param, String type) {
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            Map<Integer, List<String>> map = new HashMap<>();
            map = param.stream().collect(Collectors.groupingBy(ConfirmDto::getMonitorServerId, Collectors.mapping(ConfirmDto::getEventid, Collectors.toList())));
            for (Integer key: map.keySet()) {
                List<String> eventids = map.get(key);
                MWZabbixAPIResult result = mwtpServerAPI.eventacknowledge(key, eventids, type);
                log.info("确认事件 result:" + result);
                if (result.isFail()) {
                    log.error("处理告警zabbix中返回的错误信息{}", result.getData());
                    return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, String.valueOf(result.getData()));
                }
            }
            List<AlertConfirmUserParam> confirmUserParams = new ArrayList<>();
            for(ConfirmDto dto : param){
                AlertConfirmUserParam temp = new AlertConfirmUserParam();
                temp.setUserId(userId);
                temp.setMonitorServerId(dto.getMonitorServerId());
                temp.setEventId(dto.getEventid());
                temp.setType(type);
                confirmUserParams.add(temp);
            }
            assetsDao.insertConfirmUserTables(confirmUserParams);
            return Reply.ok();
        } catch (Exception e) {
            log.error("ERROR_LOG[][][]告警事件确认[][],type:[]{},case{}.", type, e);
            return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, ErrorConstant.ALARM_HANDLER_MSG_300004);
        }
    }

    @Override
    public Reply getItemByTriggerId(Integer monitorServerId, String objectid) {
        try {
            List<MWItemDto> list = mwalertManager.getItemByTriggerId(monitorServerId, objectid);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("ERROR_LOG[][][]告警查询item[][]objectid:[]{}case{}", objectid, e);
            return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, ErrorConstant.ALARM_HANDLER_MSG_300004);
        }
    }

    @Override
    public Reply getHistoryByItemId(MWItemDto mwItemDto) {
        try {
            MWAlertHistoryDto history = mwalertManager.getHistoryByItemId(mwItemDto);
            return Reply.ok(history);
        } catch (Exception e) {
            log.error("ERROR_LOG[][][]告警查询item的历史数据[][]mwItemDto:[]{}case{}", mwItemDto, e);
            return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, ErrorConstant.ALARM_HANDLER_MSG_300004);
        }
    }

    @Override
    public Reply getLuceneByTitle(String title) {
        try {
            String[] fields = new String[]{"title", "triggerCause", "solution"};
            log.info("getLuceneByTitle fields:" + fields.toString());
            List<Map> list = LuceneUtils.searchByMultiple(fields, title, 10);
            log.info("getLuceneByTitle list:" + list.toString());
            list.forEach(map -> {
                Integer likeCount = mwKnowledgeService.getLikeOrHateCount(map.get("id").toString(), 0);
                map.put(AlertEnum.TYPE.toString(), likeCount);
                log.info("getLuceneByTitle likeCount:" + likeCount);
            });
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("ERROR_LOG[][][]告警查询知识库[][]title:[]{}case{}", title, e);
            return Reply.fail(ErrorConstant.ALERT_ACTION_LUCENE_CODE_300016, ErrorConstant.ALERT_ACTION_LUCENE_MAG_300016);
        }

    }

    /*@Override
    public String getAssetsMonitor(MwTangibleassetsDTO mwTangibleassetsDTO) {
        List<Map> webmap = assetsDao.getWebMonitor(mwTangibleassetsDTO.getId());
        List<Map> linkmap = assetsDao.getLink(mwTangibleassetsDTO.getAssetsId(), mwTangibleassetsDTO.getMonitorServerId());
        StringBuffer sb = new StringBuffer();
        if (null != webmap && webmap.size() > 0) {
            sb.append("关联Web监测:[");
            for (int i = 0; i < webmap.size(); i++) {
                sb.append(webmap.get(i).get("webName").toString()).append(":").append(webmap.get(i).get("webUrl").toString()).append(",");
                if (i == 2) {
                    sb.append("...");
                    sb.append("等"+webmap.size()+"条Web名称");
                    break;
                }
            }
            sb.append("]").append('\n');
        }
        if (null != linkmap && linkmap.size() > 0) {
            sb.append("关联线路名称:[");
            for (int i = 0; i < linkmap.size(); i++) {
                sb.append(linkmap.get(i).get("linkName").toString()).append(",");
                if (i == 2) {
                    sb.append("...");
                    sb.append("等"+linkmap.size()+"条线路名称");
                    break;
                }
            }
            sb.append("]");
        }
        if (sb.toString().length() > 0) {
            return sb.toString();
        }
        return null;
    }*/


    @Autowired
    AssociatedSubject associatedSubject;

    @Override
    public String getAssetsMonitor(MwTangibleassetsDTO mwTangibleassetsDTO) {

        //associatedSubject.addAssociatedAlarm(new LinkAssociatedAlram(mwTangibleassetsDTO));
        //associatedSubject.addAssociatedAlarm(new WebAssociatedAlarm(mwTangibleassetsDTO));
        StringBuffer linkSb = getAssociatedAlarmLink(mwTangibleassetsDTO);
        StringBuffer webSb = getAssociatedAlarmWeb(mwTangibleassetsDTO);
        linkSb.append(webSb);
        //String allAssociated = associatedSubject.notifyAllAssociated();
        return linkSb.toString();
    }
    public StringBuffer getAssociatedAlarmWeb(MwTangibleassetsDTO mwTangibleassetsDTO) {
        StringBuffer sb = new StringBuffer();
        synchronized (sb){
            List<Map> webmap = assetsDao.getWebMonitor(mwTangibleassetsDTO.getId());
            if (null != webmap && webmap.size() > 0) {
                sb.append("关联Web监测:[");
                for (int i = 0; i < webmap.size(); i++) {
                    sb.append(webmap.get(i).get("webName").toString()).append(":").append(webmap.get(i).get("webUrl").toString()).append(",");
                    if (i == 2) {
                        sb.append("...");
                        sb.append("等" + webmap.size() + "条Web名称");
                        break;
                    }
                }
                sb.append("]");
            }
            return sb;
        }
    }
    public StringBuffer getAssociatedAlarmLink(MwTangibleassetsDTO mwTangibleassetsDTO) {
        StringBuffer sb = new StringBuffer();
        synchronized (sb){
            List<Map> linkmap = assetsDao.getLink(mwTangibleassetsDTO.getAssetsId(), mwTangibleassetsDTO.getMonitorServerId(),mwTangibleassetsDTO.getInBandIp());
            log.info("关联线路名称：" + linkmap);
            log.info("关联线路名称getAssetsId：" + mwTangibleassetsDTO.getAssetsId());
            log.info("关联线路名称getMonitorServerId：" + mwTangibleassetsDTO.getMonitorServerId());
            if (null != linkmap && linkmap.size() > 0) {
                sb.append("关联线路名称:[");
                for (int i = 0; i < linkmap.size(); i++) {
                    sb.append(linkmap.get(i).get("linkName").toString()).append(",");
                    if (i == 2) {
                        sb.append("...");
                        sb.append("等"+linkmap.size()+"条线路名称");
                        break;
                    }
                }
                sb.append("]");
            }
            return sb;
        }



    }

    @Override
    public List<ZbxAlertDto> getFilterSelect(List<ZbxAlertDto> mwAlertDtos, AlertParam alertParam) throws ParseException {
        if(CollectionUtils.isEmpty(mwAlertDtos)) return mwAlertDtos;
        ListSortUtil<ZbxAlertDto> listSortutil = new ListSortUtil<>();
        mwAlertDtos = listSortutil.sort(mwAlertDtos,"clock",1);
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotBlank(alertParam.getFuzzyQuery())) {
            alertParam.setFuzzyQuery(alertParam.getFuzzyQuery().toUpperCase());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) ->m.getName() != null && m.getName().toUpperCase().contains(alertParam.getFuzzyQuery()) ||
                            m.getSeverity().toUpperCase().contains(alertParam.getFuzzyQuery()) || m.getAlertType().toUpperCase().contains(alertParam.getFuzzyQuery()) ||
                            m.getObjectName().toUpperCase().contains(alertParam.getFuzzyQuery()) || m.getMonitorServerName().toUpperCase().contains(alertParam.getFuzzyQuery()) ||
                            m.getIp() != null && m.getIp().toUpperCase().contains(alertParam.getFuzzyQuery()) || m.getLongTime().toUpperCase().contains(alertParam.getFuzzyQuery()) ||
                            m.getAcknowledged().toUpperCase().equals(alertParam.getFuzzyQuery()) || m.getEventid().toUpperCase().contains(alertParam.getFuzzyQuery()) ||
                            m.getRclock().toUpperCase().contains(alertParam.getFuzzyQuery()) || m.getClock().toUpperCase().contains(alertParam.getFuzzyQuery()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getName())) {
            alertParam.setName(alertParam.getName().toUpperCase());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) ->m.getName() != null && m.getName().toUpperCase().contains(alertParam.getName()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && null != alertParam.getSeverity() && alertParam.getSeverity().size() > 0) {
            List<String> severityList = new ArrayList<>();
            for (String severitys : alertParam.getSeverity()) {
                severityList.add(severitys);
            }
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> severityList.contains(m.getSeverity()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getAlertType())) {
            alertParam.setAlertType(alertParam.getAlertType().toUpperCase());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) ->{
                                if(StringUtils.isNotBlank(m.getAlertType())){
                                    return m.getAlertType().toUpperCase().contains(alertParam.getAlertType());
                                }
                                return false;
                            })
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getObjectName())) {
            alertParam.setObjectName(alertParam.getObjectName().toUpperCase());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getObjectName().toUpperCase().contains(alertParam.getObjectName()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getMonitorServerName())) {
            alertParam.setMonitorServerName(alertParam.getMonitorServerName().toUpperCase());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> {
                        if(StringUtils.isNotBlank(m.getMonitorServerName())){
                            return m.getMonitorServerName().toUpperCase().contains(alertParam.getMonitorServerName());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getIp())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) ->m.getIp() != null && m.getIp().contains(alertParam.getIp()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getLongTime())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getLongTime().contains(alertParam.getLongTime()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getAcknowledged())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> alertParam.getAcknowledged().equals(m.getAcknowledged()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getEventid())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getEventid().contains(alertParam.getEventid()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getRclock())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getRclock().contains(alertParam.getRclock()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && alertParam.getQueryMonitorServerId() != null) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) ->m.getMonitorServerId() != null && m.getMonitorServerId().equals(alertParam.getQueryMonitorServerId()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getStartTime()) && StringUtils.isNotEmpty(alertParam.getEndTime())) {

            Long startTime = simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd").parse(alertParam.getStartTime()));
            Long endTime = simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd").parse(alertParam.getEndTime())) + 86400000;
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> {
                        try {
                            return startTime < simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(m.getClock().trim())) && endTime > simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(m.getClock().trim()));
                        } catch (ParseException e) {
                            log.error("筛选失败:{}",e);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS'Z'"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss"))
                .toFormatter();
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getClockStart()) && StringUtils.isNotEmpty(alertParam.getClockEnd())) {
            ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
            Long startTime = simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd").parse(alertParam.getClockStart()));
            Long endTime = simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd").parse(alertParam.getClockEnd())) + 86400000;
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> {
                       if( startTime < LocalDateTime.parse(m.getClock().trim(), formatter).atZone(chinaZone).toInstant().toEpochMilli() && endTime > LocalDateTime.parse(m.getClock().trim(), formatter).atZone(chinaZone).toInstant().toEpochMilli()){
                           return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(mwAlertDtos) && CollectionUtils.isNotEmpty(alertParam.getQueryHostIds())) {
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> alertParam.getQueryHostIds().contains(m.getHostid()))
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getModelClassify())){
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getModelClassify() != null && alertParam.getModelClassify().contains(m.getModelClassify()))
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(mwAlertDtos) && StringUtils.isNotEmpty(alertParam.getModelSystem())){
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((ZbxAlertDto m) -> m.getModelSystem() != null && alertParam.getModelSystem().contains(m.getModelSystem()))
                    .collect(Collectors.toList());
        }
       /* if (alertParam.getIsSeverDay().equals("1")) {
            Collections.sort(mwAlertDtos, new Comparator<MWAlertDto>() {
                @Override
                public int compare(MWAlertDto o1, MWAlertDto o2) {
                    int flag = Integer.parseInt(o1.getSeverity()) - Integer.parseInt(o2.getSeverity());//按照等级去排序
                    return flag;
                }
            });
        }
        if (StringUtils.isNotEmpty(alertParam.getDays())) {
            List<String> daysList = new ArrayList<>();
            daysList.add(alertParam.getDays());
            mwAlertDtos = mwAlertDtos.stream()
                    .filter((MWAlertDto m) -> daysList.contains(m.getSeverity()))
                    .collect(Collectors.toList());
        }*/
        if(CollectionUtils.isNotEmpty(mwAlertDtos)){
            Collections.sort(mwAlertDtos, new Comparator<ZbxAlertDto>() {
                @Override
                public int compare(ZbxAlertDto o1, ZbxAlertDto o2) {
                    LocalDateTime dateTime1 = LocalDateTime.parse(o1.getClock().trim(), formatter);
                    LocalDateTime dateTime2 = LocalDateTime.parse(o2.getClock().trim(), formatter);
                    return dateTime2.compareTo(dateTime1);
                }

            });
        }
        String loginName = iLoginCacheInfo.getLoginName();
        if(!alertLevel.equals(AlertEnum.HUAXING.toString())){

            List<IgnoreAlertDto> ignoreAlert = assetsDao.selectIgnoreTable();
            if(CollectionUtils.isNotEmpty(ignoreAlert)){
                List<IgnoreAlertDto> finalIgnoreAlert = ignoreAlert;
                List<ZbxAlertDto> finalMwAlertDtos = mwAlertDtos;
                mwAlertDtos = mwAlertDtos.stream()
                        .filter(m -> ! finalIgnoreAlert.stream()
                                .anyMatch(m1 -> m.getEventid().equals(m1.getEventid()) &&  m.getMonitorServerId().equals(m1.getMonitorServerId())))
                        .collect(Collectors.toList());
                ignoreAlert = ignoreAlert.stream().filter(m -> !finalMwAlertDtos.stream()
                                .anyMatch(m1 -> m.getMonitorServerId().equals(m1.getMonitorServerId()) && m.getEventid().equals(m1.getEventid())))
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(ignoreAlert) && iLoginCacheInfo.getRoleId(loginName).equals("0")){
                    assetsDao.deleteIgnoreTable(ignoreAlert);
                }
            }
        }

        return mwAlertDtos;
    }

    //日期转换为毫秒
    private Long simpleDateFormat(Date s) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(s);
        return calendar.getTimeInMillis();
    }

    @Override
    public Reply getSendInfo(RecordParam param){
        try {
            /*String roleId = iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName());
            if(!roleId.equals(MWUtils.ROLE_TOP_ID)){
                return null;
            }*/
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(new Date());
            cd.add(Calendar.DATE,recordDate);
            Date date = cd.getTime();
            assetsDao.deleteRecord(date);
            List<AlertRecordTableDTO> alertRecordTableDTO = assetsDao.getSendInfo(param);
            HashSet<Integer> ids = new HashSet<>();
            if(CollectionUtils.isEmpty(alertRecordTableDTO)) return  null;
            for(AlertRecordTableDTO dto : alertRecordTableDTO){
                ids.add(dto.getId());
            }
            List<AlertRecordTableDTO> recordTableDTOS = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(ids)){
                List<Integer> idList = new ArrayList<>(ids);
                recordTableDTOS = assetsDao.getAlertRecordUserIds(idList);
            }

            for(AlertRecordTableDTO dto : alertRecordTableDTO){
                dto.setUserName(getUserName(dto.getId(),recordTableDTOS));
            }

            if(alertRecordTableDTO != null && alertRecordTableDTO.size() > 0){
                for (AlertRecordTableDTO s : alertRecordTableDTO){
                    if(s.getIsSuccess() == 0){
                        s.setResultState("成功");
                    }else{
                        s.setResultState("失败");
                    }
                }
            }
            alertRecordTableDTO = getFilterSelect(alertRecordTableDTO,param);
            PageList pageList = new PageList();
            List newList = pageList.getList(alertRecordTableDTO, param.getPageNumber(), param.getPageSize());
            PageInfo pageInfo = new PageInfo<>(alertRecordTableDTO);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(param.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("getSendInfo 错误：" + e);
            return Reply.fail("查询失败！");
        }
    }

    private String getUserName(Integer id,List<AlertRecordTableDTO> recordTableDTOS){
        StringBuffer sb = new StringBuffer();
        for(AlertRecordTableDTO dto : recordTableDTOS){
            if(id.equals(dto.getId())){
                sb.append(dto.getUserName()).append(",");
            }
        }
        return sb.toString();
    }

    private List<AlertRecordTableDTO> getFilterSelect(List<AlertRecordTableDTO> alertRecordTableDTO, RecordParam param) throws ParseException {
        if(alertRecordTableDTO != null && alertRecordTableDTO.size() > 0){
            if (StringUtils.isNotEmpty(param.getSeachAll())) {
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) -> m.getMethod().toUpperCase().contains(param.getSeachAll().toUpperCase()) ||
                                m.getText().toUpperCase().contains(param.getSeachAll().toUpperCase()) || m.getText().toUpperCase().contains(param.getSeachAll().toUpperCase()) ||
                                m.getUserName().toUpperCase().contains(param.getSeachAll().toUpperCase()) || m.getResultState().toUpperCase().contains(param.getSeachAll().toUpperCase()))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotEmpty(param.getMethod())) {
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) -> m.getMethod().toUpperCase().contains(param.getMethod().toUpperCase()))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotEmpty(param.getText())) {
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) -> m.getText().toUpperCase().contains(param.getText().toUpperCase()))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotEmpty(param.getUserName())) {
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) ->{
                            if(m.getUserName()!=null){
                                return m.getUserName().toUpperCase().contains(param.getUserName().toUpperCase());
                            }else {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }
            if(StringUtils.isNotEmpty(param.getResultState())) {
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) -> m.getResultState().toUpperCase().contains(param.getResultState().toUpperCase()))
                        .collect(Collectors.toList());
            }
            if(param.getStartDate() != null && param.getEndDate() != null) {

                Long startTime = param.getStartDate().getTime();
                Long endTime = param.getEndDate().getTime() + 86400000;
                alertRecordTableDTO = alertRecordTableDTO.stream()
                        .filter((AlertRecordTableDTO m) -> {
                            return startTime < m.getDate().getTime() && endTime > m.getDate().getTime();
                        })
                        .collect(Collectors.toList());
            }

        }
        return alertRecordTableDTO;
    }

    /*
     * 用户登录后获取告警相关信息
     */
    @Override
    public void login() {
        AlertParam alertParam = new AlertParam();
        alertParam.setPageSize(Integer.MAX_VALUE);
        getZipCurrAlertPageFromRedis(alertParam);
        getZipCurrHostProblemFromRedis();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BlockingQueue blockingQueue = new ArrayBlockingQueue<>(1);
        executorService = new ThreadPoolExecutor(0 ,1 ,60 ,TimeUnit.SECONDS ,blockingQueue);
        ObtainAlertLevel obtainAlertLevel = ObtainAlertLevelFactory.getObtainAlertLevel(alertLevel);
        obtainAlertLevel.getAlertLevel();
        String key = TAI_SHENG_KEY + "_";
        Set<String> keys = redisUtils.keys(key + "*");
        if(CollectionUtils.isNotEmpty(keys)){
            redisUtils.del(keys);
        }

    }

    @Override
    public void sendMessage(String message) {
        receiveAlertMessageTime = new Date();
    }

    @Override
    public void add(AddAndUpdateParam addAndUpdateParam) {
        if(StringUtils.isNotEmpty(addAndUpdateParam.getLinkTargetIp())){
            linkChangeTime = new Date();
        }
    }

    @Override
    public void modify(AddAndUpdateParam addAndUpdateParam) {
        linkChangeTime = new Date();
    }

    @Override
    public void delete(List<String> linkIds) {
        if(null != linkIds) {
            linkChangeTime = new Date();
        }
    }
    @Override
    public List<ZbxAlertDto> getCurrentAltertList(AlertParam dto) {
        return mwalertManager.getCurrentAltertList(dto);
    }

    @Override
    public Reply getAlertLevel(){
        List<String> result = new ArrayList<>(MWAlertLevelParam.alertLevelMap.values());
        return Reply.ok(result);
    }

    @Override
    public Reply reasonEditor(AlertReasonEditorParam param){
        try{
            Integer count = assetsDao.selectCountAlertSolutionTable(param);
            if(count == 0){
                assetsDao.insertAlertSolutionTable(param);
            }else {
                assetsDao.updateAlertSolutionTable(param);
            }
            return Reply.ok();
        }catch (Exception e){
            log.error("解决方案编辑失败：{}",e);
            return Reply.fail(e.getMessage());
        }

    }

    @Override
    public Reply closeEventId(List<CloseDto> param){
        try{
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            if(!iLoginCacheInfo.getRoleId(loginName).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            List<CloseDto> closeDtos = new ArrayList<>();
            if(param.get(0).getClose()){
                Map<Integer,List<CloseDto>> collect = new HashMap<>();
                collect = param.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0).collect(Collectors.groupingBy(CloseDto::getMonitorServerId));
                for (Integer serverId : collect.keySet()) {
                    List<String> triggerIds = new ArrayList<>();
                    for (CloseDto dto : collect.get(serverId)){
                        triggerIds.add(dto.getObjectId());
                    }
                    MWZabbixAPIResult result = mwtpServerAPI.getAcknowledgeidByObjectId(serverId, triggerIds);
                    if (result != null && result.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
                        JsonNode data = (JsonNode) result.getData();
                        data.forEach(event -> {
                            String acknowledged = event.get(AlertEnum.ACKNOWLEDGED.toString()).asText();
                            String objectId = event.get(AlertEnum.OBJECTID.toString()).asText();
                            if(acknowledged.equals("1")){
                                CloseDto dto = new CloseDto();
                                dto.setClose(true);
                                dto.setMonitorServerId(serverId);
                                dto.setObjectId(objectId);
                                closeDtos.add(dto);
                            }

                        });
                    }

                }
            }else {
                closeDtos.addAll(param);
            }
            if(CollectionUtils.isEmpty(closeDtos)) return Reply.fail("请确认后再关闭！");
            for (CloseDto dto: closeDtos) {
                MWZabbixAPIResult result = mwtpServerAPI.triggerClose(dto.getMonitorServerId(), dto);
                log.info("关闭触发器 result:" + result);
                dto.setOperatorId(userId);
            }
            if(param.get(0).getClose()){
                assetsDao.insertTriggercloseTable(closeDtos);
            }else if(!param.get(0).getClose()){
                assetsDao.deleteTriggercloseTable(closeDtos);
            }

            return Reply.ok();
        }catch (Exception e){
            log.error("解决方案编辑失败：{}",e);
            return Reply.fail(e.getMessage());
        }

    }

    @Override
    public List<String> getAssetsStatusByHisAlert(AssetsStatusQueryParam alertParam){
        List<String> result = new ArrayList<>();
        result = assetsDao.getRecordHostIds(alertParam.getHostids(),alertParam.getStartTime(),alertParam.getEndTime());
        return result;
    }

    @Override
    public Reply getTiegger(){
        try{
            List<CloseDto> closeDtos = assetsDao.getTriggercloseTableTriggerIds();
            if(CollectionUtils.isEmpty(closeDtos)) return null;
            List<Map> monitorMapList = assetsDao.getMonitorServerName();
            Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
            Map<Integer,List<CloseDto>> collect = new HashMap<>();
            collect = closeDtos.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0).collect(Collectors.groupingBy(CloseDto::getMonitorServerId));
            for (Integer serverId : collect.keySet()) {
                List<String> triggerIds = new ArrayList<>();
                for (CloseDto dto : collect.get(serverId)){
                    triggerIds.add(dto.getObjectId());
                }
                MWZabbixAPIResult result = mwtpServerAPI.getTriggeInfo(serverId, triggerIds);
                if (result != null && result.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
                    JsonNode data = (JsonNode) result.getData();
                    data.forEach(event -> {
                        String triggerid = event.get(AlertEnum.TRIGGERID.toString()).asText();
                        String description = event.get(AlertEnum.DESCRIPTIONEN.toString()).asText();
                        for (CloseDto dto : closeDtos){
                            if(dto.getObjectId().equals(triggerid)){
                                dto.setDescription(description);
                                dto.setMonitorServerName(monitorMap.get(dto.getMonitorServerId()));
                            }
                        }
                    });
                }
            }
            return Reply.ok(closeDtos);
        }catch (Exception e){
            log.error("获取关闭触发器信息失败：{}",e);
            return Reply.fail(e.getMessage());
        }

    }

    @Override
    public Reply ignoreAlert(List<IgnoreAlertDto> params) {
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        params = params.stream().map(m ->{
               m.setId(UUIDUtils.getUUID());
               m.setOperatorId(userId);
            return m;
        }).collect(Collectors.toList());
        assetsDao.insertIgnoreTable(params);
        return Reply.ok();
    }

    @Override
    public Reply getignoreAlert(IgnoreAlertDto param) {
        List<Map> monitorMapList = assetsDao.getMonitorServerName();
        Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
        List<IgnoreAlertDto> ignoreAlert = assetsDao.selectIgnoreTable();
        PageList pageList = new PageList();
        if(CollectionUtils.isEmpty(ignoreAlert)) ignoreAlert = new ArrayList<>();
        for(IgnoreAlertDto dto : ignoreAlert){
            dto.setMonitorServerName(monitorMap.get(dto.getMonitorServerId()));
        }
        List newList = pageList.getList(ignoreAlert, param.getPageNumber(), param.getPageSize());
        PageInfo pageInfo = new PageInfo<>(ignoreAlert);
        pageInfo.setPages(pageList.getPages());
        pageInfo.setPageNum(param.getPageNumber());
        pageInfo.setEndRow(pageList.getEndRow());
        pageInfo.setStartRow(pageList.getStartRow());
        pageInfo.setList(newList);
        return Reply.ok(pageInfo);
    }

    @Override
    public void triggerExport(HttpServletResponse response){
        ExcelWriter excelWriter = null;
        try{
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
            if(isVir){
                MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
                mwCommonAssetsDto.setUserId(userId);
                mwCommonAssetsDto.setIsQueryAssetsState(false);
                mwCommonAssetsDto.setAlertQuery(false);
                mwCommonAssetsDto.setMonitorFlag(true);
                mwTangibleassetsDTOS = mwAssetsManager.getAllAssetsByUserId(mwCommonAssetsDto);
            }else{
                QueryTangAssetsParam qparam = new QueryTangAssetsParam();
                qparam.setUserId(userId);
                qparam.setIsQueryAssetsState(false);
                if(alertLevel.equals(AlertEnum.HUAXING.toString())){
                    qparam.setAlertQuery(true);
                }
                qparam.setMonitorFlag(true);
                mwTangibleassetsDTOS = mwAssetsManager.getAssetsTable(qparam);
            }
            Map<Integer, List<String>> map = mwAssetsManager.getAssetsByServerId(mwTangibleassetsDTOS);

            List<Map> monitorMapList = assetsDao.getMonitorServerName();
            Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
            List<TriggerInfoParam> params = new ArrayList<>();
            for (Integer serverId : map.keySet()) {
                log.info("current alert serverId:" + serverId);
                List<String> hostIds = map.get(serverId);
                log.info("current alert hostIds:" + hostIds.size());
                log.info("current alert hostIds:" + hostIds);
                log.info("hostid分组:" + hostidGroupCount);
                if (hostIds.size() > 0) {
                    List<List<String>> partition = Lists.partition(hostIds, hostidGroupCount);
                    for (List<String> partitionHostIds : partition) {
                        HashMap<String, Object> param = new HashMap();
                        HashMap<String, Object> filter = new HashMap();
                        param.put("hostids", partitionHostIds);
                        param.put("selectHosts", "extend");
                        MWZabbixAPIResult result = mwtpServerAPI.triggerGet(serverId,param,filter);
                        if (result != null && result.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
                            JsonNode triggerInfos = (JsonNode) result.getData();
                            triggerInfos.forEach(trigger -> {
                                TriggerInfoParam temp = JSON.parseObject(trigger.toString(),TriggerInfoParam.class);
                                JsonNode hosts = trigger.get("hosts");
                                String levelInfo = trigger.get("priority").asText();
                                temp.setHostid(hosts.get(0).get("hostid").asText());
                                temp.setLevel(MWAlertLevelParam.alertLevelMap.get(levelInfo));
                                temp.setMonitorServerId(serverId);
                                params.add(temp);
                            });

                        }
                        //List<TriggerInfoParam> params = JSONArray.parseArray(date.getData().toString(),TriggerInfoParam.class);
                        log.info("hostid分组:" + hostidGroupCount);
                    }
                }
            }
            List<MwTangibleassetsTable> finalMwTangibleassetsDTOS = mwTangibleassetsDTOS;
            params.forEach(p -> {
                finalMwTangibleassetsDTOS.stream().filter(dto -> p.getHostid().equals(dto.getAssetsId())).findFirst().ifPresent(dto -> {
                    p.setAssetsName(dto.getAssetsName());
                    p.setIp(dto.getInBandIp());
                    p.setMonitorServerName(monitorMap.get(p.getMonitorServerId()));
                });
            });

            Set<String> fields = new HashSet<>();
            //fields.add(AlertEnum.EXCELALERTPARAM.toString());
            fields.add("assetsName");
            fields.add("ip");
            fields.add("description");
            fields.add("level");
            fields.add("expression");
            fields.add("monitorServerName");

            String fileName = "trigger"; //导出文件名
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 头的策略
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);//设置头居中
            // 内容的策略
            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            WriteFont contentWriteFont = new WriteFont();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            // 字体大小
            contentWriteFont.setFontHeightInPoints((short) 12);
            contentWriteCellStyle.setWriteFont(contentWriteFont);
            // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
            HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            //创建easyExcel写出对象
            excelWriter = EasyExcel.write(response.getOutputStream(), TriggerInfoParam.class).registerWriteHandler(horizontalCellStyleStrategy).build();

            //计算sheet分页
            WriteSheet sheet = EasyExcel.writerSheet(0, AlertEnum.SHEET.toString())
                    .includeColumnFiledNames(fields)
                    .build();
            excelWriter.write(params, sheet);


        }catch (Exception e){
            log.error("导出报错:{}",e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public Reply getAlertCount(AlertCountParam param){
        try {
            Map<String,Integer> data = new HashMap<>();
            AlertParam alertParam = new AlertParam();
            Integer dateType = param.getDateType();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(dateType != null && dateType == 1){//今天时间
                List<Date> today = ReportDateUtil.getToday();
                alertParam.setStartTime(format.format(today.get(0)));
                alertParam.setEndTime(format.format(today.get(1)));
            }
            if(dateType != null && dateType == 0){//自定义时间
                alertParam.setStartTime(param.getStartTime());
                alertParam.setEndTime(param.getEndTime());
            }
            alertParam.setPageSize(Integer.MAX_VALUE);
            List<String> severityList = new ArrayList<>();
            severityList.add(AlertEnum.AVERAGE.toString());
            severityList.add(AlertEnum.ERROR.toString());
            severityList.add(AlertEnum.DISASTER.toString());
            alertParam.setSeverity(severityList);
            Reply reply = getCurrAlertPage(alertParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS && reply.getData() != null){
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<ZbxAlertDto> list = pageInfo.getList();
                if(CollectionUtils.isNotEmpty(list)){
                    for (ZbxAlertDto zbxAlertDto : list) {
                        String severity = zbxAlertDto.getSeverity();
                        if(data.containsKey(severity)){
                            Integer count = data.get(severity);
                            data.put(severity,count+1);
                        }else{
                            data.put(severity,1);
                        }
                    }
                }
                data.put(AlertEnum.ALL.toString(),list.size());
            }
            return Reply.ok(data);
        }catch (Exception e){
            log.error("获取告警级数量错误:{}",e);
            return Reply.fail("获取告警级数量错误");
        }
    }

    @Override
    public Reply getAlert(AlertCountParam param){
        try {
            AlertParam alertParam = new AlertParam();
            BeanUtils.copyProperties(param,alertParam);
            Reply reply = getCurrAlertPage(alertParam);
            return Reply.ok(reply);
        }catch (Exception e){
            log.error("获取告警级错误:{}",e);
            return Reply.fail("获取告警级错误");
        }

    }

    @Override
    public Reply getEventFlowByEventId(Integer monitorServerId, String eventid){
        try {
            List<AlertConfirmUserParam> editorParams = new ArrayList<>();
            ZbxAlertDto result = new ZbxAlertDto();
            AlertParam alertParam = new AlertParam();
            alertParam.setPageNumber(1);
            alertParam.setPageSize(10);
            alertParam.setMonitorServerId(monitorServerId);
            alertParam.setEventid(eventid);
            Reply reply = getCurrAlertPage(alertParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS && reply.getData() != null){
                PageInfo pageInfo = (PageInfo) reply.getData();
                List<ZbxAlertDto> list = pageInfo.getList();
                if(CollectionUtils.isNotEmpty(list)){
                    result = list.get(0);
                    AlertConfirmUserParam temp = new AlertConfirmUserParam();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    temp.setConfirmDate(sdf.parse(result.getClock()));
                    temp.setType("alertDate");
                    editorParams.add(temp);
                }
            }
            List<AlertConfirmUserParam> confirmUserParamList = assetsDao.selectConfirmByEventId(monitorServerId,eventid);
            if(CollectionUtils.isNotEmpty(confirmUserParamList)) {

                List<Integer> userIds = confirmUserParamList.stream()
                        .filter(m -> m.getUserId() != null)
                        .map(AlertConfirmUserParam::getUserId).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(userIds)){
                    List<AlerUserOrgParam> orgParams = assetsDao.selectOrgByUserIds(userIds);
                    confirmUserParamList = confirmUserParamList
                            .stream()
                            .filter(m -> m.getUserId() != null)
                            .map(m -> {
                                orgParams.forEach(org ->{
                                    if(org.getUserId().equals(m.getUserId())){
                                        m.setOrgName(org.getOrgName());
                                    }
                                });
                                return m;
                            }).collect(Collectors.toList());

                }
                editorParams.addAll(confirmUserParamList);
            }
            result.setEditorParams(editorParams);
            return Reply.ok(result);
        }catch (Exception e){
            log.error("查询事件闭环错误:{}",e);
            return Reply.fail("查询事件闭环错误");
        }
    }

    @Override
    public Reply getAlertMessage(AlertCountParam param){
        try{
            String startTime = param.getStartTime();
            String endTime = param.getEndTime();
            HashMap map = new HashMap();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            switch (param.getDateType()){
                case 1:
                    //本日
                    List<Date> today = ReportDateUtil.getToday();
                    startTime = sdf.format(today.get(0));
                    endTime = sdf.format(today.get(1));
                    break;
                case 2:
                    //本周
                    List<Date> week = ReportDateUtil.getWeek();
                    startTime = sdf.format(week.get(0));
                    endTime = sdf.format(week.get(1));
                    break;
                case 3:
                    //本月
                    List<Date> month = ReportDateUtil.getMonth();
                    startTime = sdf.format(month.get(0));
                    endTime = sdf.format(month.get(1));
                    break;
                case 4:
                    //今年
                    List<Date> year = ReportDateUtil.getYear();
                    startTime = sdf.format(year.get(0));
                    endTime = sdf.format(year.get(1));
                    break;
            }

            Integer successNum = assetsDao.selectCountRecordByDate(0,startTime,endTime);
            Integer failNum = assetsDao.selectCountRecordByDate(1,startTime,endTime);
            map.put("successNum",successNum);
            map.put("failNum",failNum);
            map.put("total",successNum + failNum);
            return Reply.ok(map);
        }catch (Exception e){
            log.error("查询消息总数失败:{}",e);
            return Reply.fail("查询消息总数失败");
        }
    }

    @Override
    public Reply getIsAlert(List<QueryAlertStateParam> params){
        AlertParam alertParam = new AlertParam();
        List<String> severity = new ArrayList<>();
        severity.add(AlertLevelEnum.WARNING.getName());
        severity.add(AlertLevelEnum.NORMAL.getName());
        severity.add(AlertLevelEnum.ERROR.getName());
        severity.add(AlertLevelEnum.DISASTER.getName());
        alertParam.setSeverity(severity);
        alertParam.setPageNumber(1);
        alertParam.setPageSize(Integer.MAX_VALUE);
        Reply reply = getCurrAlertPage(alertParam);
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<ZbxAlertDto> alertDtos = pageInfo.getList();
            params.forEach(f -> {
                Boolean result = alertDtos.stream()
                        .anyMatch(a -> a.getHostid().equals(f.getHostid()) && a.getMonitorServerId()== f.getMonitorServerId());
                f.setIsAlert(result);
            });
        }
        return Reply.ok(params);
    }

}
