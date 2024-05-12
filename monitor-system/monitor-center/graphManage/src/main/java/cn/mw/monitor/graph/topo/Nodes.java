package cn.mw.monitor.graph.topo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class Nodes {
    @XmlElement
    public List<Node> node;

    public Nodes(){
        this.node = new ArrayList<Node>();
    }

    @XmlTransient
    public List<Node> getNode() {
        return this.node;
    }

    public void setNode(List<Node> node) {
        this.node = node;
    }
}
