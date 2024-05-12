package cn.mw.monitor.graph.topo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class Lines {

    @XmlElement
    public List<Line> line;

    public Lines(){
        this.line = new ArrayList<Line>();
    }

    @XmlTransient
    public List<Line> getLine() {
        return this.line;
    }

    public void setLine(List<Line> line) {
        this.line = line;
    }
}
