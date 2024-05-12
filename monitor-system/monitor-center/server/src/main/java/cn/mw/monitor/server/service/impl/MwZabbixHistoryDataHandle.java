package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.service.server.api.dto.HistoryListDto;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDtoBySer;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwZabbixHistoryDataHandle
 * @Description 处理zabbix历史数据查询，根据日期自动判断是否取趋势接口数据
 * @Author gengjb
 * @Date 2023/1/9 11:10
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwZabbixHistoryDataHandle {


    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwServerManager mwServerManager;

    private final String MW_INTERFACE_IN_TRAFFIC = "MW_INTERFACE_IN_TRAFFIC";

    private final String MW_INTERFACE_OUT_TRAFFIC = "MW_INTERFACE_OUT_TRAFFIC";

    private final String CPU_UTILIZATION = "CPU_UTILIZATION";

    private final String MEMORY_UTILIZATION = "MEMORY_UTILIZATION";

    /**
     * zabbix历史数据查询处理
     * @param serverHistoryDto
     */
    public List handleHistory(ServerHistoryDto serverHistoryDto){
        try {
            List list = new ArrayList();
            List realList = new ArrayList();
            if(serverHistoryDto.getIsTrend() != null && !serverHistoryDto.getIsTrend()){
                realList.add(list);
                return realList;
            }
            //目前先实现线路模块
            List<String> name = serverHistoryDto.getName();
            if(CollectionUtils.isEmpty(name)){
                realList.add(list);
                return realList;
            }
            List<Long> dates = getDateType(serverHistoryDto.getDateType(), serverHistoryDto.getDateStart(), serverHistoryDto.getDateEnd());
            if(CollectionUtils.isEmpty(dates)){
                realList.add(list);
                return realList;
            }
            //计算时间天数，是否需要查询趋势数据
            Long startTime = dates.get(0);
            Long endTime = dates.get(1);
            long day = (endTime - startTime) / (86400);
            log.info("查询zabbix历史数据趋势"+day);
            //获取到资产监控项的itemID
            int monitorServerId = serverHistoryDto.getMonitorServerId();//zabbix服务器ID
            String assetsId = serverHistoryDto.getAssetsId();//主机ID
            Map<String,String> hostMap = new HashMap<>();
            List<String> itemIds = getItemIds(monitorServerId, assetsId, name, hostMap,name);
            log.info("查询zabbix历史数据趋势获取itemId："+itemIds+"获取到的host与ITEM"+hostMap);
            if(CollectionUtils.isEmpty(itemIds)){
                realList.add(list);
                return realList;
            }
            //查询趋势数据
            MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.trendBatchGet(monitorServerId,itemIds, startTime, endTime);
            log.info("查询zabbix历史数据趋势获取趋势数据："+mwZabbixAPIResult);
            //趋势数据处理
            list = trendDataHandle(mwZabbixAPIResult, hostMap,name);
            realList.add(list);
            return realList;
        }catch (Throwable e){
            log.error("处理zabbix历史数据查询失败",e);
        }
        return null;
    }


    private List trendDataHandle(MWZabbixAPIResult mwZabbixAPIResult,Map<String,String> hostMap,List<String> itemName){
        if(mwZabbixAPIResult == null || mwZabbixAPIResult.isFail()){return null;}
        Map<String,List<MWItemHistoryDto>> map = new HashMap<>();
        Map<String,List<MWItemHistoryDto>> maxMap = new HashMap<>();
        Map<String,List<MWItemHistoryDto>> minMap = new HashMap<>();
        String unitAvg = "";
        String unitMax = "";
        String unitMin = "";
        Double avgMaxValue = new Double(0);
        Double maxValue = new Double(0);
        Double minValue = new Double(0);
        JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
        for (JsonNode node : jsonNode) {
            //取平均值
            MWItemHistoryDto historyDto = new MWItemHistoryDto();
            double value = node.get("value_avg").asDouble();
            String itemId = node.get("itemid").asText();
            long clock = node.get("clock").asLong();
            historyDto.setDoubleValue(value);
            historyDto.setClock(clock+"");
            historyDto.setItemid(itemId);
            historyDto.setLastValue((long) value);
            historyDto.setDateTime(new Date(clock*1000));
            //进行单位转换
            String units = hostMap.get(itemId).split(",")[1];//单位
            Map<String, String> convertedValue = new HashMap<>();
            if(itemName.get(0).contains(MW_INTERFACE_IN_TRAFFIC) || itemName.get(0).contains(MW_INTERFACE_OUT_TRAFFIC)){
                convertedValue = UnitsUtil.getValueMap(value + "", "Mbps", units);
            }else{
                convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(value), units);
            }
            String v = convertedValue.get("value");
            if(StringUtils.isNotBlank(v) && Double.parseDouble(v) > avgMaxValue){
                avgMaxValue = Double.parseDouble(v);
            }
            unitAvg = convertedValue.get("units");
            historyDto.setValue(convertedValue.get("value"));
            if(map != null && map.containsKey(itemId)){
                List<MWItemHistoryDto> dtos = map.get(itemId);
                dtos.add(historyDto);
                map.put(itemId,dtos);
            }else{
                List<MWItemHistoryDto> dtos = new ArrayList<>();
                dtos.add(historyDto);
                map.put(itemId,dtos);
            }

            //取最大值
            MWItemHistoryDto maxhistoryDto = new MWItemHistoryDto();
            double maxvalue = node.get("value_max").asDouble();
            maxhistoryDto.setDoubleValue(maxvalue);
            maxhistoryDto.setClock(clock+"");
            maxhistoryDto.setItemid(itemId);
            maxhistoryDto.setLastValue((long) maxvalue);
            maxhistoryDto.setDateTime(new Date(clock*1000));
            //进行单位转换
            Map<String, String> valueMapMax = new HashMap<>();
            if(itemName.get(0).contains(MW_INTERFACE_IN_TRAFFIC) || itemName.get(0).contains(MW_INTERFACE_OUT_TRAFFIC)){
                valueMapMax = UnitsUtil.getValueMap(maxvalue + "", "Mbps", units);
            }else{
                valueMapMax = UnitsUtil.getConvertedValue(new BigDecimal(maxvalue), units);
            }
            String vMax = valueMapMax.get("value");
            if(StringUtils.isNotBlank(vMax) && Double.parseDouble(vMax) > maxValue){
                maxValue = Double.parseDouble(vMax);
            }
            unitMax = valueMapMax.get("units");
            maxhistoryDto.setValue(valueMapMax.get("value"));
            if(maxMap != null && maxMap.containsKey(itemId)){
                List<MWItemHistoryDto> dtos = maxMap.get(itemId);
                dtos.add(maxhistoryDto);
                maxMap.put(itemId,dtos);
            }else{
                List<MWItemHistoryDto> dtos = new ArrayList<>();
                dtos.add(maxhistoryDto);
                maxMap.put(itemId,dtos);
            }

            //取最小值
            MWItemHistoryDto minhistoryDto = new MWItemHistoryDto();
            double minvalue = node.get("value_min").asDouble();
            minhistoryDto.setDoubleValue(minvalue);
            minhistoryDto.setClock(clock+"");
            minhistoryDto.setItemid(itemId);
            minhistoryDto.setLastValue((long) minvalue);
            minhistoryDto.setDateTime(new Date(clock*1000));
            //进行单位转换
            Map<String, String> valueMapMin = new HashMap<>();
            if(itemName.get(0).contains(MW_INTERFACE_IN_TRAFFIC) || itemName.get(0).contains(MW_INTERFACE_OUT_TRAFFIC)){
                valueMapMin = UnitsUtil.getValueMap(minvalue + "", "Mbps", units);
            }else{
                valueMapMin = UnitsUtil.getConvertedValue(new BigDecimal(minvalue), units);
            }
            String vMin = valueMapMin.get("value");
            if(StringUtils.isNotBlank(vMin) && Double.parseDouble(vMin) > minValue){
                minValue = Double.parseDouble(vMin);
            }
            unitMin = valueMapMin.get("units");
            minhistoryDto.setValue(valueMapMin.get("value"));
            if(minMap != null && minMap.containsKey(itemId)){
                List<MWItemHistoryDto> dtos = minMap.get(itemId);
                dtos.add(minhistoryDto);
                minMap.put(itemId,dtos);
            }else{
                List<MWItemHistoryDto> dtos = new ArrayList<>();
                dtos.add(minhistoryDto);
                minMap.put(itemId,dtos);
            }
        }
        List realList = new ArrayList();
        //如果是多CPU多内存，需要取平均值
        if(itemName.get(0).contains(CPU_UTILIZATION) || itemName.get(0).contains(MEMORY_UTILIZATION)){
            Map<String,Object> dataMap = new HashMap<>();
            List<MWItemHistoryDto> dtos = handlerManyItemName(map);
            List<MWItemHistoryDto> maxDtos = handlerManyItemName(maxMap);
            List<MWItemHistoryDto> minDtos = handlerManyItemName(minMap);
            //排序，按照时间排序
            if(CollectionUtils.isNotEmpty(dtos)){
                listSort(dtos);
            }
            if(CollectionUtils.isNotEmpty(maxDtos)){
                listSort(maxDtos);
            }
            if(CollectionUtils.isNotEmpty(minDtos)){
                listSort(minDtos);
            }
            dataMap.put("maxData", maxDtos);
            dataMap.put("minData", minDtos);
            dataMap.put("avgData", dtos);
            dataMap.put("maxUsring", String.valueOf(maxValue));
            dataMap.put("minUsring", String.valueOf(minValue));
            dataMap.put("avgUsring", String.valueOf(avgMaxValue));
            dataMap.put("unitByMax", unitMax);
            dataMap.put("unitByMin", unitMin);
            dataMap.put("healthUnit", "");
            dataMap.put("healthValue", "");
            dataMap.put("unitByAvg", unitAvg);
            dataMap.put("realData", dtos);
            dataMap.put("realUsring", String.valueOf(avgMaxValue));
            dataMap.put("unitByReal", unitAvg);
            List<String> values = hostMap.values().stream().collect(Collectors.toList());
            dataMap.put("titleName", mwServerManager.getChName(values.get(0).split(",")[2]));
            dataMap.put("lastUpdateTime",SeverityUtils.getDate(new Date()));
            dataMap.put("delay", "0");
            realList.add(dataMap);
            return realList;
        }
        for (Map.Entry<String, String> entry : hostMap.entrySet()) {
            Map<String,Object> dataMap = new HashMap<>();
            String key = entry.getKey();
            String name = entry.getValue().split(",")[2];
            List<MWItemHistoryDto> dtos = map.get(key);
            List<MWItemHistoryDto> maxDtos = maxMap.get(key);
            List<MWItemHistoryDto> minDtos = minMap.get(key);
            //排序，按照时间排序
            if(CollectionUtils.isNotEmpty(dtos)){
                listSort(dtos);
            }
            if(CollectionUtils.isNotEmpty(maxDtos)){
                listSort(maxDtos);
            }
            if(CollectionUtils.isNotEmpty(minDtos)){
                listSort(minDtos);
            }
            dataMap.put("maxData", maxDtos);
            dataMap.put("minData", minDtos);
            dataMap.put("avgData", dtos);
            dataMap.put("maxUsring", String.valueOf(maxValue));
            dataMap.put("minUsring", String.valueOf(minValue));
            dataMap.put("avgUsring", String.valueOf(avgMaxValue));
            dataMap.put("unitByMax", unitMax);
            dataMap.put("unitByMin", unitMin);
            dataMap.put("healthUnit", "");
            dataMap.put("healthValue", "");
            dataMap.put("unitByAvg", unitAvg);
            dataMap.put("realData", dtos);
            dataMap.put("realUsring", String.valueOf(avgMaxValue));
            dataMap.put("unitByReal", unitAvg);
            dataMap.put("titleName", mwServerManager.getChName(name));
            dataMap.put("lastUpdateTime",SeverityUtils.getDate(new Date()));
            dataMap.put("delay", "0");
            realList.add(dataMap);
        }
        return realList;
    }



    private List<MWItemHistoryDto> handlerManyItemName( Map<String,List<MWItemHistoryDto>> map){
        List<MWItemHistoryDto> historyDtos = new ArrayList<>();
        for (String key : map.keySet()) {
            List<MWItemHistoryDto> dtos = map.get(key);
            historyDtos.addAll(dtos);
        }
        List<MWItemHistoryDto> realDatas = new ArrayList<>();
        //按时间分组求平均值
        Map<String, List<MWItemHistoryDto>> listMap = historyDtos.stream().collect(Collectors.groupingBy(item -> item.getClock()));
        for (String clock : listMap.keySet()) {
            List<MWItemHistoryDto> dtos = listMap.get(clock);
            MWItemHistoryDto historyDto = dtos.get(0);
            double value = dtos.stream().map(MWItemHistoryDto::getValue).collect(Collectors.toList()).stream().mapToDouble(item -> Double.parseDouble(item)).reduce((a, b) -> a + b).getAsDouble();
            historyDto.setValue(new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).toString());
            historyDto.setDoubleValue(value);
            realDatas.add(historyDto);
        }
        return realDatas;
    }

    private void listSort(List<MWItemHistoryDto> dtos){
        Collections.sort(dtos, new Comparator<MWItemHistoryDto>() {
            @Override
            public int compare(MWItemHistoryDto o1, MWItemHistoryDto o2) {
                try {
                    if (o1.getDateTime() == null || o2.getDateTime() == null) {
                        return 1;
                    }
                    Date dt1 = o1.getDateTime();
                    Date dt2 = o2.getDateTime();
                    if (dt1.getTime() < dt2.getTime()) {
                        return -1;
                    } else if (dt1.getTime() > dt2.getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    private List<String> getItemIds(int monitorServerId, String assetsId, List<String> name, Map<String,String> hostMap,List<String> itemName){
        MWZabbixAPIResult result;
        if(itemName.get(0).contains("[") || itemName.get(0).contains("]")){
             result = mwtpServerAPI.itemGetbyNameList(monitorServerId, name, assetsId,true);
        }else{
            result = mwtpServerAPI.itemGetbyNameList(monitorServerId, name, assetsId,false);
        }
        if(result == null || result.isFail()){return null;}
        List<String> itemIds = new ArrayList<>();
        //数据集
        JsonNode jsonNode = (JsonNode) result.getData();
        for (JsonNode node : jsonNode) {
            itemIds.add(node.get("itemid").asText());
            hostMap.put(node.get("itemid").asText(),node.get("hostid").asText()+","+node.get("units").asText()+","+node.get("name").asText());
        }
        return itemIds;
    }

    /**
     * 处理时间
     * @param dateType
     */
    public List<Long> getDateType(Integer dateType,String startTimeStr,String endTimeStr) throws ParseException {
        List<Long> dates = new ArrayList<>();
        if(dateType == null){return dates;}
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long endTime = System.currentTimeMillis() / 1000;
        //开始时间，默认是一小时前
        long startTime = DateUtils.addHours(new Date(), -1).getTime() / 1000;
        switch (dateType) {//1：hour 2:day 3:week 4:month
            case 1:
                startTime = DateUtils.addHours( new Date(), -1).getTime() / 1000;
                break;
            case 2:
                startTime = DateUtils.addDays( new Date(), -1).getTime() / 1000;
                break;
            case 3:
                startTime = DateUtils.addWeeks(new Date(), -1).getTime() / 1000;
                break;
            case 4:
                startTime = DateUtils.addMonths(new Date(), -1).getTime() / 1000;
                break;
            case 5:
                startTime = format.parse(startTimeStr).getTime()/1000;
                endTime = format.parse(endTimeStr).getTime()/1000;
                break;
            default:
                break;
        }
        dates.add(startTime);
        dates.add(endTime);
        return dates;
    }

    /**
     * 判断是否从趋势接口获取数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param serverId 服务器ID
     * @param itemId
     */
    public List<MWItemHistoryDtoBySer> getAssetsUsability(Long startTime,Long endTime,Integer serverId,String itemId){
        List<MWItemHistoryDtoBySer> historyDtoToDay = new ArrayList<>();
        if(startTime == null || endTime == null || serverId == null || StringUtils.isBlank(itemId)){return historyDtoToDay;}
        //ji算时间是否大于三天
        long day = (endTime - startTime) / (86400);
        if(day <= 3){return historyDtoToDay;}
        //查询趋势数据
        List<String> itemIds = Arrays.asList(new String[]{itemId});
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.trendBatchGet(serverId,itemIds, startTime, endTime);
        log.info("资产可用性取趋势数据"+mwZabbixAPIResult);
        if(mwZabbixAPIResult == null || mwZabbixAPIResult.isFail()){return historyDtoToDay;}
        JsonNode jsonNode = (JsonNode) mwZabbixAPIResult.getData();
        for (JsonNode node : jsonNode){
            int value = node.get("value_avg").asInt();
            long clock = node.get("clock").asLong();
            MWItemHistoryDtoBySer dtoBySer = new MWItemHistoryDtoBySer();
            dtoBySer.setItemid(itemId);
            dtoBySer.setClock(clock+"");
            dtoBySer.setValue(value+"");
            dtoBySer.setLastValue((long) value);
            historyDtoToDay.add(dtoBySer);
        }
        return historyDtoToDay;
    }
}
