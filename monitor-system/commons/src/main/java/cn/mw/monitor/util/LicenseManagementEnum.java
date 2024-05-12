package cn.mw.monitor.util;


import java.util.Arrays;
import java.util.List;

public enum LicenseManagementEnum {
    //服务器应用监控：服务器，应用，中间件，数据库，外带资产,
    ASSETS_MANAGE_SERVER("assets_manage_server",Arrays.asList(1,6,7,8,69)),
    //网络性能：网络设备，安全设备，线路
    ASSETS_MANAGE_NET("assets_manage_net", Arrays.asList(2,3,9)),
    //存储设备
    ASSETS_MANAGE_STORAGE("assets_manage_storage",Arrays.asList(4)),
    //web
    ASSETS_MANAGE_WEB("assets_manage_web",Arrays.asList(11));

    private List<Integer> typeId;
    private String moduleName;

    public static String getModuleName(Integer typeId) {
        for (LicenseManagementEnum lic : values()) {
            if(lic.getTypeId().contains(typeId)){
                return lic.getModuleName();
            }
        }
        return null;
    }

    public String getModuleName() {
        return moduleName;
    }
    public List<Integer> getTypeId() {
        return typeId;
    }

    LicenseManagementEnum(String moduleName,List<Integer> typeId) {
        this.moduleName = moduleName;
        this.typeId = typeId;
    }
}
