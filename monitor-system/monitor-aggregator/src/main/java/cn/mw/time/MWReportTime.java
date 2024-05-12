package cn.mw.time;

import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.link.dao.MWNetWorkLinkDao;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDtos;
import cn.mw.monitor.report.param.ReportBase;
import cn.mw.monitor.report.param.ReportMessageMapperParam;
import cn.mw.monitor.report.param.ReportUserParam;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.solarwind.dao.MwMonitorSolarReportDao;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.service.MwEmailManageService;
import cn.mw.monitor.weixin.entity.AlertRecordTable;
import cn.mw.monitor.weixin.service.EmailService;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xhy
 * @date 2020/7/9 16:06
 * 报表定时器
 */
@Component
@Slf4j
public class MWReportTime {

    @Value("${report.excelPath}")
    private String path;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MwReportService mwReportService;

    @Resource
    private MwReportDao mwReportDao;

    @Resource
    private MwMonitorSolarReportDao mwMonitorSolarReportDao;
    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;
    @Resource
    private MwEmailManageService mwEmailManage;
    @Autowired
    private EmailService emailService;

//    @Async
//    @Scheduled(cron = "0 0 18 * * ?")
    public void reportEmail() {
        List<MwReportTable> reports = mwReportDao.selectOneDayReport();
        for (MwReportTable report : reports) {
            int id = Integer.parseInt(report.getReportId());
            switch (id) {
                //性能报表
                case 1:
                    dealXnRepoet(report, 1);
                    break;
                //磁盘使用率报表
                case 2:
                    dealXnRepoet(report, 2);
                    break;
                //网络性能统计报表
                case 3:
                    dealXnRepoet(report, 3);
                    break;
                //线路流量报表
                case 4:
                    //System.err.println();
                    break;
                default:
                    break;
            }
        }
    }

    //性能报表
    private void dealXnRepoet(MwReportTable report, int switchType) {
        //查询发送方信息
        List<ReportMessageMapperParam> fromMappers = mwReportDao.selectMessageMapperReport(report.getReportId());
        for (ReportMessageMapperParam fromMapper : fromMappers) {
            //发送方式
            String type = fromMapper.getRuleType();
            switch (type) {
                //推送方式
                case "1":
                    break;
                //微信方式
                case "2":
                    break;
                //邮件方式
                case "3":
                    sendXnEmailReport(fromMapper, report, switchType);
                    break;
                default:
                    break;
            }
        }
    }

    //发送性能报表--邮件方式
    private void sendXnEmailReport(ReportMessageMapperParam fromMapper, MwReportTable report, int switchType) {
        //1 查询邮件发送方信息
        EmailFrom from = mwEmailManage.selectEmailFrom(fromMapper.getRuleId());

        //2 查询接收人（每个人对应看到的资产的信息不一样，该信息邮件单个处理数据，不使用群发）
        List<UserDTO> users = report.getPrincipal();
        List<ReportUserParam> receives = mwReportDao.selectPriLists(userDTOToInteger(users));

        //3 根据接收人查询可以看到的报表数据分别发送
        for (ReportUserParam receive : receives) {
            String email = receive.getEmail();
            if (email != null && !email.equals("")) {
                String pathName = "";
                String message = "";
                if (switchType == 1) {
                    //根据负责人（接收人）查询报表
                    List<CpuAndMemoryDto> datas = (List<CpuAndMemoryDto>) xnReport(receive, switchType);

                    //将查询出来的资产导出成excel
                    pathName = exporteXCEL(datas, switchType);

                    //用邮件发送excel
                    message = emailService.sendReportEmail(receive.getEmail(), "性能报表", from, pathName);

                    //保存成功的历史记录
                    saveRecord(message, pathName, receive.getLoginName(), "性能报表");
                } else if (switchType == 2) {
                    //根据负责人（接收人）查询报表
                    List<TrendDiskDto> datas = (List<TrendDiskDto>) xnReport(receive, switchType);

                    //将查询出来的资产导出成excel
                    pathName = exporteXCEL(datas, switchType);

                    //用邮件发送excel
                    message = emailService.sendReportEmail(receive.getEmail(), "磁盘使用率报表", from, pathName);

                    //保存成功的历史记录
                    saveRecord(message, pathName, receive.getLoginName(), "磁盘使用率报表");
                } else if (switchType == 3) {
                    //根据负责人（接收人）查询报表
                    List<TrendNetDto> datas = (List<TrendNetDto>) xnReport(receive, switchType);

                    //将查询出来的资产导出成excel
                    pathName = exporteXCEL(datas, switchType);

                    //用邮件发送excel
                    message = emailService.sendReportEmail(receive.getEmail(), "网络性能统计报表", from, pathName);

                    //保存成功的历史记录
                    saveRecord(message, pathName, receive.getLoginName(), "网络性能统计报表");
                }
            }
        }

    }

