package cn.mw.monitor.visualized.enums;

/**
 * 可视化中控大屏枚举
 */
public enum VisualizedZkSoftWareEnum {

    TYPE_IOT("IOT"),
    TYPE_DB("数据库"),
    NORMAL("NORMAL"),
    ABNORMAL("ABNORMAL"),
    WARNING("WARNING"),
    SUB_TYPE_DISTRIBUTION("配电柜监测"),
    ;

    private String name;

    VisualizedZkSoftWareEnum( String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
