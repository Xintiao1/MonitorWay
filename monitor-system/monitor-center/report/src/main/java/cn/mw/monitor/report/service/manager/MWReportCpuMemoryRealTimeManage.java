package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName MWReportCpuMemoryRealTime
 * @Author gengjb
 * @Date 2022/5/12 10:02
 * @Version 1.0
 **/
@Component
@Slf4j
public class MWReportCpuMemoryRealTimeManage {

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    private Pattern pattern = Pattern.compile("^(.*MEMORY_UTILIZATION|.*CPU_UTILIZATION)$");

    /**
     * 获取CPU与内存的实时数据
     * @param param
     */
    public PageInfo getCpuAndMemoryRealTimeData(RunTimeQueryParam param, List<MwTangibleassetsTable> mwTangibleassetsTables){
        List<RunTimeItemValue> realData = new ArrayList<>();
        //过滤资产条件数据
        if(!CollectionUtils.isEmpty(param.getIds()) && !CollectionUtils.isEmpty(mwTangibleassetsTables)){
            List<String> ids = param.getIds();
            Iterator<MwTangibleassetsTable> iterator = mwTangibleassetsTables.iterator();
            while(iterator.hasNext()){
                MwTangibleassetsTable next = iterator.next();
                if(!ids.contains(next.getId())){
                    iterator.remove();
                }
            }
        }
        //进行资产数据分组，按照服务器ID进行分组
        Map<Integer,List<String>> assetsMap = new HashMap<>();
        Map<String,MwTangibleassetsTable> dtoMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsTables) {
            Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
            String assetsId = mwTangibleassetsDTO.getAssetsId();
            if(monitorServerId == null || monitorServerId == 0 || StringUtils.isBlank(assetsId)){continue;}
            dtoMap.put(assetsId,mwTangibleassetsDTO);
            if(assetsMap.isEmpty() || assetsMap.get(monitorServerId) == null){
                List<String> assetsIds = new ArrayList<>();
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
                continue;
            }
            if(!assetsMap.isEmpty() && assetsMap.get(monitorServerId) != null){
                List<String> assetsIds = assetsMap.get(monitorServerId);
                assetsIds.add(assetsId);
                assetsMap.put(monitorServerId,assetsIds);
            }
        }
        //根据分组数据查询zabbix最新数据
        if(assetsMap.isEmpty()){
            PageInfo page = new PageInfo<>(realData);
            page.setTotal(realData.size());
            page.setList(realData);
            return page;
        }
        Map<String,List<Double>> hostidAndValueMap = new HashMap<>();
        //处理资产数据
        Map<String, RunTimeItemValue> itemValueMap = handleAssets(dtoMap);
        List<String> itemNames = new ArrayList<>();
        itemNames.add(RunTimeReportManager.CPU_UTILIZATION);
        itemNames.add(RunTimeReportManager.MEMORY_UTILIZATION);
        for (Integer serverId : assetsMap.keySet()) {
            List<String> hostids = assetsMap.get(serverId);
            MWZabbixAPIResult result0 = mwtpServerAPI.itemGetbyType(serverId, itemNames, hostids, false);
            if (result0.getCode() == 0) {
                JsonNode jsonNode = (JsonNode) result0.getData();
                if (jsonNode.size() > 0) {
                    for (int i = 0; i < jsonNode.size(); i++) {
                        if (null == jsonNode.get(i)|| null == jsonNode.get(i).get("hostid")) {
                            continue;
                        }
                        String hostid =jsonNode.get(i).get("hostid").asText();
                        double lastvalue = jsonNode.get(i).get("lastvalue").asDouble();
                        String unit = jsonNode.get(i).get("units").asText();
                        String name = jsonNode.get(i).get("name").asText();
                        Matcher matcher = pattern.matcher(name);
                        if(!matcher.find() || itemValueMap.isEmpty() || itemValueMap.get(hostid) == null)continue;
                        RunTimeItemValue runTimeItemValue = itemValueMap.get(hostid);
                        if(StringUtils.isNotBlank(name) && name.contains(RunTimeReportManager.CPU_UTILIZATION)){
                            if(!hostidAndValueMap.isEmpty() && hostidAndValueMap.get(hostid) != null){
                                List<Double> doubles = hostidAndValueMap.get(hostid);
                                doubles.add(lastvalue);
                                hostidAndValueMap.put(hostid,doubles);
                            }else{
                                List<Double> doubles = new ArrayList<>();
                                doubles.add(lastvalue);
                                hostidAndValueMap.put(hostid,doubles);
                            }
                            itemValueMap.put(hostid,runTimeItemValue);
                        }
                        if(StringUtils.isNotBlank(name) && name.contains(RunTimeReportManager.MEMORY_UTILIZATION)){
                            runTimeItemValue.setMemoryUtilizationRate(new BigDecimal(lastvalue).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue() + unit);
                            itemValueMap.put(hostid,runTimeItemValue);
                        }
                    }
                }
            }
        }
        log.info("流量实时报表查询1："+itemValueMap);
        log.info("流量实时报表查询2："+hostidAndValueMap);
        if(!hostidAndValueMap.isEmpty()){
            for (String hostId : hostidAndValueMap.keySet()) {
                List<Double> doubles = hostidAndValueMap.get(hostId);
                RunTimeItemValue runTimeItemValue = itemValueMap.get(hostId);
                double sum = 0;
                if(CollectionUtils.isNotEmpty(doubles)){
                    for (Double aDouble : doubles) {
                        sum += aDouble;
                    }
                }
                if(CollectionUtils.isNotEmpty(doubles)){
                    runTimeItemValue.setCpuUtilizationRate(new BigDecimal(sum / doubles.size()).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue() + "%");
                }else{
                    runTimeItemValue.setCpuUtilizationRate(new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue() + "%");
                }
                itemValueMap.put(hostId,runTimeItemValue);
            }
        }
        //设置数据
        if(!itemValueMap.isEmpty()){
            for (String hostId : itemValueMap.keySet()) {
                RunTimeItemValue runTimeItemValue = itemValueMap.get(hostId);
                if(StringUtils.isBlank(runTimeItemValue.getCpuUtilizationRate()) && StringUtils.isBlank(runTimeItemValue.getMemoryUtilizationRate()))continue;
                realData.add(runTimeItemValue);
            }
        }
        log.info("流量实时报表查询3："+realData);
        if(CollectionUtils.isEmpty(realData)){
            PageInfo page = new PageInfo<>(realData);
            page.setTotal(realData.size());
            page.setList(realData);
            return page;
        }
        Integer pageNumber = param.getPageNumber();
        Integer pageSize = param.getPageSize();
        int fromIndex = pageSize * (pageNumber -1);
        int toIndex = pageSize * pageNumber;
        if(toIndex > realData.size()){
            toIndex = realData.size();
        }
        List<RunTimeItemValue> runTimeItemValueList = realData.subList(fromIndex, toIndex);
        PageInfo pageInfo = new PageInfo<>(runTimeItemValueList);
        pageInfo.setTotal(realData.size());
        pageInfo.setList(runTimeItemValueList);
        return pageInfo;
    }

    /**
     * 处理资产数据
     * @param dtoMap
     */
    private Map<String,RunTimeItemValue> handleAssets(Map<String,MwTangibleassetsTable> dtoMap){
        Map<String,RunTimeItemValue> itemValueMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (String hostId : dtoMap.keySet()) {
            MwTangibleassetsTable tangibleassetsTable = dtoMap.get(hostId);
            RunTimeItemValue itemValue = new RunTimeItemValue();
            itemValue.setAssetName(tangibleassetsTable.getAssetsName());
            itemValue.setAssetsId(tangibleassetsTable.getAssetsId());
            itemValue.setIp(tangibleassetsTable.getInBandIp());
            itemValue.setTime(format.format(new Date()));
            itemValueMap.put(hostId,itemValue);
        }
        return itemValueMap;
    }
}
