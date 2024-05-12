package cn.mw.monitor.server.serverdto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/26 17:00
 * @Version 1.0
 */
@Data
public class AlarmDTO {
    //告警事件id
    private String eventid;
    private String severity;
    private String name;
    private String clock;
    private String longTime;
    private String objectid;
}
