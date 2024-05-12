package cn.mw.monitor.model.data;

import cn.mw.monitor.service.graph.NodeParam;

public class SortNode {
    private NodeParam nodeParam;
    private int index;

    public SortNode(NodeParam nodeParam ,int index){
        this.nodeParam = nodeParam;
        this.index = index;
    }

    public NodeParam getNodeParam() {
        return nodeParam;
    }

    public void setNodeParam(NodeParam nodeParam) {
        this.nodeParam = nodeParam;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
