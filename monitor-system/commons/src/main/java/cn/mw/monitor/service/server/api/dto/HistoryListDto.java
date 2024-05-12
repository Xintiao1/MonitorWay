package cn.mw.monitor.service.server.api.dto;

import cn.mw.monitor.service.webmonitor.model.MwHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/28 14:24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryListDto {
//    @ExcelProperty(value = "最新数据时间", index = 2)
//    @ColumnWidth(18)
    private String lastUpdateTime;
//    @ExcelProperty(value = "最新数据", index = 1)
//    @ColumnWidth(18)
    private String lastUpdateValue;
//    @ExcelProperty(value = "监控标题", index = 0)
//    @ColumnWidth(18)
    private String titleName;
//    @ExcelProperty(value = "单位", index = 5)
//    @ColumnWidth(18)
    private String unit;
    //    @ExcelProperty(value = "一段时间数据")
//    @ColumnWidth(36)
    private List<MwHistoryDTO> dataList;
    private List<MWItemHistoryDto> dataLists;
    //表示当前颗粒度
    private String delay;

////    @ExcelProperty(value = {"一段时间数据", "时间"}, index = 3)
////    @ColumnWidth(18)
//    private Date date;
////    @ExcelProperty(value = {"一段时间数据", "数据值"}, index = 4)
////    @ColumnWidth(18)
//    private String value;
}
