package cn.mw.monitor.service.topo.model;

import cn.mw.monitor.service.topo.param.TopoGroupDragParam;

public interface PositionChange {
    void process(TopoGroupDragParam topoGroupDragParam ,TopoGroupInfo topoGroupInfo
            ,TopoGroupContext topoGroupContext ,TopoGroupChange callback);
}
