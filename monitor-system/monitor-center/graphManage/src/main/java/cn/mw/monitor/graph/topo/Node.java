package cn.mw.monitor.graph.topo;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.graph.neo4j.Entity;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.topo.api.MwAssetsLogoService;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Node extends Entity {
    private static String UNKONW_MAC = "未知";

    private boolean virtualNode = false;

    @XmlAttribute
    private List<Integer> anchorPoint;

    private String childAnchor;

    private Integer  openConnect;

    @XmlAttribute
    private int assetTypeId;

    @XmlAttribute
    private String assetName;

    @XmlAttribute
    public boolean hasChild = false;

    @XmlAttribute
    public String parentId = "";

    @XmlAttribute
    private Integer index;

    @XmlAttribute
    private Integer relatedIndex;

    @XmlAttribute
    private int relatedLineIndex;

    @XmlAttribute
    private String relatedType;

    @XmlAttribute
    private String ip;

    @XmlAttribute
    private String mac = UNKONW_MAC;

    /*
     * 设备名称
     */
    @XmlAttribute
    private String deviceName;

    /*
     * 设备描述
     */
    @XmlAttribute
    private String deviceDesc;

    @XmlTransient
    private List<Line> connectLines;

    @XmlTransient
    private List<Node> connectNodes;

    @XmlAttribute
    private boolean isRoot = false;

    @XmlTransient
    private DeviceTypeInfo deviceType;

    @XmlAttribute
    private int level;

    //元素类型
    @XmlAttribute
    private String elementType;

    //坐标-横
    @XmlAttribute
    private double x;

    //坐标-竖
    @XmlAttribute
    private double y;

    //图片信息
    @XmlAttribute
    private String image;

    //描述
    @XmlAttribute
    private String text;

    //文本位置信息
    @XmlAttribute
    private String textPosition;

    //告警
    @XmlAttribute
    private String larm;

    //宽
    @XmlAttribute
    private double width;

    //高
    @XmlAttribute
    private double height;

    @XmlAttribute
    private String fontColor;

    @XmlAttribute
    private String font;

    @XmlAttribute
    private String fillColor;

    //是否过滤
    @XmlAttribute
    private boolean filterEnable = false;

    //上联节点
    @XmlElement
    private List<Integer> parenNodeIndexs;

    //节点绑定的资产id
    @XmlAttribute
    private String tangibleId;

    //节点关联的接口名称
    @XmlAttribute
    private List<Integer> interfaceList;

    //特殊节点,用于背景显示
    @XmlAttribute
    private String backgroundColor;
    @XmlAttribute
    private String backgroundPic;
    @XmlAttribute
    private String backgroundType;

    public DeviceTypeInfo getDeviceType() {
        return deviceType;
    }

    public Node(){
        connectNodes = new ArrayList<Node>();
        connectLines = new ArrayList<Line>();
    }

    @XmlTransient
    public List<Integer> getAnchorPoint() {
        return anchorPoint;
    }

    public void addAnchorPoint(int index){
        if(null == anchorPoint){
            anchorPoint = new ArrayList<>();
        }
        anchorPoint.add(index);
    }

    public void setAnchorPoint(List<Integer> anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    @XmlTransient
    public String getChildAnchor() {
        return childAnchor;
    }

    public void setChildAnchor(String childAnchor) {
        this.childAnchor = childAnchor;
    }

    @XmlTransient
    public int getAssetTypeId() {
        return assetTypeId;
    }

    public void setAssetTypeId(int assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    @XmlTransient
    public List<Integer> getParenNodeIndexs() {
        return parenNodeIndexs;
    }

    public void setParenNodeIndexs(List<Integer> parenNodeIndexs) {
        this.parenNodeIndexs = parenNodeIndexs;
    }

    @XmlTransient
    public boolean isVirtualNode() {
        return virtualNode;
    }

    public void setVirtualNode(boolean virtualNode) {
        this.virtualNode = virtualNode;
    }

    @XmlTransient
    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    @XmlTransient
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @XmlTransient
    public Integer getRelatedIndex() {
        return relatedIndex;
    }

    public void setRelatedIndex(Integer relatedIndex) {
        this.relatedIndex = relatedIndex;
    }

    @XmlTransient
    public int getRelatedLineIndex() {
        return relatedLineIndex;
    }

    public void setRelatedLineIndex(int relatedLineIndex) {
        this.relatedLineIndex = relatedLineIndex;
    }

    @XmlTransient
    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    @XmlTransient
    public String getIp() {
        return (null == ip?"":ip);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @XmlTransient
    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @XmlTransient
    public List<Node> getConnectNodes() {
        return connectNodes;
    }

    public void setConnectNodes(List<Node> connectNodes) {
        this.connectNodes = connectNodes;
    }

    public void addConnect(Node node){
        this.connectNodes.add(node);
    }

    @XmlTransient
    public List<Line> getConnectLines() {
        return connectLines;
    }

    public void setConnectLines(List<Line> connectLines) {
        this.connectLines = connectLines;
    }

    public void addConnectLine(Line line){
        this.connectLines.add(line);
    }

    @XmlTransient
    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    @XmlTransient
    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    @XmlTransient
    public boolean isFilterEnable() {
        return filterEnable;
    }

    public void setFilterEnable(boolean filterEnable) {
        this.filterEnable = filterEnable;
    }

    @XmlTransient
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @XmlTransient
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @XmlTransient
    public String getImage() {
        if (StringUtils.isEmpty(image)) {
            StringBuffer fileNameInTable = new StringBuffer(Constants.MWAPI_BASE_URL)
                    .append(MwAssetsLogoService.MODULE).append("/").append(MwAssetsLogoService.NORMAL_LOGO);
            return fileNameInTable.toString();
        }
        return image;
    }

    @XmlTransient
    public String getOriginImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @XmlTransient
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlTransient
    public String getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(String textPosition) {
        this.textPosition = textPosition;
    }

    @XmlTransient
    public String getLarm() {
        return larm;
    }

    public void setLarm(String larm) {
        this.larm = larm;
    }

    @XmlTransient
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @XmlTransient
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @XmlTransient
    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    @XmlTransient
    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    @XmlTransient
    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    @Override
    public boolean equals(Object o) {
        Node node = (Node) o;
        if (null == ip || null == this.mac || null == node.ip || null == node.mac) {
            return false;
        }

        return this.ip.equals(node.ip) && this.mac.equals(node.mac);
    }

    @XmlTransient
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @XmlTransient
    public String getDeviceDesc() {
        return deviceDesc;
    }

    public void setDeviceDesc(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    @XmlTransient
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @XmlTransient
    public String getTangibleId() {
        return tangibleId;
    }

    public void setTangibleId(String tangibleId) {
        this.tangibleId = tangibleId;
    }

    @XmlTransient
    public List<Integer> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(List<Integer> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public void addInterface(Integer index){
        if(null == interfaceList){
            interfaceList = new ArrayList<>();
        }
        interfaceList.add(index);
    }

    public Integer getOpenConnect() {
        return openConnect;
    }

    public void setOpenConnect(Integer openConnect) {
        this.openConnect = openConnect;
    }


    @XmlTransient
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @XmlTransient
    public String getBackgroundPic() {
        return backgroundPic;
    }

    public void setBackgroundPic(String backgroundPic) {
        this.backgroundPic = backgroundPic;
    }

    @XmlTransient
    public String getBackgroundType() {
        return backgroundType;
    }

    public void setBackgroundType(String backgroundType) {
        this.backgroundType = backgroundType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, mac);
    }

    @Override
    public String toString() {
        return "Node{" +
                "hasChild=" + hasChild +
                ", parentId='" + parentId + '\'' +
                ", index=" + index +
                ", ip='" + ip + '\'' +
                ", assetName=" + assetName +
                ", mac='" + mac + '\'' +
                ", isRoot=" + isRoot +
                ", virtualNode=" + virtualNode +
                ", deviceType=" + deviceType +
                ", level=" + level +
                ", x=" + x +
                ", y=" + y +
                '}';
    }


    public void extractData(MwTangibleassetsTable mwTangibleassetsTable){
        this.ip = mwTangibleassetsTable.getInBandIp();
    }
}
