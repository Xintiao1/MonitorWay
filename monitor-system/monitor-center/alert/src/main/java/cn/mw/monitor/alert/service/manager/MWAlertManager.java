package cn.mw.monitor.alert.service.manager;

import cn.mw.monitor.alert.dto.*;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.service.alert.dto.*;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.MwServerCommons;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixAPIResultCode;
import cn.mwpaas.common.utils.CollectionUtils;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xhy
 * @date 2020/3/27 14:25
 */
@Component
@Slf4j(topic = "MWAlertController")
public class MWAlertManager {

    @Value("${alert.debug}")
    private boolean debug;

    @Value("${triggerid.num}")
    private Integer triggerIdGroupCount;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    public List<ZbxAlertDto> getCurrentAltertList(AlertParam dto) {
        log.info("dto{}", dto);
        log.info("getMonitorServerId{}", dto.getMonitorServerId());
        MWZabbixAPIResult alert = mwtpServerAPI.alertGetByCurrent(dto.getMonitorServerId(), dto);
        if(debug) {
            log.info("當前告警: " + alert);
        }
        List<ZbxAlertDto> relist = new ArrayList<>();
        if (alert != null && alert.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
            JsonNode triggerid_data = (JsonNode) alert.getData();
            if(debug){
                log.info("triggerid_data{}", triggerid_data);
            }

            List<String> triggeridList = new ArrayList<>();
            if (triggerid_data.size() > 0) {
                try{
                    triggerid_data.forEach(trigger -> {
                        JsonNode lastEvent = trigger.get(AlertEnum.LASTEVENT.toString());
                        if(lastEvent.size() > 0){
                            triggeridList.add(trigger.get(AlertEnum.LASTEVENT.toString()).get(AlertEnum.EVENTIDEN.toString()).asText());
                        }
                    });
                }catch (Exception e){
                    log.error("triggeridList Error:" + e.getMessage().toString());
                }

            }
            log.info("triggeridList{}", triggeridList.size());
            if (triggeridList.size() > 0) {
                log.info("查询开始标记");
                List<List<String>> partition = Lists.partition(triggeridList, triggerIdGroupCount);
                for(List<String> triggerIds : partition){
                    List<ZbxAlertDto> temp = new ArrayList<>();
                    log.info("分组数量:" + triggerIds.size());
                    MWZabbixAPIResult currentAlter = mwtpServerAPI.eventGettByTriggers2(dto.getMonitorServerId(), triggerIds);
                    log.info("查询结束标记");
                    temp = getData(currentAlter, dto.getMonitorServerId());
                    if(CollectionUtils.isNotEmpty(temp)){
                        relist.addAll(temp);
                    }
                }

            }
        }
        return relist;
    }

    /**
     * 封装数据
     *
     * @param result
     * @return
     */

    //CountDownLatch latch = new CountDownLatch(totalNumberOf);
    public List<ZbxAlertDto> getData(MWZabbixAPIResult result, Integer monitorServerId) {
        try{
            log.info("getDate start :" + new Date());
            List<ZbxAlertDto> relist = new ArrayList();
            if (result.getCode() == MWZabbixAPIResultCode.SUCCESS.code()) {
                if(debug){
                    log.info("result.getData():{}", result.getData());
                    log.info("result.getData():{}", result);
                }
                JsonNode event_data = (JsonNode) result.getData();
                if (null != event_data && event_data.size() > 0) {
                    //final  ExecutorService executor = new ThreadPoolExecutor();//Executors.newFixedThreadPool(2);
                    log.info("循环遍历 start :" + new Date());
                    List<String> rEventIds = new ArrayList<>();
                    List<String> hostIds = new ArrayList<>();
                    event_data.forEach(event -> {
                        String rEventId = event.get(AlertEnum.R_EVENTID.toString()).asText();
                        if (null != rEventId && !"0".equals(rEventId)) {
                            rEventIds.add(rEventId);
                        }
                        String hostid = event.get(AlertEnum.HOSTS.toString()).get(0).get(AlertEnum.HOSTID.toString().toLowerCase()).asText();
                        hostIds.add(hostid);
                    });
                    MWZabbixAPIResult eventGetResult = new MWZabbixAPIResult();
                    if(rEventIds.size() > 0){
                        log.info("monitorServerId:" + monitorServerId);
                        log.info("rEventIds:" + rEventIds);
                        eventGetResult = mwtpServerAPI.eventGetByEventids(monitorServerId, rEventIds);
                    }
                    MWZabbixAPIResult finalEventGetResult = eventGetResult;
                    log.info("hostIds:" + hostIds);
                    List<AssetsDto> assetsList = mwModelViewCommonService.getAssetsByIds(hostIds);
                    event_data.forEach(event -> {
                        ZbxAlertDto dto = null;
                        try {
                            dto = getRelist(event,monitorServerId,finalEventGetResult,assetsList);
                        } catch (Exception e) {
                            log.error("getRelist" ,e);
                        }
                        if(null != dto && dto.getMessage()!=null){
//                            log.info("当前告警message:"  + dto);
                            String msg = toJsonString(dto.getMessage());
                            msg = converUnicodeToChar(msg);
                            String[] strs = msg.split(",");
                            HashMap<String, String> map = new HashMap<>();
                            String regex = ":";
                            for (String s : strs) {
                                String s1 = s.substring(0, s.indexOf(regex) + 1).replaceAll(":", "");
                                String s2 = s.substring(s.indexOf(regex) + 1);
                                map.put(s1, s2);
                            }
                            dto.setHostName(map.get(AlertEnum.HOSTNAME.toString()));
                            dto.setProblem(map.get(AlertEnum.PROBLEMDETAILS.toString()));
                            dto.setState(map.get(AlertEnum.NOWSTATE.toString()));
                        }
                        relist.add(dto);
                    });
                    log.info("循环遍历 end :" + new Date());
                /*executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    log.error("线程错误：" + e.getMessage().toString());
                }*/
                }
            }
            log.info("getData end :" + new Date());
            return relist;
        }catch (Exception e){
            log.error("错误返回 :{}",e);
            return null;
        }

    }

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

