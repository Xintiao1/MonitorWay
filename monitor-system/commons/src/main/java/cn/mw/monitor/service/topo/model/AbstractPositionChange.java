package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupDragParam;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
abstract class AbstractPositionChange implements PositionChange{

    public void process(TopoGroupDragParam topoGroupDragParam ,TopoGroupInfo topoGroupInfo ,TopoGroupContext topoGroupContext
            ,TopoGroupChange callback){
        GroupInfo to = topoGroupContext.getGroupInfoMap().get(topoGroupDragParam.getToId());
        if(StringUtils.isEmpty(to.getTopoGroupDTO().getParentId())){
            return;
        }

        List<TopoGroupOrder> srcList = topoGroupInfo.getTopoGroupOrderList();
        Map<String ,TopoGroupOrder> orderMap = srcList.stream().collect(Collectors.toMap(TopoGroupOrder::getId,
                v -> v,
                (key1 , key2) -> key2));

        //找到需要移动id
        GroupInfo groupInfo = null;
        if(StringUtils.isNotEmpty(topoGroupDragParam.getDragerId())) {
            groupInfo = getMoveGroupInfo(topoGroupDragParam.getDragerId(), topoGroupContext.getRoot());
        }

        if(null == groupInfo){
            log.error("process find no groupInfo");
            return;
        }
        List<TopoGroupOrder> pendingMoveList = groupInfo.getOrderList(orderMap);

        TopoGroupPositionType positionType = TopoGroupPositionType.valueOf(topoGroupDragParam.getPosition());
        int destIndex = 0;

        srcList.removeAll(pendingMoveList);

        for (int i = 0; i < srcList.size(); i++) {
            TopoGroupOrder topoGroupOrder = srcList.get(i);
            //找到目的位置
            if (topoGroupOrder.getId().equals(topoGroupDragParam.getToId())) {
                destIndex = i;
            }
        }

        if(positionType == TopoGroupPositionType.inner) {
            int childNum = to.getChildNum();
            destIndex += childNum;
        }

        doProcess(srcList ,pendingMoveList ,destIndex ,topoGroupInfo);

        if(null != callback){
            GroupInfo from = topoGroupContext.getGroupInfoMap().get(topoGroupDragParam.getDragerId());

            if(!from.getTopoGroupDTO().getParentId().equals(to.getTopoGroupDTO().getParentId())
            || positionType == TopoGroupPositionType.inner){
                GroupInfo toParent = topoGroupContext.getGroupInfoMap().get(to.getTopoGroupDTO().getParentId());
                GroupInfo fromParent = topoGroupContext.getGroupInfoMap().get(from.getTopoGroupDTO().getParentId());

                TopoGroupDTO toTopoGroupDTO = null;
                if(positionType != TopoGroupPositionType.inner || TopoComponetType.topo == to.getType()){
                    toTopoGroupDTO = toParent.getTopoGroupDTO();
                }else{
                    toTopoGroupDTO = to.getTopoGroupDTO();
                }

                callback.changeParent(from.getTopoGroupDTO() ,fromParent.getTopoGroupDTO() ,toTopoGroupDTO ,from.getType());
            }
        }
    }

    private GroupInfo getMoveGroupInfo(String id ,GroupInfo groupInfo){
        if(id.equals(groupInfo.getTopoGroupDTO().getId())){
            return groupInfo;
        }else{
            if(null != groupInfo.getChildGroups()){
                for(GroupInfo child : groupInfo.getChildGroups()){
                    GroupInfo ret = getMoveGroupInfo(id ,child);
                    if(null != ret){
                        return ret;
                    }
                }
            }
        }

        return null;
    }

    private void addChild(GroupInfo groupInfo ,Set<String> moveSet){
        if(null != groupInfo.getChildGroups()){
            for(GroupInfo child : groupInfo.getChildGroups()){
                moveSet.add(child.getTopoGroupDTO().getId());
                addChild(child ,moveSet);
            }
        }
    }

    abstract void doProcess(List<TopoGroupOrder> srcList ,List<TopoGroupOrder> pendingMoveList ,int destIndex
            ,TopoGroupInfo topoGroupInfo);
}
