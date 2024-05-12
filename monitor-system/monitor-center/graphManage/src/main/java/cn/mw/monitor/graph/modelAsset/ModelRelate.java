package cn.mw.monitor.graph.modelAsset;

import cn.mwpaas.common.utils.StringUtils;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RelationshipEntity(type = "RelateModel")
public class ModelRelate {

    @Id
    @GeneratedValue
    private Long relationId;

    @StartNode
    private ModelAsset startNode;

    @EndNode
    private ModelAsset endNode;

    @Convert(ModelRelationDTOConverter.class)
    @Property
    private ModelRelationDTO  modelRelationDTO;

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public ModelAsset getStartNode() {
        return startNode;
    }

    public void setStartNode(ModelAsset startNode) {
        this.startNode = startNode;
    }

    public ModelAsset getEndNode() {
        return endNode;
    }

    public void setEndNode(ModelAsset endNode) {
        this.endNode = endNode;
    }

    public ModelRelationDTO getModelRelationDTO() {
        return modelRelationDTO;
    }

    public void setModelRelationDTO(ModelRelationDTO modelRelationDTO) {
        this.modelRelationDTO = modelRelationDTO;
    }

    public String genKey(){
        List<String> ids = new ArrayList<>();
        ids.add(startNode.getId().toString());
        ids.add(endNode.getId().toString());
        Collections.sort(ids);
        String key = StringUtils.join(ids ,"-");
        return key;
    }
}