    //保存发送记录
    private void saveRecord(String message, String pathName, String to, String switchName) {
        if (message.equals("success")) {
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod(switchName);
            recored.setText("接收人:" + to + ",附件地址：" + pathName);
            recored.setIsSuccess(0);
            recored.setHostid("");
            //mwWeixinTemplateDao.insertRecord(recored);
        } else {
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod(switchName);
            recored.setText("接收人:" + to + ",附件地址：" + pathName);
            recored.setIsSuccess(1);
            recored.setHostid("");
            //mwWeixinTemplateDao.insertRecord(recored);
        }
    }

    private String exporteXCEL(Object datas, int switchType) {
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;

        if (switchType == 1) {
            //2字段显示
            Set<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("cpuFreeRage");
            includeColumnFiledNames.add("memoryFreeRage");
            includeColumnFiledNames.add("cpuMaxValue");
            includeColumnFiledNames.add("cpuMinValue");
            includeColumnFiledNames.add("cpuAvgValue");
            includeColumnFiledNames.add("memoryMaxValue");
            includeColumnFiledNames.add("memoryMinValue");
            includeColumnFiledNames.add("memoryAvgValue");

            //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<CpuAndMemoryDto>> list = getSubLists((List<CpuAndMemoryDto>) datas, 50000);

            //4创建easyExcel写出对象
            ExcelWriter excelWriter = EasyExcel.write(pathName, CpuAndMemoryDto.class).build();

            //5计算sheet分页
            Integer sheetNum = list.size() % 50000 == 0 ? list.size() / 50000 : list.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(list.get(i), sheet);
            }

            //6导出
            excelWriter.finish();
        } else if (switchType == 2) {
            //2字段显示
            Set<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("typeName");
            includeColumnFiledNames.add("diskTotal");
            includeColumnFiledNames.add("diskFree");
            includeColumnFiledNames.add("diskMaxValue");
            includeColumnFiledNames.add("diskMinValue");
            includeColumnFiledNames.add("diskAvgValue");

            //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<TrendDiskDto>> list = getSubLists((List<TrendDiskDto>) datas, 50000);

            //4创建easyExcel写出对象
            ExcelWriter excelWriter = EasyExcel.write(pathName, TrendDiskDto.class).build();

            //5计算sheet分页
            Integer sheetNum = list.size() % 50000 == 0 ? list.size() / 50000 : list.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(list.get(i), sheet);
            }

