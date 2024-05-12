package cn.mw.monitor.common.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/6/3 7:25
 * @Version 1.0
 */
public class ZabbixItemConstant {

    public static final Map<Integer, String> ASSETS_STATES_ITEM_NAMES = new HashMap<>();
    public static final Map<Integer, String> diskItemName = new HashMap<>();
    public static final Map<Integer, String> netItemName = new HashMap<>();
    public static final Map<Integer, String> ASSETS_UPTIME_ITEM_NAMES = new HashMap<>();
    public static final Map<Long, String> COLORMAP = new HashMap<>();
    public static final Map<Long, String> COLORVALUEMAP = new HashMap<>();
    public static final List<String> ITEMNAME_SELECT = Arrays.asList("MW_DB_QUERIES_PERSECOND", "MW_DB_QUESTION_PERSECOND", "MW_SLOW_QUERIES_PERSECOND");
    public static final List<String> ITEMNAME_STORAGEVOL = Arrays.asList("MW_STORAGE_VOLSIZE", "MW_STORAGE_VOLUSED");

    public static final List<String> ASSETS_STATUS = Arrays.asList("ICMP_PING", "AGENT_PING_STATUS", "MW_DB_STATUS");
    public static final List<String> NEW_ASSETS_STATUS = Arrays.asList("ICMP_PING", "AGENT_PING_STATUS", "MW_DB_STATUS","MW_HOST_AVAILABLE");
    public static final Map<Integer, String> storeItemName = new HashMap<>();
    public static final List<String> ITEMNAME = Arrays.asList("MWVM_CPU_CORES", "MWVM_CPU_FREQUENCY", "MWVM_CPU_USAGE", "MWVM_MEMORY_TOTAL", "MWVM_MEMORY_USED", "TOTAL_DATASTORE_SIZE", "FREE_DATASTORE_UTILIZATION", "MWVM_MEMORY_USAGE","VM_POWER_STATE");
    public static final Map<Integer, String> urlTree = new HashMap<>();

    static {
        ASSETS_STATES_ITEM_NAMES.put(0, "ICMP_PING");
        ASSETS_STATES_ITEM_NAMES.put(1, "AGENT_PING_STATUS");
        ASSETS_STATES_ITEM_NAMES.put(2, "MW_DB_STATUS");

        diskItemName.put(0, "DISK_UTILIZATION");
        diskItemName.put(1, "DISK_USED");
        diskItemName.put(2, "DISK_FREE");
        diskItemName.put(3, "DISK_TOTAL");

        netItemName.put(0, "INTERFACE_NAME");
        netItemName.put(1, "INTERFACE_OUT_TRAFFIC");
        netItemName.put(2, "INTERFACE_IN_TRAFFIC");
        netItemName.put(3, "INTERFACE_OUT_DROPPED");
        netItemName.put(4, "INTERFACE_IN_DROPPED");
        netItemName.put(5, "INTERFACE_SPEED");
        netItemName.put(6, "INTERFACE_STATUS");


        ASSETS_UPTIME_ITEM_NAMES.put(0, "system.uptime");
        ASSETS_UPTIME_ITEM_NAMES.put(1, "mysql.uptime");


        COLORMAP.put(-1L, "#999999");
        COLORMAP.put(0L, "#EE3736");
        COLORMAP.put(1L, "#38AF56");
        COLORMAP.put(2L, "#38AF56");


        COLORVALUEMAP.put(-1L, "未管理");
        COLORVALUEMAP.put(0L, "不可用");
        COLORVALUEMAP.put(1L, "可用");
        COLORVALUEMAP.put(2L, "可用");

        storeItemName.put(0, "AVERAGE_DATASTORE_READ_LATENCY");
        storeItemName.put(1, "FREE_DATASTORE_UTILIZATION");
        storeItemName.put(2, "TOTAL_DATASTORE_SIZE");
        storeItemName.put(3, "AVERAGE_DATASTORE_WRITE_LATENCY");

        urlTree.put(0, "assetsHost");
        urlTree.put(1, "dataCenter");
        urlTree.put(2, "cluster");
        urlTree.put(3, "host");
        urlTree.put(4, "virtual");
        urlTree.put(5, "store");
    }

    public static final String INTERFACE_DESCR = "INTERFACE_DESCR";
    public static final String INTERFACES_INFO = "INTERFACES_INFO";
    public static final String INTERFACES = "INTERFACES";
    public static final String MW_HOST_AVAILABLE = "MW_HOST_AVAILABLE";
    public static final String APPLICATION_NAME = "All";
    public static final String APPLICATION_LLD = "LLD";
    public static final String INTERFACE_INDEX = "INTERFACE_INDEX";
    public static final String CPU_UTILIZATION = "CPU利用率";
    public static final String MW_WIN_SOFTWARELIST = "MW_WIN_SOFTWARELIST";
    public static final String MW_WINDOWS_SOFTWARELIST = "MW_WINDOWS_SOFTWARELIST";
    public static final String MW_MACOS_SOFTWARELIST = "MW_MACOS_SOFTWARELIST";
    public static final String MW_DH_CHANNEL = "MW_DH_CHANNEL";

    public static final String HOSTCOMPUTER = "宿主机";
    public static final String VMWARE = "虚拟机";

    public static final String STORE = "store";
    public static final String VHOST = "vHost";

    public static final String AGENT_PING_STATUS = "AGENT_PING_STATUS";
    public static final String SOFTWARELIST = "SOFTWARELIST";
    public static final String VMTOOLS_VERSION = "VMTOOLS_VERSION";

    public static final String CPU_CORE = "CPU_CORE";
    public static final String MEMORY_TOTAL = "MEMORY_TOTAL";
}
