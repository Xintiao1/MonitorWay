package cn.mw.monitor.service.timetask.dto;


public enum SelectNameEnum {
    ALERT_TITLE("告警标题"),
    DATA_SOURCE("数据来源"),
    ASSET("资产"),
    ASSETS_TYPE("资产类型"),
    TAG("标签"),
    ORG("机构"),
    TARGET("指标"),
    IFMODE("接口模式"),
    MODELSYSTEMNAME("业务系统"),
    ALERT_TAG("告警标记"),
    KEYDEVICES("关键设备"),
    InterfaceModeDesc("接口描述"),
    GROUP("用户组");


    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    SelectNameEnum(String name) {
        this.name = name;
    }
}
