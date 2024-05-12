package cn.mw.monitor.service.zbx.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

@Data
public class IgnoreAlertDto extends BaseParam {
    private String id;
    private Integer monitorServerId;
    private String monitorServerName;
    private String eventid;
    private String userName;
    private Boolean isIgnore;
    private Date ignoreDate;
    private Integer operatorId;
    private String name;//告警标题

}
