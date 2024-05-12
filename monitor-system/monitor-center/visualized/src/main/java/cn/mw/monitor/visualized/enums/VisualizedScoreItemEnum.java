package cn.mw.monitor.visualized.enums;

/**
 * @author gengjb
 * @description 监控项枚举
 * @date 2023/9/27 14:04
 */
public enum VisualizedScoreItemEnum {

    /**
     * 进程状态
     */
    PROCESS_HEALTH("PROCESS_HEALTH", "进程状态"),
    /**
     * 数据库状态
     */
    MW_ORACLE_PYTHON_GET_VERSION("MW_ORACLE_PYTHON_GET_VERSION", "数据库状态"),
    /**
     * CPU利用率
     */
    CPU_UTILIZATION("CPU_UTILIZATION", "CPU利用率"),
    /**
     * 内存利用率
     */
    MEMORY_UTILIZATION("MEMORY_UTILIZATION", "内存利用率"),
    /**
     * 磁盘利用率
     */
    MW_DISK_UTILIZATION("MW_DISK_UTILIZATION", "磁盘利用率"),
    /**
     * PING状态
     */
    ICMP_PING("ICMP_PING", "PING状态"),

    /**
     * 流量入
     */
    MW_INTERFACE_IN_TRAFFIC("MW_INTERFACE_IN_TRAFFIC","流量入"),

    /**
     * 流量出
     */
    MW_INTERFACE_OUT_TRAFFIC("MW_INTERFACE_OUT_TRAFFIC","流量出"),

    /**
     * 流量利用率入
     */
    INTERFACE_IN_UTILIZATION("INTERFACE_IN_UTILIZATION","流量利用率入"),

    /**
     * 流量利用率出
     */
    INTERFACE_OUT_UTILIZATION("INTERFACE_OUT_UTILIZATION","流量利用率出")

    ;

    private String itemName;

    private String desc;

    VisualizedScoreItemEnum(String itemName, String desc) {
        this.itemName = itemName;
        this.desc = desc;
    }

    public String getItemName() {
        return itemName;
    }

    public static VisualizedScoreItemEnum getByItemName(String itemName) {
        for (VisualizedScoreItemEnum itemEnum : values()) {
            if (itemName.equals(itemEnum.getItemName())) {
                return itemEnum;
            }
        }
        return null;
    }
}
