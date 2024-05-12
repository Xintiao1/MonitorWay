package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.List;

@Data
public class MwModelFromUserParam {
    private String instanceName;
    private String AlarmEventName;
    private String FromUser;
    private String relationSector;
    //告警级别
    private String modelAlertLevel;
    //业务系统
    private String modelSystem;
    //业务分类
    private String modelClassify;
    private List<Integer> groupIds;
    private List<Integer> userIds;
    private List<List<Integer>> orgIds;
    private Boolean isAlert;
}
