package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("公有ip地址")
public class AddPubIpAddressParam {
    //与IP地址段关联id
    private int linkId;
    //ip地址
    private String ipAddress;
    //ip地址信息 IPV4/IPV6
    private String ipType;
    //国家
    private String country;
    //省
    private String state;
    //市
    private String city;
    //区
    private String region;
    //供应商
    private String isp;
    //经度
    private String longitude;
    //纬度
    private String latitude;

}
