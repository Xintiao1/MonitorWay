package cn.mw.monitor.service.graph;


import cn.mw.monitor.service.graph.NodeParam;
import org.neo4j.ogm.response.model.NodeModel;

public class InstanceRelateNode {
    private String fromId;
    private String relationShip;
    private String toId;
    private String[] labels;

    public InstanceRelateNode(NodeModel from ,NodeModel to ,String relationShip ,String[] labels){
        this.relationShip = relationShip;
        this.fromId = from.getPropertyList().get(0).toString().replaceAll("id :" ,"").trim();
        this.toId = to.getPropertyList().get(0).toString().replaceAll("id :" ,"").trim();
        this.labels = labels;
    }

    public String getRelationShip() {
        return relationShip;
    }

    public void setRelationShip(String relationShip) {
        this.relationShip = relationShip;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public String[] getLabels() {
        return labels;
    }

    public NodeParam getResetNodeParamByFrom(){
        NodeParam nodeParam = new NodeParam(this.fromId);
        nodeParam.initEmptyNode(nodeParam.getComboId());
        return nodeParam;
    }
}
