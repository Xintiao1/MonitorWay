package cn.mw.monitor.report.timer;

import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.MWMplsCacheDataDto;
import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.LineFlowReportParam;
import cn.mw.monitor.report.param.MwAssetsUsabilityParam;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.impl.MWReportHandlerDataLogic;
import cn.mw.monitor.report.service.manager.CpuMemTrendHandler;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MwManualTimeTaskRun
 * @Description 报表定时任务手动执行类
 * @Author gengjb
 * @Date 2022/1/4 10:31
 * @Version 1.0
 **/
@Component
@Slf4j(topic = "timerController")
public class MwManualTimeTaskRun {
    @Autowired
    private MwReportTerraceManageService terraceManageService;

    @Resource
    private MwReportTerraceManageDao terraceManageDao;

    private SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");

    @Resource
    private MwReportService mwReportService;

    @Autowired
    private MWUserDao userDao;

    @Autowired
    private MwServerService serverService;

    @Autowired
    private MWNetWorkLinkService workLinkService;

    @Value("${report.maxDay}")
    private int maxDay;

    @Value("${report.initializeDay}")
    private int initializeDay;

    @Value("${report.trend.day}")
    private int trendDay;

    @Autowired
    private CpuMemTrendHandler trendHandler;

    /**
     * 缓存线路流量统计天数据
     */
    public void manualLinkReportDailyDataCache(){
        try {
            log.info("判断线路流量统计数据是否需要删除");
            checkDelDailyData("mw_linkflow_report_daily");
            log.info("线路流量统计数据每天存储开始"+new Date());
            PageHelper.startPage(0, 100000000);
            List<Date> dates = terraceManageDao.selectReportDate("mw_linkflow_report_daily");
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dates)){
                for (Date dateStr : dates) {
                    exists.add(formatDate(dateStr));
                }
            }
            //按照日期每天存数据
//            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            //如果初始化天数为0，默认为60天
            if(initializeDay == 0){
                initializeDay = 60;
            }
            int number = 0;
            Long  time = toDayStartTime();
            for (int i = 0; i < initializeDay; i++) {
                number += 1;
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                try {
                    if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                        continue;
                    }
                    TrendParam param = new TrendParam();
                    param.setTimingType(true);
                    param.setDateType(20);
                    param.setPageNumber(1);
                    param.setPageSize(100000);
                    List<String> chooseTime = getChooseTime(startTime, endTime);
                    param.setChooseTime(chooseTime);
                    log.info("进行手动线路流量数据查询");
                    Reply reply = terraceManageService.selectReportLinkNews(param);
                    log.info("手动线路流量数据查询成功"+reply);
                    if(reply != null && reply.getData() != null){
                        //线路流量数据
                        List<LineFlowReportParam> retDatas = (List<LineFlowReportParam>) reply.getData();
                        if(!CollectionUtils.isEmpty(retDatas)){
                            for (LineFlowReportParam retData : retDatas) {
                                retData.setSaveTime(longToDate(endTime));
                                retData.setUpdateSuccess(true);
                            }
                            //数据进行数据库缓存
                            if(!CollectionUtils.isEmpty(retDatas)){
                                Iterator<LineFlowReportParam> iterator = retDatas.iterator();
                                List<LineFlowReportParam> newList = new ArrayList<>();
                                while(iterator.hasNext()){
                                    LineFlowReportParam next = iterator.next();
                                    newList.add(next);
                                    if(newList.size() == 100){
                                        terraceManageDao.saveLinkReportDaily(newList);
                                        newList.clear();
                                    }
                                }
                                if(!CollectionUtils.isEmpty(newList)){
                                    terraceManageDao.saveLinkReportDaily(newList);
                                }
                            }
                        }else{
                            List<LineFlowReportParam> nullData = new ArrayList<>();
                            LineFlowReportParam flowReportParam = new LineFlowReportParam();
                            flowReportParam.setSaveTime(longToDate(endTime));
                            flowReportParam.setUpdateSuccess(false);
                            nullData.add(flowReportParam);
                            terraceManageDao.saveLinkReportDaily(nullData);
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

    /**
     * 判断是否需要存储线路流量统计上周数据
     */
    public void manualLinkReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_linkflow_report_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<LineFlowReportParam> lineFlowReportParams = terraceManageDao.selectLinkReportDailyData(lastWeek.get(0), lastWeek.get(1), null);
            if(CollectionUtils.isEmpty(lineFlowReportParams)){
                return;
            }
            //进行数据计算
            Long startTime = lastWeek.get(0).getTime();
            Long endTime = lastWeek.get(1).getTime();
            List<LineFlowReportParam> lineFlowReportWeekData = MWReportHandlerDataLogic.handleLinkReportData(lineFlowReportParams, startTime, endTime,startWeekDate+"~"+endWeekDate);
            if(CollectionUtils.isEmpty(lineFlowReportWeekData)){
                return;
            }
            //插入数据库数据
            int count = terraceManageDao.saveLinkReportWeekly(lineFlowReportWeekData);
            log.error("线路流量统计上周数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("定时缓存线路流量统计报表上周数据失败",e);
        }
    }

    /**
     * 判断是否需要存储线路流量统计上月数据
     */
    public void manualLinkReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_linkflow_report_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库周数据
            List<LineFlowReportParam> lineFlowReportParams = terraceManageDao.selectLinkReportDailyData(lastMonth.get(0), lastMonth.get(1), null);
            if(CollectionUtils.isEmpty(lineFlowReportParams)){
                return;
            }
            //进行数据计算
            Long startTime = lastMonth.get(0).getTime();
            Long endTime = lastMonth.get(1).getTime();
            List<LineFlowReportParam> lineFlowReportWeekData = MWReportHandlerDataLogic.handleLinkReportData(lineFlowReportParams, startTime, endTime,startWeekDate+"~"+endWeekDate);
            if(CollectionUtils.isEmpty(lineFlowReportWeekData)){
                return;
            }
            //插入数据库数据
            int count = terraceManageDao.saveLinkReportMonthly(lineFlowReportWeekData);
            log.error("线路流量统计上月数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("定时缓存线路流量统计报表上月数据失败",e);
        }
    }




    /**
     * 缓存CPU与内存报表天级数据
     */
    public void manualCpuAndMemoryReportDailyDataCache() {
        try {
            log.info("判断CPU与内存情况报表数据是否需要删除");
            checkDelDailyData("mw_cpuandmemory_daily");
            log.info("开始进行CPU与内存情况报表定时缓存数据" + new Date());
            //数据库中已有的数据时间
            PageHelper.startPage(0, 100000000);
            List<Date> dates = terraceManageDao.selectReportDate("mw_cpuandmemory_daily");
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dates)){
                for (Date dateStr : dates) {
                    exists.add(formatDate(dateStr));
                }
            }
//            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            //如果初始化天数为0，默认为60天
            if(initializeDay == 0){
                initializeDay = 60;
            }
            Long  time = toDayStartTime();
            for (int i = 0; i < initializeDay; i++){
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                    continue;
                }

                Reply reply;
                int days = calculatedTrendDay(startTime);
                log.info("CPU报表定时任务执行 MwManualTimeTaskRun"+days+":::"+trendDay);
                if(days > trendDay){
                    reply = trendHandler.getCpuMemTrendInfo(startTime, endTime);
                }else{
                    RunTimeQueryParam param = new RunTimeQueryParam();
                    param.setDateType(20);
                    param.setTimingType(true);
                    param.setReportType(1);
                    List<String> chooseTime = getChooseTime(startTime, endTime);
                    param.setChooseTime(chooseTime);
                    reply =terraceManageService.selectReportCPUNews(param);
                }
                if (reply != null && CollectionUtils.isNotEmpty((Collection<?>) reply.getData())) {
                    //CPU与内存数据
                    List<RunTimeItemValue> retDatas = (List<RunTimeItemValue>) reply.getData();
                    if (!CollectionUtils.isEmpty(retDatas)) {
                        for (RunTimeItemValue retData : retDatas) {
                            retData.setSaveTime(longToDate(endTime));
                            retData.setUpdateSuccess(true);
                        }
                    }
                    //数据进行数据库缓存
                    if(!CollectionUtils.isEmpty(retDatas)){
                        log.info("添加CPU与内存情况缓存数据" + new Date());
                        int count = terraceManageDao.saveCpuAndMemoryReportDaily(retDatas);
                        log.info("添加CPU与内存情况缓存数据成功。条数为：" + count + new Date());
                    }
                }else{
                    List<RunTimeItemValue> nullData = new ArrayList<>();
                    RunTimeItemValue runTimeItemValue = new RunTimeItemValue();
                    runTimeItemValue.setSaveTime(longToDate(endTime));
                    runTimeItemValue.setUpdateSuccess(false);
                    nullData.add(runTimeItemValue);
                    int count = terraceManageDao.saveCpuAndMemoryReportDaily(nullData);
                }
            }
        } catch (Exception e) {
            log.error("定时缓存CPU与内存情况报表数据失败" + e.getMessage());
        }
    }

    /**
     * 判断是否需要存储CPU与内存上周数据
     */
    public void manualCpuAndMemoryReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_cpuandmemory_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<RunTimeItemValue> lineFlowReportParams = terraceManageDao.selectCpuAndMemoryReportDailyData(lastWeek.get(0), lastWeek.get(1), null);
            if(CollectionUtils.isEmpty(lineFlowReportParams)){
                return;
            }
            //进行数据计算
            Long startTime = lastWeek.get(0).getTime();
            Long endTime = lastWeek.get(1).getTime();
            List<RunTimeItemValue> runTimeItemValues = MWReportHandlerDataLogic.handleCpuAndMomeryReportData(lineFlowReportParams, startTime, endTime);
            if(CollectionUtils.isEmpty(runTimeItemValues)){
                return;
            }
            String weekDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveCpuAndMemoryReportWeekly(runTimeItemValues,weekDate);
            log.error("CPU与内存报表上周数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("CPU与内存报表上周数据添加失败",e);
        }
    }

    /**
     * 判断是否需要存储CPU与内存上月数据
     */
    public void manualCpuAndMemoryReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_cpuandmemory_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库月数据
            List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectCpuAndMemoryReportDailyData(lastMonth.get(0), lastMonth.get(1), null);
            if(CollectionUtils.isEmpty(runTimeItemValues)){
                return;
            }
            //进行数据计算
            Long startTime = lastMonth.get(0).getTime();
            Long endTime = lastMonth.get(1).getTime();
            List<RunTimeItemValue> monthValues = MWReportHandlerDataLogic.handleCpuAndMomeryReportData(runTimeItemValues, startTime, endTime);
            if(CollectionUtils.isEmpty(monthValues)){
                return;
            }
            String monthDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveCpuAndMemoryReportMonthly(monthValues,monthDate);
            log.error("CPU与内存报表上月数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("CPU与内存报表上月数据添加成功失败",e);
        }
    }

    /**
     * 缓存磁盘使用情况报表天级数据
     */
    public void manualDiskUseReportDailyDataCache(){
        try {
            log.info("判断磁盘使用情况报表数据是否需要删除");
            checkDelDailyData("mw_diskuse_daily");
            log.info("开始进行磁盘使用情况报表定时缓存数据" + new Date());
            //数据库中已有的数据时间
            PageHelper.startPage(0, 100000000);
            List<Date> dates = terraceManageDao.selectReportDate("mw_diskuse_daily");
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dates)){
                for (Date dateStr : dates) {
                    exists.add(formatDate(dateStr));
                }
            }
