package cn.mw.monitor.service.topo.model;

public interface TopoGroupChange {
    void changeParent(TopoGroupDTO fromId ,TopoGroupDTO fromParent ,TopoGroupDTO toParent
            ,TopoComponetType componetType);
}
