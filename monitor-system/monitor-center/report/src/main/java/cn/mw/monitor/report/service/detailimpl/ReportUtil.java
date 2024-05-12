package cn.mw.monitor.report.service.detailimpl;

import cn.mw.monitor.report.dto.HistoryValueDto;
import cn.mw.monitor.report.dto.ItemData;
import cn.mw.monitor.report.dto.TrendDto;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDto;
import cn.mw.monitor.report.param.ExcelReportParam;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2020/12/21 15:01
 */
public class ReportUtil {
    public static List<HistoryValueDto> getValueData(MWZabbixAPIResult result) {
        List<HistoryValueDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(HistoryValueDto.builder().value(data.get("value").asDouble()).build());
                });
            }

        }
        return list;
    }

    public static List<HistoryValueDto> getTrendValueData(MWZabbixAPIResult result) {
        List<HistoryValueDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(HistoryValueDto.builder().value(data.get("value_avg").asDouble()).build());
                });
            }

        }
        return list;
    }

    public static Map<String,List<HistoryValueDto>> getValueDataMap(MWZabbixAPIResult result) {
        Map<String,List<HistoryValueDto>> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (null != node && node.size() > 0) {
                node.forEach(data -> {
                    String itemId = data.get("itemid").asText();
                    List<HistoryValueDto> list = map.get(itemId);
                    if(null == list){
                        list = new ArrayList<>();
                        map.put(itemId, list);
                    }
                    list.add(HistoryValueDto.builder()
                            .value(data.get("value").asDouble()).clock(data.get("clock").asLong()).build());
                });
            }

        }
        return map;
    }


    public static Map<String,List<HistoryValueDto>> getTrendValueDataMap(MWZabbixAPIResult result) {
        Map<String,List<HistoryValueDto>> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (null != node && node.size() > 0) {
                node.forEach(data -> {
                    String itemId = data.get("itemid").asText();
                    List<HistoryValueDto> list = map.get(itemId);
                    if(null == list){
                        list = new ArrayList<>();
                        map.put(itemId, list);
                    }
                    list.add(HistoryValueDto.builder()
                            .value(data.get("value_avg").asDouble()).clock(data.get("clock").asLong()).build());
                });
            }

        }
        return map;
    }

    public static Map<String,List<HistoryValueDto>> getValueData(MWZabbixAPIResult result, Map<String, String> itemHostMap) {
        Map<String,List<HistoryValueDto>> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    if(null != data.get("itemid")) {
                        String itemId = data.get("itemid").asText();
                        String hostId = itemHostMap.get(itemId);
                        if(StringUtils.isNotEmpty(hostId)){
                            List<HistoryValueDto> list = map.get(hostId);
                            if(null == list){
                               list = new ArrayList<>();
                                map.put(hostId, list);
                            }
                            list.add(HistoryValueDto.builder().value(data.get("value").asDouble()).clock(data.get("clock").asLong()).build());
                        }
                    }
                });
            }

        }
        return map;
    }


    public static Map<String,List<HistoryValueDto>> getTrendValueData(MWZabbixAPIResult result, Map<String, String> itemHostMap) {
        Map<String,List<HistoryValueDto>> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    if(null != data.get("itemid")) {
                        String itemId = data.get("itemid").asText();
                        String hostId = itemHostMap.get(itemId);
                        if(StringUtils.isNotEmpty(hostId)){
                            List<HistoryValueDto> list = map.get(hostId);
                            if(null == list){
                                list = new ArrayList<>();
                                map.put(hostId, list);
                            }
                            HistoryValueDto valueDto = HistoryValueDto.builder().value(data.get("value_avg").asDouble())
                                    .maxValue(data.get("value_max").asDouble())
                                    .minValue(data.get("value_min").asDouble())
                                    .clock(data.get("clock").asLong()).build();
                            list.add(valueDto);
                        }
                    }
                });
            }

        }
        return map;
    }



    public static List<HistoryValueDto> getValueTimeData(MWZabbixAPIResult result) {
        List<HistoryValueDto> list = new ArrayList<>();
        if (result.getCode() == 0) {
            JsonNode node = (JsonNode) result.getData();
            if (node.size() > 0) {
                node.forEach(data -> {
                    list.add(HistoryValueDto.builder().value(data.get("value").asDouble()).clock(data.get("clock").asLong()).itemid(data.get("itemid").asText()).build());
                });
            }

        }
        return list;
    }

    public static TrendDto getTrendDtoNotUnit(List<HistoryValueDto> list) {
        TrendDto trendDto = new TrendDto();
        if (null != list && list.size() > 0) {
            List<HistoryValueDto> maxDtos = list.stream().filter(item -> item.getMaxValue() != null).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(maxDtos)){
                trendDto.setValueMax(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble()));
            }else{
                trendDto.setValueMax(String.valueOf(maxDtos.stream().mapToDouble(HistoryValueDto::getMaxValue).max().getAsDouble()));
            }
            List<HistoryValueDto> minDtos = list.stream().filter(item -> item.getMinValue() != null).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(minDtos)){
                trendDto.setValueMin(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble()));
            }else{
                trendDto.setValueMin(String.valueOf(minDtos.stream().mapToDouble(HistoryValueDto::getMinValue).min().getAsDouble()));
            }
            trendDto.setValueAvg(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble()));
        } else {
            trendDto.setValueMax("0");
            trendDto.setValueMin("0");
            trendDto.setValueAvg("0");
        }
        return trendDto;
    }

    public static TrendDto getTrendDtoNotUnit1(List<HistoryValueDto> list) {
        TrendDto trendDto = new TrendDto();
        if (null != list && list.size() > 0) {
            trendDto.setValueAvg(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble()));
        } else {
            trendDto.setValueAvg("0");
        }
        return trendDto;
    }

    public static TrendDto getTrendDto(List<HistoryValueDto> list, String unit) {
        TrendDto trendDto = new TrendDto();
        if (null != list && list.size() > 0) {
            trendDto.setValueMax(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).max().getAsDouble()), unit));
            trendDto.setValueMin(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).min().getAsDouble()), unit));
            trendDto.setValueAvg(UnitsUtil.getValueWithUnits(String.valueOf(list.stream().mapToDouble(HistoryValueDto::getValue).average().getAsDouble()), unit));
        } else {
            trendDto.setValueMax(0 + unit);
            trendDto.setValueMin(0 + unit);
            trendDto.setValueAvg(0 + unit);
        }
        return trendDto;
    }

    public static List<ItemData> getItemDataResult(MWZabbixAPIResult result) {
        JsonNode resultData = (JsonNode) result.getData();
        List<ItemData> list = new ArrayList<>();
        if (result.getCode() == 0 && resultData.size() > 0) {
            resultData.forEach(item -> {
                ItemData itemData = new ItemData();
                itemData.setName(item.get("name").asText());
                itemData.setValue(item.get("lastvalue").asText() + item.get("units").asText());
                list.add(itemData);
            });
        }
        return list;
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