//            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            //如果初始化天数为0，默认为60天
            if(initializeDay == 0){
                initializeDay = 60;
            }
            Long  time = toDayStartTime();
            for (int i = 0; i < initializeDay; i++){
                Long k = 86400000l*(i+1);
                Long startTime = time - k;
                Long v = 86400000l*i;
                Long endTime = time -1000-v;
                if(!CollectionUtils.isEmpty(exists) && exists.contains(formatDate(longToDate(startTime)))){
                    continue;
                }
                TrendParam param = new TrendParam();
                param.setDateType(20);
                param.setTimingType(true);
                List<String> chooseTime = getChooseTime(startTime, endTime);
                param.setChooseTime(chooseTime);
                Reply reply = terraceManageService.selectReportDiskUse(param);
                if (reply != null && reply.getData() != null) {
                    //CPU与内存数据
                    PageInfo pageInfo = (PageInfo) reply.getData();
                    if(pageInfo == null){
                        continue;
                    }
                    List<TrendDiskDto> dtos = pageInfo.getList();
                    if (!CollectionUtils.isEmpty(dtos)) {
                        for (TrendDiskDto retData : dtos) {
                            retData.setSaveTime(longToDate(endTime));
                            retData.setUpdateSuccess(true);
                        }
                    }
                    //数据进行数据库缓存
                    if(!CollectionUtils.isEmpty(dtos)){
                        log.info("添加磁盘使用情况情况缓存数据" + new Date());
                        int count = terraceManageDao.saveDiskUseReportDaily(dtos);
                        log.info("添加磁盘使用情况情况缓存数据成功。条数为：" + count + new Date());
                    }
                }else{
                    List<TrendDiskDto> nullData = new ArrayList<>();
                    TrendDiskDto diskDto = new TrendDiskDto();
                    diskDto.setSaveTime(longToDate(endTime));
                    diskDto.setUpdateSuccess(false);
                    nullData.add(diskDto);
                    int count = terraceManageDao.saveDiskUseReportDaily(nullData);
                }
            }
        } catch (Exception e) {
            log.error("定时缓存磁盘使用情况情况报表数据失败" + e.getMessage());
        }
    }

    /**
     * 判断是否需要存储磁盘使用情况上周数据
     */
    public void manualDiskUseReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_diskuse_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<TrendDiskDto> diskDtos = terraceManageDao.selectDiskUseReportDailyData(lastWeek.get(0), lastWeek.get(1), null);
            if(CollectionUtils.isEmpty(diskDtos)){
                return;
            }
            //进行数据计算
            Long startTime = lastWeek.get(0).getTime();
            Long endTime = lastWeek.get(1).getTime();
            List<TrendDiskDto> weekDiskDtos = MWReportHandlerDataLogic.handleDiskUseReportData(diskDtos, startTime, endTime);
            if(CollectionUtils.isEmpty(weekDiskDtos)){
                return;
            }
            String weekDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveDiskUseReportWeekly(weekDiskDtos,weekDate);
            log.error("磁盘使用情况报表上周数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("磁盘使用情况报表上周数据添加失败",e);
        }
    }

    /**
     * 判断是否需要存储磁盘使用情况上月数据
     */
    public void manualDiskUseReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_diskuse_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库月数据
            List<TrendDiskDto> diskDtos = terraceManageDao.selectDiskUseReportDailyData(lastMonth.get(0), lastMonth.get(1), null);
            if(CollectionUtils.isEmpty(diskDtos)){
                return;
            }
            //进行数据计算
            Long startTime = lastMonth.get(0).getTime();
            Long endTime = lastMonth.get(1).getTime();
            List<TrendDiskDto> monthDiskDtos = MWReportHandlerDataLogic.handleDiskUseReportData(diskDtos, startTime, endTime);
            if(CollectionUtils.isEmpty(monthDiskDtos)){
                return;
            }
            String monthDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveDiskUseReportMonthly(monthDiskDtos,monthDate);
            log.error("磁盘使用情况报表上月数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("磁盘使用情况报表上月数据添加成功失败",e);
        }
    }



    /**
     * 缓存资产可用性报表天数据
     */
    public void manualAssetUsabilityReportDailyDataCache() {
        try {
            log.info("判断资产可用性报表数据是否需要删除");
            checkDelDailyData("mw_assetsusability_daily");
            log.info("开始进行资产可用性报表定时缓存数据" + new Date());
            Integer dateType = 12;
            String StartTime = getStratDaysOfMonth(new Date(), 0, -1, 0);
            PageHelper.startPage(0, 100000000);
            List<Date> perforDaysCheck = terraceManageDao.selectReportDate("mw_assetsusability_daily");
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(perforDaysCheck)){
                for (Date dateStr : perforDaysCheck) {
                    exists.add(formatDate(dateStr));
                }
            }
            //取于自定义
//            Integer performDays = getDaysOfMonth(new Date(), 0, -1, 0);
//            if (perforDaysCheck.size() + 1 == performDays) {
//                performDays = 1;
//            }
            //如果初始化天数为0，默认为60天
            if(initializeDay == 0){
                initializeDay = 60;
            }

            Long time = toDayStartTime();
            for (int k = 0; k < initializeDay; k++) {
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
                            retData.setSaveTime(longToDate(startTime));
                            retData.setUpdateSuccess(true);

                        }
                    }
                    if (CollectionUtils.isEmpty(retDatas)) {
                        continue;
                    }
                    cacheDatas.addAll(retDatas);
                    if (!CollectionUtils.isEmpty(cacheDatas)) {
                        int count = terraceManageDao.saveAssetUsabilityReportDaily(cacheDatas);
                    }
                }
                // 增加空数据 证明zabbix数据为空
                else {
                    MwAssetsUsabilityParam mwAssetsUsabilityParam = new MwAssetsUsabilityParam();
                    mwAssetsUsabilityParam.setSaveTime(longToDate(startTime));
                    mwAssetsUsabilityParam.setUpdateSuccess(false);
                    cacheDatas.add(mwAssetsUsabilityParam);
                    int count = terraceManageDao.saveAssetUsabilityReportDaily(cacheDatas);
                }
            }
            log.error("自动化存储资产可用性报表完成");
        } catch (Exception e) {
            log.error("定时缓存资产可用性报表数据失败" + e.getMessage());
        }
    }


    /**
     * 判断是否需要资产可用性上周数据
     */
    public void manualAssetUsabilityReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_assetsusability_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<MwAssetsUsabilityParam> usabilityParams = terraceManageDao.selectAssetsUsabilityDailyData(lastWeek.get(0), lastWeek.get(1), null);
            if(CollectionUtils.isEmpty(usabilityParams)){
                return;
            }
            //进行数据计算
            Long startTime = lastWeek.get(0).getTime();
            Long endTime = lastWeek.get(1).getTime();
            List<MwAssetsUsabilityParam> weekUsabilityParams = MWReportHandlerDataLogic.handleAssetUsabilityReportData(usabilityParams, startTime, endTime);
            if(CollectionUtils.isEmpty(weekUsabilityParams)){
                return;
            }
            String weekDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveAssetUsabilityReportWeekly(weekUsabilityParams,weekDate);
            log.error("资产可用性报表上周数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("资产可用性报表上周数据添加失败",e);
        }
    }

    /**
     * 判断是否需要存储资产可用性上月数据
     */
    public void manualAssetUsabilityReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_assetsusability_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库月数据
            List<MwAssetsUsabilityParam> usabilityParams = terraceManageDao.selectAssetsUsabilityDailyData(lastMonth.get(0), lastMonth.get(1), null);
            if(CollectionUtils.isEmpty(usabilityParams)){
                return;
            }
            //进行数据计算
            Long startTime = lastMonth.get(0).getTime();
            Long endTime = lastMonth.get(1).getTime();
            List<MwAssetsUsabilityParam> monthUsabilityParams = MWReportHandlerDataLogic.handleAssetUsabilityReportData(usabilityParams, startTime, endTime);
            if(CollectionUtils.isEmpty(monthUsabilityParams)){
                return;
            }
            String monthDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveAssetUsabilityReportMonthly(monthUsabilityParams,monthDate);
            log.error("资产可用性报表上月数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("资产可用性报表上月数据添加成功失败",e);
        }
    }


    /**
     * 缓存运行状态报表天数据
     */
    public void manualRunStateReportDailyDataCache() {
        log.info("判断运行状态报表数据是否需要删除");
        checkDelDailyData("mw_runstate_daily");
        log.info("开始进行运行状态报表的数据缓存");
        Integer dateType = DateTimeTypeEnum.USER_DEFINED.getCode();
        String StartTime = getStratDaysOfMonth(new Date(), 0, -1, 0);
        PageHelper.startPage(0, 100000000);
        List<Date> perforDaysCheck = terraceManageDao.selectReportDate("mw_runstate_daily");
        List<String> exists = new ArrayList<>();
        if(!CollectionUtils.isEmpty(perforDaysCheck)){
            for (Date dateStr : perforDaysCheck) {
                exists.add(formatDate(dateStr));
            }
        }
        //取于自定义
//        Integer performDays = getDaysOfMonth(new Date(), 0, -1, 0);
//        if (perforDaysCheck.size() + 1 == performDays) {
//            performDays = 1;
//        }
        //如果初始化天数为0，默认为60天
        if(initializeDay == 0){
            initializeDay = 60;
        }

        Long time = toDayStartTime();
        for (int k = 0; k < initializeDay; k++) {
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


    /**
     * 判断是否需要存储运行状态报表上周数据
     */
    public void manualRunStateReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_runstate_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<RunTimeItemValue> itemValues = terraceManageDao.selectRunStateDailyData(lastWeek.get(0), lastWeek.get(1));
            if(CollectionUtils.isEmpty(itemValues)){
                return;
            }
            List<RunTimeItemValue> runTimeItemValueList = MWReportHandlerDataLogic.handleRunStateReportData(itemValues);
            if(CollectionUtils.isEmpty(runTimeItemValueList)){
                return;
            }
            String weekDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveRunStateReportWeekly(runTimeItemValueList,weekDate);
            log.error("运行状态报表上周数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("运行状态报表上周数据添加失败",e);
        }
    }


    /**
     * 判断是否需要存储运行状态报表上月数据
     */
    public void manualRunStateReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_runstate_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库月数据
            List<RunTimeItemValue> runTimeItemValues = terraceManageDao.selectRunStateDailyData(lastMonth.get(0), lastMonth.get(1));
            if(CollectionUtils.isEmpty(runTimeItemValues)){
                return;
            }
            List<RunTimeItemValue> runTimeItemValueList = MWReportHandlerDataLogic.handleRunStateReportData(runTimeItemValues);
            if(CollectionUtils.isEmpty(runTimeItemValueList)){
                return;
            }
            String monthDate = startWeekDate+"~"+endWeekDate;
            //插入数据库数据
            int count = terraceManageDao.saveRunStateReportMonthly(runTimeItemValueList,monthDate);
            log.error("运行状态报表上月数据添加成功，成功条数"+count+new Date());
        }catch (Exception e){
            log.error("运行状态报表上月数据添加成功失败",e);
        }
    }



    /**
     * 缓存MPLS报告天数据
     */
    public void manualMplsHistoryReportDailyDataCache(){
        try {
            log.info("判断MPLS历史数据是否需要删除");
            checkDelDailyData("mw_mplshistory_daily");
            log.info("MPLS历史数据数据每天存储开始"+new Date());
            PageHelper.startPage(0, 100000000);
            List<Date> dateStrs = terraceManageDao.selectReportDate("mw_mplshistory_daily");
            List<String> exists = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dateStrs)){
                for (Date dateStr : dateStrs) {
                    exists.add(formatDate(dateStr));
                }
            }
