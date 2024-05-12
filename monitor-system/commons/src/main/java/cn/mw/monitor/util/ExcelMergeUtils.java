package cn.mw.monitor.util;

import cn.mw.monitor.util.entity.PoiModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 单元格合并导出
 *
 * @author qzg
 * @date 2022/10/27
 */
@Slf4j
public class ExcelMergeUtils {

    /**
     * 判断是不是数字
     *
     * @param str
     * @return
     */
    private static boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /*
     * @param objData    数据
     * @param fileName   文件名
     * @param sheetName  sheet名
     * @param columns    表头
     * @param mergeIndex 需要合并的列号集合 ,在不确定的情况下 有多少列就填充多少条
     * @param request
     * @param response
     * @return
     */
    public static Boolean exportToExcelForXlsx(String excelName, String title, List<String> lableName, List<String> lable, List<Map> dataset, List mergeIndex, HttpServletResponse response) {
        boolean flag = false;
        // 创建工作薄
        XSSFWorkbook wb = new XSSFWorkbook();
        // sheet1
        XSSFSheet sheet1 = wb.createSheet(title);
        XSSFCellStyle headStyle = (XSSFCellStyle) wb.createCellStyle();
        XSSFFont headFont = (XSSFFont) wb.createFont();
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

        //表头
        sheet1.createFreezePane(0, 1);//冻结表头
        XSSFRow sheet1row1 = sheet1.createRow((short) 0);
        sheet1row1.setHeight((short) 480);
        //写入表头
        if (lableName != null && lableName.size() > 0) {
            for (int i = 0; i < lableName.size(); i++) {
                String column = lableName.get(i);
                //列
                sheet1.setColumnWidth(i, 6000);
                XSSFCell cell = sheet1row1.createCell(i);
                cell.setCellValue(column);
                cell.setCellStyle(headStyle);
            }
        }

        int dataSatrtIndex = 1;//数据开始行
        boolean isMerge = false;
        if (mergeIndex != null && mergeIndex.size() != 0) {
            isMerge = true;
        }
        Map<Integer, PoiModel> poiModels = new HashMap<Integer, PoiModel>();
        Iterator<Map> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            //数据行
            XSSFRow row = sheet1.createRow(index+dataSatrtIndex);
            Map map = it.next();
            int j = 0;
            String firstCol = "";
            String poiModelContent = "";
            for (int i = 0; i < lable.size(); i++) {
                Object obj = map.get(lable.get(i));
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                //数据列
                String content = "";
                if (obj != null) {
                    if (obj.toString().contains(".") && isNumeric(obj.toString())) {
                        content = decimalFormat.format(Float.valueOf(obj.toString()));
                    } else if (obj.toString().contains("-") && obj.toString().contains(":")) {
                        content = String.valueOf(obj).split("\\.")[0];
                    } else {
                        content = String.valueOf(obj);
                    }
                }
                if(i==0){
                    firstCol = content;
                }
                if (isMerge && mergeIndex.contains(j)) {
                    //如果该列需要合并
                    PoiModel poiModel = poiModels.get(j);
                    if(i==0 && poiModel!=null){
                        poiModelContent = poiModels.get(0).getContent();
                    }
                    if (poiModel == null) {
                        poiModel = new PoiModel();
                        poiModel.setContent(content);
                        poiModel.setRowIndex(index + dataSatrtIndex);
                        poiModel.setCellIndex(j);
                        poiModels.put(j, poiModel);
                    } else {
                        if (!poiModel.getContent().equals(content) || !poiModelContent.equals(firstCol)) {
                            //如果不同了，则将前面的数据合并写入
                            XSSFRow lastRow = sheet1.getRow(poiModel.getRowIndex());
                            XSSFCell lastCell = lastRow.createCell(poiModel.getCellIndex());//创建列
                            lastCell.setCellValue(poiModel.getContent());
                            //合并单元格
                            //根据第一列数据判断，如果第一列数据相同，则后面的列数可以进行单元格合并。
                            if (poiModel.getRowIndex() != index + dataSatrtIndex - 1 ) {
                                sheet1.addMergedRegion(new CellRangeAddress(poiModel.getRowIndex(), index + dataSatrtIndex - 1, poiModel.getCellIndex(), poiModel.getCellIndex()));
                            }
                            //将新数据存入
                            poiModel.setContent(content);
                            poiModel.setRowIndex(index + dataSatrtIndex);
                            poiModel.setCellIndex(j);
                            poiModels.put(j, poiModel);
                        }
                    }
                    row.createCell(j);//创建单元格
                } else {//该列不需要合并
                    //数据列
                    XSSFCell xssfCell = row.createCell(j);
                    xssfCell.setCellValue(content);
                }
                j++;
            }


            index++;
        }
        //将最后一份存入
        if (poiModels != null && poiModels.size() != 0) {
            for (Integer key : poiModels.keySet()) {
                PoiModel poiModel = poiModels.get(key);
                XSSFRow lastRow = sheet1.getRow(poiModel.getRowIndex());
                XSSFCell lastCell = lastRow.getCell(poiModel.getCellIndex());
                lastCell.setCellValue(poiModel.getContent());
                //合并单元格
                if (poiModel.getRowIndex() != index + dataSatrtIndex - 1) {
                    sheet1.addMergedRegion(new CellRangeAddress(poiModel.getRowIndex(), index + dataSatrtIndex - 1, poiModel.getCellIndex(), poiModel.getCellIndex()));
                }
            }
        }

        try {
            Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
            String fileName = excelName + milliSecond + ".xlsx";
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();

//            File file = new File("D:/" + fileName);
//            FileOutputStream fileOutputStream = new FileOutputStream(file);
//            wb.write(fileOutputStream);
//            fileOutputStream.close();

            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();

            wb.close();
            flag = true;
        } catch (FileNotFoundException e) {
            flag = false;
            log.error("ExportExcel:文件不存在, case by {}", e);

        } catch (IOException e) {
            flag = false;
            log.error("ExportExcel:文件写入错误, case by {}", e);
        }
        return flag;
    }
}
