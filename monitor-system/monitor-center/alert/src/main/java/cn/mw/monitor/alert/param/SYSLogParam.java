package cn.mw.monitor.alert.param;

import lombok.Data;

@Data
public class SYSLogParam {

    private String ruleId;

    private String host;

    private Integer port;

    //1:tcp;2:udp;3tcp/tls
    private Integer agreementType;

    private String algorithm;

    private String path;

    private String password;
    
}
