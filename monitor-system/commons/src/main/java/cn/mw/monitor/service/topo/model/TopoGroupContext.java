package cn.mw.monitor.service.topo.model;

import java.util.HashMap;
import java.util.Map;

public class TopoGroupContext {
    private GroupInfo root;
    private Map<String ,GroupInfo> groupInfoMap = new HashMap<>();


    public void addGroupInfoMap(GroupInfo groupInfo){
        groupInfoMap.put(groupInfo.getTopoGroupDTO().getId() ,groupInfo);
    }

    public GroupInfo getRoot() {
        return root;
    }

    public void setRoot(GroupInfo root) {
        this.root = root;
    }

    public Map<String, GroupInfo> getGroupInfoMap() {
        return groupInfoMap;
    }

    public void setGroupInfoMap(Map<String, GroupInfo> groupInfoMap) {
        this.groupInfoMap = groupInfoMap;
    }
}
