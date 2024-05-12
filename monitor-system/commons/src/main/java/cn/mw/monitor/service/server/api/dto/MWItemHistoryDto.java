package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/4/25 16:15
 */
@Data
public class MWItemHistoryDto implements Comparable<MWItemHistoryDto> {
    private String itemid;
    private String clock;
    private String value;
    private String ns;
    private Long lastValue;
    private Date dateTime;
    private Double doubleValue;
    //降序排序
    @Override
    public int compareTo(MWItemHistoryDto o) {
        return o.getValue().compareTo(getValue());
    }
}
