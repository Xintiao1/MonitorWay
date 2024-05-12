package cn.joinhealth.zbx.enums.action;

import lombok.Data;

@Data
public class AlterDto {
    private String hostid;
    private String objectids;
    private String eventid;
    private String altype;
    //过滤条件
    private String severity;
    private String subject;
    private String acknowledged;
    private Long startTime;
    private Long endTime;

}
