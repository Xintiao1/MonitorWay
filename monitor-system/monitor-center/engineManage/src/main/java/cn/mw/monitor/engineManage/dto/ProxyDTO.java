package cn.mw.monitor.engineManage.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProxyDTO {
    private String proxyid;
    private String proxy_address;
    private String host;//代理的名称
    private String status;//5 - 主动代理 6 - 被动代理
    private String description;//
    private Long lastaccess;//上一次代理连接zabbix server的时间
    private String interfaceid;
    private String dns;
    private String ip;
    private String port;
    private String useip;//0 - 用DNS名称链接 1 - 用IP地址连接
    private String hostid;
    private List<HostBase> hosts;
}
