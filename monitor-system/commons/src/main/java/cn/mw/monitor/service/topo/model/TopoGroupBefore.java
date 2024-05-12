package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupDragParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TopoGroupBefore extends AbstractPositionChange {

    @Override
    void doProcess(List<TopoGroupOrder> srcList, List<TopoGroupOrder> pendingMoveList, int destIndex, TopoGroupInfo topoGroupInfo) {
        List<TopoGroupOrder> list = new ArrayList<>();
        list.addAll(srcList.subList(0 ,destIndex));
        list.addAll(pendingMoveList);
        list.addAll(srcList.subList(destIndex ,srcList.size()));
        topoGroupInfo.setTopoGroupOrderList(list);
    }
}
