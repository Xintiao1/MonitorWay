package cn.mw.monitor.user.advcontrol;

import lombok.Data;

@Data
public class IPMessage {
    private String ip;

    public IPMessage(String ip){
        this.ip = ip;
    }
}
