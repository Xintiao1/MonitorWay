package cn.mw.monitor.assetsTemplate.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qzg
 * @date 2021/6/24
 */

//这里所需要的参数是  表格文件名  表格的sheet的名称   表格的头部字段名称  头部对应的字段 和数据集合，以及response
@Slf4j
public class ExportExcel {
    public static boolean exportExcel(String excelName, String title, List<String> lableName, List<String> lable, List<Map> dataset, String pattern, HttpServletResponse response) throws IOException {

        Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
//        String fileName = excelName + "-" + milliSecond;
        String fileName = "资产模板导出"+milliSecond + ".xls";
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
                log.error("fail to exportExcel , cause:{}", e);
            }
        }

        Sheet sheet = workbook.createSheet(title);
        CellStyle style = workbook.createCellStyle();

        // 列名
        Row row = sheet.createRow(0);
        for (int i = 0; i < lableName.size(); i++) {
            Cell cell = row.createCell(i);
            sheet.setColumnWidth(i, 5000);
            cell.setCellValue(lableName.get(i));
        }

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
                } else if (obj instanceof Integer) {
                    cell.setCellValue((Integer) obj);
                } else if (obj instanceof Double) {
                    cell.setCellValue((Double) obj);
                } else if (obj instanceof Boolean) {
                    cell.setCellValue((Boolean) obj);
                } else {
                    cell.setCellValue((String) obj);
                }
            }
        }
        FileOutputStream fileOutputStream;
        try {
            //文件导出到D盘
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
            log.error("fail to exportExcel:文件不存在 , cause:{}", e);
        } catch (IOException e) {
            flag = false;
            log.error("fail to exportExcel:文件写入错误, cause:{}", e);

        }
        return flag;
    }


    public static String createExcel(String name, Map data, HttpServletResponse response) throws IOException {
        Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String path = name + milliSecond + ".xls";
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + path);
        ServletOutputStream outputStream = response.getOutputStream();
        Map<String, String> ziDuan = (Map<String, String>) data.get("ziDuan");
        List listData=(List) data.get("listData");
        //listData头部插入一条空数据，用于循环表头数据。
        listData.add(0,Arrays.asList());
        Object[] keys = ziDuan.keySet().toArray();
        String[] ziDuanKeys = new String[keys.length];
        for (int k = 0; k < keys.length; k++) {
            String temp = keys[k].toString();
            ziDuanKeys[k] = temp;
        }
        try {
            File newFile = new File(path);
            newFile.createNewFile();
            ////System.out.println("创建文件成功:" + path);
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet();
            //表头样式
            HSSFCellStyle headStyle = wb.createCellStyle();
            HSSFFont headFont = wb.createFont();
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

            for (int i = 0; i < listData.size(); i++) {
                HSSFRow row = sheet.createRow(i);
                Object obj = listData.get(i);
                for (int j = 0; j < ziDuanKeys.length; j++) {
                    HSSFCell cell = row.createCell(j);
                    if (i == 0) {
                        sheet.setColumnWidth(j, 6000);
                        cell.setCellValue(new HSSFRichTextString(ziDuan.get(ziDuanKeys[j])));
                        cell.setCellStyle(headStyle);
                    }
                    else {
                        String ziDuanName = (String) ziDuanKeys[j];
                        ////System.out.println(ziDuanName);
                        ziDuanName = ziDuanName.replaceFirst(ziDuanName
                                .substring(0, 1), ziDuanName.substring(0, 1).toUpperCase());
                        ziDuanName = "get" + ziDuanName;
                        Class clazz = Class.forName(obj.getClass().getName());
                        Method[] methods = clazz.getMethods();
                        Pattern pattern = Pattern.compile(ziDuanName);
                        Matcher mat = null;
                        for (Method m : methods) {
                            mat = pattern.matcher(m.getName());
                            if (mat.find()) {
                                Object shuXing = m.invoke(obj, null);
                                if (shuXing != null) {
                                    cell.setCellValue(shuXing.toString());//这里可以做数据格式处理
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            }
                        }

                    }
                }
            }
//            OutputStream out = new FileOutputStream("D:/" + path);
//            wb.write(out);//写入File
//            out.flush();
//            out.close();

            wb.write(outputStream);//下载到桌面
            outputStream.flush();
            outputStream.close();
            wb.close();
            return null;
        } catch (Exception e) {
            log.error("fail to createExcel, cause:{}", e);
            return null;
        }
    }
}


