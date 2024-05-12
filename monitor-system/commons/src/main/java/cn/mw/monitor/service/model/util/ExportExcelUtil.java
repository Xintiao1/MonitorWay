package cn.mw.monitor.service.model.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * @author qzg
 * @date 2021/6/24
 */

//这里所需要的参数是  表格文件名  表格的sheet的名称   表格的头部字段名称  头部对应的字段 和数据集合，以及response
public class ExportExcelUtil {
    private static final Logger logger = LoggerFactory.getLogger("ExportExcel");

    private final static Long PAGE_SIZE = 10000l;

    public static boolean exportExcel(String excelName, String title, List<String> lableName, List<String> lable, List<Map> dataset, String pattern, HttpServletResponse response) throws IOException {

        Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
//        String fileName = excelName + "-" + milliSecond;
        String fileName = excelName + milliSecond + ".xls";
//        String fileNames = new String(fileName.getBytes("utf-8"),"iso8859-1")+".xlsx";

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        ServletOutputStream outputStream = response.getOutputStream();
        boolean flag = false;
        Workbook workbook = null;
        if (fileName.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (fileName.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            try {
                throw new Exception("invalid file name, should be xls or xlsx");
            } catch (Exception e) {
                logger.error("fail to exportExcel param{}, case by {}", "", e);
            }
        }

        Sheet sheet = workbook.createSheet(title);
        CellStyle style = workbook.createCellStyle();
        //表头样式
        HSSFCellStyle headStyle = (HSSFCellStyle) workbook.createCellStyle();
        HSSFFont headFont = (HSSFFont) workbook.createFont();
        headFont.setFontHeightInPoints((short) 11);
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
        // 列名
        Row row = sheet.createRow(0);
        for (int i = 0; i < lableName.size(); i++) {
            Cell cell = row.createCell(i);
            sheet.setColumnWidth(i, 6000);
            cell.setCellValue(lableName.get(i));
            cell.setCellStyle(headStyle);
        }
        if (dataset != null) {
            Iterator<Map> it = dataset.iterator();
            int index = 0;
            while (it.hasNext()) {
                index++;
                row = sheet.createRow(index);
                Map map = it.next();
                Set<String> mapKey = (Set<String>) map.keySet();
                for (int i = 0; i < lable.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object obj = map.get(lable.get(i));
                    if (obj != null && obj instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        cell.setCellValue(sdf.format(obj));
                    } else if (obj != null && obj instanceof LocalDateTime) {
                        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        cell.setCellValue(df.format((TemporalAccessor) obj));
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);
                    } else if (obj instanceof Boolean) {
                        cell.setCellValue((Boolean) obj);
                    } else if (obj instanceof Long) {
                        cell.setCellValue((Long) obj);
                    } else if (obj instanceof List) {
                        List val = (List) obj;
                    } else {
                        cell.setCellValue((String) obj);
                    }
                }
            }
        }
        FileOutputStream fileOutputStream;
        try {
//            File file = new File("D:/" + fileName);
//            fileOutputStream = new FileOutputStream(file);
//            workbook.write(fileOutputStream);
//            fileOutputStream.close();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            workbook.close();
            flag = true;
        } catch (FileNotFoundException e) {
            flag = false;
            logger.error("ExportExcel:文件不存在, case by {}", e);

        } catch (IOException e) {
            flag = false;
            logger.error("ExportExcel:文件写入错误, case by {}", e);
        }
        return flag;
    }

    public static String exportExcel(String excelName, String title, List<String> lableName, List<String> lable, List<Map> dataset, String path, String pattern) throws IOException {
        Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String fileName = excelName + ".xls";
        String filePaths = path + "/" + fileName;
        boolean flag = false;
        Workbook workbook = null;
        if (fileName.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (fileName.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            try {
                throw new Exception("invalid file name, should be xls or xlsx");
            } catch (Exception e) {
                logger.error("fail to exportExcel param{}, case by {}", "", e);
            }
        }

        Sheet sheet = workbook.createSheet(title);
        CellStyle style = workbook.createCellStyle();
        //表头样式
        HSSFCellStyle headStyle = (HSSFCellStyle) workbook.createCellStyle();
        HSSFFont headFont = (HSSFFont) workbook.createFont();
        headFont.setFontHeightInPoints((short) 11);
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
        // 列名
        Row row = sheet.createRow(0);
        for (int i = 0; i < lableName.size(); i++) {
            Cell cell = row.createCell(i);
            sheet.setColumnWidth(i, 6000);
            cell.setCellValue(lableName.get(i));
            cell.setCellStyle(headStyle);
        }
        if (dataset != null) {
            Iterator<Map> it = dataset.iterator();
            int index = 0;
            while (it.hasNext()) {
                index++;
                row = sheet.createRow(index);
                Map map = it.next();
                Set<String> mapKey = (Set<String>) map.keySet();
                for (int i = 0; i < lable.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object obj = map.get(lable.get(i));
                    if (obj != null && obj instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        cell.setCellValue(sdf.format(obj));
                    } else if (obj != null && obj instanceof LocalDateTime) {
                        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        cell.setCellValue(df.format((TemporalAccessor) obj));
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);
                    } else if (obj instanceof Boolean) {
                        cell.setCellValue((Boolean) obj);
                    } else if (obj instanceof Long) {
                        cell.setCellValue((Long) obj);
                    } else if (obj instanceof List) {
                        List val = (List) obj;
                    } else {
                        cell.setCellValue((String) obj);
                    }
                }
            }
        }
        FileOutputStream fileOutputStream;
        try {
            File file = new File(filePaths);
            if(file.exists()){
                file.delete();
            }
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            logger.error("ExportExcel:文件不存在, case by {}", e);

        } catch (IOException e) {
            logger.error("ExportExcel:文件写入错误, case by {}", e);
        }
        return filePaths;
    }

}


