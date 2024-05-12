package cn.mw.zbx.common;

import java.util.regex.Pattern;

public class ZbxConstants {
    public final static String NO_DATA = "无数据";
    public final static String  CPU_UTILIZATION = "CPU_UTILIZATION";
    public static final Pattern CPUPattern = Pattern.compile("^(\\[.+\\])*CPU_UTILIZATION$");

    public final static String  MEMORY_UTILIZATION = "MEMORY_UTILIZATION";
    public final static String  DISK_UTILIZATION = "DISK_UTILIZATION";
    public final static String MEM_UTILIZATION = "MEM_UTILIZATION";
    public final static  String ICMP_RESPONSE_TIME = "ICMP_RESPONSE_TIME";
    public final static String  MW_INTERFACE_IN_TRAFFIC = "MW_INTERFACE_IN_TRAFFIC";
    public final static String MW_INTERFACE_OUT_TRAFFIC = "MW_INTERFACE_OUT_TRAFFIC";
    public final static String MW_INTERFACE_IN_DROPPED = "INTERFACE_IN_DROP_RATE";
    public final static String MW_INTERFACE_OUT_DROPPED = "INTERFACE_OUT_DROP_RATE";
    public final static String MW_INTERFACE_IN_UTILIZATION = "INTERFACE_IN_UTILIZATION";
    public final static String MW_INTERFACE_OUT_UTILIZATION = "INTERFACE_OUT_UTILIZATION";

    public final static String MW_DISK_USED = "MW_DISK_USED";
    public final static String MW_DISK_FREE = "MW_DISK_FREE";
    public final static String MW_DISK_TOTAL = "MW_DISK_TOTAL";

    public final static String MEMORY_USED = "MEMORY_USED";
    public final static String MEMORY_FREE = "MEMORY_FREE";
    public final static String MEMORY_TOTAL = "MEMORY_TOTAL";


}
