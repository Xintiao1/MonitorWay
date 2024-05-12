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
import cn.mwpaas.common.utils.DateUtils;
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
 * @author gengjb
 * @description 每日凌晨缓存
 * @date 2023/12/25 9:54
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwVisualizedDayDataTime {

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

    @Value("${visualized.init}")
    private Boolean isInit;

    @Value("${visualized.init.day}")
    private Integer initDay;

    /**
     * 缓存昨日数据信息
     * @return
     */
//    @Scheduled(cron = "0 0/2 * * * ?")
    public TimeTaskRresult cacheYesterDayInfo(){
        log.info(">>>>>>>MwVisualizedDayDataTime>>>>>>start");
        TimeTaskRresult result = new TimeTaskRresult();
        try {
            //获取资产
            List<MwTangibleassetsTable> tangibleassetsTables = getAssetsInfo();
            if(CollectionUtils.isEmpty(tangibleassetsTables)){return null;}
            log.info("MwVisualizedDayDataTime{} cacheYesterDayInfo() tangibleassetsTables::"+tangibleassetsTables.size());
            //分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            List<MwVisualizedCacheHistoryDto> cacheHistoryDtos = new ArrayList<>();
            List<List<Date>> initTime = getInitTime();
            if(CollectionUtils.isEmpty(initTime)){
                List<Date> yesterday = getYesterday();
                cacheHistoryDtos = getZabbixHistroy(groupMap, assetsMap,5,yesterday);
                List<List<MwVisualizedCacheHistoryDto>> subLists = Lists.partition(cacheHistoryDtos, groupCount);
                for (List<MwVisualizedCacheHistoryDto> historyDtos : subLists) {
                    visualizedManageDao.insertVisualizedDayData(historyDtos);
                }
            }else{
                for (List<Date> dates : initTime) {
                    List<MwVisualizedCacheHistoryDto> histroy = getZabbixHistroy(groupMap, assetsMap, 5, dates);
                    //数据存储到数据库
                    List<List<MwVisualizedCacheHistoryDto>> subLists = Lists.partition(histroy, groupCount);
                    for (List<MwVisualizedCacheHistoryDto> historyDtos : subLists) {
                        visualizedManageDao.insertVisualizedDayData(historyDtos);
                    }
                }
            }
            result.setSuccess(true);
            result.setResultType(0);
            result.setResultContext("获取昨天数据的趋势数据:成功");
        }catch (Throwable e){
            log.error("MwVisualizedDayDataTime{} cacheYesterDayInfo::",e);
            result.setSuccess(false);
            result.setResultType(0);
            result.setResultContext("获取昨天数据的趋势数据:失败");
            result.setFailReason(e.getMessage());
        }
        log.info(">>>>>>>MwVisualizedDayDataTime>>>>>>end");
        return result;
    }



    /**
     * 获取昨日的历史数据
     * @param groupMap
     * @param assetsMap
     */
    private List<MwVisualizedCacheHistoryDto> getZabbixHistroy(Map<Integer, List<String>> groupMap,Map<String,MwTangibleassetsTable> assetsMap,Integer itemType,List<Date> dates){
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
                        cacheHistoryDto.setTime(DateUtils.formatDate(dates.get(0)));
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
        return mwAssetsManager.getAssetsTable(assetsParam);
    }

    public static List<Date> getYesterday(){
        Date dateStart;
        Date dateEnd;
        Calendar cal2=new GregorianCalendar();
        cal2.setTime(DateUtils.getTimesMorning());
        cal2.add(Calendar.DAY_OF_MONTH,-1);
        dateStart=cal2.getTime();
        Calendar cal3=new GregorianCalendar();
        cal3.setTime(DateUtils.getTimesNight());
        cal3.add(Calendar.DAY_OF_MONTH,-1);
        dateEnd=cal3.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dateStart);
        list.add(dateEnd);
        return list;
    }


    private List<List<Date>> getInitTime(){
        List<List<Date>> dates = new ArrayList<>();
        if(!isInit){return dates;}
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        // 循环获取每一天的开始和结束时间
        for (int i = 0; i < initDay; i++) {
            List<Date> times = new ArrayList<>();
            Date startDate = getBeginningOfDay(calendar.getTime());
            Date endDate = getEndOfDay(calendar.getTime());
            times.add(startDate);
            times.add(endDate);
            dates.add(times);
            // 将日期往前一天
            calendar.add(Calendar.DATE, -1);
        }
        return dates;
    }

    // 将时间设置为一天的开始
    private static Date getBeginningOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // 将时间设置为一天的结束
    private static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
