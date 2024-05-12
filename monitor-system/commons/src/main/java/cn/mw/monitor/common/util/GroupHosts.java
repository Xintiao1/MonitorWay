package cn.mw.monitor.common.util;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/7/9 9:25
 * @Version 1.0
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
    private List<OrgDTO> department;

    private List<GroupDTO> group;

    public GroupHosts(String hostid, String name, String status, List<GroupHost> groups, DiscoveryRuleDTO discoveryRule, ParentHost hostDiscovery, List<OrgDTO> department, List<GroupDTO> group) {
        this.hostid = hostid;
        this.name = name;
        this.status = status;
        this.groups = groups;
        this.discoveryRule = discoveryRule;
        this.hostDiscovery = hostDiscovery;
        this.department = department;
        this.group = group;
    }

    public GroupHosts() {
    }
}
