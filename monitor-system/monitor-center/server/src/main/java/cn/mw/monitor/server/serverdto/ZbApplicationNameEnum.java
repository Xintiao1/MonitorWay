package cn.mw.monitor.server.serverdto;

/**
 * @author syt
 * @Date 2020/5/19 17:21
 * @Version 1.0
 */
public enum ZbApplicationNameEnum {

    GENERAL("GENERAL", "基本信息"),
    STATUS("STATUS", "状态"),
    PERFORMANCE("PERFORMANCE", "性能"),
    INTERFACES_INFO("INTERFACES_INFO", "端口信息"),
    INTERFACES("INTERFACES", "接口"),
    NETWORK("NETWORK", "网络"),
    HARDWARE("HARDWARE", "硬件"),
    PROCESS("PROCESS", "进程"),
    DISK_IO("DISK_IO", "磁盘IO"),
    SOFTWARE("SOFTWARE", "软件列表"),
    DISK("DISK", "磁盘"),
    DISK_INFO("DISK_INFO", "磁盘信息"),
    LINK_INFO("LINK_INFO", "链路信息"),
    LINK_PERFORMANCE("LINK_PERFORMANCE", "链路性能"),
    Temperature_Sensors("Temperature_Sensors", "温度"),
    Power_Sensors("Power_Sensors", "电源"),
    Fan_Sensors("Fan_Sensors", "风扇"),
    Board_Sensors("Board_Sensors", "板卡及插槽"),
    SERVICES("SERVICES", "服务"),
    SYSTEM("SYSTEM", "系统"),
    IOT_VALUE("IOT_VALUE", "IOT参数"),
    MW_VM_CLUSTERS("MW_VM_CLUSTERS", "虚拟化集群"),
    LOG("LOG", "日志"),
    STORAGE("STORAGE", "存储"),
    IPADDRESS("IPADDRESS", "IP地址"),
    ORACLE_DB("ORACLE_DB", "ORACLE数据库"),
    ORACLE_DB_TBS("ORACLE_DB_TBS", "ORACLE数据库表空间"),
    ORACLE_DB_DISK_GROUPS("ORACLE_DB_DISK_GROUPS", "ORACLE数据库磁盘组"),
    ORACLE_DB_PDB("ORACLE_DB_PDB", "ORACLE可插拔数据库"),
    ORACLE_DB_INSTANCE("ORACLE_DB_INSTANCE", "ORACLE数据库实例"),
    ORACLE_PHYSICAL("ORACLE_PHYSICAL", "ORACLE物理"),
    ORACLE_DB_SYSTEM_METRICS("ORACLE_DB_SYSTEM_METRICS", "ORACLE数据库系统度量"),
    ORACLE_DB_ARCHIVELOG("ORACLE_DB_ARCHIVELOG", "ORACLE数据库归档日志"),
    BATTERY_Sensor("BATTERY_Sensor", "电池"),
    DISK_ARRAY("DISK_ARRAY", "磁盘阵列"),
    DISK_PHYSICAL("DISK_PHYSICAL", "物理磁盘"),
    DISK_VIRTUAL("DISK_VIRTUAL", "虚拟磁盘"),
    CHANNEL_INFO("CHANNEL_INFO", "通道信息"),
    ALERT_TRIGGER_INFO("ALERT_TRIGGER_INFO", "告警阈值详情"),//固定的table，自定义命名
    Always_On("Always_On", "同步延迟"),

    PROCESS_TOP("Top", "进程Top10"),

    ;

    private String name;
    private String chName;

    ZbApplicationNameEnum(String name, String chName) {
        this.name = name;
        this.chName = chName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getChName(String name) {
        for (ZbApplicationNameEnum z : ZbApplicationNameEnum.values()) {
            if (z.getName().equals(name)) {
                return z.chName;
            }
        }
        return null;
    }

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }
}