    public ZbxAlertDto getRelist(JsonNode event, Integer monitorServerId, MWZabbixAPIResult eventGetResult, List<AssetsDto> assetsList) throws Exception{
        ZbxAlertDto newDto = new ZbxAlertDto();
        if (null != event.get(AlertEnum.HOSTS.toString()) && event.get(AlertEnum.HOSTS.toString()).size() > 0) {
            String hostid = event.get(AlertEnum.HOSTS.toString()).get(0).get(AlertEnum.HOSTID.toString().toLowerCase()).asText();
            String objectid = event.get(AlertEnum.OBJECTID.toString()).asText();
            AssetsDto assets = getAssetsDto(hostid,monitorServerId,assetsList);
            if (null != assets) {
                newDto.setIp(assets.getAssetsIp());
                newDto.setObjectName(assets.getAssetsName());
                newDto.setAlertType(assets.getAssetsType());
                newDto.setMonitorServerId(assets.getMonitorServerId());
                newDto.setAssetsId(assets.getId());
                newDto.setMonitorServerName(assets.getMonitorServerName());
                newDto.setUrl(assets.getUrl());
                newDto.setParam(assets.getParam());
            }
            newDto.setObjectid(objectid);
            newDto.setHostid(hostid);
            newDto.setEventid(event.get(AlertEnum.EVENTIDEN.toString()).asText());
            if(MWAlertLevelParam.alertLevelMap.containsKey(event.get(AlertEnum.SEVERITY.toString()).asText())){
                newDto.setSeverity(MWAlertLevelParam.alertLevelMap.get(event.get(AlertEnum.SEVERITY.toString()).asText()));
            }
            newDto.setObjectid(event.get(AlertEnum.OBJECTID.toString()).asText());
            newDto.setName(event.get(AlertEnum.NAME.toString()).asText());
            newDto.setAcknowledged(event.get(AlertEnum.ACKNOWLEDGED.toString()).asText().equals(AlertAssetsEnum.Zero.toString()) ? AlertAssetsEnum.unconfirmed.toString() : AlertAssetsEnum.confirmed.toString());//确认状态
            //恢复事件ID不为空则是恢复时间
            if (null != event.get(AlertEnum.R_EVENTID.toString()).asText() && !AlertAssetsEnum.Zero.toString().equals(event.get(AlertEnum.R_EVENTID.toString()).asText())) {
                String rEventId = event.get(AlertEnum.R_EVENTID.toString()).asText();

                if(debug){
                    log.info("告警事件id: " + event);
                    log.info("恢復事件ID：" + rEventId);
                }

                //MWZabbixAPIResult eventGetResult = mwtpServerAPI.eventGetByEventid(monitorServerId, rEventId);
                List<Long> ns = new ArrayList();
                JsonNode rEvents = (JsonNode) eventGetResult.getData();
                rEvents.forEach(r_event -> {
                    if(r_event.get(AlertEnum.EVENTIDEN.toString()).asText().equals(event.get(AlertEnum.R_EVENTID.toString()).asText())){
                        ns.add(Long.valueOf(r_event.get(AlertEnum.CLOCK.toString()).asText()));
                    }
                });
                /*rEvent.forEach(r_event -> {
                    ns.add(Long.valueOf(r_event.get("clock").asText()));
                });*/
                newDto.setR_eventid(event.get(AlertEnum.R_EVENTID.toString()).asText());
                Long rclock = Long.valueOf(event.get(AlertEnum.CLOCK.toString()).asText());

                if(ns.size() > 0 && ns != null){
                    Long clock = ns.get(0) - rclock;
                    newDto.setLongTime(SeverityUtils.getLastTime(clock));//持续时间
                    newDto.setRclock(SeverityUtils.getDate(ns.get(0)));
                }else{
                    newDto.setLongTime(AlertEnum.NEVER.toString());//持续时间
                    newDto.setRclock(AlertEnum.NEVER.toString());
                }
                newDto.setClock(SeverityUtils.getDate(rclock));
            } else {
                newDto.setClock(SeverityUtils.getDate(event.get(AlertEnum.CLOCK.toString()).asLong()));
                newDto.setRclock(AlertEnum.NOTRECOVERED.toString());
                newDto.setLongTime(SeverityUtils.CalculateTime(newDto.getClock()));//持续时间
            }

            if (null != event.get(AlertEnum.ALERTS.toString()) && event.get(AlertEnum.ALERTS.toString()).size() > 0) {
                TreeNode alerts = event.get(AlertEnum.ALERTS.toString());
                for(int i=0;i<alerts.size();i++){
                    String message = event.get(AlertEnum.ALERTS.toString()).get(i).get(AlertEnum.MESSAGE.toString()).asText();
                    if(message.contains(AlertEnum.ALERTTITLE.toString())){
                        newDto.setAlertid(event.get(AlertEnum.ALERTS.toString()).get(i).get(AlertEnum.ALERTID.toString()).asText());
                        newDto.setMessage(event.get(AlertEnum.ALERTS.toString()).get(i).get(AlertEnum.MESSAGE.toString()).asText());
                        newDto.setSubject(event.get(AlertEnum.ALERTS.toString()).get(i).get(AlertEnum.SUBJECT.toString()).asText());
                        break;
                    }
                }

            }
        }
        return newDto;
    }

