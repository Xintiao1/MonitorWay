package cn.mw.monitor.model.view;

import cn.mw.monitor.service.graph.EdgeParam;

public class SortEdge implements Comparable<SortEdge>{
    private EdgeParam edgeParam;
    private int endNodePos;

    public SortEdge(EdgeParam edgeParam, int endNodePos){
        this.edgeParam = edgeParam;
        this.endNodePos = endNodePos;
    }

    @Override
    public int compareTo(SortEdge o) {
        return this.endNodePos - o.getEndNodePos();
    }

    public EdgeParam getEdgeParam() {
        return edgeParam;
    }

    public int getEndNodePos() {
        return endNodePos;
    }
}
