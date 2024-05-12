package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xhy
 * @date 2020/4/25 16:15
 */
@Data
public class MWItemHistoryDtoBySer implements Serializable {
    private String itemid;
    private String clock;
    private String value;
    private String ns;
    private Long lastValue;
    private Date dateTime;
    private Double doubleValue;
}
