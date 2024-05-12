package cn.mw.monitor.smartdisc.model;

import lombok.Data;

@Data
public class MWNmapPortGroup {

    private Integer id;
    private String portName;
    private String tcpPortGroup;
    private String udpPortGroup;
    private Boolean deleteFlag;
}
