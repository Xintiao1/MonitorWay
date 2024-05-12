package cn.mw.monitor.model.param.citrix;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/8
 * 负载均衡LB关联列表数据
 */
@Data
public class MwModelLBCitrixListData {
    //应用名称 A
    private String name;
    //VirtualServer VS_A_运营商X_端口1
    private String virtualServerName;
    //公网ip:端口   运营商X:端口1
    private String virtualServerIpPort;
    //Virtual Service  内网服务器名称_协议_内网端口
    private String virtualServiceName;
    //内网ip:端口   内网服务器Ip:端口1
    private String serviceIpPort;
}
