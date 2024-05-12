package cn.mw.monitor.graph.topo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

public class Group {

    @XmlElement
    public Lines lines = new Lines();

    @XmlElement
    public Nodes nodes = new Nodes();

    @XmlElement
    public LockInfo lockInfo = new LockInfo();

    @XmlTransient
    public Lines getLines() {
        return this.lines;
    }

    public void setLines(Lines lines) {
        this.lines = lines;
    }

    @XmlTransient
    public Nodes getNodes() {
        return nodes;
    }

    public void setNodes(Nodes nodes) {
        this.nodes = nodes;
    }

    public Map<Integer, List<Line>> getLineMapByStartIndex(){
        Map<Integer, List<Line>> linesMap = new HashMap<>();
        for(Line line :lines.getLine()) {
            int index = line.getStartIndex();
            List<Line> lineList = linesMap.get(index);
            if (null == lineList) {
                lineList = new ArrayList<>();
                linesMap.put(index, lineList);
            }
            lineList.add(line);
        }
        return linesMap;
    }

    public Map<String, List<Line>> findLineMapByLineCode(){
        Map<String, List<Line>> linesMap = new HashMap<>();
        for(Line line :lines.getLine()) {
            String codeStr = line.getCodesStr();
            List<Line> lineList = linesMap.get(codeStr);
            if (null == lineList) {
                lineList = new ArrayList<>();
                linesMap.put(codeStr, lineList);
            }
            lineList.add(line);
        }
        return linesMap;
    }

    @XmlTransient
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void addLockLine(Integer lockIndex) {
        this.lockInfo.addLockLine(lockIndex);
    }

    public void addLockNode(Integer lockIndex) {
        this.lockInfo.addLockNode(lockIndex);
    }

    public Map<Integer, Line> getLineMapByIndex(){
        Map<Integer, Line> linesMap = new HashMap<>();
        for(Line line :lines.getLine()) {
            linesMap.put(line.getIndex() ,line);
        }
        return linesMap;
    }

    public Map<Integer, List<Line>> getLineMapByEndIndex(){
        Map<Integer, List<Line>> linesMap = new HashMap<>();
        for(Line line :lines.getLine()) {
            int index = line.getEndIndex();
            List<Line> lineList = linesMap.get(index);
            if (null == lineList) {
                lineList = new ArrayList<>();
                linesMap.put(index, lineList);
            }
            lineList.add(line);
        }
        return linesMap;
    }

    public Map<Integer, Node> getNodeIndexMap(){
        Map<Integer, Node> nodeIndexMap = new HashMap<>();
        for(Node node : nodes.getNode()){
            nodeIndexMap.put(node.getIndex() ,node);
        }
        return nodeIndexMap;
    }

    public Map<String, Node> getNodeIpMap(){
        Map<String, Node> nodeIpMap = new HashMap<>();
        for(Node node : nodes.getNode()){
            nodeIpMap.put(node.getIp() ,node);
        }
        return nodeIpMap;
    }

    public Set<Line> getLineSet(){
        Set<Line> lineSet = new HashSet<>(lines.getLine());
        return lineSet;
    }

    public Set<Node> getNodeSet(){
        Set<Node> nodeSet = new HashSet<>(nodes.getNode());
        return nodeSet;
    }

    public void removeLine(Line line){
        lines.getLine().remove(line);
    }

    public void addLine(Line line){
        lines.getLine().add(line);
    }

    public void addNode(Node node){
        nodes.getNode().add(node);
    }
}
