package cn.mw.monitor.wireless.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Comparator;

/**
 * @author qzg
 * @date 2021/6/23
 */
@Data
@Builder
public class WirelessHistoryValueDto implements Comparator<WirelessHistoryValueDto> {
    private Double value;
    private Long clock;
    private String strValue;

    @Override
    public int compare(WirelessHistoryValueDto o1, WirelessHistoryValueDto o2) {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value == o2.value) {
            return 0;
        } else {
            return -1;
        }
    }
}
