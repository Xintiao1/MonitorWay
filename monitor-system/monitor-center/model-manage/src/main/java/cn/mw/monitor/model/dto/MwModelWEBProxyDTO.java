package cn.mw.monitor.model.dto;

import lombok.Data;

@Data
public class MwModelWEBProxyDTO {
    private Integer monitorServerId;
    private String proxyid;
    private String host;
    private String status;
    private String proxy_address;
}
