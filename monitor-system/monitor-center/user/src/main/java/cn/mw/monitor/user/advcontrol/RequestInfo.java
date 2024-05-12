package cn.mw.monitor.user.advcontrol;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = 1807680989834272869L;

    private String loginName;
    private String ip;
    private String mac;
    private String time;

    public RequestInfo(String ip, String mac,String time){
        this.ip = ip;
        this.mac = mac;
        this.time = time;
    }
}
