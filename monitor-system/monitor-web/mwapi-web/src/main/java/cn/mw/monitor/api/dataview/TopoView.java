package cn.mw.monitor.api.dataview;

import lombok.Data;

import java.util.List;

@Data
public class TopoView {
    private boolean isAssetsFilter;
    private List<List<LineView>> lines;
    private List<List<NodeView>> nodes;
}
