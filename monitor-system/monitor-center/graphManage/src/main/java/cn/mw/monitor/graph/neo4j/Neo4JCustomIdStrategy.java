package cn.mw.monitor.graph.neo4j;

import cn.mw.monitor.graph.neo4j.Entity;
import org.neo4j.ogm.id.IdStrategy;

public class Neo4JCustomIdStrategy implements IdStrategy {
    @Override
    public Object generateId(Object o) {
        Entity entity = (Entity) o;
        return entity.getId();
    }
}
