package cn.mw.monitor.service.tpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ProxyServerInfo {
    private String serverType;
    private String version;
    private String user;
    private String passwd;
    private String url;
}
