package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.constant.ReportConstant;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.param.IpAddressReportParam;
import cn.mw.monitor.report.service.IpAddressReportService;
import cn.mw.monitor.report.service.IpAssressRepostExpoetService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName
 * @Description IP地址报表导出实现类
 * @Author gengjb
 * @Date 2023/3/13 11:32
 * @Version 1.0
 **/
@Service
@Slf4j
public class IpAssressRepostExpoetServiceImpl implements IpAssressRepostExpoetService {

    @Autowired
    private IpAddressReportService ipAddressReportService;

    private final float lineHeight = (float)20.0;

    private final int colSpan = 1;

    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Override
    public void ipAddressReportExportExcel(IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response) {
        try {
            param.setDateType(3);
            //获取数据
            Reply reply = ipAddressReportService.getIpAddressExportData(param);
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS){
                return;
            }
            IpAddressReportDto addressReportDto = (IpAddressReportDto) reply.getData();
            exportIpAddressExcel(addressReportDto,response);
        }catch (Throwable e){
            log.error("IP地址报表导出excel失败",e);
        }
    }

    @Override
    public void ipAddressReportExportPdf(IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response) {
        try {
            param.setDateType(3);
            //获取数据
            Reply reply = ipAddressReportService.getIpAddressExportData(param);
            if (null == reply || reply.getRes() != PaasConstant.RES_SUCCESS){
                return;
            }
            IpAddressReportDto addressReportDto = (IpAddressReportDto) reply.getData();
            exportPdf(addressReportDto,response);
        }catch (Throwable e){
            log.error("IP地址报表导出pdf失败",e);
        }
    }

    private void exportIpAddressExcel(IpAddressReportDto addressReportDto,HttpServletResponse response) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();// 创建一个Excel文件
        createSheet1(workbook,addressReportDto.getIpAddressSegmentDto());
        createSheet2(workbook,addressReportDto.getUtilizationTopNMap());
        createSheet3(workbook,addressReportDto.getClassifyDtoMap());
        createSheet4(workbook,addressReportDto.getUpdateNumberMap());
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
    }


    private void createSheet1(HSSFWorkbook workbook, List<IpAddressUtilizationDto> utilizationDtos){
        HSSFSheet sheet = workbook.createSheet(IpAddressColumnEnum.TITLE_CENSUS.getName());// 创建一个Excel的Sheet
        // Sheet样式
        HSSFCellStyle sheetStyle = workbook.createCellStyle();
        HSSFCellStyle headstyle = workbook.createCellStyle();
        // 列头的样式
        HSSFCellStyle columnHeadStyle = workbook.createCellStyle();
        excelStyle(workbook,sheet,sheetStyle,headstyle,columnHeadStyle);
        // 创建第一行
        HSSFRow row0 = sheet.createRow(0);
        // 设置行高
        row0.setHeight((short) 500);
        // 创建第一列
        HSSFCell cell0 = row0.createCell(0);
        cell0.setCellValue(new HSSFRichTextString(IpAddressColumnEnum.TITLE_CENSUS.getName()));
        cell0.setCellStyle(headstyle);
        /**
         * 合并单元格
         *    第一个参数：第一个单元格的行数（从0开始）
         *    第二个参数：第二个单元格的行数（从0开始）
         *    第三个参数：第一个单元格的列数（从0开始）
         *    第四个参数：第二个单元格的列数（从0开始）
         */
        CellRangeAddress range = new CellRangeAddress(0, 0, 0, 4);
        sheet.addMergedRegion(range);
        Map<Integer,Integer> maxWidth = new HashMap<>();
        // 创建第二行
        HSSFRow row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.NAME.getName()));
        maxWidth.put(0,IpAddressColumnEnum.NAME.getName().getBytes().length*256+200);
        row1.createCell(1).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.IP_SEGMENT_AMOUNT.getName()));
        maxWidth.put(1,IpAddressColumnEnum.IP_SEGMENT_AMOUNT.getName().getBytes().length*256+200);
        row1.createCell(2).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.UTILIZATION_LTEQUALTOFIFTY.getName()));
        maxWidth.put(2,IpAddressColumnEnum.UTILIZATION_LTEQUALTOFIFTY.getName().getBytes().length*256+200);
        row1.createCell(3).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.UTILIZATION_FIFTYTOEIGHTY.getName()));
        maxWidth.put(3,IpAddressColumnEnum.UTILIZATION_FIFTYTOEIGHTY.getName().getBytes().length*256+200);
        row1.createCell(4).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.UTILIZATION_GTEQUALTOEIGHTY.getName()));
        maxWidth.put(4,IpAddressColumnEnum.UTILIZATION_GTEQUALTOEIGHTY.getName().getBytes().length*256+200);

        int i = 2;
        for (IpAddressUtilizationDto utilizationDto : utilizationDtos) {
            HSSFRow hssfRow = sheet.createRow(i);
            hssfRow.createCell(0).setCellValue(new HSSFRichTextString(utilizationDto.getGroupName()));
            hssfRow.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(utilizationDto.getIpAddressSegmentAmount())));
            hssfRow.createCell(2).setCellValue(new HSSFRichTextString(String.valueOf(utilizationDto.getLtEqualToFiftyAmount())));
            hssfRow.createCell(3).setCellValue(new HSSFRichTextString(String.valueOf(utilizationDto.getFiftyToEightyAmount())));
            hssfRow.createCell(4).setCellValue(new HSSFRichTextString(String.valueOf(utilizationDto.getGtEqualToEightyAmount())));
            i++;
        }
        for (int j = 0;j<5;j++){
            sheet.setColumnWidth(j,maxWidth.get(j));
        }
    }

    private void createSheet2(HSSFWorkbook workbook,Map<String,List<IpAddressUtilizationTopNDto>> map){
        HSSFSheet sheet = workbook.createSheet(IpAddressColumnEnum.TITLE_UTILIZATION_TOPN.getName());// 创建一个Excel的Sheet
        // Sheet样式
        HSSFCellStyle sheetStyle = workbook.createCellStyle();
        HSSFCellStyle headstyle = workbook.createCellStyle();
        // 列头的样式
        HSSFCellStyle columnHeadStyle = workbook.createCellStyle();
        excelStyle(workbook,sheet,sheetStyle,headstyle,columnHeadStyle);
        // 创建第一行
        HSSFRow row0 = sheet.createRow(0);
        // 设置行高
        row0.setHeight((short) 500);
        // 创建第一列
        HSSFCell cell0 = row0.createCell(0);
        cell0.setCellValue(new HSSFRichTextString(IpAddressColumnEnum.TITLE_UTILIZATION_TOPN.getName()));
        cell0.setCellStyle(headstyle);
        //合并单元格
        CellRangeAddress range = new CellRangeAddress(0, 0, 0, 1);
        sheet.addMergedRegion(range);
        Map<Integer,Integer> maxWidth = new HashMap<>();
        int row = 1;
        for (Map.Entry<String, List<IpAddressUtilizationTopNDto>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<IpAddressUtilizationTopNDto> value = entry.getValue();
            // 创建第一行
            HSSFRow row1 = sheet.createRow(row);
            // 设置行高
            row1.setHeight((short) 500);
            // 创建第一列
            HSSFCell cell1 = row1.createCell(0);
            cell1.setCellValue(new HSSFRichTextString(key));
            cell1.setCellStyle(columnHeadStyle);
            CellRangeAddress range2 = new CellRangeAddress(row, row, 0, 1);
            sheet.addMergedRegion(range2);
            row++;
            // 创建第一行
            HSSFRow columnNamerow = sheet.createRow(row);
            columnNamerow.setHeight((short) 300);
            columnNamerow.createCell(0).setCellValue(new HSSFRichTextString(ReportConstant.IP_UTILIZATION_TOPN_COLUMN.get(0)));
            columnNamerow.createCell(1).setCellValue(new HSSFRichTextString(ReportConstant.IP_UTILIZATION_TOPN_COLUMN.get(1)));
            row++;
            for (IpAddressUtilizationTopNDto ipAddressUtilizationTopNDto : value) {
                HSSFRow hssfRow = sheet.createRow(row);
                hssfRow.createCell(0).setCellValue(new HSSFRichTextString(ipAddressUtilizationTopNDto.getNetWorksName()));
                maxWidth.put(0,Math.max(ipAddressUtilizationTopNDto.getNetWorksName().getBytes().length*256+200,maxWidth.get(0)==null?0:maxWidth.get(0)));
                hssfRow.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(ipAddressUtilizationTopNDto.getCurrUtilization())));
                maxWidth.put(1,Math.max(String.valueOf(ipAddressUtilizationTopNDto.getCurrUtilization()).getBytes().length*256+200,maxWidth.get(0)==null?0:maxWidth.get(0)));
                row++;
            }
        }
        for (int j = 0;j<2;j++){
            sheet.setColumnWidth(j,maxWidth.get(j));
        }
    }

    private void createSheet3(HSSFWorkbook workbook,Map<String,IpAddressOperateClassifyDto> map){
        HSSFSheet sheet = workbook.createSheet(IpAddressColumnEnum.TITLE_OPERATECLASSOFT_CENSUS.getName());// 创建一个Excel的Sheet
        // Sheet样式
        HSSFCellStyle sheetStyle = workbook.createCellStyle();
        HSSFCellStyle headstyle = workbook.createCellStyle();
        // 列头的样式
        HSSFCellStyle columnHeadStyle = workbook.createCellStyle();
        excelStyle(workbook,sheet,sheetStyle,headstyle,columnHeadStyle);
        // 创建第一行
        HSSFRow row0 = sheet.createRow(0);
        // 设置行高
        row0.setHeight((short) 500);
        // 创建第一列
        HSSFCell cell0 = row0.createCell(0);
        cell0.setCellValue(new HSSFRichTextString(IpAddressColumnEnum.TITLE_OPERATECLASSOFT_CENSUS.getName()));
        cell0.setCellStyle(headstyle);
        /**
         * 合并单元格
         */
        CellRangeAddress range = new CellRangeAddress(0, 0, 0, 1);
        sheet.addMergedRegion(range);
        int row = 1;
        for (Map.Entry<String, IpAddressOperateClassifyDto> entry : map.entrySet()) {
            String key = entry.getKey();
            IpAddressOperateClassifyDto value = entry.getValue();
            // 创建第一行
            HSSFRow row1 = sheet.createRow(row);
            // 设置行高
            row1.setHeight((short) 500);
            // 创建第一列
            HSSFCell cell1 = row1.createCell(0);
            cell1.setCellValue(new HSSFRichTextString(key));
            cell1.setCellStyle(columnHeadStyle);
            CellRangeAddress range2 = new CellRangeAddress(row, row, 0, 1);
            sheet.addMergedRegion(range2);
            row++;
            // 创建第一行
            HSSFRow columnNamerow = sheet.createRow(row);
            columnNamerow.setHeight((short) 300);
            columnNamerow.createCell(0).setCellValue(new HSSFRichTextString(ReportConstant.IP_OPERATE_CLASSIFY_COLUMN.get(0)));
            columnNamerow.createCell(1).setCellValue(new HSSFRichTextString(ReportConstant.IP_OPERATE_CLASSIFY_COLUMN.get(1)));
            row++;
            HSSFRow hssfRow1 = sheet.createRow(row);
            hssfRow1.createCell(0).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.DISTRIBUTION.getName()));
            hssfRow1.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(value.getDisOperateNumber())));
            row++;
            HSSFRow hssfRow2 = sheet.createRow(row);
            hssfRow2.createCell(0).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.UPDATE.getName()));
            hssfRow2.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(value.getUpdateOperateNumber())));
            row++;
            HSSFRow hssfRow3 = sheet.createRow(row);
            hssfRow3.createCell(0).setCellValue(new HSSFRichTextString(IpAddressColumnEnum.RETRIEVE.getName()));
            hssfRow3.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(value.getRetrieveOperateNumber())));
            row++;
        }
        sheet.setColumnWidth(0,6856);
        sheet.setColumnWidth(1,3000);
    }

    private void createSheet4(HSSFWorkbook workbook,Map<String,List<IpAddressUpdateNumberDto>> map){
        HSSFSheet sheet = workbook.createSheet(IpAddressColumnEnum.TITLE_UPDATE.getName());// 创建一个Excel的Sheet
        // Sheet样式
        HSSFCellStyle sheetStyle = workbook.createCellStyle();
        HSSFCellStyle headstyle = workbook.createCellStyle();
        // 列头的样式
        HSSFCellStyle columnHeadStyle = workbook.createCellStyle();
        excelStyle(workbook,sheet,sheetStyle,headstyle,columnHeadStyle);
        // 创建第一行
        HSSFRow row0 = sheet.createRow(0);
        // 设置行高
        row0.setHeight((short) 500);
        // 创建第一列
        HSSFCell cell0 = row0.createCell(0);
        cell0.setCellValue(new HSSFRichTextString(IpAddressColumnEnum.TITLE_UPDATE_TOPN.getName()));
        cell0.setCellStyle(headstyle);
        /**
         * 合并单元格
         */
        CellRangeAddress range = new CellRangeAddress(0, 0, 0, 1);
        sheet.addMergedRegion(range);
        Map<Integer,Integer> maxWidth = new HashMap<>();
        int row = 1;
        for (Map.Entry<String, List<IpAddressUpdateNumberDto>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<IpAddressUpdateNumberDto> value = entry.getValue();
            // 创建第一行
            HSSFRow row1 = sheet.createRow(row);
            // 设置行高
            row1.setHeight((short) 500);
            // 创建第一列
            HSSFCell cell1 = row1.createCell(0);
            cell1.setCellValue(new HSSFRichTextString(key));
            cell1.setCellStyle(columnHeadStyle);
            CellRangeAddress range2 = new CellRangeAddress(row, row, 0, 1);
            sheet.addMergedRegion(range2);
            row++;
            // 创建第一行
            HSSFRow columnNamerow = sheet.createRow(row);
            columnNamerow.setHeight((short) 300);
            columnNamerow.createCell(0).setCellValue(new HSSFRichTextString(ReportConstant.IP_UPDATE_NUMBER_COLUMN.get(0)));
            columnNamerow.createCell(1).setCellValue(new HSSFRichTextString(ReportConstant.IP_UPDATE_NUMBER_COLUMN.get(1)));
            row++;
            for (IpAddressUpdateNumberDto dto : value) {
                HSSFRow hssfRow = sheet.createRow(row);
                hssfRow.createCell(0).setCellValue(new HSSFRichTextString(dto.getGroupName()));
                maxWidth.put(0,Math.max(dto.getGroupName().getBytes().length*256+200,maxWidth.get(0)==null?0:maxWidth.get(0)));
                hssfRow.createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(dto.getUpdateNumber())));
                maxWidth.put(1,Math.max(String.valueOf(dto.getUpdateNumber()).getBytes().length*256+200,maxWidth.get(0)==null?0:maxWidth.get(0)));
                row++;
            }
        }
        for (int j = 0;j<2;j++){
            sheet.setColumnWidth(j,maxWidth.get(j));
        }
    }


    private void excelStyle(HSSFWorkbook workbook,HSSFSheet sheet,HSSFCellStyle sheetStyle, HSSFCellStyle headstyle,HSSFCellStyle columnHeadStyle){
        // 设置列的样式
        for (int i = 0; i <= 14; i++) {
            sheet.setDefaultColumnStyle((short) i, sheetStyle);
        }
        // 设置字体
        HSSFFont headfont = workbook.createFont();
        headfont.setFontName("黑体");
        headfont.setFontHeightInPoints((short) 20);// 字体大小
        // 另一个样式
        headstyle.setFont(headfont);
        headstyle.setLocked(true);
        headstyle.setWrapText(true);// 自动换行
        // 另一个字体样式
        HSSFFont columnHeadFont = workbook.createFont();
        columnHeadFont.setFontName("宋体");
        columnHeadFont.setFontHeightInPoints((short) 15);
        // 列头的样式
        columnHeadStyle.setFont(columnHeadFont);
        columnHeadStyle.setLocked(true);
        columnHeadStyle.setWrapText(true);
    }

    /**
     * 导出PDF
     */
    private void exportPdf(IpAddressReportDto addressReportDto,HttpServletResponse response) throws IOException, DocumentException {
        String uuid = UUID.randomUUID() + ".pdf";
        //模板上传目录
        String MODULE = "report-upload";
        String temPath = new File(filePath + File.separator + MODULE).getAbsolutePath() + File.separator;
        String saveFilePathAndName = temPath + uuid;
        File outputFile = new File(saveFilePathAndName);
        //检测是否存在目录
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        File file = new File(saveFilePathAndName);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            log.error("创建文件失败", e1);
        }
        Document document = initFile(file);
        try {
            file.createNewFile();  //生成一个pdf文件
        } catch (IOException e) {
            log.error("创建Pdf文件失败", e);
        }
        //处理中文显示问题，使用资源字体
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font dateTitle = new Font(bfChinese,7, Font.NORMAL);
        Font title = new Font(bfChinese,20,Font.BOLD);//文字加粗
        Font textFont = new Font(bfChinese,15,Font.NORMAL);//文字正常
        createUtilizationPdf(title,document,textFont,addressReportDto.getIpAddressSegmentDto());
        createUtilizationTopNPdf(title,document,textFont,addressReportDto.getUtilizationTopNMap(),bfChinese);
        createClassifyPdf(title,document,textFont,addressReportDto.getClassifyDtoMap(),bfChinese);
        createUpdateNumberPdf(title,document,textFont,addressReportDto.getUpdateNumberMap(),bfChinese);
        document.close();
        //通过文件流读取到文件，再将文件通过response的输出流，返回给页面下载
        File f = new File(saveFilePathAndName);
        FileInputStream fileInputStream = new FileInputStream(f);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(uuid.getBytes("utf-8")));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[512];
        int bytesToRead = -1;
        while ((bytesToRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesToRead);
        }
    }


    public Document initFile(File file) {
        Document document = new Document();
        document.setPageSize(PageSize.A1);// 设置页面大小
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        } catch (Exception e) {
            log.error("初始化PDF大小失败", e);
        }
        return document;
    }

    /**
     * 设置分配/变更次数TopNPDF信息
     * @param title 标题字体
     * @param document 文档
     * @param textFont 内容字体
     * @param updateNumberMap 数据
     * @throws DocumentException
     */
    private void createUpdateNumberPdf(Font title, Document document, Font textFont, Map<String, List<IpAddressUpdateNumberDto>> updateNumberMap, BaseFont bfChinese) throws DocumentException {
        document.newPage();
        Paragraph paragraph = createTitle(title, IpAddressColumnEnum.TITLE_UPDATE_TOPN.getName());
        document.add(paragraph);
        for (Map.Entry<String, List<IpAddressUpdateNumberDto>> entry : updateNumberMap.entrySet()) {
            String key = entry.getKey();
            List<IpAddressUpdateNumberDto> value = entry.getValue();
            //小标题
            Font font = new Font(bfChinese,17,Font.BOLD);//文字加粗
            Paragraph smallParagraph = createTitle(font, key);
            document.add(smallParagraph);
            //生成一个2列的表格
            PdfPTable table = new PdfPTable(2);
            for (String s : ReportConstant.IP_UPDATE_NUMBER_COLUMN) {
                createTableCell(s, textFont, table, lineHeight, colSpan);
            }
            for (IpAddressUpdateNumberDto numberDto : value) {
                for (int i = 0;i < 2;i++){
                    if(i == 0){
                        createTableCell(numberDto.getGroupName(), textFont, table, lineHeight, colSpan);
                    }
                    if(i == 1){
                        createTableCell(String.valueOf(numberDto.getUpdateNumber()), textFont, table, lineHeight, colSpan);
                    }
                }
            }
            document.add(table);
        }
    }

    /**
     * 设置IP管理里操作分类统计PDF信息
     * @param title 标题字体
     * @param document 文档
     * @param textFont 内容字体
     * @param classifyDtoMap 数据
     * @throws DocumentException
     */
    private void createClassifyPdf(Font title, Document document, Font textFont, Map<String, IpAddressOperateClassifyDto> classifyDtoMap, BaseFont bfChinese) throws DocumentException {
        document.newPage();
        Paragraph paragraph = createTitle(title, IpAddressColumnEnum.TITLE_OPERATECLASSOFT_CENSUS.getName());
        document.add(paragraph);
        for (Map.Entry<String, IpAddressOperateClassifyDto> entry : classifyDtoMap.entrySet()) {
            String key = entry.getKey();
            IpAddressOperateClassifyDto classifyDto = entry.getValue();
            //小标题
            Font font = new Font(bfChinese,17,Font.BOLD);//文字加粗
            Paragraph smallParagraph = createTitle(font, key);
            document.add(smallParagraph);
            //生成一个2列的表格
            PdfPTable table = new PdfPTable(2);
            for (String s : ReportConstant.IP_OPERATE_CLASSIFY_COLUMN) {
                createTableCell(s, textFont, table, lineHeight, colSpan);
            }
            for (int i = 0;i < 3;i++){
                if(i == 0){
                    for (int j = 0;j < 2;j++){
                        if(j == 0){
                            createTableCell(IpAddressColumnEnum.DISTRIBUTION.getName(), textFont, table, lineHeight, colSpan);
                        }
                        if(j == 1){
                            createTableCell(String.valueOf(classifyDto.getDisOperateNumber()), textFont, table, lineHeight, colSpan);
                        }
                    }
                }

                if(i == 1){
                    for (int j = 0;j < 2;j++){
                        if(j == 0){
                            createTableCell(IpAddressColumnEnum.RETRIEVE.getName(), textFont, table, lineHeight, colSpan);
                        }
                        if(j == 1){
                            createTableCell(String.valueOf(classifyDto.getRetrieveOperateNumber()), textFont, table, lineHeight, colSpan);
                        }
                    }
                }

                if(i == 2){
                    for (int j = 0;j < 2;j++){
                        if(j == 0){
                            createTableCell(IpAddressColumnEnum.UPDATE.getName(), textFont, table, lineHeight, colSpan);
                        }
                        if(j == 1){
                            createTableCell(String.valueOf(classifyDto.getUpdateOperateNumber()), textFont, table, lineHeight, colSpan);
                        }
                    }
                }
            }
            document.add(table);
        }
    }

    /**
     * 设置网段IP利用率TopNPDF信息
     * @param title 标题字体
     * @param document 文档
     * @param textFont 内容字体
     * @param utilizationTopNMap 数据
     * @throws DocumentException
     */
    private void createUtilizationTopNPdf(Font title, Document document, Font textFont, Map<String, List<IpAddressUtilizationTopNDto>> utilizationTopNMap,BaseFont bfChinese) throws DocumentException {
        document.newPage();
        Paragraph paragraph = createTitle(title, IpAddressColumnEnum.TITLE_UTILIZATION_TOPN.getName());
        document.add(paragraph);
        for (Map.Entry<String, List<IpAddressUtilizationTopNDto>> entry : utilizationTopNMap.entrySet()) {
            String key = entry.getKey();
            List<IpAddressUtilizationTopNDto> value = entry.getValue();
            //小标题
            Font font = new Font(bfChinese,17,Font.BOLD);//文字加粗
            Paragraph smallParagraph = createTitle(font, key);
            document.add(smallParagraph);
            //生成一个2列的表格
            PdfPTable table = new PdfPTable(2);
            for (String s : ReportConstant.IP_UTILIZATION_TOPN_COLUMN) {
                createTableCell(s, textFont, table, lineHeight, colSpan);
            }
            for (IpAddressUtilizationTopNDto ipAddressUtilizationTopNDto : value) {
                for (int i = 0;i < 2;i++){
                    if(i == 0){
                        createTableCell(ipAddressUtilizationTopNDto.getNetWorksName(), textFont, table, lineHeight, colSpan);
                    }
                    if(i == 1){
                        createTableCell(String.valueOf(ipAddressUtilizationTopNDto.getCurrUtilization()), textFont, table, lineHeight, colSpan);
                    }
                }
            }
            document.add(table);
        }
    }


    /**
     * 设置IP地址使用率统计PDF信息
     * @param title 标题字体
     * @param document 文档
     * @param textFont 内容字体
     * @param utilizationDtos 数据
     * @throws DocumentException
     */
    private void createUtilizationPdf(Font title,Document document,Font textFont,List<IpAddressUtilizationDto> utilizationDtos) throws DocumentException {
        //生成一个5列的表格
        PdfPTable table = new PdfPTable(5);
        Paragraph paragraph = createTitle(title, IpAddressColumnEnum.TITLE_CENSUS.getName());
        document.add(paragraph);
        for (String column : ReportConstant.IP_UTILIZATION_COLUMN) {
            createTableCell(column, textFont, table, lineHeight, colSpan);
        }
        for (IpAddressUtilizationDto utilizationDto : utilizationDtos) {
            for (int i = 0;i < 5;i++){
                if(i == 0){
                    createTableCell(utilizationDto.getGroupName(), textFont, table, lineHeight, colSpan);
                }
                if(i == 1){
                    createTableCell(String.valueOf(utilizationDto.getIpAddressSegmentAmount()), textFont, table, lineHeight, colSpan);
                }
                if(i == 2){
                    createTableCell(String.valueOf(utilizationDto.getLtEqualToFiftyAmount()), textFont, table, lineHeight, colSpan);
                }
                if(i == 3){
                    createTableCell(String.valueOf(utilizationDto.getFiftyToEightyAmount()), textFont, table, lineHeight, colSpan);
                }
                if(i == 4){
                    createTableCell(String.valueOf(utilizationDto.getGtEqualToEightyAmount()), textFont, table, lineHeight, colSpan);
                }
            }
        }
        document.add(table);
    }

    //创建标题
    private Paragraph createTitle(Font font, String title){
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(5);
        paragraph.setSpacingAfter(5);
        paragraph.setAlignment(1);
        return paragraph;
    }

    private static void createTableCell(String text, Font font, PdfPTable table, float lineHeight, int colSapn) {
        PdfPCell cell;
        cell = new PdfPCell(new Paragraph(text,font));
        //合并单元格列
        cell.setColspan(colSapn);
        //设置水平居中
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        //设置垂直居中
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(lineHeight);
        //将单元格内容添加到表格中
        table.addCell(cell);
    }
}
