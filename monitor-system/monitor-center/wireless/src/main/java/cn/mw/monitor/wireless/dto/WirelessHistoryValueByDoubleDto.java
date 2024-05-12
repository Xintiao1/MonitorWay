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
public class WirelessHistoryValueByDoubleDto implements Comparator<WirelessHistoryValueByDoubleDto> {
    private Double value;
    private String clock;

    @Override
    public int compare(WirelessHistoryValueByDoubleDto o1, WirelessHistoryValueByDoubleDto o2) {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value == o2.value) {
            return 0;
        } else {
            return -1;
        }
    }


}