//            //按照日期每天存数据
//            int daysOfMonth = getDaysOfMonth(new Date(), 0, -1, 0);
            //如果初始化天数为0，默认为60天
            if(initializeDay == 0){
                initializeDay = 60;
            }
            Long  time = toDayStartTime();
            //查询所有线路数据
            List<NetWorkLinkDto> linkData = getLinkData();
            for (int i = 0; i < initializeDay; i++) {
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
                            if(linkDatum.getRootAssetsParam().getMonitorServerId() == null)continue;
                            dto.setMonitorServerId(linkDatum.getRootAssetsParam().getMonitorServerId());
                        }
                        if(StringUtils.isNotBlank(valuePort) && "TARGET".equals(valuePort)){
                            rootPort.add("["+linkDatum.getTargetPort()+"]MW_INTERFACE_IN_TRAFFIC");
                            rootPort.add("["+linkDatum.getTargetPort()+"]MW_INTERFACE_OUT_TRAFFIC");
                            if(linkDatum.getTargetAssetsParam().getMonitorServerId() == null)continue;
                            dto.setMonitorServerId(linkDatum.getTargetAssetsParam().getMonitorServerId());
                        }
                        dto.setName(rootPort);
                        dto.setValueType("AVG");
                        log.info("param={"+dto+"}");
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
                                                                sortAcceptData.add(dateFormat.format(dateTime)+","+lastValue+","+unitByReal);
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
                            terraceManageDao.saveMplsHistoryReportDaily(sendStr,acceptStr,longToDate(endTime),linkDatum.getLinkName(),sortSendStr,sortAcceptStr,true);
                        }else{
                            terraceManageDao.saveMplsHistoryReportDaily("","", longToDate(endTime),linkDatum.getLinkName(),"","",false);
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
     * 判断是否需要存储MPLS报表上周数据
     */
    public void manualMplsHistoryReportWeeklyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_mplshistory_weekly");
            //获取上周时间
            List<Date> lastWeek = ReportDateUtil.getLastWeek();
            String startWeekDate = this.format.format(lastWeek.get(0));
            String endWeekDate = this.format.format(lastWeek.get(1));
            //查询数据库周数据
            List<Map<String, String>> maps = terraceManageDao.selectMplsLineHistoryDailyData(lastWeek.get(0), lastWeek.get(1),null);
            //将相同的线路进行分组
            Map<String,List<String>> sendMap = new HashMap<>();
            Map<String,List<String>> acceptMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(maps)){
                for (Map<String, String> map : maps) {
                    String linkName = map.get("linkName");
                    String sendData = map.get("sendData");
                    String acceptData = map.get("acceptData");
                    if(sendMap.isEmpty() || sendMap.get(linkName) == null){
                        if(StringUtils.isNotBlank(sendData)){
                            List<String> ls = JSON.parseObject(sendData, List.class);
                            sendMap.put(linkName,ls);
                        }
                        if(acceptMap.isEmpty() || acceptMap.get(linkName) == null){
                            if(StringUtils.isNotBlank(acceptData)){
                                List<String> ls = JSON.parseObject(acceptData, List.class);
                                acceptMap.put(linkName,ls);
                            }
                        }
                        continue;
                    }
                    if(!sendMap.isEmpty() && sendMap.get(linkName) != null){
                        if(StringUtils.isNotBlank(sendData)){
                            List<String> sends = sendMap.get(linkName);
                            List<String> ls = JSON.parseObject(sendData, List.class);
                            sends.addAll(ls);
                            sendMap.put(linkName,sends);
                        }
                        if(!acceptMap.isEmpty() && acceptMap.get(linkName) != null){
                            if(StringUtils.isNotBlank(acceptData)){
                                List<String> accepts = acceptMap.get(linkName);
                                List<String> ls = JSON.parseObject(acceptData, List.class);
                                accepts.addAll(ls);
                                acceptMap.put(linkName,accepts);
                            }
                        }
                    }
                }
            }
            if(sendMap.isEmpty() || acceptMap.isEmpty()){
                return;
            }
            List<MWMplsCacheDataDto> realData = new ArrayList<>();
            String weekDate = startWeekDate+"~"+endWeekDate;
            for (String key : sendMap.keySet()) {
                List<String> sends = sendMap.get(key);
                List<String> accepts = acceptMap.get(key);
                if(CollectionUtils.isEmpty(sends) || CollectionUtils.isEmpty(accepts))continue;
                if(sends.size() == accepts.size()){
                    for (int i = 0; i < sends.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setWeekData(weekDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
                if(sends.size() > accepts.size()){
                    for (int i = 0; i < accepts.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setWeekData(weekDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
                if(sends.size() < accepts.size()){
                    for (int i = 0; i < sends.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setWeekData(weekDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
            }
            if(CollectionUtils.isEmpty(realData))return;
            //进行周数据添加
            if(!CollectionUtils.isEmpty(realData)){
                Iterator<MWMplsCacheDataDto> iterator = realData.iterator();
                List<MWMplsCacheDataDto> newList = new ArrayList<>();
                while(iterator.hasNext()){
                    MWMplsCacheDataDto next = iterator.next();
                    newList.add(next);
                    if(newList.size() == 100){
                        terraceManageDao.saveMplsHistoryReportWeekly(newList);
                        newList.clear();
                    }
                }
                if(!CollectionUtils.isEmpty(newList)){
                    terraceManageDao.saveMplsHistoryReportWeekly(newList);
                }
            }
            log.info("MPLS报表周数据添加成功");
        }catch (Exception e){
            log.error("MPLS报表上周数据添加失败",e);
        }
    }


    /**
     * 判断是否需要存储MPLS报表上月数据
     */
    public void manualMplsHistoryReportMonthlyDataCache(){
        try {
            terraceManageDao.deleteReportCacheData("mw_mplshistory_monthly");
            //获取上月时间
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            String startWeekDate = this.format.format(lastMonth.get(0));
            String endWeekDate = this.format.format(lastMonth.get(1));
            //查询数据库月数据
            List<Map<String, String>> maps = terraceManageDao.selectMplsLineHistoryDailyData(lastMonth.get(0), lastMonth.get(1),null);
            //将相同的线路进行分组
            Map<String,List<String>> sendMap = new HashMap<>();
            Map<String,List<String>> acceptMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(maps)){
                for (Map<String, String> map : maps) {
                    String linkName = map.get("linkName");
                    String sendData = map.get("sendData");
                    String acceptData = map.get("acceptData");
                    if(sendMap.isEmpty() || sendMap.get(linkName) == null){
                        if(StringUtils.isNotBlank(sendData)){
                            List<String> ls = JSON.parseObject(sendData, List.class);
                            sendMap.put(linkName,ls);
                        }
                        if(acceptMap.isEmpty() || acceptMap.get(linkName) == null){
                            if(StringUtils.isNotBlank(acceptData)){
                                List<String> ls = JSON.parseObject(acceptData, List.class);
                                acceptMap.put(linkName,ls);
                            }
                        }
                        continue;
                    }
                    if(!sendMap.isEmpty() && sendMap.get(linkName) != null){
                        if(StringUtils.isNotBlank(sendData)){
                            List<String> sends = sendMap.get(linkName);
                            List<String> ls = JSON.parseObject(sendData, List.class);
                            sends.addAll(ls);
                            sendMap.put(linkName,sends);
                        }
                        if(!acceptMap.isEmpty() && acceptMap.get(linkName) != null){
                            if(StringUtils.isNotBlank(acceptData)){
                                List<String> accepts = acceptMap.get(linkName);
                                List<String> ls = JSON.parseObject(acceptData, List.class);
                                accepts.addAll(ls);
                                acceptMap.put(linkName,accepts);
                            }
                        }
                    }

                }
            }
            if(sendMap.isEmpty() || acceptMap.isEmpty()){
                return;
            }
            List<MWMplsCacheDataDto> realData = new ArrayList<>();
            String monthDate = startWeekDate+"~"+endWeekDate;
            for (String key : sendMap.keySet()) {
                List<String> sends = sendMap.get(key);
                List<String> accepts = acceptMap.get(key);
                if(CollectionUtils.isEmpty(sends) || CollectionUtils.isEmpty(accepts))continue;
                if(sends.size() == accepts.size()){
                    for (int i = 0; i < sends.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setMonthDate(monthDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
                if(sends.size() > accepts.size()){
                    for (int i = 0; i < accepts.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setMonthDate(monthDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
                if(sends.size() < accepts.size()){
                    for (int i = 0; i < sends.size(); i++) {
                        MWMplsCacheDataDto dataDto = new MWMplsCacheDataDto();
                        dataDto.setLinkName(key);
                        String strSend = sends.get(i);
                        String strAccept = accepts.get(i);
                        dataDto.setSend(strSend);
                        dataDto.setAccept(strAccept);
                        dataDto.setMonthDate(monthDate);
                        dataDto.setUpdateSuccess(true);
                        realData.add(dataDto);
                    }
                }
            }
            if(CollectionUtils.isEmpty(realData))return;
            //进行周数据添加
            if(!CollectionUtils.isEmpty(realData)){
                Iterator<MWMplsCacheDataDto> iterator = realData.iterator();
                List<MWMplsCacheDataDto> newList = new ArrayList<>();
                while(iterator.hasNext()){
                    MWMplsCacheDataDto next = iterator.next();
                    newList.add(next);
                    if(newList.size() == 100){
                        terraceManageDao.saveMplsHistoryReportMonthly(newList);
                        newList.clear();
                    }
                }
                if(!CollectionUtils.isEmpty(newList)){
                    terraceManageDao.saveMplsHistoryReportMonthly(newList);
                }
            }
            log.info("MPLS报表月数据添加成功");
        }catch (Exception e){
            log.error("MPLS报表上月数据添加成功失败",e);
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
            if(!CollectionUtils.isEmpty(list)){
                return list;
            }
        }
        return null;
    }


    private String formatDate(Date date){
        return format.format(date);
    }

    /**
     * 判断当前是否是月第一天
     * @return
     */
    private boolean isFirstDayOfMonth(){
        Date currDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
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

    /**
     * 根据配置文件判断是否需要删除数据库数据
     * @param tableName
     */
    public void checkDelDailyData(String tableName){
        //查询数据库所有时间，并排序
        List<Date> dateList = terraceManageDao.selectReportDate(tableName);
       /* if(maxDay != 0 && dateList != null && dateList.size() > maxDay){
            //说明数据库数据时间超市最大时间，需要进行数据删除
            List<Date> delList = dateList.subList(maxDay, dateList.size());
            //删除数据
            if(CollectionUtils.isEmpty(delList))return;
            terraceManageDao.deleteReportDailyData(tableName,delList);
        }*/
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
