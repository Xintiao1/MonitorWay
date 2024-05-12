package cn.mw.monitor.graph.neo4j;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

public abstract class Entity {
    @Id
    @GeneratedValue(strategy = Neo4JCustomIdStrategy.class)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
