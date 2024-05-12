package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapExploreResult {

    private Integer id;
    private String ip;
    //操作系统
    private String osType;
    //附加信息
    private String extraInfo;
    private String reasonTTL;
    private String port;
    //服务名
    private String serviceName;
    private String ipType;
    private String product;
    //协议
    private String agreement;
    private String reason;
    //主机名
    private String hostName;
    private String state;

    private Boolean deleteFlag;

}
