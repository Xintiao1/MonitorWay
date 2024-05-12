package cn.mw.module.security.common;

/**
 * topic字段映射规则模式
 */
public enum AlertFieldEnum {
    事件标题("eventTitle"),
    主机ID("hostId"),
    主机名称("hostName"),
    主机IP("hostIp"),
    严重级别("alarmLevel"),
    事件内容("eventContent"),
    事件时间("eventTime"),
    事件ID("eventId"),
    事件状态("currentState"),
    业务("business"),
    指标类型("targetType"),
    指标名("targetName"),
    事件来源("eventSource");

    private String value;
    private AlertFieldEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
