package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description CPU内存趋势处理
 * @Author gengjb
 * @Date 2023/4/18 20:22
 * @Version 1.0
 **/
@Component
@Slf4j
public class CpuMemTrendHandler {

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MWUserCommonService userCommonService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据时间查询趋势数据
     * @param startTime
     * @param endTime
     */
    public Reply getCpuMemTrendInfo(Long startTime, Long endTime){
        try {
            //获取资产信息
            QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
            assetsParam.setPageNumber(1);
            assetsParam.setPageSize(Integer.MAX_VALUE);
            assetsParam.setUserId(userCommonService.getAdmin());
            //根据类型查询资产数据
            List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
            log.info("CPU报表趋势资产数据"+mwTangAssetses);
            if(CollectionUtils.isEmpty(mwTangAssetses)){return null;}
            //根据监控服务器ID进行数据分组
            Map<Integer, List<String>> groupMap = mwTangAssetses.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            log.info("CPU报表趋势资产数据分组"+groupMap);
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            mwTangAssetses.forEach(item->{
                assetsMap.put(item.getAssetsId(),item);
            });
            return Reply.ok(getCpuMemMonitor(groupMap,startTime,endTime,assetsMap));
        }catch (Throwable e){
            log.error("查询CPU内存报表趋势数据失败",e);
            return null;
        }
    }

