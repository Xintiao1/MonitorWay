package cn.mw.monitor.report.timer;

import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.LineFlowReportParam;
import cn.mw.monitor.report.param.MwAssetsUsabilityParam;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.impl.DateTypeEnum;
import cn.mw.monitor.report.service.manager.CpuMemTrendHandler;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MwReportManageTime
 * @Description 猫维报表管理定时任务
 * @Author gengjb
 * @Date 2021/11/4 14:09
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j(topic = "timerController")
public class MwReportManageTime {

    @Value("${report.diskUse.scheduleBatchInsert}")
    private int diskUseScheduleBatchInsert;

    @Autowired
    private MwReportTerraceManageService terraceManageService;

    @Resource
    private MwReportTerraceManageDao terraceManageDao;

    @Autowired
    private MWUserDao userDao;

    @Autowired
    private MWNetWorkLinkService workLinkService;

    @Resource
    private MwReportService mwReportService;


    @Autowired
    private MwServerService serverService;

    private Boolean waitFor = false;

    private SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");

    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Value("${report.getAssetsAbblie.open}")
    private boolean getAssetsAbblie;
    @Value("${report.getRedisRunTime.open}")
    private boolean getRedisRunTime;


    @Value("${report.trend.day}")
    private int trendDay;

    @Autowired
    private CpuMemTrendHandler trendHandler;

    /**
     * 缓存线路流量报表数据
     * 每天凌晨一点执行
     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void getLInkFlowReportDataCache() {
//        log.info("开始进行线路流量统计报表定时缓存数据" + new Date());
//        try {
//            List<LineFlowReportParam> cacheDatas = new ArrayList<>();
//            List<Integer> dateTypes = new ArrayList<>();
//            dateTypes.add(1);
//            dateTypes.add(5);
//            dateTypes.add(8);
//            for (Integer dateType : dateTypes) {
//                TrendParam param = new TrendParam();
//                param.setDateType(dateType);
//                param.setTimingType(true);
//                //获取线路流量昨天，上周，上月的数据
//                log.info("查询zabbix服务器中线路流量数据,类型：" + dateType + new Date());
//                Reply reply = terraceManageService.selectReportLinkNews(param);
//                if (reply != null && reply.getData() != null) {
//                    //线路流量数据
//                    List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
//                    if (!CollectionUtils.isEmpty(retDatas)) {
//                        for (LineFlowReportParam retData : retDatas) {
//                            retData.setType(dateType);
//                        }
//                    }
//                    if (CollectionUtils.isEmpty(retDatas)) {
//                        continue;
//                    }
//                    cacheDatas.addAll(retDatas);
//                }
//                log.info("查询zabbix服务器中线路流量数据结束,类型：" + dateType + new Date());
//            }
//            //数据进行数据库缓存
//            log.info("删除原有线路流量缓存数据" + new Date());
//            terraceManageDao.deleteLinkFlowCacheData(null);
//            if (!CollectionUtils.isEmpty(cacheDatas)) {
//                log.info("添加线路流量缓存数据" + new Date());
//                int count = terraceManageDao.saveLinkFlowCacheData(cacheDatas);
//                log.info("添加线路流量缓存数据成功。条数为：" + count + new Date());
//            }
//        } catch (Exception e) {
//            log.error("定时缓存线路流量统计报表数据失败" + e.getMessage());
//        }
//    }


    /**
     * 缓存线路流量统计上月数据
     */
//    @Scheduled(cron = "0 40 3 * * ?")
    public void cacheLineFlowLastMonthData(){
        try {
            log.info("线路流量统计数据每天存储开始"+new Date());
            List<Date> dateStrs = terraceManageDao.selectLineFlowCount();
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dateStrs)){
                for (Date dateStr : dateStrs) {
                    exists.add(formatDate(dateStr));
                }
            }
            log.info("线路流量统计数据每天存储2"+dateStrs+new Date());
            //按照日期每天存数据
            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            int number = 0;
            Long  time = toDayStartTime();
            for (int i = 0; i < daysOfMonth; i++) {
                number += 1;
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                try {
                    log.info("线路流量统计数据每天存储3"+new Date());
                    if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                        continue;
                    }
                    log.info("线路流量统计数据每天存储4"+longToDate(startTime));
                    TrendParam param = new TrendParam();
                    param.setTimingType(true);
                    param.setDateType(20);
                    List<String> chooseTime = getChooseTime(startTime, endTime);
                    param.setChooseTime(chooseTime);
                    log.info("线路流量统计数据每天存储5"+new Date());
                    Reply reply = terraceManageService.selectReportLinkNews(param);
                    log.info("线路流量统计数据每天存储6"+new Date());
                    if(reply != null && reply.getData() != null){
                        //线路流量数据
                        List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
                        log.info("线路流量统计数据每天存储7"+retDatas+new Date());
                        if(!CollectionUtils.isEmpty(retDatas)){
                            for (LineFlowReportParam retData : retDatas) {
                                retData.setSaveTime(longToDate(endTime));
                                retData.setTime(chooseTime.get(0).substring(0,10)+"~"+chooseTime.get(1).substring(0,10));
                            }
                            log.info("查询zabbix服务器中线路流量数据每天结束,"+new Date());
                            //数据进行数据库缓存
                            if(!CollectionUtils.isEmpty(retDatas)){
                                log.info("添加线路流量缓存数据每天"+new Date());
                                int count = terraceManageDao.saveLinkFlowCacheData(retDatas);
                                log.info("添加线路流量缓存数据每天成功。条数为："+count+new Date());
                            }
                        }else{
                            List<LineFlowReportParam> nullData = new ArrayList<>();
                            LineFlowReportParam flowReportParam = new LineFlowReportParam();
                            flowReportParam.setSaveTime(longToDate(endTime));
                            nullData.add(flowReportParam);
                            int count = terraceManageDao.saveLinkFlowCacheData(nullData);
                        }
                    }
                }catch (Exception e){
                    log.error("流量统计每天存储数据执行异常，时间"+formatDate(longToDate(startTime)),e);
                }
            }
            log.info("线路流量统计数据每天存储结束,循环次数"+number+new Date());
        }catch (Exception e){
            log.error("定时缓存线路流量统计报表上月数据失败",e);
        }
    }



    private String formatDate(Date date){
        return format.format(date);
    }


    /**
     * 缓存CPU与内存报表数据
     */