    public AssetsDto getAssetsDto(String hostid, Integer monitorServerId, List<AssetsDto> assetsList ){
        if(!CollectionUtils.isNotEmpty(assetsList)) return null;
        for(AssetsDto dto : assetsList){
            if(hostid.equals(dto.getAssetsId()) && monitorServerId.equals(dto.getMonitorServerId())){
                return dto;
            }
        }
        return null;
    }

    public List<MWHistDto> getAlarmHistEvent(Integer monitorServerId, String objectid) {
        MWZabbixAPIResult eventHistAlert = mwtpServerAPI.getEventHistAlert(monitorServerId, objectid);
        JsonNode eventHist_data = (JsonNode) eventHistAlert.getData();
        List<MWHistDto> list = new ArrayList<>();
        if (eventHist_data.size() > 0) {
            eventHist_data.forEach(eventHist -> {
                MWHistDto mwHistDto = new MWHistDto();
                mwHistDto.setHistEventId(eventHist.get(AlertEnum.EVENTIDEN.toString()).asText());
                mwHistDto.setHistTime(SeverityUtils.getDate(eventHist.get(AlertEnum.CLOCK.toString()).asLong()));
                mwHistDto.setHistAcknowledged(eventHist.get(AlertEnum.ACKNOWLEDGED.toString()).asText().equals(AlertAssetsEnum.Zero.toString()) ? AlertEnum.UNCONFIRMED.toString() : AlertEnum.CONFIRMED.toString());
                Long rclock = Long.valueOf(eventHist.get(AlertEnum.CLOCK.toString()).asText());
                Long ns = Long.valueOf(eventHist.get(AlertEnum.NS.toString()).asText()) / 1000000;
                Long clock = rclock - ns;
                mwHistDto.setHistTime(SeverityUtils.getDate(clock));
                mwHistDto.setHistRTime(eventHist.get(AlertEnum.R_EVENTID.toString()).asText().equals(AlertAssetsEnum.Zero.toString()) ? AlertEnum.NOTRECOVERED.toString() : SeverityUtils.getDate(rclock));
                list.add(mwHistDto);
            });
        }
        return list;
    }

