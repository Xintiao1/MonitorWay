package cn.mw.monitor.weixin.entity;

import lombok.Data;

import java.util.Date;
import java.util.HashSet;

@Data
public class AlertRecordTable {

    private Integer id;
    private Date date;    //告警时间
    private String method;//告警方式
    private String text; //告警内容
    private Integer isSuccess;//是否发送成功  0:成功   非0：失败(有失败码就存，没有存1)
    private String hostid;
    private String eventid;//事件ID
    private String error; //报错信息
    private String title;//告警标题
    private String ip;//主机IP
    private Boolean isAlarm;
    private HashSet<Integer> userIds;
    private String alertDate;
}
