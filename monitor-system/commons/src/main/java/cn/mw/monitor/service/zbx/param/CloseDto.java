package cn.mw.monitor.service.zbx.param;

import lombok.Data;

import java.util.Date;

@Data
public class CloseDto {
    private Integer monitorServerId;
    private String monitorServerName;
    private String objectId;
    private Boolean close;
    private Date closeDate;
    private int operatorId;
    private String description;
    private String userName;
}
