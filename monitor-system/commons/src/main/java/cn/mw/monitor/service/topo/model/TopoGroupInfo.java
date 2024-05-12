package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupDragParam;
import lombok.Data;

import java.util.*;

@Data
public class TopoGroupInfo {
    private List<TopoGroupOrder> topoGroupOrderList = new ArrayList<>();
    private static Map<TopoGroupPositionType ,PositionChange> positionChangeMap;

    static {
        positionChangeMap = new HashMap<>();
        positionChangeMap.put(TopoGroupPositionType.before ,new TopoGroupBefore());
        positionChangeMap.put(TopoGroupPositionType.after ,new TopoGroupAfter());
        positionChangeMap.put(TopoGroupPositionType.inner ,new TopoGroupInner());
    }

    public void updateOrderInfo(GroupInfo groupInfo){
        Map<String ,Integer> orderMap = new HashMap<>();
        for(TopoGroupOrder topoGroupOrder: topoGroupOrderList){
            orderMap.put(topoGroupOrder.getId() ,topoGroupOrder.getOrder());
        }
        orderGroupInfo(groupInfo ,orderMap);
    }

    private void orderGroupInfo(GroupInfo groupInfo ,Map<String ,Integer> orderMap){
        if(null != groupInfo.getChildGroups()){
            for(GroupInfo child : groupInfo.getChildGroups()){
                Integer order = orderMap.get(child.getTopoGroupDTO().getId());
                if(null != order) {
                    child.setOrder(order);
                }
            }
            List<GroupInfo> list = groupInfo.getChildGroups();
            Collections.sort(list);

            for(GroupInfo child : groupInfo.getChildGroups()){
                orderGroupInfo(child ,orderMap);
            }
        }
    }

    public void rePosition(TopoGroupDragParam topoGroupDragParam ,TopoGroupContext topoGroupContext ,TopoGroupChange callback){
        TopoGroupPositionType type = TopoGroupPositionType.valueOf(topoGroupDragParam.getPosition());
        PositionChange positionChange = positionChangeMap.get(type);
        positionChange.process(topoGroupDragParam ,this ,topoGroupContext ,callback);
        for(int i = 0; i < topoGroupOrderList.size(); i++){
            TopoGroupOrder topoGroupOrder = topoGroupOrderList.get(i);
            topoGroupOrder.setOrder(i);
        }
    }

    public void extractFrom(GroupInfo groupInfo){
        visit(groupInfo);
    }

    private void visit(GroupInfo groupInfo){
        TopoGroupOrder topoGroupOrder = new TopoGroupOrder();
        topoGroupOrder.setId(groupInfo.getTopoGroupDTO().getId());
        this.topoGroupOrderList.add(topoGroupOrder);

        if(null != groupInfo.getChildGroups()){
            for(GroupInfo child : groupInfo.getChildGroups()){
                visit(child);
            }
        }
    }
}
