package cn.mw.monitor.visualized.time;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedCacheHistoryDto;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
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
 * @ClassName
 * @Description 获取当天数据的趋势数据
 * @Author gengjb
 * @Date 2023/5/20 21:18
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedModuleHistoryTime {

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private ModuleIDManager idManager;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Value("${visualized.group.count}")
    private Integer groupCount;

    @Value("${visualized.host.group}")
    private Integer hostGroupCount;

//    @Scheduled(cron = "0 0/5 * * * ?")
    public TimeTaskRresult getHistoryInfo(){
        log.info(">>>>>>>MwVisualizedModuleHistoryTime>>>>>>start");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取资产
            List<MwTangibleassetsTable> tangibleassetsTables = getAssetsInfo();
            if(CollectionUtils.isEmpty(tangibleassetsTables)){return null;}
            log.info("MwVisualizedModuleHistoryTime{} getHistoryInfo() tangibleassetsTables::"+tangibleassetsTables.size());
            //分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = getZabbixHistroy(groupMap, assetsMap,2);
            getInterFaceTrendInfo(tangibleassetsTables,cacheHistoryDtos);
            if(CollectionUtils.isEmpty(cacheHistoryDtos)){return null;}
            //先删除原先缓存数据
            visualizedManageDao.deleteVisualizedCacheHistoryMonitorInfo();
            List<List<MwVisualizedCacheHistoryDto>> subLists = Lists.partition(cacheHistoryDtos, groupCount);
            for (List<MwVisualizedCacheHistoryDto> historyDtos : subLists) {
                visualizedManageDao.visualizedCacheHistoryMonitorInfo(historyDtos);
            }
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("获取当天数据的趋势数据:成功");
        }catch (Throwable e){
            log.error("MwVisualizedModuleHistoryTime{} getHistoryInfo::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("获取当天数据的趋势数据:失败");
            result.setFailReason(e.getMessage());
        }
        log.info(">>>>>>>MwVisualizedModuleHistoryTime>>>>>>end");
        return result;
    }

    private final String FILTER_NAME = "互联网";

    /**
     * 是否有需要筛选的数据
     * @param tangibleassetsTables
     */
    private void getInterFaceTrendInfo(List<MwTangibleassetsTable> tangibleassetsTables,List<MwVisualizedCacheHistoryDto> cacheHistoryDtos){
        List<MwTangibleassetsTable> tables = tangibleassetsTables.stream().filter(item -> StringUtils.isNotBlank(item.getModelClassify()) && item.getModelClassify().equals(FILTER_NAME)).collect(Collectors.toList());
        log.info("MwVisualizedModuleHistoryTime{} getInterFaceTrendInfo() tables::"+tables.size());
        if(CollectionUtils.isEmpty(tables)){return;}
        //分组
        Map<Integer, List<String>> groupMap = tables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
        tables.forEach(item->{
            assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
        });
        List<MwVisualizedCacheHistoryDto> historyDtos = getZabbixHistroy(groupMap, assetsMap,3);
        log.info("MwVisualizedModuleHistoryTime{} getInterFaceTrendInfo() historyDtos::"+historyDtos.size());
        cacheHistoryDtos.addAll(historyDtos);
    }

    /**
     * 获取当天的历史数据
     * @param groupMap
     * @param assetsMap
     */
    private List<MwVisualizedCacheHistoryDto> getZabbixHistroy(Map<Integer, List<String>> groupMap,Map<String,MwTangibleassetsTable> assetsMap,Integer itemType){
        List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = new ArrayList<>();
        List<String> itemNames = visualizedManageDao.selectCacheItemByType(itemType);
        log.info("getHistoryInfo{} getZabbixHistroy()::itemNames"+itemNames);
        if(CollectionUtils.isEmpty(itemNames)){return cacheHistoryDtos;}
        for (Integer serverId : groupMap.keySet()) {
            List<String> hosts = groupMap.get(serverId);
            log.info("MwVisualizedModuleHistoryTime{} getZabbixHistroy() hosts::"+hosts.size());
            List<List<String>> partition = Lists.partition(hosts, hostGroupCount);
            for (List<String> hostIds : partition) {
                //查询监控项数据
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, itemNames, hostIds);
                if(result == null || result.isFail()){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                Map<String,ItemApplication> itemApplicationMap = new HashMap<>();
                itemApplications.forEach(item->{
                    itemApplicationMap.put(item.getItemid(),item);
                });
                List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
                //获取今天的日期
                List<Date> dates = getToDay();
                List<ItemTrendApplication> applicationList = new ArrayList<>();
                //历史数据分组查询
                List<List<String>> itemss = Lists.partition(itemIds, hostGroupCount);
                log.info("getHistoryInfo{} getZabbixHistroy()::itemssGroup::"+itemss.size());
                for (List<String> items : itemss) {
                    MWZabbixAPIResult trendResult = mwtpServerAPI.trendBatchGet(serverId, items, dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000);
                    if(trendResult == null || trendResult.isFail()){continue;}
                    boolean validJSON = MwVisualizedUtil.isValidJSON(JSON.toJSONString(String.valueOf(trendResult.getData())));
                    if(!validJSON){continue;}
                    applicationList.addAll(JSONArray.parseArray(String.valueOf(trendResult.getData()), ItemTrendApplication.class));
                }
                //按照itemid分组
                Map<String, List<ItemTrendApplication>> trendMap = applicationList.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
                for (String itemId : trendMap.keySet()) {
                    List<ItemTrendApplication> trendApplications = trendMap.get(itemId);
                    ItemApplication itemApplication = itemApplicationMap.get(itemId);
                    if(CollectionUtils.isEmpty(trendApplications) || itemApplication == null){continue;}
                    for (ItemTrendApplication trendApplication : trendApplications) {
                        MwVisualizedCacheHistoryDto cacheHistoryDto = new MwVisualizedCacheHistoryDto();
                        cacheHistoryDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
                        cacheHistoryDto.extractFrom(itemApplication,assetsMap.get(serverId+itemApplication.getHostid()),trendApplication);
                        cacheHistoryDtos.add(cacheHistoryDto);
                    }
                }
            }
        }
        return cacheHistoryDtos;
    }

    /**
     * 获取资产信息
     */
    private List<MwTangibleassetsTable> getAssetsInfo(){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userService.getAdmin());
//        assetsParam.setAssetsTypeSubId(187);
        return mwAssetsManager.getAssetsTable(assetsParam);
    }

    /**
     * 获取今天的日期
     * @return
     */
    private List<Date> getToDay(){
        List<Date> dates = new ArrayList<>();
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        // 设置时间为今天的开始时间
         calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        dates.add(date);
        dates.add(new Date());
        return dates;
    }

}
