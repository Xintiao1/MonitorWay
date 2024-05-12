package cn.mw.monitor.ipaddressmanage.param;

import lombok.Data;

@Data
public class QueryLocAddressParam {
    //国家
    String country;
    //省 州
    String state;
    //城市
    String city;
    //县
    String region;
    //详细地址
    String fullAdress;
    //查询类型
    String type;
}
