package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.report.dto.RunStateBarChartDto;
import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.ReportWordParam;
import cn.mw.monitor.report.service.MwReportService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Tables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xwpf.usermodel.XWPFChart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 报表导出管管理
 * @date 2023/9/15 14:12
 */
@Component
@Slf4j
public class ReportExportManage {

    @Autowired
    private MwReportService mwreportService;

    private final String CPU_UTILIZATION = "CPU_UTILIZATION";


    private final List<String> ITEM_NAMES = Arrays.asList("CPU_UTILIZATION","MEMORY_UTILIZATION");
    private final String MEMORY_UTILIZATION = "MEMORY_UTILIZATION";

    private final String X_NAME = "名称";

    private final String Y_NAME = "值(单位:%)";

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWUserCommonService userCommonService;


    /**
     * 运行状态报表导出柱状图
     */
    public String runStateExportBarChart(InputStream inputStream1,ReportWordParam param,String temPath){
        try {
            File file = new File(temPath);
            InputStream inputStream = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(inputStream);
            RunTimeQueryParam queryParam = getRunStateQueryTime(param);
            List<RunStateBarChartDto> barChartDtos = getRunStateInfo(queryParam);
            if(CollectionUtils.isEmpty(barChartDtos)){return null;}
            Map<String, List<RunStateBarChartDto>> listMap = barChartDtos.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeName() + item.getItemName()));
            for (Map.Entry<String, List<RunStateBarChartDto>> entry : listMap.entrySet()) {
                List<RunStateBarChartDto> chartDtos = entry.getValue();
                //数据分组
                List<List<RunStateBarChartDto>> partition = Lists.partition(chartDtos, 5);
                for (List<RunStateBarChartDto> runStateBarChartDtos : partition) {
                    //设置柱状图
                    XWPFChart chart = document.createChart(15 * Units.EMU_PER_CENTIMETER, 10 * Units.EMU_PER_CENTIMETER);
                    //设置图表标题
                    String title = runStateBarChartDtos.get(0).getAssetsTypeName()+"     "+runStateBarChartDtos.get(0).getItemName();
                    chart.setTitleText(title);
                    chart.setTitleOverlay(false);
                    XDDFChartLegend legend = chart.getOrAddLegend();
                    legend.setPosition(LegendPosition.BOTTOM);//图例位子
                    //创建横座标
                    XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                    xAxis.setTitle(X_NAME);
                    //获取资产名称
                    List<String> assetsNames = runStateBarChartDtos.stream().map(RunStateBarChartDto::getAssetsName).collect(Collectors.toList());
                    //设置横坐标数据
                    XDDFCategoryDataSource xAxisSource = XDDFDataSourcesFactory.fromArray(assetsNames.toArray(new String[0]));
                    //设置纵坐标
                    XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);
                    yAxis.setTitle(Y_NAME);
                    yAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
                    List<Double> values = runStateBarChartDtos.stream().map(RunStateBarChartDto::getValue).collect(Collectors.toList());
                    XDDFNumericalDataSource<Double> yAxisSource = XDDFDataSourcesFactory.fromArray(values.toArray(new Double[0]));
                    XDDFBarChartData barChart = (XDDFBarChartData) chart.createData(ChartTypes.BAR, xAxis, yAxis);
                    barChart.setBarDirection(BarDirection.COL);
                    XDDFBarChartData.Series barSeries = (XDDFBarChartData.Series) barChart.addSeries(xAxisSource, yAxisSource);
                    chart.plot(barChart);
                }
            }
            String fileName = UUIDUtils.getUUID();
            FileOutputStream outFile = new FileOutputStream(temPath + fileName + ".docx");
            document.write(outFile);
            outFile.close();
            document.close();
            return temPath + fileName + ".docx";
        }catch (Throwable e){
            log.error("ReportExportManage{} runStateExportBarChart() ERROR",e);
        }
        return null;
    }


    /**
     * 获取运行状态CPU数据
     */
    private List<RunStateBarChartDto> getRunStateInfo(RunTimeQueryParam queryParam){
        List<RunStateBarChartDto> barChartDtos = new ArrayList<>();
        for (String itemName : ITEM_NAMES) {
            queryParam.setItemName(itemName);
            Reply reply = mwreportService.getRunTimeItemUtilization(queryParam);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS) {
                List<RunTimeItemValue> timeItemValues = (List<RunTimeItemValue>) reply.getData();
                if(CollectionUtils.isEmpty(timeItemValues)){return barChartDtos;}
                List<String> assetsIds = timeItemValues.stream().map(RunTimeItemValue::getAssetsId).collect(Collectors.toList());
                Map<String, String> assetsTypeMap = getAssetsTypeinfo(assetsIds);
                timeItemValues.forEach(item->{
                    RunStateBarChartDto chartDto = new RunStateBarChartDto();
                    chartDto.extractFrom(item,assetsTypeMap.get(item.getAssetsId()));
                    barChartDtos.add(chartDto);
                });
            }
        }
        return barChartDtos;
    }

    /**
     * 根据资产ID获取资产类型信息
     */
    private Map<String, String> getAssetsTypeinfo(List<String> assetsIds){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userCommonService.getAdmin());
        assetsParam.setAssetsIds(assetsIds);
        List<MwTangibleassetsTable> mwTangAssetses = mwAssetsManager.getAssetsTable(assetsParam);
        //分组，key为ID，value为类型
        Map<String, String> assetsTypeMap = mwTangAssetses.stream().collect(Collectors.toMap(MwTangibleassetsTable::getId, MwTangibleassetsTable::getAssetsTypeName));
        return assetsTypeMap;
    }

    /**
     * 获取查询参数
     * @param param
     * @throws ParseException
     */
    private RunTimeQueryParam getRunStateQueryTime(ReportWordParam param) throws ParseException {
        RunTimeQueryParam queryParam = new RunTimeQueryParam();
        queryParam.setDateType(param.getDateType());
        queryParam.setDateStart(param.getDateStart());
        queryParam.setDateEnd(param.getDateEnd());
        if (param.getDateType()==6){
            queryParam.setChooseTime(MWUtils.getStartOrEndDate(param.getDateStart(),param.getDateEnd()));
        }
        queryParam.setDataSize(Integer.MAX_VALUE);
        queryParam.setTimingType(false);
        return queryParam;
    }
}
