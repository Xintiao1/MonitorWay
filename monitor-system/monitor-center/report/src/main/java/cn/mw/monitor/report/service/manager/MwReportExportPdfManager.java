package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.param.CpuNewsReportExportParam;
import cn.mw.monitor.report.param.LineFlowReportParam;
import cn.mwpaas.common.utils.CollectionUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @ClassName MwReportExportPdfManager
 * @Description 报表导出PDF管理类
 * @Author gengjb
 * @Date 2022/4/18 10:27
 * @Version 1.0
 **/
@Slf4j
public class MwReportExportPdfManager {
    Document document = new Document();// 建立一个Document对象

    private static Font headfont;// 设置字体大小
    private static Font keyfont;// 设置字体大小
    private static Font textfont;// 设置字体大小

    static {
        //中文格式
        BaseFont bfChinese;
        try {
            // 设置中文显示
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            headfont = new Font(bfChinese, 18, Font.BOLD);// 设置字体大小
            keyfont = new Font(bfChinese, 13, Font.NORMAL);// 设置字体大小
            textfont = new Font(bfChinese, 10, Font.NORMAL);// 设置字体大小
        } catch (Exception e) {
            log.error("导出PDF设置中文失败", e);
        }
    }

    /**
     * 文成文件
     */
    public MwReportExportPdfManager(File file) {
        document.setPageSize(PageSize.A2);// 设置页面大小
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        } catch (Exception e) {
            log.error("设置PDF大小失败", e);
        }
    }

    public MwReportExportPdfManager() {

    }

    public void initFile(File file) {
        document.setPageSize(PageSize.A2);// 设置页面大小
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        } catch (Exception e) {
            log.error("初始化PDF大小失败", e);
        }
    }


    int maxWidth = 1024;

    /**
     * 为表格添加一个内容
     *
     * @param value 值
     * @param font  字体
     * @param align 对齐方式
     * @return 添加的文本框
     */
    public PdfPCell createCell(String value, Font font, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        cell.setPhrase(new Phrase(value, font));
        return cell;
    }

    /**
     * 为表格添加一个内容
     *
     * @param value 值
     * @param font  字体
     * @return 添加的文本框
     */
    public PdfPCell createCell(String value, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setFixedHeight(60);
        cell.setPaddingLeft(5.23f);
        cell.setPaddingBottom(5);
        cell.setPaddingTop(5);
        cell.setPhrase(new Phrase(value, font));
        return cell;
    }

    /**
     * 为表格添加一个内容
     *
     * @param value     值
     * @param font      字体
     * @param align     对齐方式
     * @param colspan   占多少列
     * @param boderFlag 是否有有边框
     * @return 添加的文本框
     */
    public PdfPCell createCell(String value, Font font, int align, int colspan,
                               boolean boderFlag) {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        cell.setColspan(colspan);
        cell.setPhrase(new Phrase(value, font));
        cell.setPadding(3.0f);
        if (!boderFlag) {
            cell.setBorder(0);
            cell.setPaddingTop(15.0f);
            cell.setPaddingBottom(8.0f);
        }
        return cell;
    }

    /**
     * 创建一个title
     */
    public Paragraph createTitle(String title){
        Paragraph paragraph = new Paragraph(title,headfont);
        paragraph.setSpacingBefore(5);
        paragraph.setSpacingAfter(5);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    /**
     * 创建一个表格对象
     *
     * @param colNumber 表格的列数
     * @return 生成的表格对象
     */
    public PdfPTable createTable(int colNumber) {
        PdfPTable table = new PdfPTable(colNumber);
        try {
            table.setWidthPercentage(100);
            table.setTotalWidth(maxWidth);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
        } catch (Exception e) {
            log.error("创建PDF表格失败", e);
        }
        return table;
    }


    public <T> void generatePDF(String[] head, List<T> list, int colNum) {
        PdfPTable table = createTable(colNum);
        byte[] data = new byte[0];
        try {
            String titleName = "";
            if(CollectionUtils.isNotEmpty(list)){
                T t = list.get(0);
                if(t instanceof LineFlowReportParam){
                    //蓝月亮流量报表
                    titleName = "流量详情报表";
                }
                if(t instanceof CpuNewsReportExportParam){
                    //蓝月亮CPU报表
                    titleName = "CPU内存报表";
                }

            }
            Paragraph title = createTitle(titleName);
            document.add(title);

            //设置表头
            for (int i = 0; i < colNum; i++) {
//                table.addCell(createCell(head[i], keyfont, Element.ALIGN_CENTER));
                table.addCell( createCell(head[i], keyfont));
            }
            if(CollectionUtils.isNotEmpty(list)){
                T t = list.get(0);
                if(t instanceof LineFlowReportParam){
                    //蓝月亮流量报表
                    lYLLinkReportData(table,list,colNum);
                }
                if(t instanceof CpuNewsReportExportParam){
                    //蓝月亮CPU报表
                    lYLCpuAndMemoryReportData(table,list,colNum);
                }
            }
        } catch (Exception e) {
            log.error("创建PDF表格数据失败", e);
        }
        //关闭流
        document.close();
    }

    /**
     * 设置蓝月亮流量报表数据
     */
    public <T> void lYLLinkReportData(PdfPTable table, List<T> list, int colNum) throws DocumentException {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            LineFlowReportParam t = (LineFlowReportParam) list.get(i);
            for (int j = 0; j < colNum; j++) {
                //添加数据
                if (j == 0) {
                    table.addCell(createCell(t.getTime(), textfont));
                }
                if (j == 1) {
                    table.addCell(createCell(t.getAssetsName(), textfont));
                }

                if (j == 2) {
                    table.addCell(createCell(t.getInterfaceName(), textfont));
                }

                if (j == 3) {
                    table.addCell(createCell(t.getAcceptFlowMax(), textfont));
                }

                if (j == 4) {
                    table.addCell(createCell(t.getAcceptFlowAvg(), textfont));
                }

                if (j == 5) {
                    table.addCell(createCell(t.getAcceptFlowMin(), textfont));
                }

                if (j == 6) {
                    table.addCell(createCell(t.getAcceptTotalFlow(), textfont));
                }

                if (j == 7) {
                    table.addCell(createCell(t.getSendingFlowMax(), textfont));
                }

                if (j == 8) {
                    table.addCell(createCell(t.getSendingFlowAvg(), textfont));
                }

                if (j == 9) {
                    table.addCell(createCell(t.getSendingFlowMin(), textfont));
                }

                if (j == 10) {
                    table.addCell(createCell(t.getSendTotalFlow(), textfont));
                }
            }
        }
        document.add(table);
    }


    /**
     * 设置蓝月亮流量报表数据
     */
    public <T> void lYLCpuAndMemoryReportData(PdfPTable table, List<T> list, int colNum) throws DocumentException {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CpuNewsReportExportParam t = (CpuNewsReportExportParam) list.get(i);
            for (int j = 0; j < colNum; j++) {
                //添加数据
                if (j == 0) {
                    table.addCell(createCell(t.getBrand(), textfont));
                }
                if (j == 1) {
                    table.addCell(createCell(t.getLocation(), textfont));
                }

                if (j == 2) {
                    table.addCell(createCell(t.getAssetName(), textfont));
                }

                if (j == 3) {
                    table.addCell(createCell(t.getIp(), textfont));
                }

                if (j == 4) {
                    table.addCell(createCell(t.getDiskUserRate(), textfont));
                }

                if (j == 5) {
                    table.addCell(createCell(t.getMaxValue(), textfont));
                }

                if (j == 6) {
                    table.addCell(createCell(t.getAvgValue(), textfont));
                }

                if (j == 7) {
                    table.addCell(createCell(t.getIcmpResponseTime(), textfont));
                }

                if (j == 8) {
                    table.addCell(createCell(t.getIcmpPing(), textfont));
                }
            }
        }
        document.add(table);
    }


    /**
     * 提供外界调用的接口，生成以head为表头，list为数据的pdf
     *
     * @param head //数据表头
     * @param list //数据
     * @return //excel所在的路径
     */
    public <T> String generatePDFs(String[] head, List<T> list, String filePath) {
        File outputFile = new File(filePath);
        //检测是否存在目录
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            log.error("创建文件失败", e1);
        }
        initFile(file);
        try {
            file.createNewFile();  //生成一个pdf文件
        } catch (IOException e) {
            log.error("创建Pdf文件失败", e);
        }
        new MwReportExportPdfManager(file).generatePDF(head, list, head.length);
        return filePath;
    }
}
