package cn.mw.monitor.accountmanage.entity;

import lombok.Data;


@Data
public class MwAlerthistory7daysParam {
    //主键
    private Integer id;
    private String assetsId;//资产主键id
    private String monitorServerName;//监控服务器主机名称
    private String eventid;
    private String alertid;
    private String objectid;
    private String r_eventid;
    private String name;
    private String severity;//告警等级
    private String objectName;
    private String alertType;//主机的类型 从数据库中获取
    private String ip;
    private String clock;//告警时间
    private String rclock;
    private String longTime;//持续时间
    private String acknowledged;//确认状态

    private String hostid;//主机id

    //基本信息
    private String subject;//消息主题。用于消息告警
    private String message;//消息文本。用于消息告警
    private String recoverTime;//恢复时间
    private Integer userId;

    private Integer monitorServerId;



}
