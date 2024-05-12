package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Data
public class CpuHandler implements RunTimeItemValueHandler{

    private MWTPServerAPI mwtpServerAPI;
    private List<Integer> valueType;
    private int monitorServerId;
    private Long startTime;
    private Long endTime;
    private Pattern pattern = Pattern.compile(".*CPU_UTILIZATION$");


    public CpuHandler(int monitorServerId, MWTPServerAPI mwtpServerAPI, List<Integer> valueType, Long startTime, Long endTime){
        this.mwtpServerAPI = mwtpServerAPI;
        this.monitorServerId = monitorServerId;
        this.valueType = valueType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public void handle(List<HostGroup> hostGroups, Map<String, RunTimeItemValue> runTimeItemValueMap, Map<String, String> itemHostMap) {
        Map<String,List<HistoryValueDto>> allValueDataMap = new HashMap<>();
        Set<String> trendItemids = new HashSet<>();
        for(HostGroup hostGroup1 : hostGroups){
            trendItemids.addAll(hostGroup1.getItemDatas().parallelStream()
                    .filter(data-> {
                        Matcher m = pattern.matcher(data.getItemName());
                        return m.find();
                    })
                    .map(data -> data.getItemId())
                    .collect(Collectors.toList()));
        }
        allValueDataMap = getZabbixTrendInfo(monitorServerId, new ArrayList<>(trendItemids), startTime, endTime,itemHostMap);
        if(allValueDataMap == null){
            for(HostGroup hostGroup1 : hostGroups){
                //过滤itemId,只获取cpu利用率数据
                List<String> itemIds = hostGroup1.getItemDatas().parallelStream()
                        .filter(data-> {
                            Matcher m = pattern.matcher(data.getItemName());
                            return m.find();
                        })
                        .map(data -> data.getItemId())
                        .collect(Collectors.toList());
                log.info("hostGroup hostIds size:{};filter itemids size:{}",hostGroup1.getHostIds().size(), itemIds.size());
                log.info("cpu报表查询数据CPU历史记录222"+itemIds);
                for (Integer type : valueType) {
                    MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId
                            , hostGroup1.getHostIds(), itemIds, startTime, endTime, type);
                    log.info("cpu报表查询数据CPU历史记录"+historyResult);
                    if (historyResult.getCode() != 0) {
                        log.warn("getThreadValue", historyResult.getMessage());
                    }
                    Map<String, List<HistoryValueDto>> valueDataMap = ReportUtil.getValueData(historyResult, itemHostMap);
                    log.info("cpu报表查询数据CPU历史记录数据转换"+historyResult);
                    if(allValueDataMap == null){
                        allValueDataMap = new HashMap<>();
                    }
                    allValueDataMap.putAll(valueDataMap);
                }
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Iterator<Map.Entry<String,List<HistoryValueDto>>> allValueDataIt = allValueDataMap.entrySet().iterator();
        while (allValueDataIt.hasNext()) {
            Map.Entry<String,List<HistoryValueDto>> entry = allValueDataIt.next();
            String key = BatchGetThreadValue.genHostIdKey(monitorServerId, entry.getKey());
            RunTimeItemValue runTimeItemValue = runTimeItemValueMap.get(key);
            if(null == runTimeItemValue){
                continue;
            }

            TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(entry.getValue());
            String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            runTimeItemValue.setAvgValue(values + "%");
            runTimeItemValue.setSortLastAvgValue(Double.valueOf(trendDtoNotUnit.getValueAvg()));
            if (CollectionUtils.isEmpty(entry.getValue())) {
                runTimeItemValue.setMaxValue(new BigDecimal(trendDtoNotUnit.getValueMax()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                runTimeItemValue.setMaxValueTime(format.format(new Date(startTime*1000)));
                runTimeItemValue.setMinValue(new BigDecimal(trendDtoNotUnit.getValueMin()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                runTimeItemValue.setMinValueTime(format.format(new Date(startTime*1000)));
            } else {
                String maxTime = "";
                String minTime = "";
                List<HistoryValueDto> valueDtos = entry.getValue();
                if(CollectionUtils.isNotEmpty(valueDtos)){
                    double max = valueDtos.stream().mapToDouble(HistoryValueDto::getMaxValue).max().getAsDouble();
                    double min = valueDtos.stream().mapToDouble(HistoryValueDto::getMinValue).min().getAsDouble();
                    for (HistoryValueDto historyValueDto : valueDtos) {
                        Double maxValue = historyValueDto.getMaxValue();
                        Double minValue = historyValueDto.getMinValue();
                        Long clock = historyValueDto.getClock();
                        if(maxValue != null && maxValue == max){
                            //设置最大值出现时间
                            maxTime = format.format(new Date(clock*1000));
                        }
                        if(minValue != null && minValue == min){
                            //设置最小值出现时间
                            minTime = format.format(new Date(clock*1000));
                        }
                    }
                }
                runTimeItemValue.setMaxValue(new BigDecimal(trendDtoNotUnit.getValueMax()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                runTimeItemValue.setMaxValueTime(maxTime);
                runTimeItemValue.setMinValue(new BigDecimal(trendDtoNotUnit.getValueMin()).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                runTimeItemValue.setMinValueTime(minTime);
            }
        }
        log.info("CPU报表数据设置"+runTimeItemValueMap);
        //取延迟信息
        Map<String,List<HistoryValueDto>> icmpReposnseMap = new HashMap<>();
        for(HostGroup hostGroup1 : hostGroups){
            List<String> icmpPingItems = new ArrayList<>();
            List<String> icmpReposnseItems = new ArrayList<>();
            List<ZabbixItemData> itemDatas = hostGroup1.getItemDatas();
            if(CollectionUtils.isEmpty(itemDatas))continue;
            for (ZabbixItemData itemData : itemDatas) {
                String itemName = itemData.getItemName();
                if("ICMP_RESPONSE_TIME".equals(itemName)){
                    icmpReposnseItems.add(itemData.getItemId());
                }
            }
            if(CollectionUtils.isNotEmpty(icmpReposnseItems)){
                Map<String,List<HistoryValueDto>> valueDataMap = getZabbixTrendInfo(monitorServerId, icmpReposnseItems, startTime, endTime,itemHostMap);
                if(valueDataMap == null){
                    valueDataMap = new HashMap<>();
                    MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId
                            , hostGroup1.getHostIds(), icmpReposnseItems, startTime, endTime, 0);
                    if (historyResult.getCode() != 0) {
                        log.warn("getThreadValue", historyResult.getMessage());
                    }
                     valueDataMap = ReportUtil.getValueData(historyResult, itemHostMap);
                    if (historyResult.getCode() != 0) {
                        log.warn("getThreadValue", historyResult.getMessage());
                    }
                }
                if(!valueDataMap.isEmpty()){
                    for (String key : valueDataMap.keySet()) {
                        List<HistoryValueDto> historyValueDtos = valueDataMap.get(key);
                        if(CollectionUtils.isEmpty(historyValueDtos)){continue;}
                        for (HistoryValueDto historyValueDto : historyValueDtos) {
                            Double value = historyValueDto.getValue();
                            BigDecimal decimal = new BigDecimal(value.toString());
                            Map<String, String> sumValues = UnitsUtil.getValueMap(decimal.toString(),"ms","s");
                            historyValueDto.setValue(Double.parseDouble(sumValues.get("value")));
                        }
                    }
                }
                icmpReposnseMap.putAll(valueDataMap);
            }
        }
        Iterator<Map.Entry<String,List<HistoryValueDto>>> icmpReposnseIterator = icmpReposnseMap.entrySet().iterator();
        while (icmpReposnseIterator.hasNext()) {
            Map.Entry<String,List<HistoryValueDto>> entry = icmpReposnseIterator.next();
            String key = BatchGetThreadValue.genHostIdKey(monitorServerId, entry.getKey());
            RunTimeItemValue runTimeItemValue = runTimeItemValueMap.get(key);
            if(null == runTimeItemValue){
                continue;
            }

            TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(entry.getValue());
            String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            runTimeItemValue.setIcmpResponseTime(values + "ms");
        }
    }


    /**
     * 获取趋势数据信息
     */
    private Map<String, List<HistoryValueDto>> getZabbixTrendInfo(Integer monitorServerId, List<String> itemIds,Long startTime, Long endTime,Map<String, String> itemHostMap){
        log.info("CPU报表趋势查询::"+startTime+":::"+endTime);
        log.info("CPU报表趋势查询items::"+itemIds);
        //判断时间是否大于12小时
        if((endTime - startTime) < (3600*5)){return null;}
        //取趋势数据
        MWZabbixAPIResult result = mwtpServerAPI.trendBatchGet(monitorServerId, itemIds, startTime, endTime);
        log.info("CPU报表趋势查询2::"+result);
        if(result == null || result.isFail()){return null;}
        Map<String, List<HistoryValueDto>> trendValueDataMap = ReportUtil.getTrendValueData(result,itemHostMap);
        log.info("CPU报表趋势查询3::"+trendValueDataMap);
        return trendValueDataMap;
    }
}
