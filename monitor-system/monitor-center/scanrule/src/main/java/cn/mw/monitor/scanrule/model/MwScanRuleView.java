package cn.mw.monitor.scanrule.model;

import lombok.Data;

import java.util.List;

@Data
public class MwScanRuleView {
    private Integer ruleId;
    private String name = "";
    private List<IpRangeView> ipRange;
    private List<IpsubnetsView> ipsubnets;
    private String iplist = "";
    private boolean ipv6checked;
    private Integer monitorServerId;
    private List<RuleView> scanrules;
    //引擎id
    private String engineId;
}
