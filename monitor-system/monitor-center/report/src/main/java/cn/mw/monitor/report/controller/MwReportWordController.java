package cn.mw.monitor.report.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.ReportWordParam;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.report.service.manager.ReportExportManage;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.state.DateTimeTypeEnum;
import cn.mw.monitor.state.RuntimeReportState;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.ChartSingleSeriesRenderData;
import com.deepoove.poi.data.Charts;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.Tables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author syt
 * @Date 2021/9/29 15:26
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "导出word", tags = "")
public class MwReportWordController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MwReportWordController.class.getName());
    //模板上传目录
    static final String MODULE = "report-upload";
    @Autowired
    private MwReportService mwreportService;

    @Autowired
    private MWMessageService mwMessageService;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    private final static String classPath;

    @Autowired
    private ReportExportManage reportExportManage;

    @Value("${runState.barChart}")
    private Boolean runStateBarChart;


    static {
        classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    }


    @PostMapping("/report/word")
    @ResponseBody
    @ApiOperation(value = "导出word")
    public void wordOut(@RequestBody ReportWordParam param, HttpServletRequest request, HttpServletResponse response) {

        HashMap<String, Object> data = null;
        File file = null;
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;
        BufferedOutputStream out = null;
        try {
            String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator;
            //打成jar包以后读取不到文件
            ClassPathResource resource = new ClassPathResource("templates/reportTemplate.docx");
//            File sourceFile = resource.getFile();
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
            logger.error("导出word" + template);
            data = getRenderData(param);
            template.render(data);

            String fileName = UUIDUtils.getUUID();
            File outputFile = new File(temPath + fileName);
            //检测是否存在目录
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            FileOutputStream outFile = new FileOutputStream(temPath + fileName + ".docx");
            template.write(outFile);
            outFile.flush();
            outFile.close();
            template.close();
            //通过文件流读取到文件，再将文件通过response的输出流，返回给页面下载
            file = new File(temPath + fileName + ".docx");
            inputStream = new FileInputStream(file);
            //是否导出文档CPU与内存的柱状图
            if(runStateBarChart){
                String path = reportExportManage.runStateExportBarChart(inputStream, param, temPath);
                if(StringUtils.isNotBlank(path)){
                    file = new File(path);
                    inputStream = new FileInputStream(file);
                }
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            fileName = fileName + ".docx";
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[512];
            int bytesToRead = -1;
            while ((bytesToRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesToRead);
            }

        } catch (Throwable e) {
            logger.error("导出word失败！",e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (file != null) file.delete();//删除临时文件
                } catch (IOException e) {
                    logger.error("删除临时文件出错！",e);
                }
            }
        }
    }

    private HashMap<String, Object> getRenderData(ReportWordParam param) throws ParseException {
        HashMap<String, Object> data = new HashMap<>();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(date);
        data.put("dateTime",s);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
        String s1 = sdf1.format(date);
        data.put("dateTimeChinese",s1);
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
                if (param.getDateType()==6){
                    top.setChooseTime(MWUtils.getStartOrEndDate(param.getDateStart(),param.getDateEnd()));
                }
                if (top.getDataSize()==null||top.getDataSize()<=0){
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
                           if (top.getDateType()!= DateTimeTypeEnum.TODAY.getCode()){
                               for (int i = 0; i < listTop.size(); i++) {
                                   try{
                                       listTop.get(i).setAssetUtilization(listTop.get(i).getOutInterfaceAvgValue());
                                   }catch (Exception e){
                                       //遇到一场直接放弃
                                   }
                               }
                           }else{
                                    for (int i = 0; i < listTop.size(); i++) {
                                        try{
                                            listTop.get(i).setAssetUtilization(listTop.get(i).getOutInterfaceAvgValue());
                                        }catch (Exception e){
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
//        data.put("title2", "CPU利用率（TOP5）");
//        data.put("title3", "内存利用率（TOP5）");
//        data.put("title4", "丢包率（TOP5）");
//        data.put("title5", "磁盘利用率（TOP5）");
//        data.put("title6", "接口利用率（TOP5）");
//        data.put("title7", "资产可用率（TOP5）");


        //表格1标头字段设置
        //表格1标头字段设置
        headerData = Rows.of("分类状态", "安全设备","服务器", "网络设备")
                .textColor("000000")
                .bgColor("87CEEB").create();

        listRowList = new ArrayList<>();
        listRowList.add(headerData);
        RunTimeQueryParam assetsParam = new RunTimeQueryParam();
        assetsParam.setDateType(param.getDateType());
        assetsParam.setChooseTime(new ArrayList<>());
        if (param.getDateType()==6){
            assetsParam.setDateStart(MWUtils.getStartOrEndTime(param.getDateStart(),0));
            assetsParam.setDateEnd(MWUtils.getStartOrEndTime(param.getDateEnd(),1));
            assetsParam.setChooseTime(MWUtils.getStartOrEndDate(param.getDateStart(),param.getDateEnd()));
        }
        log.info("导出word资产数据，查询条件为"+assetsParam);
        reply = mwreportService.getRunTimeReportOfAeest(assetsParam);
        if (reply.getRes() == PaasConstant.RES_SUCCESS) {
            Map<String, List<Integer>> tableData1 = (Map<String, List<Integer>>) reply.getData();
            log.info("导出word资产数据，数据为"+tableData1);
            //饼状图
//            tableData1.get("total").remove(0);
            List<Integer> total = tableData1.get("total");
            total = new ArrayList<>(total);
            total.remove(0);
            ChartSingleSeriesRenderData pie = Charts.ofSingleSeries("资产状态", new String[]{"分类正常" + total.get(0), "分类异常" + total.get(1), "分类新增" + total.get(2)})
                    .series("", total.toArray(new Integer[]{}))
                    .create();
            logger.info("pie  信息 {}", tableData1.get("total").toArray(new Integer[]{}));
            data.put("LineCharts1", pie);

            List<String> strings = Arrays.asList("分类总数", "分类正常", "分类异常", "分类新增");
            for (int i = 0; i < 4; i++) {
                //一行数据
                listRowList.add(Rows.create(strings.get(i)
                        , null == tableData1.get("安全设备")?"":tableData1.get("安全设备").get(i).toString()
                        , null == tableData1.get("服务器")?"":tableData1.get("服务器").get(i).toString()
                        , null == tableData1.get("网络设备")?"":tableData1.get("网络设备").get(i).toString()));
            }
            data.put("table1", Tables.create(listRowList.toArray(new RowRenderData[]{})));
        }

//        //折线图
//        RunTimeQueryParam chartParam = new RunTimeQueryParam();
//        chartParam.setDateType(param.getDateType());
//        chartParam.setDateStart(param.getDateStart());
//        chartParam.setDateEnd(param.getDateEnd());
//        List<String> titles = Arrays.asList("告警次数", "资产数量", "异常状态");
//        for (Integer trendType : param.getTrendTypes()) {
//            chartParam.setTrendType(trendType);
//            reply = mwreportService.getRunTimeReportTrend(chartParam);
//            if (reply.getRes() == PaasConstant.RES_SUCCESS) {
//                PeriodTrendDto periodTrendDto = (PeriodTrendDto) reply.getData();
//
//                ChartMultiSeriesRenderData chart = Charts.ofMultiSeries(titles.get(trendType), periodTrendDto.getDate().toArray(new String[]{}))
//                        .addSeries("", periodTrendDto.getCount().toArray(new Integer[]{}))
//                        .create();
//                data.put("LineCharts" + (trendType + 2), chart);
//            }
//
//        }

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
                    String big = param.get(i).getAssetUtilization().replaceAll("%","");
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
}
