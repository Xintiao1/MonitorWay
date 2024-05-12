package cn.mw.monitor.service.topo.model;

import java.util.ArrayList;
import java.util.List;

public class TopoGroupInner extends AbstractPositionChange {

    @Override
    void doProcess(List<TopoGroupOrder> srcList, List<TopoGroupOrder> pendingMoveList, int destIndex, TopoGroupInfo topoGroupInfo) {
        List<TopoGroupOrder> list = new ArrayList<>();

        int end = destIndex + 1;
        if(end < srcList.size()) {
            list.addAll(srcList.subList(0, destIndex + 1));
            list.addAll(pendingMoveList);
            list.addAll(srcList.subList(destIndex + 1, srcList.size()));
        }else{
            list.addAll(srcList);
            list.addAll(pendingMoveList);
        }
        topoGroupInfo.setTopoGroupOrderList(list);
    }
}
