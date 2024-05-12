package cn.mw.monitor.user.advcontrol;

import lombok.Data;

@Data
public class MacMessage {
    private String mac;

    public MacMessage(String mac){
        this.mac = mac;
    }
}
