package cn.mw.monitor.graph.topo;

import cn.mw.monitor.graph.GraphContext;
import cn.mw.monitor.graph.neo4j.Entity;
import cn.mwpaas.common.utils.StringUtils;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NodeEntity(label="TopoNode")
public class Neo4jNode extends Entity {
    private static final String SPLIT = "-";
    private String name;
    private String ip;
    private boolean isRoot = false;
    private boolean pendingDel = false;

    @Relationship(type = "TopoConnect" ,direction = Relationship.OUTGOING)
    private Set<Neo4jNode> neo4jNodes;

    @Labels
    private Set<String> bindTopoIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Set<Neo4jNode> getNeo4jNodes() {
        return neo4jNodes;
    }

    public void setNeo4jNodes(Set<Neo4jNode> neo4jNodes) {
        this.neo4jNodes = neo4jNodes;
    }

    public void addNeo4jNode(Neo4jNode neo4jNode){
        if(null == neo4jNodes){
            neo4jNodes = new HashSet<>();
        }
        neo4jNodes.add(neo4jNode);
    }

    public void extractFromNode(Node node){
        String assetName = node.getAssetName();
        this.name = StringUtils.isEmpty(assetName)?node.getDeviceName():assetName;

        if(StringUtils.isEmpty(this.name)){
            this.name = GraphContext.UNKONWN;
        }
        this.ip = node.getIp();
        this.isRoot = node.isRoot();
        setId(this.name + SPLIT + this.ip);
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public Set<String> getBindTopoIds() {
        return bindTopoIds;
    }

    public void bindTopoId(String topoId){
        if(null == bindTopoIds){
            bindTopoIds = new HashSet<>();
        }
        bindTopoIds.add(topoId);
    }

    public void unbindTopoId(String topoId){
        if(null != bindTopoIds){
            bindTopoIds.remove(topoId);
        }
    }

    public void setPendingDel(boolean pendingDel) {
        this.pendingDel = pendingDel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neo4jNode neo4jNode = (Neo4jNode) o;
        return Objects.equals(getId(), neo4jNode.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
