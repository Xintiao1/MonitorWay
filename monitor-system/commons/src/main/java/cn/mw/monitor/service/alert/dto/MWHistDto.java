package cn.mw.monitor.service.alert.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/15 14:15
 */
@Data
public class MWHistDto {
    private String histEventId;
    private String histTime;
    private String histRTime;
    private String histAcknowledged;
}
