package cn.mw.monitor.weixin.entity;

import lombok.Data;

@Data
public class HuaXingAlertParam {

    private String startsAt;

    private String endsAt;

    private String status;

    private String severity;

    private String alertName;

    private String duration;

    private String projectName;

    private String ip;

    private String alertType;

    private String modelClassify;

    private String modelSystem;

    private String eventid;
}
