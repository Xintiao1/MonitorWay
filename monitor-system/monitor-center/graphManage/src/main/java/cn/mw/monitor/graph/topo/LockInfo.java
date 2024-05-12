package cn.mw.monitor.graph.topo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class LockInfo {
    @XmlAttribute
    public List<Integer> lockNodes = new ArrayList<>();

    @XmlAttribute
    public List<Integer> lockLine = new ArrayList<>();

    @XmlTransient
    public List<Integer> getLockNodes() {
        return lockNodes;
    }

    public void addLockNode(Integer lockIndex) {
        this.lockNodes.add(lockIndex);
    }

    @XmlTransient
    public List<Integer> getLockLine() {
        return lockLine;
    }

    public void addLockLine(Integer lockIndex) {
        this.lockLine.add(lockIndex);
    }

    public void addLockLines(List<Integer> lockIndexs) {
        this.lockLine.addAll(lockIndexs);
    }
}
