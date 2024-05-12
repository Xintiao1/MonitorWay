package cn.mw.monitor.service.zbx.param;

import lombok.Data;

@Data
public class ConfirmDto {
    private Integer monitorServerId;
    private String eventid;
}
