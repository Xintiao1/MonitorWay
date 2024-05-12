package cn.mw.monitor.scanrule.api.param.scanrule;

import lombok.Data;

@Data
public class IPSubnetParam {
    private String subnet;
    private boolean ipv6checked;
}
