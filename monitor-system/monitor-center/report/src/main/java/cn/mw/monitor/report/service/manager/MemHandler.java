package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.service.detailimpl.ReportUtil;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Slf4j
public class MemHandler implements RunTimeItemValueHandler {

    private List<Integer> valueType;
    private int monitorServerId;
    private Long startTime;
    private Long endTime;
    private List<String> name;
    private MWTPServerAPI mwtpServerAPI;
    private Pattern pattern = Pattern.compile("^(.*MEMORY_UTILIZATION)$");

    public MemHandler(MWTPServerAPI mwtpServerAPI, int monitorServerId,List<Integer> valueType, Long startTime, Long endTime){
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
            trendItemids.addAll( hostGroup1.getItemDatas().parallelStream()
                    .filter(data-> {
                        Matcher m = pattern.matcher(data.getItemName());
                        return m.find();
                    })
                    .map(data -> data.getItemId())
                    .collect(Collectors.toList()));
        }
        allValueDataMap = getZabbixTrendInfo(monitorServerId, new ArrayList<>(trendItemids), startTime, endTime);
        if(allValueDataMap == null){
            for(HostGroup hostGroup1 : hostGroups){
                List<String> itemIds = hostGroup1.getItemDatas().parallelStream()
                        .filter(data-> {
                            Matcher m = pattern.matcher(data.getItemName());
                            return m.find();
                        })
                        .map(data -> data.getItemId())
                        .collect(Collectors.toList());
                log.info("hostGroup hostIds size:{};filter itemids size:{}",hostGroup1.getHostIds().size(), itemIds.size());
                for (Integer type : valueType) {
                    MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByTimeAndType(monitorServerId
                            , hostGroup1.getHostIds(), itemIds, startTime, endTime, type);
                    log.info("查询内存的历史记录item"+itemIds+":::查出zabbix历史数据"+historyResult);
                    if (historyResult.getCode() != 0) {
                        log.warn("getThreadValue", historyResult.getMessage());
                    }
                    Map<String, List<HistoryValueDto>> valueDataMap = ReportUtil.getValueDataMap(historyResult);
                    if(allValueDataMap == null){
                        allValueDataMap = new HashMap<>();
                    }
                    allValueDataMap.putAll(valueDataMap);
                }

            }
        }
        Map<String,Integer> hostCountMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Iterator<Map.Entry<String,List<HistoryValueDto>>> allValueDataIt = allValueDataMap.entrySet().iterator();
        while (allValueDataIt.hasNext()){
            Map.Entry<String,List<HistoryValueDto>> entry = allValueDataIt.next();
            String itemId = entry.getKey();
            String hostId = itemHostMap.get(itemId);
            String key = BatchGetThreadValue.genHostIdKey(monitorServerId, hostId);
            RunTimeItemValue runTimeItemValue = runTimeItemValueMap.get(key);
            if(null == runTimeItemValue){
                continue;
            }
            List<HistoryValueDto> valueDtos = entry.getValue();
            TrendDto trendDtoNotUnit = ReportUtil.getTrendDtoNotUnit(valueDtos);
            String values = new BigDecimal(trendDtoNotUnit.getValueAvg()).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            for(HostGroup hostGroup1 : hostGroups){
                List<ZabbixItemData> itemDatas = hostGroup1.getItemDatas();
                for (ZabbixItemData itemData : itemDatas) {
                    String itemName = itemData.getItemName();
                    if(itemData.getItemId().equals(itemId)){
                        if(itemName.contains(RunTimeReportManager.MEMORY_UTILIZATION)){
                            double max = entry.getValue().stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble();
                            double min = entry.getValue().stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble();
                            if(hostCountMap.containsKey(runTimeItemValue.getHostId())){
                                Integer memoryCount = hostCountMap.get(runTimeItemValue.getHostId());
                                String diskUserRate = runTimeItemValue.getDiskUserRate();
                                String maxMemoryUtilizationRate = runTimeItemValue.getMaxMemoryUtilizationRate();
                                String minMemoryUtilizationRate = runTimeItemValue.getMinMemoryUtilizationRate();
                                if(StringUtils.isNotBlank(diskUserRate)){
                                    BigDecimal add = new BigDecimal(diskUserRate.replace("%", "")).add(new BigDecimal(values));
                                    BigDecimal divide = add.divide(new BigDecimal(memoryCount + 1),2, BigDecimal.ROUND_UP);
                                    runTimeItemValue.setDiskUserRate(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                                }
                                if(StringUtils.isNotBlank(maxMemoryUtilizationRate)){
                                    BigDecimal add = new BigDecimal(maxMemoryUtilizationRate.replace("%", "")).add(new BigDecimal(trendDtoNotUnit.getValueMax()));
                                    BigDecimal divide = add.divide(new BigDecimal(memoryCount + 1),2, BigDecimal.ROUND_UP);
                                    runTimeItemValue.setDiskUserRate(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                                }
                                if(StringUtils.isNotBlank(minMemoryUtilizationRate)){
                                    BigDecimal add = new BigDecimal(minMemoryUtilizationRate.replace("%", "")).add(new BigDecimal(trendDtoNotUnit.getValueMin()));
                                    BigDecimal divide = add.divide(new BigDecimal(memoryCount + 1),2, BigDecimal.ROUND_UP);
                                    runTimeItemValue.setDiskUserRate(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                                }
                            }else{
                                runTimeItemValue.setDiskUserRate(values+"%");
                                runTimeItemValue.setMaxMemoryUtilizationRate(new BigDecimal(trendDtoNotUnit.getValueMax()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                                runTimeItemValue.setMinMemoryUtilizationRate(new BigDecimal(trendDtoNotUnit.getValueMin()).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"%");
                            }
                            for (HistoryValueDto historyValueDto : valueDtos) {
                                Double value1 = historyValueDto.getValue();
                                Long clock = historyValueDto.getClock();
                                if(value1 != null && value1 == max){
                                    //设置最大值出现时间
                                    runTimeItemValue.setMemoryMaxValueTime(format.format(new Date(clock*1000)));
                                }
                                if(value1 != null && value1 == min){
                                    //设置最小值出现时间
                                    runTimeItemValue.setMemoryMinValueTime(format.format(new Date(clock*1000)));
                                }
                            }
                            checkMemoryInfo(runTimeItemValue);
                            if(hostCountMap.containsKey(runTimeItemValue.getHostId())){
                                Integer count = hostCountMap.get(runTimeItemValue.getHostId());
                                hostCountMap.put(runTimeItemValue.getHostId(),count+1);
                            }else{
                                hostCountMap.put(runTimeItemValue.getHostId(),1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验内存信息
     */
    private void checkMemoryInfo(RunTimeItemValue runTimeItemValue){
        String diskUserRate = runTimeItemValue.getDiskUserRate();
        String maxMemoryUtilizationRate = runTimeItemValue.getMaxMemoryUtilizationRate();
        String minMemoryUtilizationRate = runTimeItemValue.getMinMemoryUtilizationRate();
        if(StringUtils.isBlank(diskUserRate) || StringUtils.isBlank(maxMemoryUtilizationRate) || StringUtils.isBlank(minMemoryUtilizationRate)){return;}
        if(Double.parseDouble(diskUserRate.replace("%","")) > Double.parseDouble(maxMemoryUtilizationRate.replace("%",""))){
            String value = new BigDecimal((Double.parseDouble(maxMemoryUtilizationRate.replace("%","")) + Double.parseDouble(minMemoryUtilizationRate.replace("%",""))) / 2)
                    .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            runTimeItemValue.setDiskUserRate(value+"%");
        }
    }

    /**
     * 获取趋势数据信息
     */
    private Map<String, List<HistoryValueDto>> getZabbixTrendInfo(Integer monitorServerId, List<String> itemIds,Long startTime, Long endTime){
        log.info("CPU内存报表趋势查询::"+startTime+":::"+endTime);
        log.info("CPU内存报表趋势查询items::"+itemIds);
        //判断时间是否大于12小时
        if((endTime - startTime) < (3600*5)){return null;}
        //取趋势数据
        MWZabbixAPIResult result = mwtpServerAPI.trendBatchGet(monitorServerId, itemIds, startTime, endTime);
        log.info("CPU内存报表趋势查询2::"+result);
        if(result == null || result.isFail()){return null;}
        Map<String, List<HistoryValueDto>> trendValueDataMap = ReportUtil.getTrendValueDataMap(result);
        log.info("CPU内存报表趋势查询3::"+trendValueDataMap);
        return trendValueDataMap;
    }
}
