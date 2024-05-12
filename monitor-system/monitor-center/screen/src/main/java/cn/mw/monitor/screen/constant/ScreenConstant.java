package cn.mw.monitor.screen.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName
 * @Author gengjb
 * @Date 2023/3/26 23:31
 * @Version 1.0
 **/
public class ScreenConstant {

    //首页topN监控项
    public static final List<String> TOPN_ITEM = Arrays.asList("CPU_UTILIZATION", "ICMP_LOSS", "MEMORY_UTILIZATION"
            ,"ICMP_RESPONSE_TIME","DISK_UTILIZATION");

    //首页接口流量监控项
    public static final String INTERFACE_FLOW_KEY = "INTERFACE_IN_UTILIZATION";

    public static final String INTERFACE_OUT_UTILIZATION = "INTERFACE_OUT_UTILIZATION";

    //首页带宽监控项
    public static final String FLOW_BANDWIDTH_KEY = "INTERFACE_IN_TRAFFIC";

    //首页接口丢包率
    public static final String INTERFACE_RATE_KEY = "INTERFACE_DROP_RATE";

    public static final String HOME_PAGE_KEY = "homePage";

    //首页资产分组
    public static final String ASSETS_GROUP = "ASSETS_GROUP";

    //虚拟内存
    public static final String VMEMORY_UTILIZATION = "VMEMORY_UTILIZATION";


    public static final String ALL = "全部";


    public static final Integer TOPN_ACTION = 20;

    public static final Integer TOPNLINK_ACTION = 21;

    public static final Integer TOPFLOWERROR_ACTION = 14;

    public static final Integer TOPBANDWIDTH_ACTION = 22;

    public static final List<String> INTERFACE_RATE_NAMES = Arrays.asList("INTERFACE_IN_DROP_RATE","INTERFACE_OUT_DROP_RATE");


    public static final String INTERFACE_IN = "INTERFACE_IN";
    public static final String INTERFACE_OUT = "INTERFACE_OUT";


}
