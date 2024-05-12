package cn.mw.monitor.model.param;

import lombok.Data;

@Data
public class MwModelTPServerParam {

    private int id;

    private String monitorServerName;

    /**
     * 是否是最主要监控服务器（只能有一个主）
     */
    private Boolean mainServer;
    //监控服务器Ip
    private String monitoringServerIp;

}