//    @Scheduled(cron = "0 10 2 * * ?")
    public void geCpuAndMemoryReportDataCache() {
        log.info("开始进行CPU与内存情况报表定时缓存数据" + new Date());
        try {
            //数据库中已有的数据时间
            List<Date> cpuDates = terraceManageDao.selectCpuAndMemoryCount();
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(cpuDates)){
                for (Date dateStr : cpuDates) {
                    exists.add(formatDate(dateStr));
                }
            }
            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            Long  time = toDayStartTime();
            for (int i = 0; i < daysOfMonth; i++){
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                    continue;
                }
                Reply reply;
                int days = calculatedTrendDay(startTime);
                log.info("CPU报表定时任务执行 MwReportManageTime"+days+":::"+trendDay);
                if(days > trendDay){
                    reply = trendHandler.getCpuMemTrendInfo(startTime, endTime);
                }else{
                    RunTimeQueryParam param = new RunTimeQueryParam();
                    param.setDateType(20);
                    param.setTimingType(true);
                    List<String> chooseTime = getChooseTime(startTime, endTime);
                    param.setChooseTime(chooseTime);
                    reply = terraceManageService.selectReportCPUNews(param);
                }
                if (reply != null && reply.getData() != null) {
                    //CPU与内存数据
                    List<RunTimeItemValue> retDatas = (List<RunTimeItemValue>) reply.getData();
                    if (!CollectionUtils.isEmpty(retDatas)) {
                        for (RunTimeItemValue retData : retDatas) {
                            retData.setSaveTime(longToDate(endTime));
                        }
                    }
                    //数据进行数据库缓存
                    if(!CollectionUtils.isEmpty(retDatas)){
                        log.info("添加CPU与内存情况缓存数据" + new Date());
                        int count = terraceManageDao.saveCpuAndMemoryData(retDatas);
                        log.info("添加CPU与内存情况缓存数据成功。条数为：" + count + new Date());
                    }
                }else{
                    List<RunTimeItemValue> nullData = new ArrayList<>();
                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                    runTimeItemValue.setSaveTime(longToDate(endTime));
                    nullData.add(runTimeItemValue);
                    int count = terraceManageDao.saveCpuAndMemoryData(nullData);
                }
            }
        } catch (Exception e) {
            log.error("定时缓存CPU与内存情况报表数据失败" + e.getMessage());
        }
    }


//    /**
//     * 缓存今天线路流量报表数据 1小时执行一次
//     */
//    @Scheduled(cron = "0 */5 * * * ?")
//    public void getToDayLInkFlowReportDataCache() {
//        //数据进行数据库缓存
//        log.info("开始进行线路流量统计报表定时缓存数据" + new Date());
//        try {
//            log.info("删除原有线路流量缓存数据" + new Date());
//            terraceManageDao.deleteLinkFlowCacheData(2);
//            List<LineFlowReportParam> cacheDatas = new ArrayList<>();
//            TrendParam param = new TrendParam();
//            param.setDateType(2);
//            param.setTimingType(true);
//            param.setPageNumber(0);
//            param.setPageSize(100000);
//            //获取线路流量今天的数据
//            log.info("查询zabbix服务器中线路流量数据,类型：" + param.getDayType() + new Date());
//            Reply reply = terraceManageService.selectReportLinkNews(param);
//            if (reply != null && reply.getData() != null) {
//                //线路流量数据
//                List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
//                if (!CollectionUtils.isEmpty(retDatas)) {
//                    for (LineFlowReportParam retData : retDatas) {
//                        retData.setType(param.getDateType());
//                        retData.setTime(format.format(new Date())+"~"+format.format(new Date()));
//
//                    }
//                }
//                if (!CollectionUtils.isEmpty(retDatas)) {
//                    cacheDatas.addAll(retDatas);
//                }
//            }
//            log.info("查询zabbix服务器中线路流量数据结束,类型：" + param.getDateType() + new Date());
//            if (!CollectionUtils.isEmpty(cacheDatas)) {
//                log.info("添加线路流量缓存数据" + new Date());
//                int count = terraceManageDao.saveLinkFlowCacheData(cacheDatas);
//                log.info("添加线路流量缓存数据成功。条数为：" + count + new Date());
//            }
//        } catch (Exception e) {
//            log.error("定时缓存线路流量统计报表数据失败" + e.getMessage());
//        }
//    }

    /**
     * 缓存磁盘使用情况报表数据
     * 每天凌晨两点执行
     */