    /**
     * 得到历史告警
     *
     * @return
     */
    public List<ZbxAlertDto> getHistoryAlertList(AlertParam mwAlertDto) {
        //参数判断
        log.info("mwtpServerAPI eventGetByHistory start：" + new Date());
        log.info("mwtpServerAPI：" + mwAlertDto);
        MWZabbixAPIResult eventGetResult = mwtpServerAPI.eventGetByHistory(mwAlertDto.getMonitorServerId(), mwAlertDto);
        log.info("mwtpServerAPI eventGetByHistory end：" + new Date());
        List<ZbxAlertDto> relist = getData(eventGetResult, mwAlertDto.getMonitorServerId());
        log.info("mwtpServerAPI eventGetByHistory end：" + new Date());
        Collections.sort(relist);
        return relist;

    }


    /**
     * 根据ID得到告警确认（告警通知）
     *
     * @return
     */
    public HashMap getActionAlertByEventid(Integer monitorServerId, String eventid) {
        MWZabbixAPIResult eventGetResult = mwtpServerAPI.eventGetByEventid(monitorServerId, eventid);
        JsonNode alert_data = (JsonNode) eventGetResult.getData();
        log.info("告警详情页alert_data：" + alert_data);
        HashMap<String, String> alertmap = new HashMap<String, String>();
        if (alert_data.size() > 0) {
            alert_data.forEach(event -> {
                alertmap.put(AlertEnum.CLOCK.toString(), SeverityUtils.getDate(event.get(AlertEnum.CLOCK.toString()).asLong()));
                alertmap.put(AlertEnum.SUBJECT.toString(), event.get(AlertEnum.NAME.toString()).asText());
                alertmap.put(AlertEnum.RETRIES.toString(), event.get(AlertEnum.R_EVENTID.toString()).asText().equals(AlertAssetsEnum.Zero.toString()) ? AlertEnum.NOTRECOVERED.toString() : SeverityUtils.getDate(event.get(AlertEnum.CLOCK.toString()).asLong()));
                if (null != event.get(AlertEnum.HOSTS.toString()) && event.get(AlertEnum.HOSTS.toString()).size() > 0) {
                    String hostid = event.get(AlertEnum.HOSTS.toString()).get(0).get(AlertEnum.HOSTID.toString().toLowerCase()).asText();
                    AssetsDto assets = null;
                    try {
                        assets = mwModelViewCommonService.getAssetsById(hostid ,monitorServerId);
                    } catch (Exception e) {
                        log.error("getActionAlertByEventid {}",e);
                    }
                    if (null != assets) {
                        alertmap.put("ip", assets.getAssetsIp());
                        alertmap.put("assetsName", assets.getAssetsName());
                    }
                }
                if (null != event.get(AlertEnum.ALERTS.toString()) && event.get(AlertEnum.ALERTS.toString()).size() > 0) {
                    if(event.get(AlertEnum.ALERTS.toString()).get(0).get(AlertEnum.MESSAGE.toString()).asText().contains(AlertEnum.ALERTTITLE.toString())){
                        alertmap.put(AlertEnum.MESSAGE.toString(), event.get(AlertEnum.ALERTS.toString()).get(0).get(AlertEnum.MESSAGE.toString()).asText());
                    }
                }
                MWZabbixAPIResult triggerGetResult = mwtpServerAPI.triggerGetByTriggerid(monitorServerId, event.get(AlertEnum.OBJECTID.toString()).asText());
                JsonNode trigger_data = (JsonNode) triggerGetResult.getData();
                alertmap.put(AlertEnum.COMMENTS.toString(), trigger_data.get(0).get(AlertEnum.COMMENTS.toString()).asText());
            });
        }
        return alertmap;
    }


