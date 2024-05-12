package cn.mw.monitor.visualized.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName VisualizedConstant
 * @Description 可视化数据
 * @Author gengjb
 * @Date 2023/4/17 10:41
 * @Version 1.0
 **/
public class VisualizedConstant {

    public static final Integer MODEL_ID = 1004;

    //正常状态
    public static final String NORMAL = "NORMAL";

    //异常状态
    public static final String ABNORMAL = "ABNORMAL";

    //宕机状态
    public static final String SHUTDOWN = "SHUTDOWN";

    //宕机状态
    public static final String PER_CENT = "%";

    //资产硬件类型
    public static final List<String> HARD_WARE_TYPES = Arrays.asList(new String[]{"网络设备","存储设备","安全设备","负载均衡","服务器","虚拟化","数据库","交换机","防火墙"});

    public static final String EXPORTER_NODE_CLUSTER = "exporter-node-cluster";
    public static final String INTERFACE = "INTERFACE";
    public static final String DISK = "DISK";

    
}
