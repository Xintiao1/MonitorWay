package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapIpService extends MWNmapService{
    private Integer ipId;
    private String ip;
    private String ipType;
    private String osType;
    private String hostName;

}
