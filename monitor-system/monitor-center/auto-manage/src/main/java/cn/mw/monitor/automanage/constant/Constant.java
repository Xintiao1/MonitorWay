package cn.mw.monitor.automanage.constant;

/**
 * @author gui.quanwang
 * @className Constant
 * @description 自动化运维静态常量
 * @date 2022/4/2
 */
public class Constant {

    /**
     * 服务名称
     */
    public final static String SERVER_NAME  = "monitor_spider";

    public final static String SERVER_HOST = "http://10.18.5.60:8848";

    /**
     * 获取实例化列表
     */
    public final static String GET_INSTANCE_LIST = "/nacos/v1/ns/instance/list";

    /**
     * 更新实例化对象数据
     */
    public final static String UPDATE_INSTANCE_INFO  ="/nacos/v1/ns/instance";

}
