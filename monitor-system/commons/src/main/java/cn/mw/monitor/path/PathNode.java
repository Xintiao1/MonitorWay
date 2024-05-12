package cn.mw.monitor.path;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PathNode {
    private GNode tNode;
    private Map<String ,PrevPath> prevPathMap = new HashMap<>();

    public PathNode(GNode tNode){
        this.tNode = tNode;
    }

    public GNode gettNode() {
        return tNode;
    }

    public void settNode(GNode tNode) {
        this.tNode = tNode;
    }

    public PrevPath getPrevPath(String pathStr) {
        return prevPathMap.get(pathStr);
    }

    public int getPrevPathNum(){
        return prevPathMap.keySet().size();
    }

    public void addPrevPath(PrevPath prevPath) {
        this.prevPathMap.put(prevPath.getPath() ,prevPath);
    }

    public Map<String, PrevPath> getPrevPathMap() {
        return prevPathMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathNode pathNode = (PathNode) o;
        return Objects.equals(tNode, pathNode.tNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tNode);
    }

    @Override
    public String toString() {
        return "PathNode{" +
                "tNode=" + tNode +
                '}';
    }
}