            //6导出
            excelWriter.finish();
        } else if (switchType == 3) {
            //2字段显示
            Set<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("netName");
            includeColumnFiledNames.add("netInBpsMaxValue");
            includeColumnFiledNames.add("netInBpsMinValue");
            includeColumnFiledNames.add("netInBpsAvgValue");
            includeColumnFiledNames.add("netOutBpsMaxValue");
            includeColumnFiledNames.add("netOutBpsMinValue");
            includeColumnFiledNames.add("netOutBpsAvgValue");

            //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<TrendNetDto>> list = getSubLists((List<TrendNetDto>) datas, 50000);

            //4创建easyExcel写出对象
            ExcelWriter excelWriter = EasyExcel.write(pathName, TrendNetDto.class).build();

            //5计算sheet分页
            Integer sheetNum = list.size() % 50000 == 0 ? list.size() / 50000 : list.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(list.get(i), sheet);
            }

            //6导出
            excelWriter.finish();
        }

        return pathName;
    }


    //将list集合数据按照指定大小分成好几个小的list
    public <T> List<List<T>> getSubLists(List<T> allData, int size) {
        List<List<T>> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }

    public List<Integer> userDTOToInteger(List<UserDTO> users) {
        List<Integer> list = new ArrayList<>();
        users.forEach(user -> {
            list.add(user.getUserId());
        });
        return list;
    }


    /**
     * 每天凌晨三点执行一次
     * 将网络接口数据
     * 分别存入到对应的四张表中
     */
    // @Scheduled(cron = "0 0 3 * * ?")
    public void saveNetWorkReport() {
        log.info(">>>>start>>>saveNetWorkReport>>>>>>>>>>");
        try {
            QueryTangAssetsParam query = new QueryTangAssetsParam();
            // List<Integer> orgIds = mwUserOrgMapperDao.getAllOrgIdByUserId("admin");
            //query.setOrgIds(orgIds);
            query.setIsAdmin(true);
            Map pubCriteria = PropertyUtils.describe(query);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPubList(pubCriteria);
            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
            Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
            Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);

            List<NetWorkDto> allDayNetTrend = mwReportService.getNetTrends(mwTangibleassetsDTOS, startFrom, endTill);

            SolarTimeDto solarTimeDto = mwReportDao.selectTime(2);
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
            Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

            List<NetWorkDto> workNetTrend = mwReportService.getNetTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
            int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日
            Date dateTime = MWUtils.strToDateLong(allDayStartTime);

            if (count == 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                String workDayTableName = "mw_report_network_workday";
                String workDayWorkTimeTableName = "mw_report_network_workday_worktime";
                mwReportDao.insertReportNetWork(workDayTableName, dateTime, allDayNetTrend);
                mwReportDao.insertReportNetWork(workDayWorkTimeTableName, dateTime, workNetTrend);
            }
            String allDayTableName = "mw_report_network_allday";
            String allDayWorkTimeTableName = "mw_report_network_allday_worktime";
            mwReportDao.insertReportNetWork(allDayTableName, dateTime, allDayNetTrend);
            mwReportDao.insertReportNetWork(allDayWorkTimeTableName, dateTime, workNetTrend);
        } catch (Exception e) {
            log.error("MWReportTime_saveNetWorkReport", e.getMessage());
        }
        log.info(">>>>end>>>saveNetWorkReport>>>>>>>>>>");

    }

    /**
     * 每天凌晨三点执行一次
     * 将磁盘的接口数据
     * 分别存入到对应的四张表中
     */
    // @Scheduled(cron = "0 0 3 * * ?")
    public void saveDiskReport() {
        log.info(">>>>start>>>saveDiskReport>>>>>>>>>>");
        try {
            QueryTangAssetsParam query = new QueryTangAssetsParam();
            // List<Integer> orgIds = mwUserOrgMapperDao.getAllOrgIdByUserId("admin");
            //query.setOrgIds(orgIds);
            query.setIsAdmin(true);
            Map pubCriteria = PropertyUtils.describe(query);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPubList(pubCriteria);
            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
            Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
            Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);

            List<DiskDto> diskTrends = mwReportService.getDiskTrends(mwTangibleassetsDTOS, startFrom, endTill);

            SolarTimeDto solarTimeDto = mwReportDao.selectTime(1);
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
            Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

            List<DiskDto> workDiskTrends = mwReportService.getDiskTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
            int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日
            Date dateTime = MWUtils.strToDateLong(allDayStartTime);
            if (count == 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                String workDayTableName = "mw_report_disk_workday";
                String workDayWorkTimeTableName = "mw_report_disk_workday_worktime";
                mwReportDao.insertReportDisk(workDayTableName, dateTime, diskTrends);
                mwReportDao.insertReportDisk(workDayWorkTimeTableName, dateTime, workDiskTrends);
            }
            String allDayTableName = "mw_report_disk_allday";
            String allDayWorkTimeTableName = "mw_report_disk_allday_worktime";
            mwReportDao.insertReportDisk(allDayTableName, dateTime, diskTrends);
            mwReportDao.insertReportDisk(allDayWorkTimeTableName, dateTime, workDiskTrends);
        } catch (Exception e) {
            log.error("MWReportTime_saveNetWorkReport", e.getMessage());
        }
        log.info(">>>>end>>>saveDiskReport>>>>>>>>>>");

    }

    /**
     * 每天凌晨三点执行一次
     * 将cpu和内存的数据
     * 分别存入到对应的四张表中
     */
    //  @Scheduled(cron = "0 0 3 * * ?")
    public void saveCpuAndMemoryReport() {
        log.info(">>>>start>>>saveCpuAndMemoryReport>>>>>>>>>>");
        try {
            QueryTangAssetsParam query = new QueryTangAssetsParam();
            // List<Integer> orgIds = mwUserOrgMapperDao.getAllOrgIdByUserId("admin");
            //query.setOrgIds(orgIds);
            query.setIsAdmin(true);
            Map pubCriteria = PropertyUtils.describe(query);
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPubList(pubCriteria);
            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
            Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
            Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);

            List<CpuAndMemoryDtos> cpuAndMemoryTrends = mwReportService.getCpuAndMemoryTrends(mwTangibleassetsDTOS, startFrom, endTill);

            SolarTimeDto solarTimeDto = mwReportDao.selectTime(3);
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
            Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);

            List<CpuAndMemoryDtos> workCpuAndMemoryTrends = mwReportService.getCpuAndMemoryTrends(mwTangibleassetsDTOS, workStartFrom, workEndTill);

            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
            int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

            Date dateTime = MWUtils.strToDateLong(allDayStartTime);
            if (count == 0 && workCpuAndMemoryTrends.size() > 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                String workDayTableName = "mw_report_cpu_memory_workday";
                String workDayWorkTimeTableName = "mw_report_cpu_memory_workday_worktime";
                mwReportDao.insertReportCpuAndMemory(workDayTableName, dateTime, cpuAndMemoryTrends);
                mwReportDao.insertReportCpuAndMemory(workDayWorkTimeTableName, dateTime, workCpuAndMemoryTrends);
            }
            if (cpuAndMemoryTrends.size() > 0) {
                String allDayTableName = "mw_report_cpu_memory_allday";
                String allDayWorkTimeTableName = "mw_report_cpu_memory_allday_worktime";
                mwReportDao.insertReportCpuAndMemory(allDayTableName, dateTime, cpuAndMemoryTrends);
                mwReportDao.insertReportCpuAndMemory(allDayWorkTimeTableName, dateTime, workCpuAndMemoryTrends);
            }
        } catch (Exception e) {
            log.error("MWReportTime_saveNetWorkReport", e);
        }
        log.info(">>>>end>>>saveCpuAndMemoryReport>>>>>>>>>>");

    }

    @Resource
    private MWNetWorkLinkDao mwNetWorkLinkDao;

    @Value("${report.initializeDay}")
    private int initializeDay;

    /**
     * 初始化执行
     * 将线路的数据
     * 分别存入到对应的四张表中
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveLinkReportInitialize() {
        log.info(">>>>start>>>saveLinkReport>>>>>>>>>>");
        try {
            Long  time = toDayStartTime();
            for (int i = 0; i < initializeDay; i++) {
                List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkDao.getLinkList();
                String allDayStartTime = DateUtils.getFormatDate(time - (86400000l*(i+1)));
                String allDayEndTime = DateUtils.getFormatDate(time - 1000 - (86400000l*i));
                log.info("MWReportTime{} saveLinkReportInitialize() allDayStartTime::"+allDayStartTime+":::allDayEndTime::"+allDayEndTime);
                Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
                Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);
                List<InterfaceReportDtos> alldayLinks = mwReportService.getLinks(netWorkLinkDtos, startFrom, endTill);
                log.info("MWReportTime{} saveLinkReportInitialize() alldayLinks::"+alldayLinks.size());
                String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
                int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日
                Date dateTime = MWUtils.strToDateLong(allDayStartTime);
                if (count == 0) {
                    /**
                     * 工作日
                     * 0-24小时的数据
                     * 8-17（自定义时间段）点的数据
                     */
                    String workDayTableName = "mw_report_link_workday";
                    String workDayWorkTimeTableName = "mw_report_link_workday_worktime";
                    mwReportDao.insertReportLink(workDayTableName, dateTime, alldayLinks);
                    mwReportDao.insertReportLink(workDayWorkTimeTableName, dateTime, alldayLinks);
                }
                if (alldayLinks.size() > 0) {
                    String allDayTableName = "mw_report_link_allday";
                    String allDayWorkTimeTableName = "mw_report_link_allday_worktime";
                    mwReportDao.insertReportLink(allDayTableName, dateTime, alldayLinks);
                    mwReportDao.insertReportLink(allDayWorkTimeTableName, dateTime, alldayLinks);
                }
            }
        } catch (Exception e) {
            log.error("MWReportTime_saveLinkReport", e);
        }
        log.info(">>>>end>>>saveCpuAndMemoryReport>>>>>>>>>>");
    }


    /**
     * 处理历史数据单位
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void handlerHistoryLinkReportUnits() {
        log.info(">>>>start>>>handlerHistoryLinkReportUnits>>>>>>>>>>");
        try {
            String unit = "bps";
           //查询所有数据
            List<InterfaceReportDtos> linkReportHistory = mwReportDao.getLinkReportHistory();
            if(CollectionUtils.isEmpty(linkReportHistory)){return;}
            List<InterfaceReportDtos> newInterfaceDtos = new ArrayList<>();
            for (InterfaceReportDtos interfaceReportDtos : linkReportHistory) {
                InterfaceReportDtos newDto = new InterfaceReportDtos();
                PropertyUtils.copyProperties(newDto,interfaceReportDtos);
                //进行单位转换
                String bandUnit = interfaceReportDtos.getBandUnit();//带宽单位
                if(StringUtils.isBlank(bandUnit)){continue;}
                Double inAveragebps = interfaceReportDtos.getInAveragebps() == null?0:interfaceReportDtos.getInAveragebps();
                newDto.setInAveragebps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(inAveragebps),bandUnit,unit).get("value")));
                Double inMinbps = interfaceReportDtos.getInMinbps() == null?0:interfaceReportDtos.getInMinbps();
                newDto.setInMinbps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(inMinbps),bandUnit,unit).get("value")));
                Double inMaxbps = interfaceReportDtos.getInMaxbps() == null?0:interfaceReportDtos.getInMaxbps();
                newDto.setInMaxbps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(inMaxbps),bandUnit,unit).get("value")));
                Double outMinbps = interfaceReportDtos.getOutMinbps() == null?0:interfaceReportDtos.getOutMinbps();
                newDto.setOutMinbps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(outMinbps),bandUnit,unit).get("value")));
                Double outAveragebps = interfaceReportDtos.getOutAveragebps() == null?0:interfaceReportDtos.getOutAveragebps();
                newDto.setOutAveragebps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(outAveragebps),bandUnit,unit).get("value")));
                Double outMaxbps = interfaceReportDtos.getOutMaxbps() == null?0:interfaceReportDtos.getOutMaxbps();
                newDto.setOutMaxbps(Double.parseDouble(UnitsUtil.getValueMap(String.valueOf(outMaxbps),bandUnit,unit).get("value")));
                newInterfaceDtos.add(newDto);
            }
            if(CollectionUtils.isNotEmpty(newInterfaceDtos)){
                //修改数据库数据
                mwReportDao.deleteLinkReportHistory();
            }
            //分组插入
            List<List<InterfaceReportDtos>> partition = Lists.partition(newInterfaceDtos, 500);
            for (List<InterfaceReportDtos> interfaceReportDtos : partition) {
                mwReportDao.insertLinkReportHistory(interfaceReportDtos);
            }
        } catch (Exception e) {
            log.error("handlerHistoryLinkReportUnits", e);
        }
        log.info(">>>>end>>>handlerHistoryLinkReportUnits>>>>>>>>>>");
    }


    /**
     * 每天凌晨三点执行一次
     * 将线路的数据
     * 分别存入到对应的四张表中
     */
