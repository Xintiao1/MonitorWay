package cn.mw.monitor.scanrule.api.param.scanrule;

import lombok.Data;

@Data
public class IPRangeParam {
    private String startip;
    private String endip;
    private boolean ipv6checked;
}
