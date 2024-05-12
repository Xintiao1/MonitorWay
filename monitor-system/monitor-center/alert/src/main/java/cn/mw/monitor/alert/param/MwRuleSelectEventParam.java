package cn.mw.monitor.alert.param;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/27 14:35
 */
@Data
public class MwRuleSelectEventParam {
    private String text;
    private String hostid;
    private String title;
    private String ip;
    private Boolean isAlarm;
    private Date date;
    private Integer size;
    private String uuid;
}
