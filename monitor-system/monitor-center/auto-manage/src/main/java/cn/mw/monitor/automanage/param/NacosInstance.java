package cn.mw.monitor.automanage.param;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className NacosInstance
 * @description nacos实例对象
 * @date 2022/4/4
 */
@Data
public class NacosInstance {

    /**
     * IP地址
     */
    private String ip;

    /**
     * 端口
     */
    private String port;

    /**
     * 版本号
     */
    private String weight;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 是否健康
     */
    private Boolean healthy;

    /**
     * 是否可用
     */
    private Boolean enabled;
}