//    @Scheduled(cron = "0 0 2 * * ?")
    public void getDiskUseReportDataCache() {
        log.info("开始进行磁盘使用情况报表定时缓存数据" + new Date());
        try {
            List<DateTypeEnum> dateTypes = new ArrayList<>();
            dateTypes.add(DateTypeEnum.YESTERDAY);
            dateTypes.add(DateTypeEnum.LAST_WEEK);
            dateTypes.add(DateTypeEnum.LAST_MONTH);

            for (DateTypeEnum dateType : dateTypes) {
                //查询上一次的执行结果
                PageHelper.startPage(1, 1);
                List<TrendDiskDto> retDatas = terraceManageDao.selectDiskUseData(dateType.getType(), null);

                //未执行过任务时,需要执行
                if (null == retDatas || retDatas.size() == 0) {
                    log.info("getDiskUseReportDataCache retDatas is null");
                    updateDiskUseReportDataCache(dateType);
                    continue;
                }

                //上一次执行失败时,需要重新执行
                TrendDiskDto trendDiskDto = retDatas.get(0);
                Date lastDate = trendDiskDto.getUpdateTime();
                boolean enableByLastDate = dateType.enableByLastDate(lastDate);
                log.info("getDiskUseReportDataCache dateType:{} ,last success is {}, enableByLastDate:{}, lastDate:{}"
                        , dateType, trendDiskDto.isUpdateSuccess(), enableByLastDate, lastDate);
                if (!trendDiskDto.isUpdateSuccess()) {
                    updateDiskUseReportDataCache(dateType);
                    continue;
                }

                //上一次执行成功,每月第一天,每周第一天，每天这三个时间段都会更新上月，上周,昨天数据
                boolean enableCollectHisData = dateType.enableCollectHisData();
                log.info("getDiskUseReportDataCache update dateType:{}, date:{}, enableCollectHisData:{},last success is {}"
                        , dateType, new Date(), enableCollectHisData, trendDiskDto.isUpdateSuccess());
                if (enableCollectHisData) {
                    updateDiskUseReportDataCache(dateType);
                }
                log.info("查询zabbix服务器中磁盘使用情况数据结束,类型:{},date:{}", dateType, new Date());
            }

        } catch (Exception e) {
            log.error("定时缓存磁盘使用情况报表数据失败", e);
        }
    }

    private void updateDiskUseReportDataCache(DateTypeEnum dateType) {
        TrendParam param = new TrendParam();
        param.setPageSize(Integer.MAX_VALUE);
        param.setDateType(dateType.getType());
        param.setTimingType(true);
        //获取磁盘使用情况昨天，上周，上月的数据
        log.info("查询zabbix服务器中磁盘使用情况数据,类型:{},date:{}", dateType, new Date());
        Reply reply = terraceManageService.selectReportDiskUse(param);
        if (reply != null && reply.getData() != null) {
            //磁盘使用情况数据
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<TrendDiskDto> retDatas = pageInfo.getList();
            if (!CollectionUtils.isEmpty(retDatas)) {
                for (TrendDiskDto retData : retDatas) {
                    retData.setType(dateType.getType());
                    retData.setUpdateSuccess(true);
                }
            } else {
                //没有数据,则任务执行失败
                TrendDiskDto trendDiskDto = TrendDiskDto.builder().type(dateType.getType()).updateSuccess(false).build();
                retDatas.add(trendDiskDto);
            }

            //数据进行数据库缓存
            log.info("删除原有磁盘使用情况缓存数据" + new Date());
            Map paramMap = new HashMap<>();
            paramMap.put("type", dateType.getType());
            terraceManageDao.deleteDiskUseCacheData(paramMap);

            if (!CollectionUtils.isEmpty(retDatas)) {
                log.info("添加磁盘使用情况缓存数据" + new Date());
                List<TrendDiskDto> cacheDatas = new ArrayList<>();

                //控制每次更新数据库的行数
                for (int i = 0; i < retDatas.size(); i++) {
                    if (i % diskUseScheduleBatchInsert == 0) {
                        if (cacheDatas.size() > 0) {
                            int count = terraceManageDao.saveDiskUseCacheData(cacheDatas);
                            log.info("添加磁盘使用情况缓存数据成功。条数为:{},date:{}", count, new Date());
                            cacheDatas = new ArrayList<>();
                        }
                    }
                    cacheDatas.add(retDatas.get(i));
                }
                if (cacheDatas.size() > 0) {
                    int count = terraceManageDao.saveDiskUseCacheData(cacheDatas);
                    log.info("添加磁盘使用情况缓存数据成功。条数为:{},date:{}", count, new Date());
                }
            }
        }
    }

    /**
     * 缓存CPU与内存报表数据
     */
