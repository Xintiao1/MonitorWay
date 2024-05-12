package cn.mw.monitor.visualized.service.impl;

import cn.mw.monitor.visualized.dto.MwVisualizedViewDto;
import cn.mw.monitor.visualized.service.MwVisualizedExportService;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @ClassName MwVisualizedExportServiceImpl
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/8/24 16:31
 * @Version 1.0
 **/
@Service
public class MwVisualizedExportServiceImpl implements MwVisualizedExportService {

    @Autowired
    private MwVisualizedManageService manageService;

    /**
     * 可视化视图导出EXCEL
     * @param response
     * @param viewDto
     */
    @Override
    public void export(HttpServletResponse response, MwVisualizedViewDto viewDto) {
        try {
            Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
            String path = milliSecond + ".xls";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + path);
            ServletOutputStream outputStream = response.getOutputStream();
            Reply reply = manageService.visualizedUpdateQuery(viewDto);
            if (null == reply && reply.getRes() != PaasConstant.RES_SUCCESS);
            //取出数据
            MwVisualizedViewDto visualizedViewDto = (MwVisualizedViewDto) reply.getData();
            //获取前端组件数据，每一个元素是一个组件
            Map visualizedDatas = visualizedViewDto.getVisualizedDatas();
            //创建workbook，一个workbook对应一个excel
            HSSFWorkbook workbook = new HSSFWorkbook();
            //表头样式
            HSSFCellStyle headStyle = workbook.createCellStyle();
            HSSFFont headFont = workbook.createFont();
            headFont.setFontHeightInPoints((short) 12);
            headStyle.setFont(headFont);
            headStyle.setBorderTop(BorderStyle.THIN);
            headStyle.setFillBackgroundColor((short) 45333);
            headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headStyle.setBorderRight(BorderStyle.THIN);
            headStyle.setBorderLeft(BorderStyle.THIN);
            headStyle.setAlignment(HorizontalAlignment.CENTER); //水平居中
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER); //垂直居中
            headStyle.setWrapText(true); //自动换行
            headStyle.setFont(headFont);
            //获取组件中的数据
            if(visualizedDatas != null){
                List dataList = (List) visualizedDatas.get("nodes");
                for (Object visualizedData : dataList) {
                    Map<String,Object> map2 = (Map<String, Object>) visualizedData;
                    Map dataMap = (Map) map2.get("renderData");
                    List<Map<String,Object>> render = (List<Map<String, Object>>) dataMap.get("render");//组件中的数据
                    String name = (String) dataMap.get("name");//组件标题，作为导出的页签名称
                    //进行数据导出，每一个组件为一个页签
                    exportExcel(render,name,workbook);
                }
            }else{
                List<Map<String,Object>> render = new ArrayList<>();
                exportExcel(render,"sheet1",workbook);
            }
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void exportExcel(List<Map<String,Object>> render,String name,HSSFWorkbook workbook){
        if(CollectionUtils.isEmpty(render)){
            render = new ArrayList<>();
            Map<String,Object> map = new HashMap<>();
            map.put("无数据","需要选择指标对应数据");
            render.add(map);
        }
        //获取列名
        Set<String> columnNames = new HashSet<>();
        render.forEach(valueMap->{
            Set<String> keys = valueMap.keySet();
            columnNames.addAll(keys);
        });
        //创建页签,并设置页签名称
        HSSFSheet sheet = workbook.createSheet(name);
        //设置样式
        sheet.setDefaultColumnWidth(19);
        List<String> list = new ArrayList<>();
        //设置列名
        HSSFRow rowReportTitle = sheet.createRow(0);
        int column = 0;
        for (String columnName : columnNames) {
            HSSFCell cell = rowReportTitle.createCell(column);
            cell.setCellValue(columnName);
            column++;
            list.add(columnName);
        }
        //设置数据
        for (int i = 1; i <= render.size(); i++) {
            Map<String, Object> map = render.get(i - 1);
            HSSFRow row = sheet.createRow(i);
            int count = 0;
            for (String key : map.keySet()) {
                Object value = map.get(key);
                for (String s : list) {
                    if(key.equals(s)){
                        if(value == null){
                            row.createCell(count).setCellValue("");
                        }else{
                            row.createCell(count).setCellValue(value.toString());
                        }
                        break;
                    }
                }
                count++;
            }
        }
    }
}
