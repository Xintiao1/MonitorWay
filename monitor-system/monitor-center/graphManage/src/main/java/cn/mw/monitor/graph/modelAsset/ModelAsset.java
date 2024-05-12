package cn.mw.monitor.graph.modelAsset;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;

@NodeEntity(label="ModelAsset")
public class ModelAsset {
    @Id
    private Integer id;

    @Relationship(type = "RelateModel" ,direction = Relationship.UNDIRECTED)
    private List<ModelRelate> modelRelates;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void addModelRelate(ModelRelate modelRelate){
        if(null == modelRelates){
            modelRelates = new ArrayList<>();
        }
        modelRelates.add(modelRelate);
    }

    public List<ModelRelate> getModelRelates() {
        return modelRelates;
    }

    public void extractFrom(ModelAsset modelAsset){
        this.id = modelAsset.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelAsset that = (ModelAsset) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
