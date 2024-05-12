package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/4/27
 */
@Data
@Builder
public class MwHistoryDTO {
    private String date;
    private Double oldValue;
    private String value;

}
