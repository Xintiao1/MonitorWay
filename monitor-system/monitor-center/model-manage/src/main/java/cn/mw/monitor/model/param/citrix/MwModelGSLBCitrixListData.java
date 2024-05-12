package cn.mw.monitor.model.param.citrix;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/8
 * 负载均衡GSLB关联列表数据
 */
@Data
public class MwModelGSLBCitrixListData {
    //virtualServer名称
    private String virtualServerName;
    //service名称
    private String servicename;
    //service类型
    private String servicetype;
    //ServiceIp地址
    private String serviceIp;
    //service端口
    private String servicePort;
    private String domainname;
}
