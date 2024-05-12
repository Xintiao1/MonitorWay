package cn.mw.monitor.configmanage.entity;

import lombok.Data;


@Data
public class HttpEntity {

    private String ip;
    private int port;
    private String username;
    private String password;
    private String cmd;

    public HttpEntity(String ip, int port, String username, String password, String cmd) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.cmd = cmd;
    }

    public HttpEntity() {
    }
}
