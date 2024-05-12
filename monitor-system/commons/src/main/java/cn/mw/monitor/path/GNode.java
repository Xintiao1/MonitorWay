package cn.mw.monitor.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GNode {
    private String name;
    private List<GNode> childs = new ArrayList<>();

    public GNode(String name){
        this.name = name;
    }

    public List<GNode> getChilds() {
        return childs;
    }

    public void setChilds(List<GNode> childs) {
        this.childs = childs;
    }

    public void addChilds(GNode child) {
        this.childs.add(child);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GNode tNode = (GNode) o;
        return Objects.equals(name, tNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
