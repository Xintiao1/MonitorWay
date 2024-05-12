package cn.mw.monitor.service.scan.model;

import lombok.Data;

@Data
public class InterfaceInfo {
    private int ifIndex;
    private String name;
    private IfType ifType;
    private IfStatus ifStatus;
    private String desc;
    private String alias;
    private int mtu;
    private String mac;
    private String ip;
    private String subnetMask;
    private String ifMode;
    private String vlan;
    private Boolean vlanFlag;
    private PortType portType;
}
