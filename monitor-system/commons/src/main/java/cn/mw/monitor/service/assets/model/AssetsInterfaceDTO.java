package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.scan.model.InterfaceInfo;
import lombok.Data;

import java.util.Date;

@Data
public class AssetsInterfaceDTO {
    private int id;
    private int ifIndex;
    private String name;
    private String type;
    private String state;
    private String description;
    private String mac;
    private Integer mtu;
    private String ip;
    private String subnetMask;
    private String ifMode;
    private String vlan;
    private Boolean vlanFlag;
    private String portType;
    private String vrf;
    private String assetsId;
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
    //是否列表展示
    private Boolean showFlag;
    //是否告警
    private Boolean alertTag;

    private String hostIp;//主机Ip
    private String hostId;//主机监控Id
    private Boolean editorDesc;

    public String genID(){
        String vlanFlagStr = (null == vlanFlag?"":vlanFlag.toString());
        StringBuffer sb = new StringBuffer();
        sb.append("1").append(name)
                .append("2").append(type)
                .append("3").append(description)
                .append("4").append(mac)
                .append("5").append(mtu)
                .append("6").append(ip)
                .append("7").append(subnetMask)
                .append("8").append(ifMode)
                .append("9").append(vlanFlagStr)
                .append("10").append(portType)
                .append("11").append(state)
                .append("12").append(hostIp)
                .append("13").append(hostId)
                ;
        return sb.toString();
    }

    public void extractFromInterfaceInfo(InterfaceInfo interfaceInfo){
        this.ifIndex = interfaceInfo.getIfIndex();
        this.name = interfaceInfo.getName();
        if(interfaceInfo.getIfType()!=null){
            this.type = interfaceInfo.getIfType().name();
        }
        if(interfaceInfo.getIfStatus()!=null){
            this.state = interfaceInfo.getIfStatus().name();
        }
        this.description = interfaceInfo.getDesc();
        this.mtu = interfaceInfo.getMtu();
        this.mac = interfaceInfo.getMac();
        this.ip = interfaceInfo.getIp();
        this.subnetMask = interfaceInfo.getSubnetMask();
        this.ifMode = interfaceInfo.getIfMode();
        this.vlan = interfaceInfo.getVlan();
        this.vlanFlag = interfaceInfo.getVlanFlag();

        if(null != interfaceInfo.getPortType()){
            this.portType = interfaceInfo.getPortType().name();
        }
    }
}
