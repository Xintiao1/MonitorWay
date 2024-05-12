package cn.mw.monitor.graph.topo;

import cn.mwpaas.common.utils.StringUtils;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Line implements Comparable<Line>{

    @XmlAttribute
    private int[] codes;

    @XmlElement
    private String[] ifNames;

    @XmlAttribute
    private int startPort;

    @XmlAttribute
    private String from;

    @XmlAttribute
    String conLevel;

    @XmlAttribute
    Integer index;

    @XmlAttribute
    Integer startIndex;

    @XmlAttribute
    String startIfDesc;

    private Node startNode;

    @XmlAttribute
    String startConLevel;

    @XmlAttribute
    Integer endIndex;

    @XmlAttribute
    private int endPort;

    private Node endNode;

    @XmlAttribute
    String endIfDesc;

    @XmlAttribute
    String endConLevel;

    @XmlAttribute
    String elementType;

    @XmlAttribute
    String text;

    @XmlAttribute
    String fontColor;

    @XmlAttribute
    String strokeColor;

    @XmlAttribute
    private boolean filterEnable = false;

    @XmlAttribute
    private int lineWidth = 1;

    @XmlAttribute
    private List<Integer> interfaceList;

    public Line(){
        this.codes = new int[]{0,0};
        this.ifNames = new String[]{"",""};
    }

    public Line(int startIndex, String startIfDesc, int endIndex, String endIfDesc){
        this.startIndex = startIndex;
        this.startIfDesc = startIfDesc;
        this.endIndex = endIndex;
        this.endIfDesc = endIfDesc;

        this.codes = new int[]{startIndex, endIndex};
        this.ifNames = new String[]{startIfDesc,endIfDesc};

        Arrays.sort(this.codes);
        Arrays.sort(this.ifNames);
    }

    @XmlTransient
    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    @XmlTransient
    public String getConLevel() {
        return conLevel;
    }

    public void setConLevel(String conLevel) {
        this.conLevel = conLevel;
    }

    @XmlTransient
    public String getStartConLevel() {
        return startConLevel;
    }

    public void setStartConLevel(String startConLevel) {
        this.startConLevel = startConLevel;
    }

    @XmlTransient
    public String getEndConLevel() {
        return endConLevel;
    }

    public void setEndConLevel(String endConLevel) {
        this.endConLevel = endConLevel;
    }

    @XmlTransient
    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(int startPort) {
        this.startPort = startPort;
    }

    @XmlTransient
    public int getEndPort() {
        return endPort;
    }

    public void setEndPort(int endPort) {
        this.endPort = endPort;
    }

    @XmlTransient
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int[] getCodes() {
        return codes;
    }

    public String[] getIfNames() {
        return ifNames;
    }

    @XmlTransient
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @XmlTransient
    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
        this.codes[0] = startIndex;
    }

    @XmlTransient
    public boolean isFilterEnable() {
        return filterEnable;
    }

    public void setFilterEnable(boolean filterEnable) {
        this.filterEnable = filterEnable;
    }

    @XmlTransient
    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
        this.codes[1] = endIndex;
    }

    @XmlTransient
    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    @XmlTransient
    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    @XmlTransient
    public String getStartIfDesc() {
        return startIfDesc;
    }

    public void setStartIfDesc(String startIfDesc) {
        this.startIfDesc = startIfDesc;
        this.ifNames[0] = startIfDesc;
    }

    @XmlTransient
    public String getEndIfDesc() {
        return endIfDesc;
    }

    public void setEndIfDesc(String endIfDesc) {
        this.endIfDesc = endIfDesc;
        this.ifNames[1] = endIfDesc;
    }

    @XmlTransient
    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    @XmlTransient
    public List<Integer> getInterfaceList() {
        return interfaceList;
    }

    public void addInterface(Integer index){
        if(null == interfaceList){
            interfaceList = new ArrayList<>();
        }
        interfaceList.add(index);
    }

    public void setInterfaceList(List<Integer> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public void sortCode(){
        Arrays.sort(this.codes);
        Arrays.sort(this.ifNames);
    }



    @Override
    public String toString() {
        return "Line{" +
                "index=" + index +
                ", startIndex=" + startIndex +
                ", startIfDesc='" + startIfDesc + '\'' +
                ", codeStr='" + getCodesStr() + '\'' +
                ", endIndex=" + endIndex +
                ", endIfDesc='" + endIfDesc + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        Line line = (Line) o;

        if(Arrays.equals(this.codes, line.getCodes()) && Arrays.equals(this.ifNames, line.getIfNames())){
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int codeHash = this.codes[0] * 31 + this.codes[1];
        int ifNameHash = this.ifNames[0].hashCode() * 31 + this.ifNames[1].hashCode();
        return Objects.hash(codeHash, ifNameHash);
    }

    @XmlTransient
    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    @XmlTransient
    public String getText() {
        return text;
    }

    @XmlTransient
    public String getFontColor() {
        return fontColor;
    }


    public String getCodesStr(){
        return codes[0] + "-" + codes[1];
    }

    public boolean validatLine(){
        if(StringUtils.isEmpty(startIfDesc)
        || StringUtils.isEmpty(endIfDesc)
        ){
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Line o) {
        return index - o.index;
    }
}
