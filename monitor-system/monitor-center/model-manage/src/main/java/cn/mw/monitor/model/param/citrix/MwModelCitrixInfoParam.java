package cn.mw.monitor.model.param.citrix;

import cn.mw.monitor.model.param.ModelAssetsParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/8
 */
@Data
public class MwModelCitrixInfoParam extends ModelAssetsParam {
    private String name;
    private String servicetype;
    private String ipv46;
    private Integer port;
    private String ipaddress;
    private String iptype;
    private String servicename;
    private String domainname;
    private String servername;
    private String ip;
    private String curstate;
    private String health;
    private String lbmethod;
    private String effectivestate;
    private String downstateflush;
    private String state;
    private String svrstate;
    private String cachetype;
    private String maxclient;
    private String maxreq;
    private String type;


    //LB、GSLB关联数据列表使用
    private String serviceIp;
    private String servicePort;

    private String virtualServerName;
    private String virtualServerIpPort;
    private String virtualServiceName;
    private String serviceIpPort;

//    @ApiModelProperty("模型索引")
//    private String modelIndex;
//    @ApiModelProperty("模型索引ID")
//    private String modelIndexId;
//    @ApiModelProperty("模型实例ID")
//    private String modelInstanceId;
}
