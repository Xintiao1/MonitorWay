package cn.mw.monitor.visualized.enums;

/**
 * @author gengjb
 * @description 数字孪生字段映射枚举
 * @date 2023/8/17 9:45
 */
public enum VisualizedDigitalTwinEnum {

    CPU_UTILIZATION("CPU_UTILIZATION","cpu利用率","cpuUtilization"),
    TEMPERTURE_SENSOR_VALUE("TEMPERTURE_SENSOR_VALUE","CPU温度","cpuTemperature"),
    MEMORY_TOTAL("MEMORY_TOTAL","内存总容量","memoryTotal"),
    MEMORY_UTILIZATION("MEMORY_UTILIZATION","内存使用率","memoryUtilization"),
    MW_DISK_TOTAL("MW_DISK_TOTAL","磁盘总容量","diskTotal"),
    MW_DISK_USED("MW_DISK_USED","磁盘已使用容量","diskUsed"),
    MW_DISK_UTILIZATION("MW_DISK_UTILIZATION","磁盘使用率","diskUtilization"),
    DISK_FREE_PERCENTAGE("DISK_FREE_PERCENTAGE","磁盘剩余使用率","diskFreeUtilization"),
    MW_IOT_TEMPERTURE2_VALUE("MW_IOT_TEMPERTURE2_VALUE","温度(下)","temperatureDown"),
    INTERFACE_NAME("INTERFACE_NAME","接口名称","interfaceName"),
    INTERFACE_IPADDRESS("INTERFACE_IPADDRESS","接口IP地址","interfaceIp"),
    INTERFACE_MACADDR("INTERFACE_MACADDR","接口MAC地址","interfaceMac"),
    MW_INTERFACE_SPEED("MW_INTERFACE_SPEED","接口速率","interfaceSpeed"),
    MW_INTERFACE_IN_TRAFFIC("MW_INTERFACE_IN_TRAFFIC","接口流量(入)","interfaceInTraffic"),
    MW_INTERFACE_OUT_TRAFFIC("MW_INTERFACE_OUT_TRAFFIC","接口流量(出)","interfaceOutTraffic"),
    INTERFACE_IN_DROP_RATE("INTERFACE_IN_DROP_RATE","接口丢包(入)","interfaceInDropRate"),
    INTERFACE_OUT_DROP_RATE("INTERFACE_OUT_DROP_RATE","接口丢包(出)","interfaceOutDropRate"),
    INTERFACE_IN_ERRORS("INTERFACE_IN_ERRORS","接口错误包(入)","interfaceInErrors"),
    INTERFACE_OUT_ERRORS("INTERFACE_OUT_ERRORS","接口错误包(出)","interfaceOutErrors"),
    ;

    private String name;
    private String desc;
    private String property;

    VisualizedDigitalTwinEnum( String name,String desc,String property) {
        this.name = name;
        this.desc = desc;
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }


    public static String getProPerty(String name){
        VisualizedDigitalTwinEnum[] values = values();
        for (VisualizedDigitalTwinEnum itemEnum : values) {
            if(name.contains(itemEnum.getName())){
                return itemEnum.property;
            }
        }
        return null;
    }
}
