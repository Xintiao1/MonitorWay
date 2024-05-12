package cn.mw.monitor.hybridclouds.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @Date 2021/6/6
 */
@Data
@Builder
public class  GroupHosts {
    private String hostid;
    private String name;
    private String status;
    private List<GroupHost> groups;
    private DiscoveryRuleDTO discoveryRule;
    private ParentHost hostDiscovery;

    public GroupHosts(String hostid, String name, String status, List<GroupHost> groups, DiscoveryRuleDTO discoveryRule, ParentHost hostDiscovery) {
        this.hostid = hostid;
        this.name = name;
        this.status = status;
        this.groups = groups;
        this.discoveryRule = discoveryRule;
        this.hostDiscovery = hostDiscovery;
    }

    public GroupHosts() {
    }
}
