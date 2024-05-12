package cn.mw.monitor.model.data;

import cn.mw.monitor.service.graph.EdgeParam;

import java.util.Comparator;

public class SortNewEdge implements Comparable<SortNewEdge> {
    private EdgeParam edgeParam;
    private int startIndex;

    private int endIndex;

    public SortNewEdge(EdgeParam edgeParam){
        this.edgeParam = edgeParam;
    }

    public EdgeParam getEdgeParam() {
        return edgeParam;
    }

    public void setEdgeParam(EdgeParam edgeParam) {
        this.edgeParam = edgeParam;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public int compareTo(SortNewEdge o) {
        int compValue = getStartIndex() - o.getStartIndex();
        if(0 != compValue){
            return compValue;
        }

        int compValue1 = getEndIndex() - o.getEndIndex();
        return compValue1;
    }
}