    /**
     * 获取监控信息
     * @param groupMap
     * @param startTime
     * @param endTime
     */
    private List<RunTimeItemValue> getCpuMemMonitor(Map<Integer, List<String>> groupMap,Long startTime,Long endTime,Map<String,MwTangibleassetsTable> assetsMap){
        List<RunTimeItemValue> runTimeItemValues = new ArrayList<>();
        if(groupMap == null || groupMap.isEmpty() ){return runTimeItemValues;}
        List<String> names = new ArrayList<>();
        names.add(RunTimeReportManager.CPU_UTILIZATION);
        names.add(RunTimeReportManager.MEMORY_UTILIZATION);
        names.add(RunTimeReportManager.MEMORY_TOTAL);
        names.add(RunTimeReportManager.MEMORY_USED);
        names.add(RunTimeReportManager.ICMP_RESPONSE_TIME);
        List<ItemTrendApplication> trendApplications = new ArrayList<>();
        Map<String,ItemApplication> itemHostMap = new HashMap<>();
        for (Integer serverId : groupMap.keySet()) {
            List<String> hostIds = groupMap.get(serverId);
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(serverId, names, hostIds, false);
            List<ItemApplication> itemApplicationList = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            log.info("CPU内存报表趋势查询最新数据"+serverId+":::"+itemApplicationList);
            if(CollectionUtils.isEmpty(itemApplicationList)){continue;}
            itemApplicationList.forEach(itemApplication -> {
                itemHostMap.put(itemApplication.getItemid(),itemApplication);
            });
            //查询趋势数据
            List<String> itemIds = itemApplicationList.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
            MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(serverId, itemIds, startTime/1000, endTime/1000);
            trendApplications.addAll(JSONArray.parseArray(String.valueOf(trendResult.getData()), ItemTrendApplication.class));
        }
        log.info("CPU内存报表趋势查询历史数据"+trendApplications);
        if(CollectionUtils.isEmpty(trendApplications)){return runTimeItemValues;}
        //按itemID进行分组
        Map<String, List<ItemTrendApplication>> trendMap = trendApplications.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
        Map<String, RunTimeItemValue> itemValueMap = new HashMap<>();
        for (String itemId : trendMap.keySet()) {
            List<ItemTrendApplication> applications = trendMap.get(itemId);
            if(CollectionUtils.isEmpty(applications)){continue;}
            handlerTrendInfo(itemId,applications,itemHostMap,itemValueMap,assetsMap);
        }
        return itemValueMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 处理趋势数据
     */
    private void handlerTrendInfo(String itemId,List<ItemTrendApplication> applications, Map<String,ItemApplication> itemHostMap,Map<String, RunTimeItemValue> itemValueMap,Map<String,MwTangibleassetsTable> assetsMap){
        BigDecimal max = new BigDecimal(0);
        String maxTime = null;
        BigDecimal avg = new BigDecimal(0);
        BigDecimal min = new BigDecimal(1000);
        String minTime = null;
        for (ItemTrendApplication application : applications) {
            String value_max = application.getValue_max();
            String value_avg = application.getValue_avg();
            String value_min = application.getValue_min();
            if(StringUtils.isNotBlank(value_max) && max.compareTo(new BigDecimal(value_max)) < 1){
                max = new BigDecimal(value_max);
                maxTime = application.getClock();
            }
            if(StringUtils.isNotBlank(value_avg) && avg.compareTo(new BigDecimal(value_avg)) < 1){
                avg = new BigDecimal(value_avg);
            }
            if(StringUtils.isNotBlank(value_min) && new BigDecimal(value_min).compareTo(min) < 1){
                min = new BigDecimal(value_min);
                minTime = application.getClock();
            }
        }
        ItemApplication itemApplication = itemHostMap.get(itemId);
        String itemName = itemApplication.getName();
        if(StringUtils.isBlank(itemName)){return;}
        if(itemValueMap.containsKey(itemApplication.getHostid())){
            RunTimeItemValue runTimeItemValue = itemValueMap.get(itemApplication.getHostid());
            MwTangibleassetsTable mwTangibleassetsDTO = assetsMap.get(itemApplication.getHostid());
            runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
            runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
            runTimeItemValue.setAssetsId(mwTangibleassetsDTO.getId());
            setRunTimeValue(runTimeItemValue,itemName,itemApplication.getUnits(),max,avg,min,maxTime,minTime);
            itemValueMap.put(itemApplication.getHostid(),runTimeItemValue);
        }else{
            RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
            MwTangibleassetsTable mwTangibleassetsDTO = assetsMap.get(itemApplication.getHostid());
            runTimeItemValue.setAssetName(mwTangibleassetsDTO.getAssetsName());
            runTimeItemValue.setIp(mwTangibleassetsDTO.getInBandIp());
            runTimeItemValue.setAssetsId(mwTangibleassetsDTO.getId());
            setRunTimeValue(runTimeItemValue,itemName,itemApplication.getUnits(),max,avg,min,maxTime,minTime);
            itemValueMap.put(itemApplication.getHostid(),runTimeItemValue);
        }
    }

    private void setRunTimeValue(RunTimeItemValue runTimeItemValue,String itemName,String units,BigDecimal max,BigDecimal avg,BigDecimal min,String maxTime,String minTime){
        if(itemName.contains(RunTimeReportManager.CPU_UTILIZATION)){
            runTimeItemValue.setMaxValue(max.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setAvgValue(avg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setMinValue(min.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setMaxValueTime(maxTime != null?format.format(new Date(Long.parseLong(maxTime)*1000)):format.format(new Date()));
            runTimeItemValue.setMinValueTime(minTime != null?format.format(new Date(Long.parseLong(minTime)*1000)):format.format(new Date()));
        }
        if(itemName.contains(RunTimeReportManager.MEMORY_UTILIZATION)){
            runTimeItemValue.setMaxMemoryUtilizationRate(max.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setDiskUserRate(avg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setMinMemoryUtilizationRate(min.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+units);
            runTimeItemValue.setMemoryMaxValueTime(maxTime != null?format.format(new Date(Long.parseLong(maxTime)*1000)):format.format(new Date()));
            runTimeItemValue.setMemoryMinValueTime(minTime != null?format.format(new Date(Long.parseLong(minTime)*1000)):format.format(new Date()));
        }
        if(itemName.contains(RunTimeReportManager.MEMORY_TOTAL)){
            //进行单位转换
            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(avg, units);
            runTimeItemValue.setDiskTotal(convertedValue.get("value")+convertedValue.get("units"));
        }
        if(itemName.contains(RunTimeReportManager.MEMORY_USED)){
            //进行单位转换
            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(avg, units);
            runTimeItemValue.setDiskUser(convertedValue.get("value")+convertedValue.get("units"));
        }
        if(itemName.contains(RunTimeReportManager.ICMP_RESPONSE_TIME)){
            //进行单位转换
            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(avg, units);
            runTimeItemValue.setIcmpResponseTime(convertedValue.get("value")+convertedValue.get("units"));
        }
    }
}
