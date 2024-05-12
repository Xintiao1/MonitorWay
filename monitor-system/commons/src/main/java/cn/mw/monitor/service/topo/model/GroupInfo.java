package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class GroupInfo implements Comparable<GroupInfo>{
    private TopoComponetType type;
    private TopoGroupDTO topoGroupDTO;
    private String topoType;
    private List<GroupInfo> childGroups = new ArrayList<>();
    private boolean isFinished = true;
    private int order;

    public int getChildNum(){
        int ret = childGroups.size();
        for(GroupInfo child: childGroups){
            int childNum = getChildNum(child);
            ret += childNum;
        }
        return ret;
    }

    private int getChildNum(GroupInfo groupInfo){
        int num = groupInfo.getChildGroups().size();
        for(GroupInfo child : groupInfo.getChildGroups()){
            int childNum = getChildNum(child);
            num += childNum;
        }
        return num;
    }

    public void addChild(GroupInfo groupInfo){
        childGroups.add(groupInfo);
    }

    public List<TopoGroupOrder> getOrderList(Map<String, TopoGroupOrder> orderMap){
        TopoGroupOrder topoGroupOrder = orderMap.get(topoGroupDTO.getId());
        if(null != topoGroupOrder){
            order = topoGroupOrder.getOrder();
        }

        List<TopoGroupOrder> list = new ArrayList<>();
        TopoGroupOrder topoGroupOrder1 = new TopoGroupOrder();
        topoGroupOrder1.setId(topoGroupDTO.getId());
        list.add(topoGroupOrder1);

        if(null != childGroups){
            //设置排序值
            for(GroupInfo child: childGroups){
                setOrderGroupInfo(child ,orderMap);
            }

            //排序
            Collections.sort(childGroups);
            for(GroupInfo child: childGroups){
                orderGroupInfo(child);
            }

            //返回排序列表
            for(GroupInfo child: childGroups){
                addList(child ,list);
            }
        }

        return list;
    }

    private void setOrderGroupInfo(GroupInfo groupInfo , Map<String, TopoGroupOrder> orderMap){
        TopoGroupOrder topoGroupOrder = orderMap.get(groupInfo.getTopoGroupDTO().getId());
        if(null != topoGroupOrder){
            groupInfo.setOrder(topoGroupOrder.getOrder());
        }

        if(null != groupInfo.getChildGroups()){
            for(GroupInfo child: groupInfo.getChildGroups()){
                setOrderGroupInfo(child ,orderMap);
            }
        }
    }

    private void orderGroupInfo(GroupInfo groupInfo){
        if(null != groupInfo.getChildGroups()){
            Collections.sort(groupInfo.getChildGroups());
            for(GroupInfo child: groupInfo.getChildGroups()){
                orderGroupInfo(child);
            }
        }

    }

    private void addList(GroupInfo groupInfo ,List<TopoGroupOrder> list){
        TopoGroupOrder topoGroupOrder = new TopoGroupOrder();
        topoGroupOrder.setId(groupInfo.getTopoGroupDTO().getId());
        list.add(topoGroupOrder);
        if(null != groupInfo.getChildGroups()){
            for(GroupInfo child : groupInfo.getChildGroups()){
                addList(child ,list);
            }
        }

    }

    @Override
    public int compareTo(GroupInfo o) {
        return order - o.getOrder();
    }
}
