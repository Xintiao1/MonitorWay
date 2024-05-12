package cn.mw.module.solarwind.service;


import cn.mw.module.solarwind.dto.InterfaceReportDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.UUID;


/**
 * 生成pdf
 *
 * @author zcr
 */
@Slf4j
public class MWCreatePdf {
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
            keyfont = new Font(bfChinese, 15, Font.BOLD);// 设置字体大小
            textfont = new Font(bfChinese, 15, Font.NORMAL);// 设置字体大小
        } catch (Exception e) {
            log.error("导出PDF设置中文失败", e);
        }
    }

    /**
     * 文成文件
     */
    public MWCreatePdf(File file) {
        document.setPageSize(PageSize.A1);// 设置页面大小
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
        } catch (Exception e) {
            log.error("设置PDF大小失败", e);
        }
    }

    public MWCreatePdf() {

    }

    public void initFile(File file) {
        document.setPageSize(PageSize.A1);// 设置页面大小
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
            ClassPathResource resource = new ClassPathResource("/zgbank.png");
            InputStream in = resource.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            data = out.toByteArray();
            Image image = Image.getInstance(data);
            int x = 50;
            int y = 20;
            image.setAbsolutePosition(x + document.leftMargin(), PageSize.A1.getHeight() - y -
                    image.getHeight() - document.topMargin());
            document.add(image);

            // 添加空格，避免图片与表格冲突
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));
            table.addCell(createCell("  ", keyfont, Element.ALIGN_LEFT, colNum, false));

            //设置表头
            for (int i = 0; i < colNum; i++) {
                table.addCell(createCell(head[i], keyfont, Element.ALIGN_CENTER));
            }

            if (null != list && list.size() > 0) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    InterfaceReportDto t = (InterfaceReportDto) list.get(i);
                    for (int j = 0; j < colNum; j++) {
                        //添加数据
                        if (j == 0) {
                            table.addCell(createCell(t.getCaption(), textfont));
                        }
                        if (j == 1) {
                            table.addCell(createCell(t.getInBandwidth(), textfont));
                        }

                        if (j == 2) {
                            table.addCell(createCell(t.getInMaxbps(), textfont));
                        }

                        if (j == 3) {
                            table.addCell(createCell(t.getInMinbps(), textfont));
                        }

                        if (j == 4) {
                            table.addCell(createCell(t.getInAveragebps(), textfont));
                        }

                        if (j == 5) {
                            table.addCell(createCell(t.getInMaxUse(), textfont));
                        }

                        if (j == 6) {
                            table.addCell(createCell(t.getInAvgUse(), textfont));
                        }

                        if (j == 7) {
                            table.addCell(createCell(t.getInProportionTen(), textfont));
                        }

                        if (j == 8) {
                            table.addCell(createCell(t.getInProportionFifty(), textfont));
                        }

                        if (j == 9) {
                            table.addCell(createCell(t.getInProportionEighty(), textfont));
                        }

                        if (j == 10) {
                            table.addCell(createCell(t.getInProportionHundred(), textfont));
                        }

                        if (j == 11) {
                            table.addCell(createCell(t.getOutMaxbps(), textfont));
                        }

                        if (j == 12) {
                            table.addCell(createCell(t.getOutMinbps(), textfont));
                        }

                        if (j == 13) {
                            table.addCell(createCell(t.getOutAveragebps(), textfont));
                        }

                        if (j == 14) {
                            table.addCell(createCell(t.getOutMaxUse(), textfont));
                        }

                        if (j == 15) {
                            table.addCell(createCell(t.getOutAvgUse(), textfont));
                        }

                        if (j == 16) {
                            table.addCell(createCell(t.getOutProportionTen(), textfont));
                        }

                        if (j == 17) {
                            table.addCell(createCell(t.getOutProportionFifty(), textfont));
                        }

                        if (j == 18) {
                            table.addCell(createCell(t.getOutProportionEighty(), textfont));
                        }

                        if (j == 19) {
                            table.addCell(createCell(t.getOutProportionHundred(), textfont));
                        }
                    }
                }
            }
            //将表格添加到文档中
            document.add(table);
        } catch (Exception e) {
            log.error("创建PDF表格数据失败", e);
        }
        //关闭流
        document.close();
    }

    /**
     * 提供外界调用的接口，生成以head为表头，list为数据的pdf
     *
     * @param head //数据表头
     * @param list //数据
     * @return //excel所在的路径
     */
    public <T> String generatePDFs(String[] head, List<T> list, HttpServletResponse response, String filePath) {
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
        initFile(file);
        try {
            file.createNewFile();  //生成一个pdf文件
        } catch (IOException e) {
            log.error("创建Pdf文件失败", e);
        }
        try {
            new MWCreatePdf(file).generatePDF(head, list, head.length);
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
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
        return saveFilePathAndName;
    }

}
