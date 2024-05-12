package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupDragParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TopoGroupAfter extends AbstractPositionChange{

    @Override
    void doProcess(List<TopoGroupOrder> srcList, List<TopoGroupOrder> pendingMoveList, int destIndex, TopoGroupInfo topoGroupInfo) {
        List<TopoGroupOrder> list = new ArrayList<>();
        list.addAll(srcList.subList(0 ,destIndex + 1));
        list.addAll(pendingMoveList);
        list.addAll(srcList.subList(destIndex + 1 ,srcList.size()));
        topoGroupInfo.setTopoGroupOrderList(list);
    }
}
