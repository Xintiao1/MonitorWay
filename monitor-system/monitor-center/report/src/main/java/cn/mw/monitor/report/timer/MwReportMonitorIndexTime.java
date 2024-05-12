package cn.mw.monitor.report.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.MwReportIndexDto;
import cn.mw.monitor.report.dto.MwReportTrendCacheDto;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 报表监控指标数据缓存
 * @date 2023/9/25 9:34
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwReportMonitorIndexTime {

    @Resource
    private MwReportDao reportDao;

    @Autowired
    private MwAssetsManager assetsManager;

    @Value("${report.history.group}")
    private Integer groupCount;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private ModuleIDManager idManager;

    @Autowired
    private MWUserCommonService userService;

    /**
     * 报表历史数据缓存
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult reportIndexCacheHistory(){
        log.info(">>>>>>>MwReportMonitorIndexTime:reportIndexCache>>>>>>start");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //查询报表指标数据
            List<MwReportIndexDto> mwReportIndexDtos = reportDao.selectReportIndex();
            if(CollectionUtils.isEmpty(mwReportIndexDtos)){return result;}
            List<String> itemNames = mwReportIndexDtos.stream().map(item -> item.getItemName()).collect(Collectors.toList());
            log.info("MwReportMonitorIndexTime{} reportIndexCache() itemNames:"+itemNames);
            //获取所有资产信息
            List<MwTangibleassetsTable> tangibleassetsTables = getAssets();
            //按照serverId分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            List<MwReportTrendCacheDto> trendCacheDtos = getZabbixData(groupMap, itemNames, assetsMap);
            log.info("MwReportMonitorIndexTime{} reportIndexCache() trendCacheDtos:"+trendCacheDtos);
            if(CollectionUtils.isEmpty(trendCacheDtos)){return result;}
            //存储历史趋势数据
            List<List<MwReportTrendCacheDto>> lists = Lists.partition(trendCacheDtos, groupCount);
            int count = 0;
            for (List<MwReportTrendCacheDto> dtos : lists) {
                count += reportDao.insertReportHistoryData(dtos);
            }
            log.info("MwReportMonitorIndexTime{} reportIndexCache() success:"+count);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存报表数据:成功");
        }catch (Throwable e){
            log.error("MwReportMonitorIndexTime{} reportIndexCache() ERROR::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存报表数据:失败");
            result.setFailReason(e.getMessage());
        }
        return result;
    }



    /**
     * 获取zabbix前一天的历史趋势数据
     */
    private List<MwReportTrendCacheDto> getZabbixData(Map<Integer, List<String>> groupMap,List<String> itemNames,Map<String,MwTangibleassetsTable> assetsMap){
        List<MwReportTrendCacheDto> trendCacheDtos = new ArrayList<>();
        //获取昨天的开始与结束时间
        List<Date> yesterday = ReportDateUtil.getYesterday();
        Long startTime = yesterday.get(0).getTime() / 1000;
        Long endTime = yesterday.get(1).getTime() / 1000;
        for (Map.Entry<Integer, List<String>> entry : groupMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            //数据进行分组
            List<List<String>> partition = Lists.partition(hostIds, groupCount);
            for (List<String> hosts : partition) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hosts);
                if(result == null || result.isFail()){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                Map<String,ItemApplication> itemApplicationMap = new HashMap<>();
                itemApplications.forEach(item->{
                    itemApplicationMap.put(item.getItemid(),item);
                });
                //查询历史记录
                List<ItemTrendApplication> historyTrend = getHistoryTrend(itemApplications, serverId, startTime, endTime);
                //转换成存储数据库的DTO
                for (ItemTrendApplication itemTrendApplication : historyTrend) {
                    ItemApplication itemApplication = itemApplicationMap.get(itemTrendApplication.getItemid());
                    MwTangibleassetsTable mwTangibleassetsTable = assetsMap.get(serverId + itemApplication.getHostid());
                    MwReportTrendCacheDto trendCacheDto = new MwReportTrendCacheDto();
                    trendCacheDto.extractFrom(itemApplication,mwTangibleassetsTable,itemTrendApplication);
                    trendCacheDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                    trendCacheDtos.add(trendCacheDto);
                }
            }
        }
        return trendCacheDtos;
    }

    /**
     * 查询历史趋势数据
     * @param itemApplications
     */
    private List<ItemTrendApplication> getHistoryTrend(List<ItemApplication> itemApplications,Integer serverId,Long startTime,Long endTime){
        List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
        List<ItemTrendApplication> applicationList = new ArrayList<>();
        //历史数据分组查询
        List<List<String>> itemss = Lists.partition(itemIds, groupCount);
        for (List<String> items : itemss) {
            MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(serverId, items, startTime, endTime);
            if(trendResult == null || trendResult.isFail()){continue;}
            applicationList.addAll(JSONArray.parseArray(String.valueOf(trendResult.getData()), ItemTrendApplication.class));
        }
        return applicationList;

    }

    private List<MwTangibleassetsTable> getAssets(){
        QueryTangAssetsParam qParam = new QueryTangAssetsParam();
        qParam.setPageSize(Integer.MAX_VALUE);
        qParam.setIsQueryAssetsState(false);
        qParam.setUserId(userService.getAdmin());
        List<MwTangibleassetsTable> assetsTable = assetsManager.getAssetsTable(qParam);
        log.info("MwReportMonitorIndexTime{} getAssets() assetsTable:"+assetsTable);
        return assetsTable;
    }


    /**
     * 最新数据缓存定时任务
     * @return
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult reportIndexCacheLatest(){
        log.info(">>>>>>>MwReportMonitorIndexTime:reportIndexCacheLatest>>>>>>start");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //查询报表指标数据
            List<MwReportIndexDto> mwReportIndexDtos = reportDao.selectReportIndex();
            if(CollectionUtils.isEmpty(mwReportIndexDtos)){return result;}
            List<String> itemNames = mwReportIndexDtos.stream().map(item -> item.getItemName()).collect(Collectors.toList());
            log.info("MwReportMonitorIndexTime{} reportIndexCacheLatest() itemNames:"+itemNames);
            //获取所有资产信息
            List<MwTangibleassetsTable> tangibleassetsTables = getAssets();
            //按照serverId分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            List<MwReportTrendCacheDto> reportTrendCacheDtos = getZabbixLastValue(groupMap, itemNames, assetsMap);
            if(CollectionUtils.isEmpty(reportTrendCacheDtos)){return result;}
            //分组插入数据库
            //先删除原先的最新数据
            reportDao.deleteReportLatestData();
            List<List<MwReportTrendCacheDto>> lists = Lists.partition(reportTrendCacheDtos, groupCount);
            int count = 0;
            for (List<MwReportTrendCacheDto> dtos : lists) {
                count += reportDao.insertReportLatestData(dtos);
            }
            log.info("MwReportMonitorIndexTime{} reportIndexCacheLatest() success:"+count);
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("缓存报表最新数据:成功");
        }catch (Throwable e){
            log.error("MwReportMonitorIndexTime{} reportIndexCacheLatest() ERROR::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("缓存报表最新数据:失败");
            result.setFailReason(e.getMessage());
        }
        log.info(">>>>>>>MwReportMonitorIndexTime:reportIndexCacheLatest>>>>>>end");
        return result;
    }

    /**
     * 获取zabbix最新数据
     */
    private List<MwReportTrendCacheDto> getZabbixLastValue(Map<Integer, List<String>> groupMap,List<String> itemNames,Map<String,MwTangibleassetsTable> assetsMap){
        List<MwReportTrendCacheDto> trendCacheDtos = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : groupMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            //数据进行分组
            List<List<String>> partition = Lists.partition(hostIds, groupCount);
            for (List<String> hosts : partition) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hosts);
                if(result == null || result.isFail()){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                if(CollectionUtils.isEmpty(itemApplications)){continue;}
                for (ItemApplication itemApplication : itemApplications) {
                    MwReportTrendCacheDto trendCacheDto = new MwReportTrendCacheDto();
                    MwTangibleassetsTable tangibleassetsTable = assetsMap.get(serverId + itemApplication.getHostid());
                    trendCacheDto.extractFrom(itemApplication,tangibleassetsTable,null);
                    trendCacheDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                    trendCacheDtos.add(trendCacheDto);
                }
            }
        }
        return trendCacheDtos;
    }
}
