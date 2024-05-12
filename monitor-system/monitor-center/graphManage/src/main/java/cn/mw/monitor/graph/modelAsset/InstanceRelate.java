package cn.mw.monitor.graph.modelAsset;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "InstanceRelate")
public class InstanceRelate {

    @Id
    @GeneratedValue
    private Long relationId;

    public InstanceRelate(){}

    public InstanceRelate(InstanceAsset startNode ,InstanceAsset endNode){
        this.startNode = startNode;
        this.endNode = endNode;
    }

    @StartNode
    private InstanceAsset startNode;

    @EndNode
    private InstanceAsset endNode;

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public InstanceAsset getStartNode() {
        return startNode;
    }

    public void setStartNode(InstanceAsset startNode) {
        this.startNode = startNode;
    }

    public InstanceAsset getEndNode() {
        return endNode;
    }

    public void setEndNode(InstanceAsset endNode) {
        this.endNode = endNode;
    }

}
