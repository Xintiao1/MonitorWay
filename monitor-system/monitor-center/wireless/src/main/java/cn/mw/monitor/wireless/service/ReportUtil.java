package cn.mw.monitor.wireless.service;

import cn.mw.monitor.report.dto.ItemData;
import cn.mw.monitor.report.param.ExcelReportParam;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.wireless.dto.*;
import cn.mw.zbx.MWZabbixAPIResult;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.databind.JsonNode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/6/23
 */
public class ReportUtil {
    public static List<WirelessHistoryValueDto> getValueData(MWZabbixAPIResult result) {
        List<WirelessHistoryValueDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(WirelessHistoryValueDto.builder().value(data.get("value").asDouble()).build());
                });
            }

        }
        return list;
    }

    public static List<WirelessHistoryValueDto> getValueTimeData(MWZabbixAPIResult result) {
        List<WirelessHistoryValueDto> list = new ArrayList<>();

        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    BigDecimal big = BigDecimal.valueOf(Double.valueOf(data.get("value").asDouble()));
                    Map<String, String> convertedValue = UnitsUtil.getConvertedValue(big, "B");
                    list.add(WirelessHistoryValueDto.builder().value(data.get("value").asDouble()).clock(data.get("clock").asLong())
                            .strValue(convertedValue.get("value") + convertedValue.get("units")).build());
                });
            }

        }
        return list;
    }

    public static List<WirelessHistoryValueByLongDto> getValueLongData(MWZabbixAPIResult result) {
        List<WirelessHistoryValueByLongDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(WirelessHistoryValueByLongDto.builder().value(data.get("value").asLong()).clock(getDateByStamp(data.get("clock").asLong()*1000)).build());
                });
            }

        }
        return list;
    }

    public static List<WirelessHistoryValueByDoubleDto> getValueDoubleData(MWZabbixAPIResult result) {
        List<WirelessHistoryValueByDoubleDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(WirelessHistoryValueByDoubleDto.builder().value(data.get("value").asDouble()).clock(getDateByStamp(data.get("clock").asLong()*1000)).build());
                });
            }

        }
        return list;
    }
    public static String getDateByStamp(Long l){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(l);
        return sdf.format(date);
    }

    public static List<WirelessHistoryValueByLongDto> getValueDataAndUnits(MWZabbixAPIResult result) {
        List<WirelessHistoryValueByLongDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(WirelessHistoryValueByLongDto.builder().value(data.get("value").asLong()).clock(getDateByStamp(data.get("clock").asLong()*1000)).build());
                });
            }

        }
        return list;
    }
    public static List<WirelessHistoryValueByDoubleDto> getValueDoubleDataAndUnits(MWZabbixAPIResult result) {
        List<WirelessHistoryValueByDoubleDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(WirelessHistoryValueByDoubleDto.builder().value(data.get("value").asDouble()).clock(getDateByStamp(data.get("clock").asLong()*1000)).build());
                });
            }

        }
        return list;
    }
    public static WirelessTrendDto getWirelessTrendDtoNotUnit(List<WirelessHistoryValueDto> list) {
        WirelessTrendDto WirelessTrendDto = new WirelessTrendDto();
        if (null != list && list.size() > 0) {
            WirelessTrendDto.setValueMax(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).max().getAsDouble()));
            WirelessTrendDto.setValueMin(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).min().getAsDouble()));
            WirelessTrendDto.setValueAvg(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).average().getAsDouble()));
        } else {
            WirelessTrendDto.setValueMax("0");
            WirelessTrendDto.setValueMin("0");
            WirelessTrendDto.setValueAvg("0");
        }
        return WirelessTrendDto;
    }

    public static WirelessTrendDto getWirelessTrendDto(List<WirelessHistoryValueDto> list, String unit) {
        WirelessTrendDto WirelessTrendDto = new WirelessTrendDto();
        if (null != list && list.size() > 0) {
            WirelessTrendDto.setValueMax(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).max().getAsDouble()), unit));
            WirelessTrendDto.setValueMin(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).min().getAsDouble()), unit));
            WirelessTrendDto.setValueAvg(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(WirelessHistoryValueDto::getValue).average().getAsDouble()), unit));
        } else {
            WirelessTrendDto.setValueMax(0 + unit);
            WirelessTrendDto.setValueMin(0 + unit);
            WirelessTrendDto.setValueAvg(0 + unit);
        }
        return WirelessTrendDto;
    }

    public static List<List> getSubLists(List allData, int size) {
        List<List> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }


    public static ExcelWriter getExcelWriter(ExcelReportParam uParam, HttpServletResponse response, Class dtoclass) throws IOException {
        String fileName = null; //导出文件名
        if (uParam.getName() != null && !uParam.getName().equals("")) {
            fileName = uParam.getName();
        } else {
            fileName = System.currentTimeMillis() + "";
        }
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
}