//    @Scheduled(cron = "0 0 22 * * ?")
////    public void geCpuAndMemoryReportDataCache() {
////        log.info("开始进行CPU与内存情况报表定时缓存数据" + new Date());
////        try {
////            List<RunTimeItemValue> cacheDatas = new ArrayList<>();
////            List<Integer> dateTypes = new ArrayList<>();
////            dateTypes.add(1);
////            dateTypes.add(5);
////            dateTypes.add(8);
////            for (Integer dateType : dateTypes) {
////                RunTimeQueryParam param = new RunTimeQueryParam();
////                param.setDateType(dateType);
////                param.setTimingType(true);
////                //获取CPU与内存昨天，上周，上月的数据
////                log.info("查询zabbix服务器中CPU与内存情况数据,类型：" + dateType + new Date());
////                Reply reply = terraceManageService.selectReportCPUNews(param);
////                if (reply != null && reply.getData() != null) {
////                    //CPU与内存数据
////                    List<RunTimeItemValue> retDatas = (List<RunTimeItemValue>) reply.getData();
////                    if (!CollectionUtils.isEmpty(retDatas)) {
////                        for (RunTimeItemValue retData : retDatas) {
////                            retData.setType(dateType);
////                        }
////                    }
////                    if (CollectionUtils.isEmpty(retDatas)) {
////                        continue;
////                    }
////                    cacheDatas.addAll(retDatas);
////                }
////                log.info("查询zabbix服务器中CPU与内存情况数据结束,类型：" + dateType + new Date());
////            }
////            //数据进行数据库缓存
////            log.info("删除原有CPU与内存情况缓存数据" + new Date());
////            terraceManageDao.deleteCpuAndMemoryData();
////            if (!CollectionUtils.isEmpty(cacheDatas)) {
////                log.info("添加CPU与内存情况缓存数据" + new Date());
////                int count = terraceManageDao.saveCpuAndMemoryData(cacheDatas);
////                log.info("添加CPU与内存情况缓存数据成功。条数为：" + count + new Date());
////            }
////        } catch (Exception e) {
////            log.error("定时缓存CPU与内存情况报表数据失败" + e.getMessage());
////        }
////    }


    /**
     * 缓存资产可用性报表数据
     */
//    @Scheduled(cron = "0 0 5 * * ?")
    public void geAssetsUsabilityReportDataCache() {
        log.info("开始进行资产可用性报表定时缓存数据" + new Date());
        try {
            Integer dateType = 12;
            String StartTime = getStratDaysOfMonth(new Date(), 0, -1, 0);
            PageHelper.startPage(0, 100000000);
            List<Date> perforDaysCheck = terraceManageDao.selectAssetsUsabilityByDate(StartTime, new Date());
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(perforDaysCheck)){
                for (Date dateStr : perforDaysCheck) {
                    exists.add(formatDate(dateStr));
                }
            }
            //取于自定义
            Integer performDays = getDaysOfMonth(new Date(), 0, -1, 0);
            if (perforDaysCheck.size() + 1 == performDays) {
                performDays = 1;
            }

            Long time = toDayStartTime();
            for (int k = 0; k < performDays; k++) {
                List<MwAssetsUsabilityParam> cacheDatas = new ArrayList<>();
                Long l = 86400000l * (k + 1);
                Long c = 86400000l * k;
                Long startTime = time - l;
                Long endTime = time - 1000l - c;
                Date date = longToDate(endTime);
                if (exists.contains(formatDate(longToDate(startTime)))) {
                    continue;
                }
                List<String> chooseTime = getChooseTime(startTime, endTime);
                RunTimeQueryParam param = new RunTimeQueryParam();
                param.setDateType(dateType);
                param.setTimingType(true);
                param.setPageSize(100000);
                param.setPageNumber(0);
                param.setChooseTime(chooseTime);
                log.info("查询zabbix服务器中资产可用性数据,类型：" + dateType + new Date());
                Reply reply = terraceManageService.selectReportAssetsUsability(param, true);
                if (reply != null && reply.getData() != null) {
                    //资产可用性数据
                    PageInfo pageInfo = (PageInfo) reply.getData();
                    List<MwAssetsUsabilityParam> retDatas = (List<MwAssetsUsabilityParam>) pageInfo.getList();
                    if (!CollectionUtils.isEmpty(retDatas)) {
                        for (MwAssetsUsabilityParam retData : retDatas) {
                            retData.setType(dateType);
                        }
                    }
                    if (CollectionUtils.isEmpty(retDatas)) {
                        continue;
                    }
                    cacheDatas.addAll(retDatas);
                    if (!CollectionUtils.isEmpty(cacheDatas)) {
                        int count = terraceManageDao.saveAssetsUsabilityData(cacheDatas, date);
                    }
                }
                // 增加空数据 证明zabbix数据为空
                else {
                    MwAssetsUsabilityParam mwAssetsUsabilityParam = new MwAssetsUsabilityParam();
                    cacheDatas.add(mwAssetsUsabilityParam);
                    int count = terraceManageDao.saveAssetsUsabilityData(cacheDatas, date);
                }
            }
            log.error("自动化存储资产可用性报表完成");
        } catch (Exception e) {
            log.error("定时缓存资产可用性报表数据失败" + e.getMessage());
        }
    }


    /**
     * 缓存线路流量报表数据
     * 每天凌晨一点执行
     */

//    @Scheduled(cron = "* 20 3 * * ?")
    public void getRunTimeItemOptimizeUtilization() {
        Integer dateType = DateTimeTypeEnum.USER_DEFINED.getCode();
        String StartTime = getStratDaysOfMonth(new Date(), 0, -1, 0);
        PageHelper.startPage(0, 100000000);
        List<Date> perforDaysCheck = terraceManageDao.selectRunTimeByBelongTime(StartTime, new Date());
        List<String> exists = new ArrayList<>();
        if(!CollectionUtils.isEmpty(perforDaysCheck)){
            for (Date dateStr : perforDaysCheck) {
                exists.add(formatDate(dateStr));
            }
        }
        //取于自定义
        Integer performDays = getDaysOfMonth(new Date(), 0, -1, 0);
        if (perforDaysCheck.size() + 1 == performDays) {
            performDays = 1;
        }

        Long time = toDayStartTime();
        for (int k = 0; k < performDays; k++) {
            Long l = 86400000l * (k + 1);
            Long c = 86400000l * k;
            Long startTime = time - l;
            Long endTime = time - 1000l - c;
            if (exists.contains(formatDate(longToDate(startTime)))) {
                continue;
            }
            List<String> chooseTime = getChooseTime(startTime, endTime);
            RunTimeQueryParam param = new RunTimeQueryParam();
            param.setDateType(dateType);
            param.setTimingType(true);
            param.setDataSize(100000);
            param.setPageNumber(0);
            param.setChooseTime(chooseTime);
            try{
                mwReportService.getRunTimeItemOptimizeUtilization(param, true, false);
            }catch (Exception e){
                log.error("运行状态报表发生错误+错误插入日期+"+longToDate(startTime) ,e.toString());
                throw e ;
            }
        }
    }

    public static void main(String[] args) {

    }
