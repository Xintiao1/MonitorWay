package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Comparator;

/**
 * @author xhy
 * @date 2020/5/12 9:38
 */
@Data
@Builder
public class HistoryValueDto implements Comparator<HistoryValueDto> {
    private Double value;
    private Long clock;
    private String itemid;
    private Double maxValue;
    private Double minValue;

    @Override
    public int compare(HistoryValueDto o1, HistoryValueDto o2) {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value == o2.value) {
            return 0;
        } else {
            return -1;
        }
    }
}
