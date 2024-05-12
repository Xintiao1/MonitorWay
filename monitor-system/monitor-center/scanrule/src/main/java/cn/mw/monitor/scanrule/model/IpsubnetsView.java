package cn.mw.monitor.scanrule.model;

import lombok.Data;

@Data
public class IpsubnetsView {
    private String subnet;
    private Boolean ipv6checked;
}
