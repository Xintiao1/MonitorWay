package cn.mw.monitor.report.service.manager;

import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwCellColorSheetWriteHandler
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/5/18 14:34
 * @Version 1.0
 **/
public class MwCellColorSheetWriteHandler implements CellWriteHandler {

    private Map<Integer,List<Integer>> map;

    private Short colorIndex;

    public MwCellColorSheetWriteHandler(Map<Integer, List<Integer>> map, Short colorIndex) {
        this.map = map;
        this.colorIndex = colorIndex;
    }

    public MwCellColorSheetWriteHandler() {

    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer integer, Integer integer1, Boolean aBoolean) {

    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer integer, Boolean aBoolean) {

    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<CellData> list, Cell cell, Head head, Integer integer, Boolean aBoolean) {
        int i = cell.getColumnIndex();
        if (0 != cell.getRowIndex()) {
            List<Integer> integers = map.get(cell.getRowIndex());
            if (CollectionUtils.isNotEmpty(integers) && integers.contains(i)) {
                Workbook workbook = cell.getSheet().getWorkbook();
                //设置行高
                 writeSheetHolder.getSheet().getRow(cell.getRowIndex()).setHeight((short) (1.4 * 256));
                // 单元格策略
                 WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                // 设置背景颜色白色
                 contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                // 设置垂直居中为居中对齐
                 contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                // 设置左右对齐为靠左对齐
                 contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
                // 设置单元格上下左右边框为细边框
                 contentWriteCellStyle.setBorderBottom(BorderStyle.MEDIUM);
                 contentWriteCellStyle.setBorderLeft(BorderStyle.MEDIUM);
                 contentWriteCellStyle.setBorderRight(BorderStyle.MEDIUM);
                 contentWriteCellStyle.setBorderTop(BorderStyle.MEDIUM);
                 // 创建字体实例
                 WriteFont cellWriteFont = new WriteFont();
                 // 设置字体大小
                 cellWriteFont.setFontName("宋体");
                 cellWriteFont.setFontHeightInPoints((short) 12);
                 //设置字体颜色
                 cellWriteFont.setColor(IndexedColors.RED.getIndex());
                 //单元格颜色
//                 contentWriteCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                 contentWriteCellStyle.setWriteFont(cellWriteFont);
                 CellStyle cellStyle = StyleUtil.buildHeadCellStyle(workbook, contentWriteCellStyle);
                 //设置当前行第i列的样式
                 cell.getRow().getCell(i).setCellStyle(cellStyle);

            }
        }
    }
}
