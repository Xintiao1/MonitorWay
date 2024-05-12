package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapService {

    private Integer id;
    private String serviceName;
    private String port;
    private String state;
    private String extraInfo;
    private String reason;
    private String reasonTTL;
    private String product;
    private String agreement;

}
