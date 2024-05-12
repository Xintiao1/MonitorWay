package cn.mw.monitor.graph.modelAsset;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;

@NodeEntity(label="InstanceAsset")
public class InstanceAsset {

    @Id
    private Integer id;

    @Relationship(type = "InstanceRelate" ,direction = Relationship.UNDIRECTED)
    private List<InstanceRelate> instanceRelates;

    public InstanceAsset(){

    }

    public InstanceAsset(Integer id){
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addInstanceRelate(InstanceRelate instanceRelate){
        if(null == instanceRelates){
            instanceRelates = new ArrayList<>();
        }
        instanceRelates.add(instanceRelate);
    }

    public List<InstanceRelate> getInstanceRelates() {
        return instanceRelates;
    }

    public void setInstanceRelates(List<InstanceRelate> instanceRelates) {
        this.instanceRelates = instanceRelates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceAsset asset = (InstanceAsset) o;
        return id == asset.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
