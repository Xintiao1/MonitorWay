package cn.mw.monitor.report.timer;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.report.dao.MwReportDao;
import cn.mw.monitor.report.dao.MwReportTerraceManageDao;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.report.service.MwPatrolInspectionService;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.impl.MWReportHandlerDataLogic;
import cn.mw.monitor.report.service.impl.MwReportTypeEnum;
import cn.mw.monitor.report.util.ReportDateUtil;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.state.RuntimeReportState;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.entity.CustomJavaMailSenderImpl;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.*;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @ClassName MwReportTimeSendEmail
 * @Description 报表配置定时发送邮件
 * @Author gengjb
 * @Date 2022/1/5 19:03
 * @Version 1.0
 **/
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MwReportTimeSendEmail {

    private static final Logger logger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    private MwReportDao mwReportDao;

    @Autowired
    private MwPatrolInspectionService patrolInspectionService;


    @Autowired
    private MwReportTerraceManageDao terraceManageDao;

    @Value("${report.excelPath}")
    private String path;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Value("${report.email.filter.value}")
    private Integer reportEmailFilterValue;

    @Autowired
    private MwReportService mwreportService;

    //模板上传目录
    static final String MODULE = "report-upload";

    @Autowired
    private CustomJavaMailSenderImpl customJavaMailSender;

    @Autowired
    private MwReportTerraceManageService terraceManageService;

    /**
     * 根据报表配置定时向用户发送邮件信息
     * 目前只支持邮件方式发送
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @MwPermit(moduleName = "report_manage")
    public TimeTaskRresult reportSendEmail(String id,String name){
        log.info("邮件开始进行发送操作"+name+":"+id);
        TimeTaskRresult result = new TimeTaskRresult();
        //查询报表配置定时发送
        int reportId = Integer.parseInt(id);
        List<MwReportTable> reports = mwReportDao.selectTimeData(reportId);
        log.info("进行定时邮件发送操作"+reports+":"+id);
        //获取需要发送邮件的用户信息
        getSendEmailUser(reports);
        if(CollectionUtils.isEmpty(reports))return result;
        for (MwReportTable reportData : reports) {
            String reportName = reportData.getReportName();//报表名称
            String timeTag = reportData.getTimeTag();
            int dateType = checkDateType(name);
            if(dateType == 12)continue;
            //查询规则信息
            List<ReportMessageMapperParam> fromMappers = mwReportDao.selectMessageMapperReport(reportData.getReportId());
            //根据报表名称判断查询数据
            MwReportTypeEnum reportType = MwReportTypeEnum.getReportType(reportData.getReportId());
            for (ReportMessageMapperParam fromMapper : fromMappers) {
                result = startSendEmail(fromMapper,reportData,reportType,dateType);
            }
        }
        log.info("定时发送邮件数据返回："+result);
       return result;
    }

    /**
     * 开始构建邮件进行发送
     * @param fromMapper 报表发送规则信息
     * @param report 报表信息
     * @param reportType 报表类型
     */
    private TimeTaskRresult startSendEmail(ReportMessageMapperParam fromMapper, MwReportTable report,  MwReportTypeEnum reportType,int dateType){
        log.info("进行定时邮件发送操作2"+fromMapper+":"+report);
        //1 查询邮件发送方信息
        EmailFrom from = mwReportDao.selectEmailFromReport(fromMapper.getRuleId());
        TimeTaskRresult result = new TimeTaskRresult();
        //2 查询接收人
        List<UserDTO> users = report.getPrincipal();
        List<ReportUserParam> receives = mwReportDao.selectPriLists(userDTOToInteger(users));
        List<String> pathNames = new ArrayList<>();
        log.info("进行定时邮件发送操作3"+receives);
        if(CollectionUtils.isEmpty(receives))return result;
        for (ReportUserParam receive : receives) {
            String email = receive.getEmail();
            if(StringUtils.isBlank(email))continue;
            String pathName = "";
            String message = "";
            switch (reportType){
                case LINK_REPORT:
                    //流量报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = linkReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"流量统计报表",from,pathNames);
                    //保存成功的历史记录
                    result = saveRecord(message, pathName, receive.getLoginName(), "流量统计报表");
                    break;
                case CPUMEMORY_REPORT:
                    //CPU报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = cpuAndMemoryReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"CPU与内存报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "CPU与内存报表");
                    break;
                case DISK_REPORT:
                    //磁盘报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName =  diskReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"磁盘使用情况报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "磁盘使用情况报表");
                    break;
                case ASSETSUSE_REPORT:
                    //资产可用性报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = assetsUseReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"资产可用性报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "资产可用性报表");
                    break;
                case RUNSTATE_REPORT:
                    //运行状态报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = runStateReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"运行状态报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "运行状态报表");
                    break;
                case MPLS_REPORT:
                    //MPLS报表邮件发送
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = mplsReportHandle(dateType);
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"MPLS报告报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "MPLS报告报表");
                    break;
                case LINK_REPORT_LYL:
                    log.info("发送流量详情邮件，发送信息："+email);
                    //流量报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathNames = linkReportHandleLYL(dateType);
                    }
                    log.info("发送流量详情邮件2，附件位子："+pathNames);
                    message = sendReportEmail(email,"流量详情报表(蓝月亮)",from,pathNames);
                    log.info("发送流量详情邮件3，邮件信息："+message);
                    //保存成功的历史记录
                    if(CollectionUtils.isEmpty(pathNames)){
                        for (String name : pathNames) {
                            result = saveRecord(message, name, receive.getLoginName(), "流量详情报表(蓝月亮)");
                        }
                    }
                    log.info("发送流量详情邮件4，邮件信息："+result);
                    break;
                case CPUMEMORY_REPORT_LYL:
                    log.info("发送CPU内存邮件，发送信息："+email);
                    //CPU报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = cpuAndMemoryReportHandleLYL(dateType);
                        pathNames.add(pathName);
                    }
                    log.info("发送CPU内存邮件2，附件位子："+pathName);
                    message = sendReportEmail(email,"CPU内存报表(蓝月亮)",from,pathNames);
                    log.info("发送CPU内存邮件3，邮件信息："+message);
                    result = saveRecord(message, pathName, receive.getLoginName(), "CPU内存报表(蓝月亮)");
                    log.info("发送CPU内存邮件4，邮件信息："+result);
                    break;
                case PATROL_INSPECTION:
                    log.info("发送巡检报告邮件，发送信息："+email);
                    pathNames = PatrolInspectionReportHandle(dateType);
                    message = sendReportEmail(email,"巡检报告",from,pathNames);
                case CPU_REALTIME_REPORT:
                    //CPU报表邮件发送处理
                    if(CollectionUtils.isEmpty(pathNames)){
                        pathName = cpuRealTimeReportHandle();
                        pathNames.add(pathName);
                    }
                    message = sendReportEmail(email,"CPU与内存实时报表",from,pathNames);
                    result = saveRecord(message, pathName, receive.getLoginName(), "CPU与内存实时报表");
                    break;
            }
        }
        return result;
    }


    //保存发送记录
    private TimeTaskRresult saveRecord(String message, String pathName, String to, String switchName) {
        TimeTaskRresult result = new TimeTaskRresult();
        if (message.equals("成功")) {
            result.setSuccess(true);
            result.setObjectName(switchName).setResultType(0).setResultContext("发送邮件成功,发送对象"+to);
        } else {
            result.setSuccess(false);
            result.setObjectName(switchName).setResultType(0).setFailReason("发送邮件失败,发送对象"+to+",失败信息："+message);
        }
        return result;
    }

    /**
     * 进行邮件发送操作
     * @param to 接收邮件邮箱
     * @param subject 报表名称
     * @param emailFrom 发送方信息
     * @param pathNames 附件所在路径
     * @return
     */
    public String sendReportEmail(String to,String subject,EmailFrom emailFrom, List<String> pathNames){
        try{
            decrypt(emailFrom);
            customJavaMailSender.setUsernameFirst(emailFrom.getUsername());
            customJavaMailSender.setPasswordFirst(emailFrom.getPassword());
            customJavaMailSender.setHostFirst(emailFrom.getHostName());
            customJavaMailSender.setPortFirst(emailFrom.getPort());
            customJavaMailSender.setSSL(emailFrom.getIsSsl());

            InternetAddress from = new InternetAddress();
            from.setAddress(customJavaMailSender.getUsernameFirst());
            from.setPersonal(emailFrom.getPersonal(), "UTF-8");
            System.getProperties().setProperty("mail.mime.splitlongparameters","false");
            MimeMessage mimeMessage =customJavaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,"UTF-8");
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText("");
            if(CollectionUtils.isNotEmpty(pathNames)){
                for (String pathName : pathNames) {
                    FileSystemResource file = new FileSystemResource(new File(pathName));
                    String fileName = file.getFilename();
                    //添加附件，可多次调用该方法添加多个附件
                    mimeMessageHelper.addAttachment(fileName, file);
                }
            }
            customJavaMailSender.send(mimeMessage);
            return "成功";
        }catch (Exception e){
            log.info("报表邮件发送（附件邮件）失败:{}",e.getMessage());
            return e.getMessage();
        }
    }

    public  void decrypt(EmailFrom  email) {
        if(email !=null){
            try {
                email.setPassword(EncryptsUtil.decrypt(email.getPassword()));
            } catch (Exception e) {
                log.error("报表邮件发送（附件邮件）失败:{}",e.getMessage());
            }
        }
    }

    /**
     * MPLS报表邮件发送处理
     * @param dateType 时间类型
     * @return
     */
    private String mplsReportHandle(int dateType){
        List<MWMplsCacheDataDto> mwMplsCacheDataDtos = new ArrayList<>();
        List<Map<String, String>> mapList = new ArrayList<>();
        if(dateType == 1){//查询昨天数据
            List<Date> yesterday = (List<Date>) getDate(dateType);
           mapList = terraceManageDao.selectMplsLineHistoryDailyData(yesterday.get(0), yesterday.get(1), null);
        }
        if(dateType == 5){//查询上周数据
            String time = (String) getDate(dateType);
            mwMplsCacheDataDtos = terraceManageDao.selectMplsLineHistoryWeeklyData(time, null);
        }
        if(dateType == 8){//查询上月数据
            String time = (String) getDate(dateType);
           mwMplsCacheDataDtos = terraceManageDao.selectMplsLineHistoryMonthlyData(time, null);
        }
        ServerHistoryDto dto = new ServerHistoryDto();
        dto.setDateType(dateType);
        List<Object> objects = MWReportHandlerDataLogic.handleMplsHistoryReportData(mwMplsCacheDataDtos, mapList, dto);
        if(CollectionUtils.isEmpty(objects)) return "";
        List<MwMplsExportDto> dtos = handleMplsData(objects);
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("sendLink");
        includeColumnFiledNames.add("acceptLink");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<MwMplsExportDto>> list = getSubLists((List<MwMplsExportDto>) dtos, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, MwMplsExportDto.class).build();
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
        return pathName;
    }

    /**
     * 处理MPLS数据
     * @param objects MPLS数据集合
     */
    private List<MwMplsExportDto> handleMplsData(List<Object> objects){
        List<MwMplsExportDto> dtos = new ArrayList<>();
        List<Object> objlist = (List<Object>) objects.get(0);
        if(CollectionUtils.isEmpty(objlist))return dtos;
        Map<String,Object> sendMap = (Map<String, Object>) objlist.get(0);
        Map<String,Object> acceptMap = (Map<String, Object>) objlist.get(1);
        //发送流量数据
        List<MWItemHistoryDto> sendDtos = (List<MWItemHistoryDto>) sendMap.get("realData");
        //接收流量数据
        List<MWItemHistoryDto> acceptDtos = (List<MWItemHistoryDto>) acceptMap.get("realData");
        //发送单位
        String sendUnitByReal = (String) sendMap.get("unitByReal");
        //接收单位
        String acceptUnitByReal = (String) acceptMap.get("unitByReal");

        if(CollectionUtils.isEmpty(sendDtos) || CollectionUtils.isEmpty(acceptDtos))return dtos;
        for (int i = 0; i < sendDtos.size(); i++) {
            MWItemHistoryDto sendHistoryDto = sendDtos.get(i);
            MWItemHistoryDto acceptHistoryDto = acceptDtos.get(i);
            MwMplsExportDto dto = new MwMplsExportDto();
            dto.setSendLink(sendHistoryDto.getValue()+sendUnitByReal);
            dto.setAcceptLink(acceptHistoryDto.getValue()+acceptUnitByReal);
            dto.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sendHistoryDto.getDateTime()));
            dtos.add(dto);
        }
        return dtos;
    }
    /**
     * 运行状态报表邮件发送处理
     * @param dateType 时间类型
     * @return
     */
    private String runStateReportHandle(int dateType){
        ReportWordParam param = setParam();
        if(dateType == 1){//查询昨天数据
            param.setDateType(1);
        }
        if(dateType == 5){//查询上周数据
            param.setDateType(2);
        }
        if(dateType == 8){//查询上月数据
            List<Date> lastMonth = ReportDateUtil.getLastMonth();
            param.setDateType(6);
            param.setDateStart(lastMonth.get(0));
            param.setDateEnd(lastMonth.get(1));
        }
        String pathName = runStartReportExportWord(param);
        return pathName;
    }

    /**
     * 设置运行状态报表导出参数
     * @return
     */
    private ReportWordParam setParam(){
        ReportWordParam param = new ReportWordParam();
        List<Integer> trendTypes = new ArrayList<>();
        trendTypes.add(0);
        trendTypes.add(1);
        trendTypes.add(2);
        param.setTrendTypes(trendTypes);
        List<RunTimeQueryParam> queryParams = new ArrayList<>();
        RunTimeQueryParam queryParam1 = new RunTimeQueryParam();
        queryParam1.setItemName("DISK_UTILIZATION");
        queryParam1.setReportItemType(1);
        queryParam1.setDataSize(5);
        queryParams.add(queryParam1);
        RunTimeQueryParam queryParam2 = new RunTimeQueryParam();
        queryParam2.setItemName("ICMP_LOSS");
        queryParam2.setReportItemType(0);
        queryParam2.setDataSize(5);
        queryParams.add(queryParam2);
        RunTimeQueryParam queryParam3 = new RunTimeQueryParam();
        queryParam3.setItemName("MEMORY_UTILIZATION");
        queryParam3.setReportItemType(0);
        queryParam3.setDataSize(5);
        queryParams.add(queryParam3);
        RunTimeQueryParam queryParam4 = new RunTimeQueryParam();
        queryParam4.setItemName("CPU_UTILIZATION");
        queryParam4.setReportItemType(0);
        queryParam4.setDataSize(5);
        queryParams.add(queryParam4);
        RunTimeQueryParam queryParam5 = new RunTimeQueryParam();
        queryParam5.setItemName("INTERFACE_IN_UTILIZATION");
        queryParam5.setReportItemType(1);
        queryParam5.setDataSize(5);
        queryParams.add(queryParam5);
        RunTimeQueryParam queryParam6 = new RunTimeQueryParam();
        queryParam5.setReportItemType(1);
        queryParam5.setDataSize(5);
        queryParams.add(queryParam6);
        param.setTopQueryParams(queryParams);
        return param;
    }

    /**
     * 资产可用性报表邮件发送处理
     * @param dateType 时间类型
     * @return
     */
    private String assetsUseReportHandle(int dateType){
        List<MwAssetsUsabilityParam> assetsUsabilitys = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateRegion = "";
        if(dateType == 1){//查询昨天数据
            List<Date> yesterday = (List<Date>) getDate(dateType);
            dateRegion = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
            assetsUsabilitys = terraceManageDao.selectAssetsUsabilityDailyData(yesterday.get(0), yesterday.get(1), null);
        }
        if(dateType == 5){//查询上周数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            assetsUsabilitys = terraceManageDao.selectAssetsUsabilityWeeklyData(time, null);
        }
        if(dateType == 8){//查询上月数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            assetsUsabilitys = terraceManageDao.selectAssetsUsabilityMonthlyData(time, null);
        }
        if(CollectionUtils.isEmpty(assetsUsabilitys))return "";
        for (MwAssetsUsabilityParam assetsUsability : assetsUsabilitys) {
            assetsUsability.setTime(dateRegion);
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("ip");
        includeColumnFiledNames.add("assetsUsability");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<MwAssetsUsabilityParam>> list = getSubLists((List<MwAssetsUsabilityParam>) assetsUsabilitys, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, MwAssetsUsabilityParam.class).build();
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
        return pathName;
    }


    /**
     * 磁盘使用情况报表邮件发送处理
     * @param dateType 时间类型
     * @return
     */
    private String diskReportHandle(int dateType){
        List<TrendDiskDto> trendDiskDtos = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateRegion = "";
        if(dateType == 1){//查询昨天数据
            List<Date> yesterday = (List<Date>) getDate(dateType);
            dateRegion = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
            trendDiskDtos = terraceManageDao.selectDiskUseReportDailyData(yesterday.get(0), yesterday.get(1), null);
            diskValueFilter(trendDiskDtos);
        }
        if(dateType == 5){//查询上周数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            trendDiskDtos = terraceManageDao.selectDiskUseReportWeeklyData(time, null);
        }
        if(dateType == 8){//查询上月数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            trendDiskDtos = terraceManageDao.selectDiskUseReportMonthlyData(time, null);
        }
        if(CollectionUtils.isEmpty(trendDiskDtos))return "";
        List<DiskUseReportExportParam> diskUseReportExportParams = new ArrayList<>();
        trendDiskDtos.forEach(dto->{
            DiskUseReportExportParam exportParam = new DiskUseReportExportParam();
            exportParam.setTime(dto.getTime());
            exportParam.setAssetsName(dto.getAssetsName());
            exportParam.setIpAddress(dto.getIpAddress());
            exportParam.setDiskTotal(dto.getDiskTotal());
            exportParam.setDiskAvgValue(dto.getDiskAvgValue());
            exportParam.setDiskFree(dto.getDiskFree());
            exportParam.setDiskUsable(dto.getDiskUsable());
            exportParam.setDiskUse(dto.getDiskUse());
            exportParam.setTypeName(dto.getTypeName());
            diskUseReportExportParams.add(exportParam);
        });
        for (DiskUseReportExportParam diskUseReportExportParam : diskUseReportExportParams) {
            diskUseReportExportParam.setTime(dateRegion);
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("ipAddress");
        includeColumnFiledNames.add("typeName");
        includeColumnFiledNames.add("diskTotal");
        includeColumnFiledNames.add("diskAvgValue");
        includeColumnFiledNames.add("diskUse");
        includeColumnFiledNames.add("diskFree");
        includeColumnFiledNames.add("diskUsable");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<DiskUseReportExportParam>> list = getSubLists((List<DiskUseReportExportParam>) diskUseReportExportParams, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, DiskUseReportExportParam.class).build();
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
        return pathName;
    }

    /**
     * 磁盘数据过滤
     */
    private void diskValueFilter(List<TrendDiskDto> trendDiskDtos){
        if(CollectionUtils.isEmpty(trendDiskDtos)){return;}
        Iterator<TrendDiskDto> iterator = trendDiskDtos.iterator();
        while (iterator.hasNext()){
            TrendDiskDto diskDto = iterator.next();
            String diskAvgValue = diskDto.getDiskAvgValue();
            if(StringUtils.isNotBlank(diskAvgValue) && Double.parseDouble(diskAvgValue.replace("%","")) < reportEmailFilterValue.doubleValue()){
                iterator.remove();
            }
        }
    }

    /**
     * 蓝月亮Cpu报表邮件发送
     * @param dateType
     * @return
     */
    private String cpuAndMemoryReportHandleLYL(int dateType){
        List<CpuNewsReportExportParam> cpuNewsReportExportParams = new ArrayList<>();
        RunTimeQueryParam param = new RunTimeQueryParam();
        param.setDateType(dateType);
        param.setTimingType(false);
        param.setPageNumber(1);
        param.setPageSize(1000000);
        Reply reply = terraceManageService.selectReportCPUNews(param);
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(CollectionUtils.isEmpty(pageInfo.getList()))return "";
            List<RunTimeItemValue> runTimeItemValues = pageInfo.getList();
            runTimeItemValues.forEach(item->{
                CpuNewsReportExportParam exportParam = new CpuNewsReportExportParam();
                exportParam.setBrand(item.getBrand());
                exportParam.setLocation(item.getLocation());
                exportParam.setTime(item.getTime());
                exportParam.setAssetName(item.getAssetName());
                exportParam.setIp(item.getIp());
                exportParam.setMaxValue(item.getMaxValue());
                exportParam.setAvgValue(item.getAvgValue());
                exportParam.setMinValue(item.getMinValue());
                exportParam.setDiskTotal(item.getDiskTotal());
                exportParam.setDiskUser(item.getDiskUser());
                exportParam.setDiskUserRate(item.getDiskUserRate());
                exportParam.setIcmpResponseTime(item.getIcmpResponseTime());
                exportParam.setIcmpPing(item.getIcmpPing());
                cpuNewsReportExportParams.add(exportParam);
            });
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths +"/"+ name;
        //进行PDF数据导出
//        String[] head = {"品牌","位置","主机名称","IP地址","内存利用率","CPU最大利用率","CPU平均利用率","平均响应时间",  "设备状态"};
//        String newPathName = new MwReportExportPdfManager().generatePDFs(head, cpuNewsReportExportParams, pathName);
        //2字段显示
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("brand");
        includeColumnFiledNames.add("location");
        includeColumnFiledNames.add("assetName");
        includeColumnFiledNames.add("ip");
        includeColumnFiledNames.add("diskUserRate");
        includeColumnFiledNames.add("maxValue");
        includeColumnFiledNames.add("avgValue");
        includeColumnFiledNames.add("icmpResponseTime");
        includeColumnFiledNames.add("icmpPing");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<CpuNewsReportExportParam>> list = getSubLists((List<CpuNewsReportExportParam>) cpuNewsReportExportParams, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, CpuNewsReportExportParam.class).build();
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
        return pathName;
    }


    /**
     * CPU实时报表
     * @return
     */
    private String cpuRealTimeReportHandle(){
        RunTimeQueryParam param = new RunTimeQueryParam();
        param.setReportType(3);
        List<CpuNewsReportExportParam> cpuNewsReportExportParams = new ArrayList<>();
        param.setPageNumber(1);
        param.setPageSize(Integer.MAX_VALUE);
        Reply reply = terraceManageService.selectReportCPUNews(param);
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(CollectionUtils.isEmpty(pageInfo.getList()))return null;
            List<RunTimeItemValue> runTimeItemValues = pageInfo.getList();
            runTimeItemValues.forEach(item->{
                CpuNewsReportExportParam exportParam = new CpuNewsReportExportParam();
                exportParam.setBrand(item.getBrand());
                exportParam.setLocation(item.getLocation());
                exportParam.setTime(item.getTime());
                exportParam.setAssetName(item.getAssetName());
                exportParam.setIp(item.getIp());
                exportParam.setMaxValue(item.getMaxValue()+"("+item.getMaxValueTime()+")");
                exportParam.setAvgValue(item.getAvgValue());
                exportParam.setMinValue(item.getMinValue()+"("+item.getMinValueTime()+")");
                exportParam.setDiskTotal(item.getDiskTotal());
                exportParam.setDiskUser(item.getDiskUser());
                exportParam.setDiskUserRate(item.getDiskUserRate());
                exportParam.setIcmpResponseTime(item.getIcmpResponseTime());
                exportParam.setIcmpPing(item.getIcmpPing());
                exportParam.setMaxMemoryUtilizationRate(item.getMaxMemoryUtilizationRate()+"("+item.getMemoryMaxValueTime()+")");
                exportParam.setMinMemoryUtilizationRate(item.getMinMemoryUtilizationRate()+"("+item.getMemoryMinValueTime()+")");
                exportParam.setCpuUtilizationRate(item.getCpuUtilizationRate());
                exportParam.setMemoryUtilizationRate(item.getMemoryUtilizationRate());
                exportParam.setIsCpuColor(item.getIsCpuColor());
                exportParam.setIsMemoryColor(item.getIsMemoryColor());
                cpuNewsReportExportParams.add(exportParam);
            });
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        //2字段显示
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("assetName");
        includeColumnFiledNames.add("ip");
        includeColumnFiledNames.add("cpuUtilizationRate");
        includeColumnFiledNames.add("memoryUtilizationRate");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<CpuNewsReportExportParam>> list = getSubLists((List<CpuNewsReportExportParam>) cpuNewsReportExportParams, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, CpuNewsReportExportParam.class).build();
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
        return pathName;
    }


    /**
     * CPU与内存报表邮件发送处理
     * @param dateType 时间类型
     * @return
     */
    private String cpuAndMemoryReportHandle(int dateType){
        List<RunTimeItemValue> dataList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateRegion = "";
        if(dateType == 1){//查询昨天数据
            List<Date> yesterday = (List<Date>) getDate(dateType);
            dateRegion = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
            dataList = terraceManageDao.selectCpuAndMemoryReportDailyData(yesterday.get(0), yesterday.get(1), null);
        }
        if(dateType == 5){//查询上周数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            dataList = terraceManageDao.selectCpuAndMemoryReportWeellyData(time, null);
        }
        if(dateType == 8){//查询上月数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            dataList = terraceManageDao.selectCpuAndMemoryReportMonthlyData(time, null);
        }
        if(CollectionUtils.isEmpty(dataList))return "";
        List<RunTimeItemValue> runTimeItemValues = new ArrayList<>();
        dataList.stream().filter(distinctByKey(p -> p.getAssetName()+p.getAssetsId()))  //filter保留true的值
                .forEach(runTimeItemValues::add);
        List<CpuNewsReportExportParam> cpuNewsReportExportParams = new ArrayList<>();
        runTimeItemValues.forEach(item->{
            CpuNewsReportExportParam exportParam = new CpuNewsReportExportParam();
            exportParam.setTime(item.getTime());
            exportParam.setAssetName(item.getAssetName());
            exportParam.setIp(item.getIp());
            exportParam.setMaxValue(item.getMaxValue()+"("+item.getMaxValueTime()+")");
            exportParam.setAvgValue(item.getAvgValue());
            exportParam.setMinValue(item.getMinValue()+"("+item.getMinValueTime()+")");
            exportParam.setDiskTotal(item.getDiskTotal());
            exportParam.setDiskUser(item.getDiskUser());
            exportParam.setMaxMemoryUtilizationRate(item.getMaxMemoryUtilizationRate()+"("+item.getMemoryMaxValueTime()+")");
            exportParam.setDiskUserRate(item.getDiskUserRate());
            exportParam.setMinMemoryUtilizationRate(item.getMinMemoryUtilizationRate()+"("+item.getMemoryMinValueTime()+")");
            cpuNewsReportExportParams.add(exportParam);
        });
        for (CpuNewsReportExportParam cpuNewsReportExportParam : cpuNewsReportExportParams) {
            cpuNewsReportExportParam.setTime(dateRegion);
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        //2字段显示
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("assetName");
        includeColumnFiledNames.add("ip");
        includeColumnFiledNames.add("maxMemoryUtilizationRate");
        includeColumnFiledNames.add("diskUserRate");
        includeColumnFiledNames.add("minMemoryUtilizationRate");
        includeColumnFiledNames.add("diskUser");
        includeColumnFiledNames.add("diskTotal");
        includeColumnFiledNames.add("maxValue");
        includeColumnFiledNames.add("avgValue");
        includeColumnFiledNames.add("minValue");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<CpuNewsReportExportParam>> list = getSubLists((List<CpuNewsReportExportParam>) cpuNewsReportExportParams, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, CpuNewsReportExportParam.class).build();
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
        return pathName;
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 蓝月亮报表定时发送
     * @param dateType
     * @return
     */
    private List<String> linkReportHandleLYL(int dateType){
        List<LineFlowReportParam> lineFlowReportParams = new ArrayList<>();
        TrendParam trendParam = new TrendParam();
        trendParam.setDateType(dateType);
        trendParam.setTimingType(false);
        trendParam.setPageNumber(1);
        trendParam.setPageSize(1000000);
        Reply reply = terraceManageService.selectLylLinkFlowData(trendParam);
        if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
            PageInfo pageInfo = (PageInfo) reply.getData();
            if(CollectionUtils.isEmpty(pageInfo.getList()))return null;
            lineFlowReportParams = pageInfo.getList();
        }
        String s = "";
        if(dateType == 1){
            s = "每日报表" ;
        }else if(dateType == 5){
            s = "每周报表" ;
        }else{
            s = "月度报表" ;
        }
        List<String> pathNames = new ArrayList<>();
        //将数据按照标签位子信息进行分组处理
        Map<String, List<LineFlowReportParam>> emailDistribute = handleLinkLineRepotEmailDistribute(lineFlowReportParams);
        if(!emailDistribute.isEmpty()){
            for (String labelName : emailDistribute.keySet()) {
                List<LineFlowReportParam> lineFlowReportParamList = emailDistribute.get(labelName);
                //进行数据导出为excel
                //1文件地址+名称
                String name = labelName+s+new Date().getTime() + ".xlsx";
                Date now = new Date();
                String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
                File f = new File(paths);
                if (!f.exists()) {
                    f.mkdirs();
                }
                String pathName = paths + "/" + name;
//        //进行PDF数据导出
//        String[] head = {"时间","资产名称","接口名称","接收流量最大","接收流量平均","接收流量最小","接收总流量","发送流量最大","发送流量平均","发送流量最小","发送总流量"};
//        String newPathName = new MwReportExportPdfManager().generatePDFs(head, lineFlowReportParams, pathName);
                //2字段显示
                HashSet<String> includeColumnFiledNames = new HashSet<>();
                includeColumnFiledNames.add("time");
                includeColumnFiledNames.add("assetsName");
                includeColumnFiledNames.add("interfaceName");
                includeColumnFiledNames.add("acceptFlowMax");
                includeColumnFiledNames.add("acceptFlowAvg");
                includeColumnFiledNames.add("acceptFlowMin");
                includeColumnFiledNames.add("acceptTotalFlow");
                includeColumnFiledNames.add("sendingFlowMax");
                includeColumnFiledNames.add("sendingFlowAvg");
                includeColumnFiledNames.add("sendingFlowMin");
                includeColumnFiledNames.add("sendTotalFlow");
                //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
                List<List<LineFlowReportParam>> list = getSubLists((List<LineFlowReportParam>) lineFlowReportParamList, 50000);
                //4创建easyExcel写出对象
                ExcelWriter excelWriter = EasyExcel.write(pathName, LineFlowReportParam.class).build();
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
                pathNames.add(pathName);
            }
        }
        return pathNames;
    }


    /**
     * 流量报表邮件发送处理
     * @param dateType 时间类型
     */
    private String linkReportHandle(int dateType){
        List<LineFlowReportParam> lineFlowReportParams = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateRegion = "";
        if(dateType == 1){//查询昨天数据
            List<Date> yesterday = (List<Date>) getDate(dateType);
            dateRegion = format.format(yesterday.get(0))+"~"+format.format(yesterday.get(1));
            lineFlowReportParams = terraceManageDao.selectLinkReportDailyData(yesterday.get(0), yesterday.get(1), null);
        }
        if(dateType == 5){//查询上周数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            lineFlowReportParams = terraceManageDao.selectLinkReportWeeklyData(time, null);
        }
        if(dateType == 8){//查询上月数据
            String time = (String) getDate(dateType);
            dateRegion = time;
            lineFlowReportParams = terraceManageDao.selectLinkReportMonthlyData(time, null);
        }
        if(CollectionUtils.isEmpty(lineFlowReportParams))return "";
        for (LineFlowReportParam lineFlowReportParam : lineFlowReportParams) {
            lineFlowReportParam.setTime(dateRegion);
            lineFlowReportParam.setAcceptFlowMax(lineFlowReportParam.getAcceptFlowMax()+lineFlowReportParam.getAcceptMaxValueTime());
            lineFlowReportParam.setAcceptFlowMin(lineFlowReportParam.getAcceptFlowMin()+lineFlowReportParam.getAcceptMinValueTime());
            lineFlowReportParam.setSendingFlowMax(lineFlowReportParam.getSendingFlowMax()+lineFlowReportParam.getSendMaxValueTime());
            lineFlowReportParam.setSendingFlowMin(lineFlowReportParam.getSendingFlowMin()+lineFlowReportParam.getSendMinValueTime());
        }
        //进行数据导出为excel
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".xlsx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        //2字段显示
        HashSet<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("time");
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("interfaceName");
        includeColumnFiledNames.add("acceptFlowMax");
        includeColumnFiledNames.add("acceptFlowAvg");
        includeColumnFiledNames.add("acceptFlowMin");
        includeColumnFiledNames.add("sendingFlowMax");
        includeColumnFiledNames.add("sendingFlowAvg");
        includeColumnFiledNames.add("sendingFlowMin");
        //3将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
        List<List<LineFlowReportParam>> list = getSubLists((List<LineFlowReportParam>) lineFlowReportParams, 50000);
        //4创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(pathName, LineFlowReportParam.class).build();
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
        return pathName;
    }

    public List<Integer> userDTOToInteger(List<UserDTO> users) {
        List<Integer> list = new ArrayList<>();
        users.forEach(user -> {
            list.add(user.getUserId());
        });
        return list;
    }

    /**
     * 校验数据时间（1天，一周，一月）
     * @param timeTag 时间类型
     * @return
     */
    private int checkDateType(String timeTag){
        if(StringUtils.isBlank(timeTag))return 12;
        if("day".equals(timeTag)){
            return 1;
        }
        if("week".equals(timeTag)){
            return 5;
        }
        if("month".equals(timeTag)){
            return 8;
        }
        return 12;
    }

    /**
     * 获取时间区间
     * @param dateType 时间类型
     */
    private Object getDate(int dateType){
        String time = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        switch (dateType){
            case 1://昨天
                List<Date> yesterday = ReportDateUtil.getYesterday();
                return yesterday;
            case 5://上周
                List<Date> lastWeek = ReportDateUtil.getLastWeek();
                time = format.format(lastWeek.get(0))+"~"+format.format(lastWeek.get(1));
                return time;
            case 8://上月
                List<Date> lastMonth = ReportDateUtil.getLastMonth();
                time = format.format(lastMonth.get(0))+"~"+format.format(lastMonth.get(1));
                return time;
        }
        return time;
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




    /**
     *
     */
    private String runStartReportExportWord(ReportWordParam param){
        //1文件地址+名称
        String name = UUIDUtils.getUUID() + ".docx";
        Date now = new Date();
        String paths = path + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        File f = new File(paths);
        if (!f.exists()) {
            f.mkdirs();
        }
        String pathName = paths + name;
        HashMap<String, Object> data = null;
        File file = null;
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;
        BufferedOutputStream out = null;
        try {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator;
            //打成jar包以后读取不到文件
            ClassPathResource resource = new ClassPathResource("templates/reportTemplate.docx");
            inputStream = resource.getInputStream();

            File outputFileT = new File(temPath + "reportTemplate.docx");
            //检测是否存在目录
            if (!outputFileT.exists()) {
                if (!outputFileT.getParentFile().exists()) {
                    outputFileT.getParentFile().mkdirs();
                }
                out = new BufferedOutputStream(new FileOutputStream(temPath + "reportTemplate.docx"));
                int len = -1;
                byte[] b = new byte[1024];
                while ((len = inputStream.read(b)) != -1) {
                    out.write(b, 0, len);
                }
                if (out != null) out.close();
            }
            ConfigureBuilder builder = Configure.newBuilder();
            XWPFTemplate template = XWPFTemplate.compile(temPath + "reportTemplate.docx", builder.build());
            log.error("导出word" + template);
            data = getRenderData(param);
            template.render(data);

            String fileName = UUIDUtils.getUUID();
            File outputFile = new File(temPath + fileName);
            //检测是否存在目录
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            FileOutputStream outFile = new FileOutputStream(pathName);
            template.write(outFile);
            outFile.flush();
            outFile.close();
            template.close();
            return pathName;
        }catch (Exception e){
            log.error("运行状态报表导出word失败");
        }
        return pathName;
    }

    /**
     * 填充word模板
     *
     * @param param
     * @return
     * @throws ParseException
     */
    private HashMap<String, Object> getRenderData(ReportWordParam param) throws ParseException {
        HashMap<String, Object> data = new HashMap<>();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(date);
        data.put("dateTime", s);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
        String s1 = sdf1.format(date);
        data.put("dateTimeChinese", s1);
        data.put("title1", "资产统计");
        List<RunTimeQueryParam> topQueryParams = param.getTopQueryParams();
        Reply reply = null;

        List<RunTimeItemValue> listTop = null;
        List<RunTimeItemValue> listTopOut = null;
        RowRenderData headerData = null;
        List<RowRenderData> listRowList = new ArrayList<>();
        List<RowRenderData> rowList = null;
        if (topQueryParams != null && topQueryParams.size() > 0) {
            for (RunTimeQueryParam top : topQueryParams) {

                top.setDateType(param.getDateType());
                top.setDateStart(param.getDateStart());
                top.setDateEnd(param.getDateEnd());
                if (param.getDateType() == 6) {
                    top.setChooseTime(MWUtils.getStartOrEndDate(param.getDateStart(), param.getDateEnd()));
                }
                if (top.getDataSize() == null || top.getDataSize() <= 0) {
                    top.setDataSize(100000000);
                }
                top.setTimingType(false);
                //cpu,内存，丢包率
                if (top.getReportItemType() == RuntimeReportState.CPU_MEMORY_LOSS.getCode()) {
                    if ("MEMORY_UTILIZATION".equals(top.getItemName())) {
                        data.put("title3", "内存利用率（TOP" + top.getDataSize() + "）");
                        headerData = Rows.of("Top", "资产名称", "IP地址", "内存利用率")
                                .textColor("000000")
                                .bgColor("87CEEB").create();
                        reply = mwreportService.getRunTimeItemUtilization(top);
                        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                            listTop = (List<RunTimeItemValue>) reply.getData();
                            rowList = getRowList(listTop, 0);
                            listRowList.add(headerData);
                            listRowList.addAll(rowList);
                            data.put("table3", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                            listRowList.clear();
                        }
                    }
                    if ("CPU_UTILIZATION".equals(top.getItemName())) {
                        data.put("title2", "CPU利用率（TOP" + top.getDataSize() + "）");
                        headerData = Rows.of("Top", "资产名称", "IP地址", "CPU利用率")
                                .textColor("000000")
                                .bgColor("87CEEB").create();
                        reply = mwreportService.getRunTimeItemUtilization(top);
                        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                            listTop = (List<RunTimeItemValue>) reply.getData();
                            rowList = getRowList(listTop, 0);
                            listRowList.add(headerData);
                            listRowList.addAll(rowList);
                            data.put("table2", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                            listRowList.clear();
                        }
                    }
                    if ("ICMP_LOSS".equals(top.getItemName())) {
                        data.put("title4", "丢包率（TOP" + top.getDataSize() + "）");
                        headerData = Rows.of("Top", "资产名称", "IP地址", "丢包率")
                                .textColor("000000")
                                .bgColor("87CEEB").create();
                        reply = mwreportService.getRunTimeItemUtilization(top);
                        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                            listTop = (List<RunTimeItemValue>) reply.getData();
                            rowList = getRowList(listTop, 0);
                            listRowList.add(headerData);
                            listRowList.addAll(rowList);
                            data.put("table4", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                            listRowList.clear();
                        }
                    }

                }
                //接口，磁盘
                if (top.getReportItemType() == RuntimeReportState.INTERFACE_DISK.getCode()) {
                    if ("DISK_UTILIZATION".equals(top.getItemName())) {
                        data.put("title5", "磁盘利用率（TOP" + top.getDataSize() + "）");
                        headerData = Rows.of("Top", "资产名称", "IP地址", "分区名称", "分区率")
                                .textColor("000000")
                                .bgColor("87CEEB").create();
                        reply = mwreportService.getRunTimeItemUtilization(top);
                        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                            listTop = (List<RunTimeItemValue>) reply.getData();
                            rowList = getRowList(listTop, 1);
                            listRowList.add(headerData);
                            listRowList.addAll(rowList);
                            data.put("table5", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                            listRowList.clear();
                        }
                    }
                    if ("INTERFACE_IN_UTILIZATION".equals(top.getItemName())) {
                        data.put("title6", "接口利用率（TOP" + top.getDataSize() + "）");
                        headerData = Rows.of("Top", "资产名称", "接口名称", "接口利用率(IN)", "接口利用率(OUT)")
                                .textColor("000000")
                                .bgColor("87CEEB").create();
                        reply = mwreportService.getRunTimeItemUtilization(top);
                        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                            listTop = (List<RunTimeItemValue>) reply.getData();
                            if (top.getDateType() != DateTimeTypeEnum.TODAY.getCode()) {
                                for (int i = 0; i < listTop.size(); i++) {
                                    try {
                                        listTop.get(i).setAssetUtilization(listTop.get(i).getOutInterfaceAvgValue());
                                    } catch (Exception e) {
                                        //遇到一场直接放弃
                                    }
                                }
                            } else {
                                for (int i = 0; i < listTop.size(); i++) {
                                    try {
                                        listTop.get(i).setAssetUtilization(listTop.get(i).getOutInterfaceAvgValue());
                                    } catch (Exception e) {
                                        //遇到一场直接放弃
                                    }
                                }
                            }
                        }
                        rowList = getRowList(listTop, 2);
                        listRowList.add(headerData);
                        listRowList.addAll(rowList);
                        data.put("table6", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                        listRowList.clear();
                    }
                }
                //资产可用性
                if (top.getReportItemType() == RuntimeReportState.ASSETUTILIZATION.getCode()) {
                    data.put("title7", "资产可用率（TOP" + top.getDataSize() + "）");
                    headerData = Rows.of("Top", "资产名称", "IP地址", "可用率")
                            .textColor("000000")
                            .bgColor("87CEEB").create();
                    reply = mwreportService.getRunTimeAssetUtilization(top);
                    if (reply.getRes() == PaasConstant.RES_SUCCESS) {
                        listTop = (List<RunTimeItemValue>) reply.getData();
                        rowList = getRowList(listTop, 3);
                        listRowList.add(headerData);
                        listRowList.addAll(rowList);
                        data.put("table7", Tables.create(listRowList.toArray(new RowRenderData[]{})));
                        listRowList.clear();
                    }
                }
            }
        }
        //表格1标头字段设置
        headerData = Rows.of("", "网络设备")
                .textColor("000000")
                .bgColor("87CEEB").create();

        listRowList = new ArrayList<>();
        listRowList.add(headerData);
        RunTimeQueryParam assetsParam = new RunTimeQueryParam();
        assetsParam.setDateType(param.getDateType());
        if (param.getDateType() == 6) {
            assetsParam.setDateStart(MWUtils.getStartOrEndTime(param.getDateStart(), 0));
            assetsParam.setDateEnd(MWUtils.getStartOrEndTime(param.getDateEnd(), 1));
            assetsParam.setChooseTime(MWUtils.getStartOrEndDate(param.getDateStart(), param.getDateEnd()));
        }
        reply = mwreportService.getRunTimeReportOfAeest(assetsParam);
        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
            Map<String, List<Integer>> tableData1 = (Map<String, List<Integer>>) reply.getData();
            //饼状图
//            tableData1.get("total").remove(0);
            List<Integer> total = tableData1.get("total");
            total = new ArrayList<>(total);
            total.remove(0);
            ChartSingleSeriesRenderData pie = Charts.ofSingleSeries("资产状态", new String[]{"分类正常" + total.get(0), "分类异常" + total.get(1), "分类新增" + total.get(2)})
                    .series("", total.toArray(new Integer[]{}))
                    .create();
            log.info("pie  信息 {}", tableData1.get("total").toArray(new Integer[]{}));
            data.put("LineCharts1", pie);

            List<String> strings = Arrays.asList("分类总数", "分类正常", "分类异常", "分类新增");
            for (int i = 0; i < 4; i++) {
                //一行数据
                listRowList.add(Rows.create(strings.get(i)
                        , null == tableData1.get("网络设备") ? "" : tableData1.get("网络设备").get(i).toString()));
            }
            data.put("table1", Tables.create(listRowList.toArray(new RowRenderData[]{})));
        }
        //图片
        if (param.getImgBase64() != null && param.getImgBase64().size() > 0) {
            for (int i = 0; i < param.getImgBase64().size(); i++) {
                String rawBase64 = param.getImgBase64().get(i).substring(param.getImgBase64().get(i).indexOf(",") + 1);
                data.put("image" + (i + 1), Pictures.ofBase64(rawBase64, PictureType.JPEG).size(550, 250).create());
            }
        }

        return data;
    }

    private List<RowRenderData> getRowList(List<RunTimeItemValue> param, int type) {
        List<RowRenderData> rowList = new ArrayList<>();
        DecimalFormat format = new DecimalFormat("0.00");
        if (param != null && param.size() > 0) {
            if (type == 0) {//普通三个字段
                for (int i = 0; i < param.size(); i++) {
                    //一行数据
                    BigDecimal value = new BigDecimal(param.get(i).getAvgValue());
                    value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
                    rowList.add(Rows.create(String.valueOf(i + 1)
                            , param.get(i).getAssetName()
                            , param.get(i).getIp()
                            , value.toString() + "%"));
                }
            }
            if (type == 1) {//普通四个字段()
                for (int i = 0; i < param.size(); i++) {
                    //一行数据
                    BigDecimal value = new BigDecimal(param.get(i).getAvgValue());
                    value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
                    rowList.add(Rows.create(String.valueOf(i + 1)
                            , param.get(i).getAssetName()
                            , param.get(i).getIp()
                            , param.get(i).getDiskName()
                            , value.toString() + "%"));
                }
            }
            if (type == 2) {//普通五个字段()
                for (int i = 0; i < param.size(); i++) {
                    //一行数据
                    BigDecimal value = new BigDecimal(param.get(i).getAvgValue());
                    value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal value2 = new BigDecimal(param.get(i).getAssetUtilization());
                    value2 = value2.setScale(2, BigDecimal.ROUND_HALF_UP);
                    rowList.add(Rows.create(String.valueOf(i + 1)
                            , param.get(i).getAssetName()
                            , param.get(i).getInterfaceName()
                            , value.toString() + "%"
                            , value2.toString() + "%"));
                }
            }
            if (type == 3) {//普通四个字段(可用性)
                for (int i = 0; i < param.size(); i++) {
                    //一行数据
                    String big = param.get(i).getAssetUtilization().replaceAll("%", "");
                    BigDecimal value2 = new BigDecimal(big);
                    value2 = value2.setScale(2, BigDecimal.ROUND_HALF_UP);
                    rowList.add(Rows.create(String.valueOf(i + 1)
                            , param.get(i).getAssetName()
                            , param.get(i).getIp()
                            , value2.toString() + "%"));
                }
            }
        }
        return rowList;
    }

    /**
     * 判断当前是否是月第一天
     *
     * @return
     */
    private boolean isFirstDayOfMonth() {
        Date currDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * 获取今天是否是星期一
     *
     * @return
     */
    private boolean isFirstWeekData() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        if ("星期一".equals(sdf.format(date))) {
            return true;
        }
        return false;
    }

    /**
     * 获取需要发送邮件的用户信息
     *
     * @param reports 所有报表数据
     */
    private void getSendEmailUser(List<MwReportTable> reports) {
        if (CollectionUtils.isEmpty(reports)) return;
        for (MwReportTable report : reports) {
            String reportId = report.getReportId();
            //查询关联信息
            List<MWReportSendUserDto> sendUserDtos = mwReportDao.selectReportSendUser(reportId);
            List<Integer> userIds = new ArrayList<>();
            List<Integer> groupIds = new ArrayList<>();
            if (CollectionUtils.isEmpty(sendUserDtos)) continue;
            for (MWReportSendUserDto sendUserDto : sendUserDtos) {
                String type = sendUserDto.getType();
                int userId = sendUserDto.getUserId();
                int groupId = sendUserDto.getGroupId();
                if (StringUtils.isNotBlank(type) && "USER".equals(type)) {
                    userIds.add(userId);
                }
                if (StringUtils.isNotBlank(type) && "GROUP".equals(type)) {
                    groupIds.add(groupId);
                }
            }
            List<Integer> userGroupIds = mwReportDao.selectSendEmailUserGroup(reportId);
            if(CollectionUtils.isNotEmpty(userGroupIds)){
                for (Integer userGroupId : userGroupIds) {
                    if(userIds.contains(userGroupId))continue;
                    userIds.add(userGroupId);
                }
            }
            //查询用户信息
            if (CollectionUtils.isNotEmpty(userIds)) {
                List<UserDTO> userDTOS = mwReportDao.selectUserNews(userIds);
                report.setPrincipal(userDTOS);
            } else {
                report.setPrincipal(new ArrayList<>());
            }
            //查询用户组信息
            if (CollectionUtils.isNotEmpty(groupIds)) {
                List<GroupDTO> groupDTOS = mwReportDao.selectUserGroupNews(groupIds);
                report.setGroups(groupDTOS);
            } else {
                report.setGroups(new ArrayList<>());
            }
        }
    }

    /**
     * 流量详情报表邮件根据资产位子进行进行分发
     *
     * @param lineFlowReportParams 数据信息
     */
    private Map<String, List<LineFlowReportParam>> handleLinkLineRepotEmailDistribute(List<LineFlowReportParam> lineFlowReportParams) {
        Map<String, List<LineFlowReportParam>> lineFlowReportMap = new HashMap<>();
        if (CollectionUtils.isEmpty(lineFlowReportParams)) return lineFlowReportMap;
        //查询标签信息，标签名称为“月度报表”的标签所对应的资产信息
        List<Map<String, String>> labelAssets = mwReportDao.selectLabelAssets();
        if (CollectionUtils.isEmpty(labelAssets)) return lineFlowReportMap;
        for (Map<String, String> labelAsset : labelAssets) {
            String typeId = labelAsset.get("typeId");//资产ID
            String valueName = labelAsset.get("valueName");//对应标签位子
            if (StringUtils.isBlank(typeId) || StringUtils.isBlank(valueName)) continue;
            for (LineFlowReportParam reportParam : lineFlowReportParams) {
                String assetsId = reportParam.getAssetsId();
                if (typeId.equals(assetsId)) {
                    if (!lineFlowReportMap.isEmpty() && lineFlowReportMap.get(valueName) != null) {
                        List<LineFlowReportParam> paramList = lineFlowReportMap.get(valueName);
                        paramList.add(reportParam);
                        lineFlowReportMap.put(valueName, paramList);
                    } else {
                        List<LineFlowReportParam> paramList = new ArrayList<>();
                        paramList.add(reportParam);
                        lineFlowReportMap.put(valueName, paramList);
                    }
                }
            }
        }
        return lineFlowReportMap;
    }

    /**
     * 巡检报告邮件发送处理
     * @param dateType
     * @return
     */
    private List<String> PatrolInspectionReportHandle(int dateType){
        PatrolInspectionParam patrolInspectionParam = new PatrolInspectionParam();
        patrolInspectionParam.setDateType(dateType);
        String exportExcelPath = patrolInspectionService.getExportExcelPath(patrolInspectionParam);
        String exportWordPath = patrolInspectionService.getExportWordPath(patrolInspectionParam);
        List<String> pathNames = new ArrayList<>();
        pathNames.add(exportExcelPath);
        pathNames.add(exportWordPath);
        return pathNames;
    }
}
