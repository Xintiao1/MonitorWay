package cn.mw.monitor.scanrule.model;

import lombok.Data;

@Data
public class IpRangeView {
    private String startip;
    private String endip;
    private Boolean ipv6checked;
}