//        log.error("存放数据库"+new Date());
//        try {
//            do {
//                if (waitFor){
//                    //System.out.println("被迫等待线程");
//                    Thread.currentThread().sleep(60000);
//                }
//            }while (waitFor);
//            waitFor = true;
//            RunTimeQueryParam param = new RunTimeQueryParam();
//            param.setDateType(DateTimeTypeEnum.YESTERDAY.getCode());
//            param.setDataSize(5);
//            mwReportService.getRunTimeItemOptimizeUtilization(param,true);
//            log.error("存放上周数据"+new Date());
//            param.setDateType(DateTimeTypeEnum.LAST_WEEK.getCode());
//            mwReportService.getRunTimeItemOptimizeUtilization(param,true);
//            log.error("所有资产缓存数据结束");
//            waitFor = false;
//        }catch (Exception e){
//            waitFor = false;
//            log.error("定时缓存线路流量统计报表数据失败"+e.getMessage());
//        }
//    }



    /**
     * 缓存线路流量报表数据
     * 每天凌晨一点执行
     */
    //  @Scheduled(cron = "0 */5 5-22 * * ?")
    public void getRedisRunTime(){
        log.info("运行报表存放缓存"+new Date());
        try {
            if (!getRedisRunTime){
                return;
            }

            do {
                if (waitFor){
                    Thread.currentThread().sleep(60000);
                }
            }while (waitFor);
            waitFor = true;
            RunTimeQueryParam param = new RunTimeQueryParam();
            param.setDateType(DateTimeTypeEnum.TODAY.getCode());
            param.setDataSize(5);
            mwReportService.getRunTimeItemOptimizeUtilization(param, false, false);
            waitFor = false;
        }catch (Exception e){
            waitFor = false;
            log.error("运行报表存放缓存"+e.getMessage());
        }
    }

    //  @Scheduled(cron = "0 */5 5-22 * * ?")
    public void getAssetsAbblie(){

        try {
            if (!getAssetsAbblie){
                return;
            }
            RunTimeQueryParam param = new RunTimeQueryParam();
            param.setDateType(2);
            Reply reply = terraceManageService.selectReportAssetsUsability(param,false);
        }catch (Exception e){
            log.error("资产可用性定时任务报表错误"+e.getMessage());
        }
    }


    /**
     * 缓存运行状态统计报表数据
     */
//    @Scheduled(cron = "0 */60 * * * ?")
//    public void geRunTimeSateReportDataCache(){
//        log.info("开始进行资产可用性报表定时缓存数据"+new Date());
//        try {
//            List<MwAssetsUsabilityParam> cacheDatas = new ArrayList<>();
//            List<Integer> dateTypes = new ArrayList<>();
//            dateTypes.add(1);
//            dateTypes.add(5);
//            dateTypes.add(8);
//            for (Integer dateType : dateTypes) {
//                RunTimeQueryParam param = new RunTimeQueryParam();
//                param.setDateType(dateType);
//                param.setTimingType(true);
//                //获取资产可用性昨天，上周，上月的数据
//                log.info("查询zabbix服务器中资产可用性数据,类型："+dateType+new Date());
//                Reply reply = terraceManageService.selectReportAssetsUsability(param);
//                if(reply != null && reply.getData() != null){
//                    //资产可用性数据
//                    List<MwAssetsUsabilityParam> retDatas = (List<MwAssetsUsabilityParam>) reply.getData();
//                    if(!CollectionUtils.isEmpty(retDatas)){
//                        for (MwAssetsUsabilityParam retData : retDatas) {
//                            retData.setType(dateType);
//                        }
//                    }
//                    if(CollectionUtils.isEmpty(retDatas)){
//                        continue;
//                    }
//                    cacheDatas.addAll(retDatas);
//                }
//                log.info("查询zabbix服务器中资产可用性数据结束,类型："+dateType+new Date());
//            }
//            //数据进行数据库缓存
//            log.info("删除原有资产可用性缓存数据"+new Date());
//            terraceManageDao.deleteAssetsUsabilityData();
//            if(!CollectionUtils.isEmpty(cacheDatas)){
//                log.info("添加资产可用性缓存数据"+new Date());
//                int count = terraceManageDao.saveAssetsUsabilityData(cacheDatas);
//                log.info("添加资产可用性缓存数据成功。条数为："+count+new Date());
//            }
//        }catch (Exception e){
//            log.error("定时缓存资产可用性报表数据失败"+e.getMessage());
//        }
//    }