//    @Scheduled(cron = "0 */5 * * * ?")
    public void saveLinkReport() {
        log.info(">>>>start>>>saveLinkReport>>>>>>>>>>");
        try {
            List<NetWorkLinkDto> netWorkLinkDtos = mwNetWorkLinkDao.getLinkList();
            String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
            String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
            Long startFrom = MWUtils.getDate(allDayStartTime, MWDateConstant.NORM_DATETIME);
            Long endTill = MWUtils.getDate(allDayEndTime, MWDateConstant.NORM_DATETIME);
            log.info("MWReportTime{} saveLinkReport() allDayStartTime::"+allDayStartTime+":::allDayEndTime::"+allDayEndTime);
            List<InterfaceReportDtos> alldayLinks = mwReportService.getLinks(netWorkLinkDtos, startFrom, endTill);
            log.info("MWReportTime{} saveLinkReport() alldayLinks::"+alldayLinks.size());
            SolarTimeDto solarTimeDto = mwReportDao.selectTime(ReportBase.LINK.getId());
            String workStartTime = MWUtils.getSolarData(solarTimeDto.getStartHourTime(), solarTimeDto.getStartMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            String workEndTime = MWUtils.getSolarData(solarTimeDto.getEndHourTime(), solarTimeDto.getEndMinuteTime(), 0, MWDateConstant.NORM_DATETIME, -1);
            Long workStartFrom = MWUtils.getDate(workStartTime, MWDateConstant.NORM_DATETIME);
            Long workEndTill = MWUtils.getDate(workEndTime, MWDateConstant.NORM_DATETIME);
            log.info("MWReportTime{} saveLinkReport() workStartTime::"+workStartTime+":::workEndTime::"+workEndTime);
            List<InterfaceReportDtos> workLinks = mwReportService.getLinks(netWorkLinkDtos, workStartFrom, workEndTill);
            log.info("MWReportTime{} saveLinkReport() workLinks::"+workLinks.size());
            String solarData = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);//昨天的日期
            int count = mwMonitorSolarReportDao.selectSolarDayCount(solarData);//判断昨天是不是休息日 0 是工作日  1是休息日

            Date dateTime = MWUtils.strToDateLong(allDayStartTime);
            if (count == 0 && workLinks.size() > 0) {
                /**
                 * 工作日
                 * 0-24小时的数据
                 * 8-17（自定义时间段）点的数据
                 */
                String workDayTableName = "mw_report_link_workday";
                String workDayWorkTimeTableName = "mw_report_link_workday_worktime";
                mwReportDao.insertReportLink(workDayTableName, dateTime, workLinks);
                mwReportDao.insertReportLink(workDayWorkTimeTableName, dateTime, workLinks);
            }
            if (alldayLinks.size() > 0) {
                String allDayTableName = "mw_report_link_allday";
                String allDayWorkTimeTableName = "mw_report_link_allday_worktime";
                mwReportDao.insertReportLink(allDayTableName, dateTime, alldayLinks);
                mwReportDao.insertReportLink(allDayWorkTimeTableName, dateTime, alldayLinks);
            }
        } catch (Exception e) {
            log.error("MWReportTime_saveLinkReport", e);
        }
        log.info(">>>>end>>>saveCpuAndMemoryReport>>>>>>>>>>");

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

    //根据负责人查询报表
    public Object xnReport(ReportUserParam receive, int swithType) {
        try {
            //1:根据负责人查询出资产
            QueryTangAssetsParam query = new QueryTangAssetsParam();
            List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();

            String username = receive.getLoginName();
            Integer userId = receive.getUserId();
            String perm = mwUserOrgCommonService.getRolePermByUserId(userId);
            switch (perm) {
                case "PRIVATE":
                    query.setUserId(userId);
                    Map pubCriteria = PropertyUtils.describe(query);
                    mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPriList(pubCriteria);
                    break;
                case "PUBLIC":
//                    List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(username);
//                    List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(nodes);
                    List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(username);

                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    for (String orgName : orgNames) {
                        if (orgName.equals(MWUtils.ORG_NAME_TOP)) {
                            isAdmin = true;
                            break;
                        }
                    }
                    if (!isAdmin) {
                        // List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(username);
                        // orgIds = mwUserOrgMapperDao.getOrgIdByUserId(username);
                        orgIds = mwOrgCommonService.getOrgIdsByNodes(username);
                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        query.setOrgIds(orgIds);
                    }
                    Map priCriteria = PropertyUtils.describe(query);
                    mwTangibleassetsDTOS = mwTangibleAssetsDao.selectPubList(priCriteria);
                    break;
            }

            //2根据资产查询对应的报表
            Object lists = getReportLists(mwTangibleassetsDTOS, swithType);

            return lists;
        } catch (Throwable e) {
            log.error("根据负责人查询报表错误，错误信息"+e.getMessage());
        }
        return null;
    }

    //获取报表数据
    private Object getReportLists(List<MwTangibleassetsTable> mwTangibleassetsDTOS, int swithType) {
        String allDayStartTime = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATETIME, -1);
        String allDayEndTime = MWUtils.getSolarData(23, 59, 59, MWDateConstant.NORM_DATETIME, -1);
        TrendParam trendParam = new TrendParam();
        trendParam.setReportId(swithType);
        trendParam.setSeniorchecked(true);
        List<String> time = new ArrayList<>();
        time.add(allDayStartTime);
        time.add(allDayEndTime);
        trendParam.setChooseTime(time);
        trendParam.setMwTangibleassetsDTOS(mwTangibleassetsDTOS);
        //性能报表
        if (swithType == 1) {
            List<CpuAndMemoryDto> cpuAndMemoryTrends = mwReportService.getCpuAndMemoryTrend(trendParam);
            return cpuAndMemoryTrends;
            //磁盘使用率报表
        } else if (swithType == 2) {
            List<TrendDiskDto> diskTrends = mwReportService.getDiskTrend(trendParam);
            return diskTrends;
            //网络性能统计报表
        } else if (swithType == 3) {
            List<TrendNetDto> diskTrends = mwReportService.getNetTrend(trendParam);
            return diskTrends;
        }
        return null;
    }


}
