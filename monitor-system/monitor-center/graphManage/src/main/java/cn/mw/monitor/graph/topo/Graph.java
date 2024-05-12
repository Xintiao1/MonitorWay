package cn.mw.monitor.graph.topo;

import lombok.extern.slf4j.Slf4j;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;


@XmlRootElement(name = "graph")
@Slf4j
public class Graph {
    //有的特殊节点,类型为area的会使用999999999作为index
    //此时获取index的时候,需要过滤掉
    public static final int VALID_MAX_INDEX = 999999998;

    private int index = 0;

    @XmlAttribute
    private String backgroundFillColor;

    private boolean debug;

    public int genLineIndex(){
        index++;
        return index;
    }

    private String lineColor;

    @XmlTransient
    public int getIndex() {
        return index;
    }

    public int genIndex(){
        index++;
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @XmlElement
    public List<Group> group = new ArrayList<Group>();

    @XmlTransient
    public List<Group> getGroup() {
        return group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    @XmlTransient
    public String getBackgroundFillColor() {
        return backgroundFillColor;
    }

    public void setBackgroundFillColor(String backgroundFillColor) {
        this.backgroundFillColor = backgroundFillColor;
    }

    public void init(){
        this.group.add(new Group());
    }

    public Map<Integer, List<Line>> genIndexLineMap(List<Line> lines, boolean isStartIndex){
        Map<Integer, List<Line>> lineMap = new HashMap<>();
        for(Line line : lines){
            Integer index = 0;
            if(isStartIndex){
                index = line.getStartIndex();
            }else{
                index = line.getEndIndex();
            }
            List<Line> list = lineMap.get(index);

            if(null == list){
                list = new ArrayList<>();
                lineMap.put(line.getStartIndex(), list);
            }

            list.add(line);
        }
        return lineMap;
    }

    //过滤可能导致环形的线
    public Map<Integer, List<Line>> circleFilter(List<Line> lines, List<Node> nodes){
        Map<Integer, List<Line>> lineMap = genIndexLineMap(lines, true);

        Map<Integer, List<Line>> circleMap = new HashMap<>();
        Set<Integer> analysedIndexes = new HashSet<>();
        for(Node node: nodes) {
            Set<Integer> circleNodes = new HashSet<>();
            Set<Integer> checkNodes = new HashSet<>();
            checkNodes.add(node.getIndex());

            List<Line> circleLines = new ArrayList<>();
            log.info("circleFilter node:{}", node.getIndex());
            if(!analysedIndexes.contains(node.getIndex())) {
                boolean find = checkCircle(node.getIndex(), circleNodes, circleLines, lineMap ,checkNodes);
                if (find) {
                    circleMap.put(node.getIndex(), circleLines);
                    analysedIndexes.addAll(circleNodes);
                }
            }
        }

        return circleMap;
    }

    private boolean checkCircle(Integer startNode, Set<Integer> circleNodes
            , List<Line> circleLines ,Map<Integer, List<Line>> lineMap, Set<Integer> checkNodes){

        Set<Integer> nodes = new HashSet<>();
        nodes.addAll(checkNodes);

        List<Line> lines = lineMap.get(startNode);
        boolean find = false;

        if(null != lines){
            for(Line line : lines) {
                Integer nextIndex = line.getEndIndex();
                log.info("checkCircle startNode:{}, endNode:{}", startNode, nextIndex);

                if (nodes.contains(nextIndex)) {
                    log.info("checkCircle contains node:{}", startNode, nextIndex);
                    circleNodes.add(startNode);
                    circleLines.add(line);
                    find = true;
                    continue;
                }

                nodes.add(nextIndex);

                boolean ret = checkCircle(nextIndex, circleNodes, circleLines, lineMap ,nodes);
                if (ret) {
                    circleNodes.add(startNode);
                    circleLines.add(line);
                    find = true;
                }

                nodes = new HashSet<>();
                nodes.addAll(checkNodes);
            }
        }

        return find;
    }

    public Line newLine(String from ,int startIndex ,String upIf ,int endIndex ,String downIf ,String elementType){
        int index = genLineIndex();

        Line line = new Line(startIndex , upIf ,endIndex ,downIf);
        line.setIndex(index);
        line.setFrom(from);
        line.setStartIndex(startIndex);
        line.setEndIndex(endIndex);
        line.setStrokeColor(lineColor);
        line.setElementType(elementType);
        return line;
    }

    public Node newNode(boolean isGenIndex){
        Node node = new Node();
        if(isGenIndex) {
            int index = genIndex();
            node.setIndex(index);
        }
        return node;
    }
}