//返回定时任务要初始化多少天
    public  int getDaysOfMonth(Date date,Integer year,Integer month,Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        Integer j = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Integer i = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)-1;
        j = i+j;
        return  j;
    }

    //获取上个月第一天
    public  String getStratDaysOfMonth(Date date,Integer year,Integer month,Integer day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        String s = sdf.format(calendar.getTime());
        return  s;
    }



    //返回今天零时零点零分
    private static Long toDayStartTime(){
        Calendar currDate = new GregorianCalendar();
        currDate.set(Calendar.HOUR_OF_DAY,0);
        currDate.set(Calendar.MINUTE,0);
        currDate.set(Calendar.SECOND,0);
        Date time = currDate.getTime();
        return  time.getTime();
    }

    private List<String> getChooseTime(Long startTime, Long endTime) {
        List<String> chooseTime = new ArrayList<>();
        chooseTime.add(longToString(startTime));
        chooseTime.add(longToString(endTime));
        return chooseTime;
    }


    public static Date longToDate(long dateLong){
        Date date = new Date(dateLong);
        return date;
    }

    public String longToString(long dateLong){
        Date date = new Date(dateLong);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(date);
        return str;
    }

    /**
     * 获取今天是星期几
     * @return
     */
    private String getWeekData(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        return sdf.format(date);
    }




//
//    /**
//     * 缓存线路流量统计上月数据
//     */
//    @Scheduled(cron = "0 20 4 * * ?")
//    public void cacheLineFlowLastMonthData(){
//        try {
//            log.info("线路流量统计数据每天存储开始"+new Date());
//            //存储昨天数据
//            //获取昨天的开始时间和结束时间
//            List<Date> yesterday = ReportDateUtil.getYesterday();
//            long dailyStartTime = yesterday.get(0).getTime();
//            long dailyEndTime = yesterday.get(1).getTime();
//            TrendParam param = new TrendParam();
//            param.setTimingType(true);
//            param.setDateType(20);
//            List<String> chooseTime = getChooseTime(dailyStartTime, dailyEndTime);
//            param.setChooseTime(chooseTime);
//            Reply reply = terraceManageService.selectReportLinkNews(param);
//            //删除原来的数据
//            terraceManageDao.deleteLinkFlowCacheData("mw_linkflow_report_daily");
//            if(reply != null && reply.getData() != null){
//                //线路流量数据
//                List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
//                log.info("线路流量统计数据每天存储7"+retDatas+new Date());
//                if(!CollectionUtils.isEmpty(retDatas)){
//                    for (LineFlowReportParam retData : retDatas) {
//                        retData.setSaveTime(longToDate(dailyEndTime));
//                    }
//                    //数据进行数据库缓存
//                    if(!CollectionUtils.isEmpty(retDatas)){
//                        log.info("添加线路流量缓存数据每天"+new Date());
//                        int count = terraceManageDao.saveLinkFlowCacheData(retDatas);
//                        log.info("添加线路流量缓存数据每天成功。条数为："+count+new Date());
//                    }
//                }else{
//                    List<LineFlowReportParam> nullData = new ArrayList<>();
//                    LineFlowReportParam flowReportParam = new LineFlowReportParam();
//                    flowReportParam.setSaveTime(longToDate(dailyEndTime));
//                    nullData.add(flowReportParam);
//                    int count = terraceManageDao.saveLinkFlowCacheData(nullData);
//                }
//            }
//            //判读是否存储上周数据
//            if("星期一".equals(getWeekData())){
//                //每周一存储上周数据到数据库
//                Long  time = toDayStartTime();
//                for (int i = 0; i < 7; i++){
//                    Long k = 86400000l*(i+1);
//                    Long startTime = time - k;
//                    Long v = 86400000l*i;
//                    Long endTime = time -1000-v;
//                }
//            }else{
//                //如果不是星期一，需要查询数据库是否有数据，没有数据需要重新获取
//
//            }
//
//            List<Date> dateStrs = terraceManageDao.selectLineFlowCount();
//            List<String> exists = new ArrayList<>();
//            if(!CollectionUtils.isEmpty(dateStrs)){
//                for (Date dateStr : dateStrs) {
//                    exists.add(formatDate(dateStr));
//                }
//            }
//            log.info("线路流量统计数据每天存储2"+dateStrs+new Date());
//            //按照日期每天存数据
//            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
//            int number = 0;
//            Long  time = toDayStartTime();
//            for (int i = 0; i < daysOfMonth; i++) {
//                number += 1;
//                Long k = 86400000l*(i+1);
//                Long startTime = time - k;
//                Long v = 86400000l*i;
//                Long endTime = time -1000-v;
//                try {
//                    log.info("线路流量统计数据每天存储3"+new Date());
//                    if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
//                        continue;
//                    }
//                    log.info("线路流量统计数据每天存储4"+longToDate(startTime));
//                    TrendParam param = new TrendParam();
//                    param.setTimingType(true);
//                    param.setDateType(20);
//                    List<String> chooseTime = getChooseTime(startTime, endTime);
//                    param.setChooseTime(chooseTime);
//                    log.info("线路流量统计数据每天存储5"+new Date());
//                    Reply reply = terraceManageService.selectReportLinkNews(param);
//                    log.info("线路流量统计数据每天存储6"+new Date());
//                    if(reply != null && reply.getData() != null){
//                        //线路流量数据
//                        List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
//                        log.info("线路流量统计数据每天存储7"+retDatas+new Date());
//                        if(!CollectionUtils.isEmpty(retDatas)){
//                            for (LineFlowReportParam retData : retDatas) {
//                                retData.setSaveTime(longToDate(endTime));
//                            }
//                            log.info("查询zabbix服务器中线路流量数据每天结束,"+new Date());
//                            //数据进行数据库缓存
//                            if(!CollectionUtils.isEmpty(retDatas)){
//                                log.info("添加线路流量缓存数据每天"+new Date());
//                                int count = terraceManageDao.saveLinkFlowCacheData(retDatas);
//                                log.info("添加线路流量缓存数据每天成功。条数为："+count+new Date());
//                            }
//                        }else{
//                            List<LineFlowReportParam> nullData = new ArrayList<>();
//                            LineFlowReportParam flowReportParam = new LineFlowReportParam();
//                            flowReportParam.setSaveTime(longToDate(endTime));
//                            nullData.add(flowReportParam);
//                            int count = terraceManageDao.saveLinkFlowCacheData(nullData);
//                        }
//                    }
//                }catch (Exception e){
//                    log.error("流量统计每天存储数据执行异常，时间"+formatDate(longToDate(startTime)),e);
//                }
//            }
//            log.info("线路流量统计数据每天存储结束,循环次数"+number+new Date());
//        }catch (Exception e){
//            log.error("定时缓存线路流量统计报表上月数据失败",e);
//        }
//    }


    /**
     * 缓存MPLS报告数据
     */
