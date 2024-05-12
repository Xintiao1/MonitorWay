package cn.mw.monitor.visualized.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ClassName
 * @Description 机柜监控项
 * @Author gengjb
 * @Date 2023/3/16 9:30
 * @Version 1.0
 **/
public class RackZabbixItemConstant {

    //机柜监控项
    public static final List<String> RACKITEM = Arrays.asList("MW_IOT_DOOR1_STATUS", "MW_IOT_DOOR2_STATUS", "MW_IOT_HUMIDITY1_VALUE"
                                                        ,"MW_IOT_HUMIDITY2_VALUE","MW_IOT_NOISE1_VALUE","MW_IOT_NOISE2_VALUE"
                                                        ,"MW_IOT_TEMPERTURE1_VALUE","MW_IOT_TEMPERTURE2_VALUE");


    //配电柜监控项
    public static final List<String> DISTRIBUTION_ITEM = Arrays.asList("MW_PANEL_AC1_SINGLEPHASE_VOLTAGE", "MW_AC_CURTAILMENT", "MW_OUTPUT_CURRENT"
            ,"MW_OUTPUT_VOLTAGE","MW_PANEL_REMAINDER","MW_PANEL_BATTERY1","MW_PDB_STATUS","MW_PANEL_BATTERY_CHARGE","MW_INPUT_VOLTAGE");


    //配电柜运行数据监控项
    public static final List<String> RUNDATA_ITEM = Arrays.asList("MW_OUTPUT_VOLTAGE", "MW_OUTPUT_CURRENT", "MW_LIMITING_POINT"
            ,"MW_INPUT_VOLTAGE","MW_COMMUNICATION_STATUS","MW_PANEL_ENERGY_STATUS","MW_AC_CURTAILMENT","MW_TEMPERATURE_LIMIT_POWER");

    //整流监控项
    public static final List<String> RECTITV_ITEM = Arrays.asList("MW_AC_CURTAILMENT","MW_COMMUNICATION_STATUS","MW_LIMITING_POINT","MW_PDB_STATUS","MW_INPUT_VOLTAGE","MW_OUTPUT_CURRENT","MW_OUTPUT_VOLTAGE");


    public static final String HOST_ID = "hostid";

    public static final String NAME = "name";

    public static final String UNITS = "units";

    public static final String LASTVALUE = "lastvalue";

    public static final String OPEN = "开";

    public static final String CLOSE = "关";

    //可用性
    public static final String MW_HOST_AVAILABLE = "MW_HOST_AVAILABLE";

    //表空间使用情况
    public static final String MW_ORACLE_TBS_USED_EXTPCT = "MW_ORACLE_TBS_USED_EXTPCT";


    //数据库session会话数
    public static final String MW_ORACLE_SESSION_COUNT = "MW_ORACLE_SESSION_COUNT";

    //进程状态
    public static final String PROCESS_HEALTH = "PROCESS_HEALTH";


    public static final List<String> ZABBIX_ITEM_BASE = Arrays.asList("CPU_UTILIZATION","MEMORY_UTILIZATION","MW_DISK_UTILIZATION","AGENT_PING_STATUS","ICMP_PING","NameNode: Percent capacity remaining",
                                                                    "NameNode: Capacity remaining","ResourceManager: Active NMs","ResourceManager: Unhealthy NMs","Used Memory UTILIZATION",
                                                                    "JVM Heap usage","SystemLoadAverage","TotalFileIoErrors","NumActiveSources","MW_INTERFACE_OUT_TRAFFIC","MW_INTERFACE_IN_TRAFFIC");

    //进程状态
    public static final String ICMP_PING = "ICMP_PING";


    public static final List<String> ITEM_HOST_STATUS = Arrays.asList("AGENT_PING_STATUS","ICMP_PING","MW_HOST_AVAILABLE");

    //HADOO可用容量百分比
    public static final String USE_CAPACITY_PERCENTAGE_HADOOP = "NameNode: Percent capacity remaining";

    //HADOO可用容量
    public static final String USE_CAPACITY_HADOOP = "NameNode: Capacity remaining";

    public static final String AGENT_PING_STATUS = "AGENT_PING_STATUS";

    public static final List<String> FILTER_ITEM = Arrays.asList("VMEMORY_UTILIZATION");

    public static final String INTERFACE_IN_UTILIZATION = "INTERFACE_IN_UTILIZATION";


    public static final String INTERFACE_OUT_UTILIZATION = "INTERFACE_OUT_UTILIZATION";
}
