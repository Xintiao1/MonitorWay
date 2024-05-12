package cn.mw.monitor.graph.topo;

import java.io.Serializable;
import java.util.*;

public class DeviceTypeInfo implements Serializable {

    //猫维资产子类型表对应
    //18: 交换机, 20:防火墙
    public static Map<Integer, DeviceTypeInfo> MonitorTemplateType = new HashMap<>();

    private String deviceProductType = "";

    private String logicType = "";

    private List<String> typeOIDCharacter = new Vector();

    private List<String> typeDescCharacter = new Vector();

    public static final DeviceTypeInfo UnkownType = new DeviceTypeInfo();

    public static final DeviceTypeInfo SwitchType = new DeviceTypeInfo();

    public static final DeviceTypeInfo RouterType = new DeviceTypeInfo();

    public static final DeviceTypeInfo RouterSwitchType = new DeviceTypeInfo();

    public static final DeviceTypeInfo ComputerType = new DeviceTypeInfo();

    public static final DeviceTypeInfo HubType = new DeviceTypeInfo();

    public static final DeviceTypeInfo SwitchCloudType = new DeviceTypeInfo();

    public static final DeviceTypeInfo FireWallType = new DeviceTypeInfo();

    public static final List<DeviceTypeInfo> networkCheckType = new ArrayList<>();

    static  {
        MonitorTemplateType.put(18, SwitchType);
        MonitorTemplateType.put(20, FireWallType);

        UnkownType.setDeviceProductType("other type");
        UnkownType.setLogicType("other type");
        SwitchType.setDeviceProductType("switch");
        SwitchType.setLogicType("switch");
        RouterType.setDeviceProductType("router");
        RouterType.setLogicType("router");
        RouterSwitchType.setDeviceProductType("routerSwitch");
        RouterSwitchType.setLogicType("routerSwitch");
        ComputerType.setDeviceProductType("computer");
        ComputerType.setLogicType("computer");
        HubType.setDeviceProductType("hub");
        HubType.setLogicType("hub");
        SwitchCloudType.setDeviceProductType("switchCloud");
        SwitchCloudType.setLogicType("switchCloud");
        FireWallType.setDeviceProductType("firewall");
        FireWallType.setLogicType("firewall");

        networkCheckType.add(DeviceTypeInfo.SwitchType);
        networkCheckType.add(DeviceTypeInfo.FireWallType);
        networkCheckType.add(DeviceTypeInfo.RouterSwitchType);
        networkCheckType.add(DeviceTypeInfo.RouterType);
    }

    public String toString() { return this.deviceProductType; }

    public boolean equals(Object paramObject) {
        if (paramObject instanceof DeviceTypeInfo) {
            DeviceTypeInfo deviceTypeInfo = (DeviceTypeInfo)paramObject;
            if (getDeviceProductType().equals(deviceTypeInfo.getDeviceProductType()))
                return true;
        }
        return false;
    }

    public int hashCode() { return this.deviceProductType.hashCode(); }

    public void setDeviceProductType(String paramString) { this.deviceProductType = paramString; }

    public String getDeviceProductType() { return this.deviceProductType; }

    public void setLogicType(String paramString) { this.logicType = paramString; }

    public String getLogicType() { return this.logicType; }

}
