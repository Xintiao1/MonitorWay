package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.enums.MwRealTimeReportExportHeadEnum;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.report.service.MWRreportExportService;
import cn.mw.monitor.report.service.MwCustomReportService;
import cn.mw.monitor.report.service.MwReportTerraceManageService;
import cn.mw.monitor.report.service.manager.MwCellColorSheetWriteHandler;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MWRreportExportServiceImpl
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/7 15:21
 * @Version 1.0
 **/
@Service
@Slf4j
public class MWRreportExportServiceImpl implements MWRreportExportService {

    @Autowired
    private MwReportTerraceManageService terraceManageService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userService;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwCustomReportService customReportService;

    //资产硬件类型
    public static final List<String> itemNames = Arrays.asList(new String[]{"ICMP_PING","ICMP_RESPONSE_TIME"});
    public static final String ICMP_PING = "ICMP_PING";
    public static final String ICMP_RESPONSE_TIME = "ICMP_RESPONSE_TIME";

    @Override
    public void cpuNewsReportAllExport(RunTimeQueryParam param, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            int reportType = param.getReportType();
            List<CpuNewsReportExportParam> cpuNewsReportExportParams = new ArrayList<>();
            param.setPageNumber(1);
            param.setPageSize(1000000);
            Reply reply = terraceManageService.selectReportCPUNews(param);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList()))return;
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

            Map<Integer,List<Integer>> map = new HashMap<>();
            if(CollectionUtils.isNotEmpty(cpuNewsReportExportParams)){
                for (int i = 1; i <= cpuNewsReportExportParams.size(); i++) {
                    List<Integer> list = new ArrayList<>();
                    CpuNewsReportExportParam exportParam = cpuNewsReportExportParams.get(i - 1);
                    if(exportParam.getIsCpuColor() != null && exportParam.getIsCpuColor()){
                        list.add(9);
                    }
                    if(exportParam.getIsMemoryColor() != null && exportParam.getIsMemoryColor()){
                        list.add(3);
                    }
                    map.put(i,list);
                }
            }
            if(reportType == 1){
                String fileName = System.currentTimeMillis()+""; //导出文件名
                response.setContentType("application/vnd.ms-excel");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
                // 头的策略
                WriteCellStyle headWriteCellStyle = new WriteCellStyle();
                WriteFont headWriteFont = new WriteFont();
                headWriteFont.setFontHeightInPoints((short) 11);
                headWriteCellStyle.setWriteFont(headWriteFont);
                // 内容的策略
                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                WriteFont contentWriteFont = new WriteFont();
                // 字体大小
                contentWriteFont.setFontHeightInPoints((short) 12);
                contentWriteCellStyle.setWriteFont(contentWriteFont);
                // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
                HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
                MwCellColorSheetWriteHandler cellColorSheetWriteHandler = new MwCellColorSheetWriteHandler(map, IndexedColors.RED.getIndex());
                //创建easyExcel写出对象
                excelWriter = EasyExcel.write(response.getOutputStream(), CpuNewsReportExportParam.class).registerWriteHandler(horizontalCellStyleStrategy).registerWriteHandler(cellColorSheetWriteHandler).build();
            }else{
                excelWriter = exportReportSetNews("CPU信息", response, CpuNewsReportExportParam.class);
            }
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            if(reportType == 1){
                includeColumnFiledNames.add("time");
                includeColumnFiledNames.add("assetName");
                includeColumnFiledNames.add("ip");
                includeColumnFiledNames.add("diskUserRate");
                includeColumnFiledNames.add("maxMemoryUtilizationRate");
                includeColumnFiledNames.add("minMemoryUtilizationRate");
                includeColumnFiledNames.add("diskUser");
                includeColumnFiledNames.add("diskTotal");
                includeColumnFiledNames.add("maxValue");
                includeColumnFiledNames.add("avgValue");
                includeColumnFiledNames.add("minValue");
            }
            if(reportType == 2){
                includeColumnFiledNames.add("brand");
                includeColumnFiledNames.add("location");
                includeColumnFiledNames.add("assetName");
                includeColumnFiledNames.add("ip");
                includeColumnFiledNames.add("diskUserRate");
                includeColumnFiledNames.add("maxValue");
                includeColumnFiledNames.add("avgValue");
                includeColumnFiledNames.add("icmpResponseTime");
                includeColumnFiledNames.add("icmpPing");
            }
            if(reportType == 3){
                includeColumnFiledNames.add("time");
                includeColumnFiledNames.add("assetName");
                includeColumnFiledNames.add("ip");
                includeColumnFiledNames.add("cpuUtilizationRate");
                includeColumnFiledNames.add("memoryUtilizationRate");
            }
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(cpuNewsReportExportParams, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出CPU全部数据失败"+e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public void diskUseReportAllExport(TrendParam trendParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("磁盘使用率", response, DiskUseReportExportParam.class);
            List<DiskUseReportExportParam> diskUseReportExportParams = new ArrayList<>();
            trendParam.setPageNumber(1);
            trendParam.setPageSize(1000000);
            Reply reply = terraceManageService.selectReportDiskUse(trendParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList()))return;
                List<TrendDiskDto> trendDiskDtos = pageInfo.getList();
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
            }
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
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(diskUseReportExportParams, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出CPU全部数据失败"+e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public void assetsUsabilityReportAllExport(RunTimeQueryParam param, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            List<MwAssetsUsabilityParam> assetsUsabilityParams = new ArrayList<>();
            param.setPageNumber(1);
            param.setPageSize(1000000);
            Reply reply = terraceManageService.selectReportAssetsUsability(param,false);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList()))return;
                assetsUsabilityParams = pageInfo.getList();
            }
            excelWriter = exportReportSetNews("资产可用性", response, MwAssetsUsabilityParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("time");
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ip");
            includeColumnFiledNames.add("assetsUsability");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(assetsUsabilityParams, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出CPU全部数据失败"+e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public void lineFlowReportAllExport(TrendParam trendParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            int reportType = trendParam.getReportType();
            List<LineFlowReportParam> params = new ArrayList<>();
            trendParam.setPageNumber(1);
            trendParam.setPageSize(1000000);
            Reply reply = null;
            if(reportType == 1){
                reply = terraceManageService.selectReportLinkNews(trendParam);
            }
            if(reportType == 2){
                reply = terraceManageService.selectLylLinkFlowData(trendParam);
            }
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                PageInfo pageInfo = (PageInfo) reply.getData();
                if(CollectionUtils.isEmpty(pageInfo.getList()))return;
                params = pageInfo.getList();
            }
            excelWriter = exportReportSetNews("线路流量", response, LineFlowReportParam.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            if(reportType == 1){
                includeColumnFiledNames.add("time");
                includeColumnFiledNames.add("assetsName");
                includeColumnFiledNames.add("interfaceName");
                includeColumnFiledNames.add("acceptFlowMax");
                includeColumnFiledNames.add("acceptFlowMin");
                includeColumnFiledNames.add("acceptFlowAvg");
                includeColumnFiledNames.add("sendingFlowMax");
                includeColumnFiledNames.add("sendingFlowAvg");
                includeColumnFiledNames.add("sendingFlowMin");
            }
            if(CollectionUtils.isNotEmpty(params)){
                for (LineFlowReportParam param : params) {
                    param.setAcceptFlowMax(param.getAcceptFlowMax()+"("+param.getAcceptMaxValueTime()+")");
                    param.setAcceptFlowMin(param.getAcceptFlowMin()+"("+param.getAcceptMinValueTime()+")");
                    param.setSendingFlowMax(param.getSendingFlowMax()+"("+param.getSendMaxValueTime()+")");
                    param.setSendingFlowMin(param.getSendingFlowMin()+"("+param.getSendMinValueTime()+")");
                }
            }
           if(reportType == 2){
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
           }
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(params, sheet);
            log.info("导出成功");
        }catch (Exception e){
            log.error("导出CPU全部数据失败"+e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    /**
     * 设置导出信息
     * @param name
     * @param response
     * @param dtoclass
     * @return
     * @throws IOException
     */
    private ExcelWriter exportReportSetNews(String name,HttpServletResponse response,Class dtoclass) throws IOException {
        String fileName = System.currentTimeMillis()+""; //导出文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        //创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), dtoclass).registerWriteHandler(horizontalCellStyleStrategy).build();
        return excelWriter;
    }


    @Override
    public void assetsStatusDelayedExport(HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            //查询所有资产
            QueryTangAssetsParam qParam = new QueryTangAssetsParam();
            qParam.setUserId(userService.getAdmin());
            qParam.setIsQueryAssetsState(false);
            qParam.setPageNumber(1);
            qParam.setPageSize(Integer.MAX_VALUE);
            List<MwTangibleassetsTable> tangibleassetsTables = mwAssetsManager.getAssetsTable(qParam);
            if(CollectionUtils.isEmpty(tangibleassetsTables)){return;}
            //按照serverId分组
            Map<Integer, List<String>> groupMap = tangibleassetsTables.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
            Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
            tangibleassetsTables.forEach(item->{
                assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
            });
            Map<String, List<MwAssetsStatusDelayedReportDto>> statusAndDelayedInfo = getStatusAndDelayedInfo(groupMap, assetsMap);
            log.info("MWRreportExportServiceImpl{} assetsStatusDelayedExport() statusAndDelayedInfo::"+statusAndDelayedInfo.size());
            if(statusAndDelayedInfo == null || statusAndDelayedInfo.isEmpty()){return;}
            excelWriter = exportReportSetNews("状态与延时"+System.currentTimeMillis(),response,MwAssetsStatusDelayedReportDto.class);
            int sheetIndex = 0;
            //分页签导出数据
            for (String host : statusAndDelayedInfo.keySet()) {
                String hostName = host.split(",")[1];//页签名称
                List<MwAssetsStatusDelayedReportDto> dtoList = statusAndDelayedInfo.get(host);//导出数据
                log.info("MWRreportExportServiceImpl{} assetsStatusDelayedExport() hostName::"+hostName+"dtoList::"+dtoList.size());
                //数据排序
                dataSort(dtoList);
                HashSet<String> includeColumnFiledNames = new HashSet<>();
                includeColumnFiledNames.add("time");
                includeColumnFiledNames.add("status");
                includeColumnFiledNames.add("delayed");
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetIndex, hostName)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(dtoList, writeSheet);
                sheetIndex++;
            }
        }catch (Throwable e){
            log.error("报表导出:assetsStatusDelayedExport()",e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    private Map<String, List<MwAssetsStatusDelayedReportDto>> getStatusAndDelayedInfo(Map<Integer, List<String>> groupMap,Map<String,MwTangibleassetsTable> assetsMap){
        Map<String, List<MwAssetsStatusDelayedReportDto>> realData = new HashMap<>();
        //获取今天的日期
        List<Date> dates = MwVisualizedDateUtil.getToday();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Integer serverId : groupMap.keySet()) {
            List<String> hostIds = groupMap.get(serverId);
            //查询监控项数据
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyNameList(serverId, itemNames, hostIds,true);
            if(result == null || result.isFail()){continue;}
            List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
            log.info("MWRreportExportServiceImpl{} getStatusAndDelayedInfo() itemApplications::"+itemApplications.size());
            Map<String,ItemApplication> itemApplicationMap = new HashMap<>();
            itemApplications.forEach(item->{
                itemApplicationMap.put(item.getItemid(),item);
            });
            Map<String, List<ItemApplication>> typeMap = itemApplications.stream().collect(Collectors.groupingBy(item -> item.getValue_type()));
            Map<String,List<MwAssetsStatusDelayedReportDto>> statusDelayedReportDtoMap = new HashMap<>();
            for (String valueType : typeMap.keySet()) {
                List<String> itemIds = itemApplications.stream().map(ItemApplication::getItemid).collect(Collectors.toList());
                MWZabbixAPIResult historyResult = mwtpServerAPI.HistoryGetByTimeAndTypeASC(serverId, itemIds, dates.get(0).getTime() / 1000, dates.get(1).getTime() / 1000, Integer.parseInt(valueType));
                if(historyResult == null || historyResult.isFail()){continue;}
                List<MWItemHistoryDto> dtos = JSONArray.parseArray(String.valueOf(historyResult.getData()), MWItemHistoryDto.class);
                //按照itemId分组
                Map<String, List<MWItemHistoryDto>> itemMap = dtos.stream().collect(Collectors.groupingBy(item -> item.getItemid()));
                for (String itemId : itemMap.keySet()) {
                    ItemApplication application = itemApplicationMap.get(itemId);
                    List<MWItemHistoryDto> itemHistoryDtos = itemMap.get(itemId);
                    if(application == null || CollectionUtils.isEmpty(itemHistoryDtos)){continue;}
                    String name = application.getName();
                    String hostid = application.getHostid();
                    List<MwAssetsStatusDelayedReportDto> dtoList = new ArrayList<>();
                    if(name.contains(ICMP_PING)){
                        for (MWItemHistoryDto itemHistoryDto : itemHistoryDtos) {
                            String clock = itemHistoryDto.getClock();
                            String value = itemHistoryDto.getValue();
                            MwAssetsStatusDelayedReportDto delayedReportDto = new MwAssetsStatusDelayedReportDto();
                            delayedReportDto.setStatus(Integer.parseInt(value)==0?"异常":"正常");
                            delayedReportDto.setSortDate(Long.parseLong(clock));
                            delayedReportDto.setTime(format.format(new Date(Long.parseLong(clock)*1000)));
                            dtoList.add(delayedReportDto);
                        }
                    }
                    if(name.contains(ICMP_RESPONSE_TIME)){
                        for (MWItemHistoryDto itemHistoryDto : itemHistoryDtos) {
                            String clock = itemHistoryDto.getClock();
                            String lastValue = itemHistoryDto.getValue();
                            Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(lastValue), application.getUnits());
                            MwAssetsStatusDelayedReportDto delayedReportDto = new MwAssetsStatusDelayedReportDto();
                            delayedReportDto.setDelayed(convertedValue.get("value")+convertedValue.get("units"));
                            delayedReportDto.setSortDate(Long.parseLong(clock));
                            delayedReportDto.setTime(format.format(new Date(Long.parseLong(clock)*1000)));
                            dtoList.add(delayedReportDto);
                        }
                    }
                    List<MwAssetsStatusDelayedReportDto> mwAssetsStatusDelayedReportDtos = statusDelayedReportDtoMap.get(hostid);
                    if(mwAssetsStatusDelayedReportDtos == null){
                        statusDelayedReportDtoMap.put(hostid,dtoList);
                        continue;
                    }
                    mwAssetsStatusDelayedReportDtos.addAll(dtoList);
                }
            }
            for (String hostId : statusDelayedReportDtoMap.keySet()) {
                MwTangibleassetsTable tangibleassetsTable = assetsMap.get(serverId + hostId);
                if(tangibleassetsTable == null){continue;}
                List<MwAssetsStatusDelayedReportDto> dtoList = statusDelayedReportDtoMap.get(hostId);
                //按照时间分组
                List<MwAssetsStatusDelayedReportDto> newDtos = new ArrayList<>();
                Map<String, List<MwAssetsStatusDelayedReportDto>> listMap = dtoList.stream().collect(Collectors.groupingBy(item -> item.getTime()));
                for (String time : listMap.keySet()) {
                    List<MwAssetsStatusDelayedReportDto> reportDtos = listMap.get(time);
                    if(CollectionUtils.isEmpty(reportDtos)){continue;}
                    MwAssetsStatusDelayedReportDto newDto = new MwAssetsStatusDelayedReportDto();
                    newDto = reportDtos.get(0);
                    for (MwAssetsStatusDelayedReportDto reportDto : reportDtos) {
                        if(StringUtils.isNotBlank(reportDto.getStatus())){
                            newDto.setStatus(reportDto.getStatus());
                        }
                        if(StringUtils.isNotBlank(reportDto.getDelayed())){
                            newDto.setDelayed(reportDto.getDelayed());
                        }
                    }
                    newDtos.add(newDto);
                }
                realData.put(hostId+","+tangibleassetsTable.getAssetsName(),newDtos);
            }
        }
        return realData;
    }

    private void dataSort(List<MwAssetsStatusDelayedReportDto> dtoList){
        Collections.sort(dtoList, new Comparator<MwAssetsStatusDelayedReportDto>() {
            @Override
            public int compare(MwAssetsStatusDelayedReportDto o1, MwAssetsStatusDelayedReportDto o2) {
                if(o1.getSortDate() > o2.getSortDate()){
                    return 1;
                }
                if(o1.getSortDate() < o2.getSortDate()){
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 导出实时报表
     * @param reportParam
     * @param response
     */
    @Override
    public void exportRealTimeReport(MwCustomReportParam reportParam, HttpServletResponse response) {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("实时报表");
            HSSFRow row = sheet.createRow(0);
            //创建表头
            for (int i = 0; i < reportParam.getTableHeads().size(); i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(reportParam.getTableHeads().get(i));
            }
            reportParam.setPageSize(Integer.MAX_VALUE);
            //获取数据
            Reply reply = customReportService.getCustomReportInfo(reportParam);
            if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS){return;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<MwCustomReportDto> mwCustomReportDtos = pageInfo.getList();
            if(CollectionUtils.isEmpty(mwCustomReportDtos)){return;}
            HSSFRow tableHeads = sheet.getRow(0);
            for (int i = 0; i < mwCustomReportDtos.size(); i++) {
                //创建新的一行
                HSSFRow dataRow = sheet.createRow(i+1);
                List<MwCustomReportIndexDto> reportIndexDtos = mwCustomReportDtos.get(i).getReportIndexDtos();
                if(CollectionUtils.isEmpty(reportIndexDtos)){continue;}
                Map<String, String> indexMap = reportIndexDtos.stream().filter(item -> StringUtils.isNotBlank(item.getItemChnName())).
                                                                collect(Collectors.toMap(MwCustomReportIndexDto::getItemChnName, item -> item.getValue() + item.getUnits()));
                int count = 0;
                for (Cell cell : tableHeads) {
                    String stringCellValue = cell.getStringCellValue();
                    //获取该字段的值
                    String fieldValue = getRealTimeReportFieldValue(stringCellValue, mwCustomReportDtos.get(i), indexMap);
                    HSSFCell dataCell = dataRow.createCell(count);
                    dataCell.setCellValue(fieldValue);
                    count++;
                }
            }
            Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
            String fileName =  milliSecond + ".xls";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
        }catch (Throwable e){
            log.error("MWRreportExportServiceImpl{} exportRealTimeReport() ERROR::",e);
        }
    }

    /**
     * 获取实时报表对应列的值
     * @param stringCellValue
     * @param reportDto
     * @param indexMap
     * @return
     */
    private String getRealTimeReportFieldValue(String stringCellValue,MwCustomReportDto reportDto,Map<String, String> indexMap) throws Exception {
        //先获取枚举
        String name = MwRealTimeReportExportHeadEnum.getFieldByName(stringCellValue);
        if(StringUtils.isNotBlank(name)){
            //如果枚举获取到值，使用反射方式获取该字段的值
            Field field = reportDto.getClass().getDeclaredField(name);
            field.setAccessible(true);
            Object o = field.get(reportDto);
            if(o != null){return String.valueOf(o);}
        }
        //如果枚举没有获取到，需根据指标获取
        return indexMap.get(stringCellValue);
    }

    /**
     * 导出历史报表
     * @param reportParam
     * @param response
     */
    @Override
    public void exportHistoryReport(MwCustomReportParam reportParam, HttpServletResponse response) {
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportReportSetNews("历史数据导出"+System.currentTimeMillis(),response,MwHistoryReportExportDto.class);
            reportParam.setPageSize(Integer.MAX_VALUE);
            //查询数据
            Reply reply = customReportService.getCustomReportInfo(reportParam);
            if(reply == null || reply.getRes() != PaasConstant.RES_SUCCESS){return;}
            PageInfo pageInfo = (PageInfo) reply.getData();
            List<MwCustomReportDto> mwCustomReportDtos = pageInfo.getList();
            if(CollectionUtils.isEmpty(mwCustomReportDtos)){return;}
            List<MwHistoryReportExportDto> reportExportDtos = new ArrayList<>();
            for (MwCustomReportDto mwCustomReportDto : mwCustomReportDtos) {
                MwHistoryReportExportDto reportExportDto = new MwHistoryReportExportDto();
                reportExportDto.extractFrom(mwCustomReportDto,mwCustomReportDto.getReportIndexDtos().get(0));
                reportExportDtos.add(reportExportDto);
            }
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("assetIp");
            includeColumnFiledNames.add("businessSystem");
            includeColumnFiledNames.add("date");
            includeColumnFiledNames.add("itemChnName");
            includeColumnFiledNames.add("partitionName");
            includeColumnFiledNames.add("avgValue");
            includeColumnFiledNames.add("maxValue");
            includeColumnFiledNames.add("minValue");
            includeColumnFiledNames.add("units");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(reportExportDtos, sheet);
            log.info("导出成功");
        }catch (Throwable e){
            log.error("MWRreportExportServiceImpl{} exportHistoryReport() exportError::",e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }


    }
}

