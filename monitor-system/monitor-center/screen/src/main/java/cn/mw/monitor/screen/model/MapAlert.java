package cn.mw.monitor.screen.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MapAlert {
    private String icmp;
    private String icmpId;
    private String target;
    private String targetId;
    private String color;
    private String linkTargetIp;
    private String linkHostId;

    public String getKey(){
        return targetId + "-" + icmpId;
    }

    public void empty(){
        this.icmp = "";
        this.target = "";
    }
}