//    @Scheduled(cron = "0 10 1 * * ?")
    public void cacheMplsHistoryData(){
        try {
            log.info("MPLS历史数据数据每天存储开始"+new Date());
            PageHelper.startPage(0, 100000000);
            List<Date> dateStrs = terraceManageDao.selectMplsHistoryTime();
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dateStrs)){
                for (Date dateStr : dateStrs) {
                    exists.add(formatDate(dateStr));
                }
            }
            //按照日期每天存数据
            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            Long  time = toDayStartTime();
            //查询所有线路数据
            List<NetWorkLinkDto> linkData = getLinkData();
            for (int i = 0; i < daysOfMonth; i++) {
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                try {
                    if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                        continue;
                    }
                    if(CollectionUtils.isEmpty(linkData)){
                        return;
                    }
                    //根据线路查询每天的历史流量
                    for (NetWorkLinkDto linkDatum : linkData) {
                        log.info("查询MPLS线路数据，线路名称"+linkDatum.getLinkName());
                        ServerHistoryDto dto = new ServerHistoryDto();
                        dto.setDateStart(formatDate(longToDate(startTime))+" 00:00:00");
                        dto.setDateEnd(formatDate(longToDate(endTime))+" 23:59:59");
                        dto.setDateType(5);
                        dto.setAssetsId(linkDatum.getTargetAssetsId());
                        List<String> rootPort = new ArrayList<>();
                        String valuePort = linkDatum.getValuePort();
                        if(StringUtils.isNotBlank(valuePort) && "ROOT".equals(valuePort)){
                            rootPort.add("["+linkDatum.getRootPort()+"]MW_INTERFACE_IN_TRAFFIC");
                            rootPort.add("["+linkDatum.getRootPort()+"]MW_INTERFACE_OUT_TRAFFIC");
                            dto.setMonitorServerId(linkDatum.getRootAssetsParam().getMonitorServerId());
                        }
                        if(StringUtils.isNotBlank(valuePort) && "TARGET".equals(valuePort)){
                            rootPort.add("["+linkDatum.getTargetPort()+"]MW_INTERFACE_IN_TRAFFIC");
                            rootPort.add("["+linkDatum.getTargetPort()+"]MW_INTERFACE_OUT_TRAFFIC");
                            dto.setMonitorServerId(linkDatum.getTargetAssetsParam().getMonitorServerId());
                        }
                        dto.setName(rootPort);
                        dto.setValueType("AVG");
                        Reply historyData = serverService.getHistoryData(dto);
                        List<String> sendData = new ArrayList<>();
                        List<String> acceptData = new ArrayList<>();
                        List<String> sortSendData = new ArrayList<>();
                        List<String> sortAcceptData = new ArrayList<>();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if(historyData != null){
                            List mapList = (List) historyData.getData();
                            String s = JSON.toJSONString(mapList);
                            //因为数据量太大，只取数据进行存储
                            if(!CollectionUtils.isEmpty(mapList)){
                                for (Object o : mapList) {
                                    if(o != null){
                                        List t = (List) o;
                                        if(!CollectionUtils.isEmpty(t)){
                                            for (Object o1 : t) {
                                                Map map = (Map) o1;
                                                Object titleName = map.get("titleName");
                                                List realData = (List) map.get("realData");
                                                Object unitByReal = map.get("unitByReal");
                                                if(titleName != null && !CollectionUtils.isEmpty(realData) && titleName.toString().contains("发送")){
                                                    if(realData.size() < 24){
                                                        for (Object realDatum : realData) {
                                                            if(realDatum != null){
                                                                MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realDatum;
                                                                String lastValue = mwItemHistoryDto.getValue();
                                                                Date dateTime = mwItemHistoryDto.getDateTime();
                                                                sortSendData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                            }
                                                        }
                                                    }else{
                                                        int count = realData.size() / 24;
                                                        for (int j = 0; j < 24; j++) {
                                                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realData.get(j*count);
                                                            String lastValue = mwItemHistoryDto.getValue();
                                                            Date dateTime = mwItemHistoryDto.getDateTime();
                                                            sortSendData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                        }
                                                    }
                                                    for (Object realDatum : realData) {
                                                        if(realDatum != null){
                                                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realDatum;
                                                            String lastValue = mwItemHistoryDto.getValue();
                                                            Date dateTime = mwItemHistoryDto.getDateTime();
                                                            sendData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                        }
                                                    }
                                                }
                                                if(titleName != null && !CollectionUtils.isEmpty(realData) && titleName.toString().contains("接收")){
                                                    if(realData.size() < 24){
                                                        for (Object realDatum : realData) {
                                                            if(realDatum != null){
                                                                MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realDatum;
                                                                String lastValue = mwItemHistoryDto.getValue();
                                                                Date dateTime = mwItemHistoryDto.getDateTime();
                                                                sortSendData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                            }
                                                        }
                                                    }else{
                                                        int count = realData.size() / 24;
                                                        for (int j = 0; j < 24; j++) {
                                                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realData.get(count*j);
                                                            String lastValue = mwItemHistoryDto.getValue();
                                                            Date dateTime = mwItemHistoryDto.getDateTime();
                                                            sortAcceptData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                        }
                                                    }
                                                    for (Object realDatum : realData) {
                                                        if(realDatum != null){
                                                            MWItemHistoryDto mwItemHistoryDto = (MWItemHistoryDto) realDatum;
                                                            String lastValue = mwItemHistoryDto.getValue();
                                                            Date dateTime = mwItemHistoryDto.getDateTime();
                                                            acceptData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            String sendStr = JSON.toJSONString(sendData);
                            String acceptStr = JSON.toJSONString(acceptData);
                            String sortSendStr = JSON.toJSONString(sortSendData);
                            String sortAcceptStr = JSON.toJSONString(sortAcceptData);
                            terraceManageDao.saveMplsHistoryDataCache(sendStr,acceptStr,longToDate(endTime),linkDatum.getLinkName(),sortSendStr,sortAcceptStr);
                        }else{
                            terraceManageDao.saveMplsHistoryDataCache("","", longToDate(endTime),linkDatum.getLinkName(),"","");
                        }
                        log.info("日期为"+formatDate(longToDate(startTime))+"数据添加成功");
                    }
                }catch (Exception e){
                    log.error("MPLS历史数据数据执行异常，时间"+formatDate(longToDate(endTime)),e);
                }
            }
        }catch (Exception e){
            log.error("定时缓存MPLS历史数据数据失败",e);
        }
    }
    /**
     * 查询所有线路数据
     */
    private  List<NetWorkLinkDto> getLinkData(){
        LinkDropDownParam linkDropDownParam = new LinkDropDownParam();
        linkDropDownParam.setIsAdvancedQuery(false);
        linkDropDownParam.setPageNumber(1);
        linkDropDownParam.setPageSize(9999);
        log.info("起始页"+linkDropDownParam.getPageNumber()+"每页条数"+linkDropDownParam.getPageSize());
        UserDTO admin = userDao.selectByLoginName("admin");
        if(admin != null){
            linkDropDownParam.setUserId(admin.getUserId());
        }
        Reply reply = workLinkService.selectList(linkDropDownParam);
        if(reply == null){
            return null;
        }
        Object data = reply.getData();
        if(data != null){
            PageInfo pageInfo = (PageInfo) data;
            List<NetWorkLinkDto> list = pageInfo.getList();
            log.info("MPLS线路数据查询成功"+list);
            if(!CollectionUtils.isEmpty(list)){
                return list;
            }
        }
        return null;
    }

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 定时缓存资产状态，半小时缓存一次
     *
     */
//    @Scheduled(cron = "0 */30 * * * ?")
    public void cacheAssetsTreeData(){
        try {
            //查询所有资产
            QueryTangAssetsParam qParam = new QueryTangAssetsParam();
            Map pubCriteria = PropertyUtils.describe(qParam);
            List<MwTangibleassetsTable> mwTangibleassetsTables = mwTangibleAssetsDao.selectPubList(pubCriteria);
            if(CollectionUtils.isEmpty(mwTangibleassetsTables)){
                return;
            }
            //进行数据分组
            Map<Integer,List<String>> assetsMap = new HashMap<>();
            for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsTables) {
                Integer monitorServerId = mwTangibleassetsDTO.getMonitorServerId();
                String assetsId = mwTangibleassetsDTO.getAssetsId();
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
            if(assetsMap.isEmpty()){
                return;
            }
            Map<String, String> statusMap = new HashMap<>();
            for (Map.Entry<Integer, List<String>> entry : assetsMap.entrySet()) {
                Integer key = entry.getKey();
                List<String> value = entry.getValue();

                //有改动-zabbi
                log.info("查询资产树状结构数据资产状态开始,"+value.size()+new Date());
                MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(key, ZabbixItemConstant.ASSETS_STATUS, value);
                log.info("查询资产树状结构数据资产状态结束,"+value.size()+new Date());
                if (!statusData.isFail()) {
                    JsonNode jsonNode = (JsonNode) statusData.getData();
                    if (jsonNode.size() > 0) {
                        for (JsonNode node : jsonNode) {
                            Integer lastvalue = node.get("lastvalue").asInt();
                            String hostId = node.get("hostid").asText();
                            String status = (lastvalue == 0) ? "ABNORMAL" : "NORMAL";
                            statusMap.put(key + ":" + hostId, status);
                        }
                    }
                }
//                statusMap.put(key + ":" + value, "ABNORMAL");
            }
            if(!statusMap.isEmpty()){
                //将状态信息存入redis
                redisTemplate.opsForValue().set("assetTreeStatus", JSONObject.toJSONString(statusMap), 30, TimeUnit.MINUTES);
                log.info("资产状态redis存储成功");
            }
        }catch (Exception e){

        }
    }


    /**
     * 计算趋势相差天数
     * @return
     */
    private int calculatedTrendDay(Long startTime){
        long endTime = new Date().getTime();
        long day = (endTime - startTime) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(day));
    }

}
