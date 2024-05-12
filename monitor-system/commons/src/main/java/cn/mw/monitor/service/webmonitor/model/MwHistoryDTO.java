package cn.mw.monitor.service.webmonitor.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/4/27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwHistoryDTO {
    @ExcelProperty(value = "最新数据时间", index = 2)
    @ColumnWidth(18)
    private String lastUpdateTime;
    @ExcelProperty(value = "最新数据", index = 1)
    @ColumnWidth(18)
    private String lastUpdateValue;
    @ExcelProperty(value = "监控标题", index = 0)
    @ColumnWidth(18)
    private String titleName;
    @ExcelProperty(value = "单位", index = 5)
    @ColumnWidth(18)
    private String unit;

    @ExcelProperty(value = {"一段时间数据", "时间"}, index = 3)
    @ColumnWidth(18)
    private Date dateTime;
    @ExcelProperty(value = {"一段时间数据", "数据值"}, index = 4)
    @ColumnWidth(18)
    private String value;

}
