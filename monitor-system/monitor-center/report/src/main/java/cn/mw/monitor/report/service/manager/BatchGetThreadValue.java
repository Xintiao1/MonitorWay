package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.MwReportMemoryDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.service.GetDataByThread;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class BatchGetThreadValue {
    private static final int HOST_GROUP_SIZE = 5;
    public static final int NUMERIC_FLOAT = 0;
    public static final int NUMERIC_UNSIGN = 3;

    private static final String SEQ = "-";
    private MWTPServerAPI mwtpServerAPI;
    private int hostGroupSize = HOST_GROUP_SIZE;

    public int getHostGroupSize() {
        return hostGroupSize;
    }

    public void setHostGroupSize(int hostGroupSize) {
        this.hostGroupSize = hostGroupSize;
    }

    public BatchGetThreadValue(MWTPServerAPI mwtpServerAPI) {
        this.mwtpServerAPI = mwtpServerAPI;
    }

    public static String genRunTimeItemValueKey(RunTimeItemValue runTimeItemValue){
        return runTimeItemValue.getServerId() + SEQ + runTimeItemValue.getHostId();
    }

    private String genItemKey(Integer serverId, String itemId){
        return serverId + SEQ + itemId;
    }

    public static  String genHostIdKey(Integer serverId, String hostId){
        return serverId + SEQ + hostId;
    }

    public Map<String, RunTimeItemValue> getThreadValue(Map<String, RunTimeItemValue> runTimeItemValueMap, List<String> name, Long startTime, Long endTime) {

        //根据serverId进行分组
        Map<Integer, List<RunTimeItemValue>> rtiGroupMap = new HashMap<>();
        for(RunTimeItemValue runTimeItemValue:runTimeItemValueMap.values()){
            List<RunTimeItemValue> list = rtiGroupMap.get(runTimeItemValue.getServerId());
            if(null == list){
                list = new ArrayList<>();
                rtiGroupMap.put(runTimeItemValue.getServerId(), list);
            }
            list.add(runTimeItemValue);
        }
        List<MwReportMemoryDto> reportMemoryDtos = new ArrayList<>();
        CopyOnWriteArrayList<Future<Integer>> futureList = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        Iterator<Map.Entry<Integer, List<RunTimeItemValue>>> it = rtiGroupMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, List<RunTimeItemValue>> entry = it.next();
            Integer monitorServerId = entry.getKey();
            List<RunTimeItemValue> list = entry.getValue();

            //按每个zabbix server分组来处理数据
            GetDataByThread<Integer> getDataByThread = new GetDataByThread<Integer>() {
                @Override
                public Integer call() throws Exception {
                    List<String> hostIds = new ArrayList<>();
                    for(RunTimeItemValue runTimeItemValue: list){
                        hostIds.add(runTimeItemValue.getHostId());
                    }
                    MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(monitorServerId, name, hostIds, false);

                    //根据hostid对itemid分组
                    Map<String, String> itemHostMap = new HashMap<>();

                    Map<String, List<ZabbixItemData>> hostIdMap = new HashMap<>();
                    if (result0.getCode() == 0) {
                        JsonNode jsonNode = (JsonNode) result0.getData();
                        if (jsonNode.size() > 0) {
                            for (int i = 0; i < jsonNode.size(); i++) {
                                if (null == jsonNode.get(i)
                                    || null == jsonNode.get(i).get("hostid")
                                    || null == jsonNode.get(i).get("itemid")) {
                                    continue;
                                }
                                String hostid =jsonNode.get(i).get("hostid").asText();
                                List<ZabbixItemData> itemIdList = hostIdMap.get(hostid);
                                if(null == itemIdList){
                                    itemIdList = new ArrayList<>();
                                    hostIdMap.put(hostid, itemIdList);
                                }

                                String itemId = jsonNode.get(i).get("itemid").asText();
                                String itemName = jsonNode.get(i).get("name").asText();
                                ZabbixItemData itemData = ZabbixItemData.builder().itemId(itemId).itemName(itemName).build();
                                itemIdList.add(itemData);
                                double lastValue = jsonNode.get(i).get("lastvalue").asDouble();
                                String units =jsonNode.get(i).get("units").asText();
                                RunTimeItemValue runTimeItemValue = runTimeItemValueMap.get(monitorServerId + SEQ + hostid);
                                if(runTimeItemValue != null && (itemName.contains(RunTimeReportManager.MEMORY_TOTAL) || (itemName.contains(RunTimeReportManager.MEMORY_USED)) || (itemName.contains(RunTimeReportManager.MEMORY_FREE)))){
                                    MwReportMemoryDto mwReportMemoryDto = new MwReportMemoryDto();
                                    mwReportMemoryDto.extractFrom(lastValue,itemName,units,monitorServerId,hostid);
                                    reportMemoryDtos.add(mwReportMemoryDto);
                                }
                                itemHostMap.put(itemId, hostid);
                            }
                        }
                    }
                    getMemoryInfo(reportMemoryDtos,runTimeItemValueMap);
                    log.info("CPU报表查询主机数据"+hostIdMap);
                    //按hostid分组进行批量查询
                    Iterator<Map.Entry<String, List<ZabbixItemData>>> hostIdMapIt = hostIdMap.entrySet().iterator();
                    int count = 0;
                    List<HostGroup> hostGroups = new ArrayList<>();
                    HostGroup hostGroup = new HostGroup();
                    int group = hostGroupSize + 1;
                    while (hostIdMapIt.hasNext()) {
                        Map.Entry<String, List<ZabbixItemData>> entry = hostIdMapIt.next();
                        count++;
                        if(0 == (count % group) ){
                            hostGroups.add(hostGroup);
                            hostGroup = new HostGroup();
                        }
                        hostGroup.getHostIds().add(entry.getKey());
                        hostGroup.getItemDatas().addAll(entry.getValue());
                    }

                    hostGroups.add(hostGroup);

                    List<RunTimeItemValueHandler> handlers = new ArrayList<>();
                    List<Integer> types = new ArrayList<>();
                    types.add(NUMERIC_FLOAT);
                    types.add(NUMERIC_UNSIGN);
                    if(name.contains(RunTimeReportManager.CPU_UTILIZATION)){
                        handlers.add(new CpuHandler(monitorServerId, mwtpServerAPI, types, startTime, endTime));
                    }

                    if(name.contains(RunTimeReportManager.MEMORY_UTILIZATION)
                        || name.contains(RunTimeReportManager.MEMORY_TOTAL)
                        ||name.contains(RunTimeReportManager.MEMORY_USED)){
                        handlers.add(new MemHandler(mwtpServerAPI, monitorServerId, types, startTime, endTime));
                    }

                    for(RunTimeItemValueHandler handler : handlers){
                        log.info("CPU报表查询主机数据2"+hostGroups);
                        handler.handle(hostGroups, runTimeItemValueMap, itemHostMap);
                    }

                    for(RunTimeItemValue runTimeItemValue:runTimeItemValueMap.values()){
                        List<ZabbixItemData> itemDatas = hostIdMap.get(runTimeItemValue.getHostId());
                        if(null != itemDatas) {
                            List<String> itemids = itemDatas.stream().map(data->data.getItemId()).collect(Collectors.toList());
                            runTimeItemValue.setItemIds(new HashSet<>(itemids));
                        }
                    }

                    return 0;
                }
            };

            Future<Integer> submit = executorService.submit(getDataByThread);
            futureList.add(submit);

            for (Future<Integer> itemValueFuture : futureList) {
                try {
                    Integer result = itemValueFuture.get(3, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("getThreadValue",e);
                    itemValueFuture.cancel(true);
                }
            }
        }
        //关闭线程池
        executorService.shutdown();
        return runTimeItemValueMap;
    }

    private void getMemoryInfo(List<MwReportMemoryDto> reportMemoryDtos,Map<String, RunTimeItemValue> runTimeItemValueMap){
        if(CollectionUtils.isEmpty(reportMemoryDtos)){return;}
        //数据分组
        Map<String, List<MwReportMemoryDto>> listMap = reportMemoryDtos.stream().collect(Collectors.groupingBy(item -> item.getServerId() + SEQ + item.getHostId()));
        for (String key : listMap.keySet()) {
            RunTimeItemValue runTimeItemValue = runTimeItemValueMap.get(key);
            List<MwReportMemoryDto> memoryDtos = listMap.get(key);
            if(CollectionUtils.isEmpty(memoryDtos)){continue;}
            MwReportMemoryDto mwReportMemoryDto = memoryDtos.stream().max(Comparator.comparingDouble(MwReportMemoryDto::getMemoryUsed)).get();//获取最大已使用的数据
            Map<String, List<MwReportMemoryDto>> collect = memoryDtos.stream().collect(Collectors.groupingBy(item -> item.getItemName()));
            List<MwReportMemoryDto> dtos = collect.get(mwReportMemoryDto.getItemName().split("_")[0]+"_TOTAL");
            if(CollectionUtils.isEmpty(dtos)){continue;}
            MwReportMemoryDto totalMaxDto = dtos.stream().max(Comparator.comparingDouble(MwReportMemoryDto::getMemoryTotal)).get();
            //设置值
            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(mwReportMemoryDto.getMemoryUsed()), mwReportMemoryDto.getUnits());
            runTimeItemValue.setDiskUser(convertedValue.get("value")+convertedValue.get("units"));
            Map<String, String> totalValue = UnitsUtil.getConvertedValue(new BigDecimal(totalMaxDto.getMemoryTotal()), totalMaxDto.getUnits());
            runTimeItemValue.setDiskTotal(totalValue.get("value")+totalValue.get("units"));
        }
    }
}
