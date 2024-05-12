package cn.mw.monitor.user.advcontrol;

import lombok.Data;

@Data
public class TimeMessage {
    private String time;

    public TimeMessage() {}
    public TimeMessage(String time){
        this.time = time;
    }
}