    public List<MWItemDto> getItemByTriggerId(Integer monitorServerId, String objectid) {
        List<MWItemDto> itemList = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.triggerGetItemid(monitorServerId, objectid);
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() == 1) {
                JsonNode functions = jsonNode.get(0).get(AlertEnum.FUNCTIONS.toString());
                HashSet set = new HashSet();
                if (functions.size() > 0) {
                    functions.forEach(function -> {
                        set.add(function.get(AlertEnum.ITEMID.toString()));
                    });
                }
                List<String> itemids = new ArrayList<>(set);
                MWZabbixAPIResult itemName = mwtpServerAPI.getItemName(monitorServerId, itemids);
                if (itemName.getCode() == 0) {
                    JsonNode itemNameData = (JsonNode) itemName.getData();
                    if (itemNameData.size() > 0) {
                        itemNameData.forEach(itemData -> {
                            String ename = mwServerCommons.getChName(itemData.get(AlertEnum.NAME.toString()).asText());
                            MWItemDto itemDto = MWItemDto.builder()
                                    .itemId(itemData.get(AlertEnum.ITEMID.toString()).asText())
                                    .name(ename)
                                    .valueType(itemData.get(AlertEnum.VALUE_TYPE.toString()).asInt())
                                    .units(itemData.get(AlertEnum.UNITS.toString()).asText())
                                    .build();
                            itemList.add(itemDto);
                        });
                    }
                }
            }
        }
        return itemList;

    }

    @Autowired
    MwServerCommons mwServerCommons;

    public MWAlertHistoryDto getHistoryByItemId(MWItemDto mwItemDto) {
        MWAlertHistoryDto mwAlertHistoryDto = new MWAlertHistoryDto();
        String clock = mwItemDto.getClock();
        Long startTime = 0L;
        Long endTime = 0L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(MWUtils.strToDateLong(clock));
        endTime = calendar.getTimeInMillis() / 1000L;
        if (mwItemDto.getDateType() != null) {
            switch (mwItemDto.getDateType()) {
                case 1:
                    calendar.add(Calendar.HOUR, -1);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 2:
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 3:
                    calendar.add(Calendar.DAY_OF_MONTH, -7);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 4:
                    calendar.add(Calendar.MONTH, -1);
                    startTime = calendar.getTimeInMillis() / 1000L;
                    break;
                case 5:
                    startTime = mwItemDto.getStartDate().getTime() / 1000L;
                    endTime = mwItemDto.getEndDate().getTime() / 1000L;
                    break;
                default:
                    break;
            }
        }
        List<MwHistoryDTO> list = new ArrayList<>();
        List<MwHistoryDTO> dataList = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.HistoryGetByTimeAndType(mwItemDto.getMonitorServerId(), mwItemDto.getItemId(), startTime, endTime, mwItemDto.getValueType());
        if (result.getCode() == 0) {
            JsonNode history = (JsonNode) result.getData();
            if (history.size() > 0) {
                history.forEach(data -> {
                    MwHistoryDTO dto = MwHistoryDTO.builder()
                            .oldValue(data.get(AlertEnum.VALUE.toString()).asDouble())
                            .date(SeverityUtils.getDate(data.get(AlertEnum.CLOCK.toString()).asLong()))
                            .build();
                    list.add(dto);
                });

                double maxValue = list.stream().mapToDouble(MwHistoryDTO::getOldValue).max().getAsDouble();
                Map<String, String> map = UnitsUtil.getValueAndUnits(String.valueOf(maxValue), mwItemDto.getUnits());
                String units = map.get(AlertEnum.UNITS.toString());
                list.forEach(historyData -> {
                    String value = historyData.getOldValue().toString();
                    if (null != units && StringUtils.isNotEmpty(units)) {
                        value = UnitsUtil.getValueMap(value, units, mwItemDto.getUnits()).get(AlertEnum.VALUE.toString());
                    }
                    MwHistoryDTO dto = MwHistoryDTO.builder()
                            .value(value)
                            .date(historyData.getDate())
                            .build();
                    dataList.add(dto);

                });
                mwAlertHistoryDto.setDataList(dataList);
                mwAlertHistoryDto.setLastUpdateTime(MWUtils.strToDateLong(MWUtils.getDate(endTime)));
                String title = mwServerCommons.getChName(mwItemDto.getName());
                mwAlertHistoryDto.setTitle(title);
                MWZabbixAPIResult unitResult = mwtpServerAPI.getItemName(mwItemDto.getMonitorServerId(), Arrays.asList(mwItemDto.getItemId()));
                if (unitResult.getCode() == 0) {
                    JsonNode unit = (JsonNode) unitResult.getData();
                    if (unit.size() > 0) {
                        unit.forEach(data -> {
                            if(data.get(AlertEnum.UNITS.toString()) != null){
                                mwAlertHistoryDto.setUnit(data.get(AlertEnum.UNITS.toString()).asText());
                            }
                        });
                    }
                }
            }
        }
        return mwAlertHistoryDto;

    }


    public Integer getAlertTodayHistory(Integer serverId, List<String> hostIds, String timeTill) {
        MWZabbixAPIResult result = mwtpServerAPI.problemget(serverId, hostIds, timeTill);
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            int count = jsonNode.asInt();
            return count;
        }
        return null;
    }


}
