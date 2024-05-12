package cn.mw.monitor.engineManage.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/25 10:35
 * @Version 1.0
 */
@Data
public class EngineProxyDTO {
    //引擎主键
    private String engineId;
    //第三方监控服务器id
    private int monitorServerId;
    //zabbix代理id
    private String proxyId;
    //监控主机数量
    private int monitorHostNumber;
    //监控监控项数量
    private int monitoringItemsNumber;
}
