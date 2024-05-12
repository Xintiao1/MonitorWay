package cn.mw.monitor.service.license.param;

import java.util.Arrays;
import java.util.List;

public enum LicenseModuleldEnum {

    HOME_MANAGE("home_manage", Arrays.asList(1)),
    MONITOR_SCREEN("monitor_screen",Arrays.asList(2)),
    REPORT_MANAGE("report_manage",Arrays.asList(27)),
    TOPO_MANAGE("topo_manage",Arrays.asList(3)),
    KNOWLEDGE_BASE("knowledge_base",Arrays.asList(30)),
    SYS_MANAGE("sys_manage",Arrays.asList(36)),
    PROP_MANAGE("prop_manage",Arrays.asList(7)),
    IP_MANAGE("ip_manage",Arrays.asList(26)),
    POLLING_MANAGE("polling_manage",Arrays.asList(39)),
    LOG_SECURITY("log_security",Arrays.asList(224)),
    AUTO_MANAGE("auto-manage",Arrays.asList(237)),
    ASSETS_MANAGE("assets_manage",Arrays.asList(31,32,33,34,37)),
    MW_MONITOR("mw_monitor",Arrays.asList(11,12,13,14,15,16,17,19,19,20,21,22,96,192,210)),
    NETFLOW_MANAGE("netflow_manage",Arrays.asList(247,248,270)),
    MODEL_MANAGE("model_manage",Arrays.asList(216));

    private String name;
    private List<Integer> moduleIds;
    LicenseModuleldEnum(String name, List<Integer> moduleIds){
        this.name = name;
        this.moduleIds = moduleIds;
    }
    public String getName(){
        return name;
    }
    public List<Integer> getModuleIds(){
        return moduleIds;
    }
    public static List<Integer> getModuleIdsByName(String name) {
        for (LicenseModuleldEnum val : LicenseModuleldEnum.values()){
            if(val.getName().equals(name)){
                return val.getModuleIds();
            }
        }
        return null;
    }
}
