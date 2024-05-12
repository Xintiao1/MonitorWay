package cn.mw.monitor.util.entity;

import lombok.Data;

@Data
public class TCP_UDPFrom {

    private String host;
    private Integer port;
    private String algorithm;
    private String password;
    private String path;
    private String tls;
    private String keyType;
    private Integer agreementType;
}
