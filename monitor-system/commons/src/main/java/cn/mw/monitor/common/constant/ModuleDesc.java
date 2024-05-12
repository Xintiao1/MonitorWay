package cn.mw.monitor.common.constant;


public enum ModuleDesc {
    ASSETS_MANAGE_NET("assets_manage_net","网络性能监控系统v4.4.2","网络性能监控系统v4.4.2 NPM SLCOUNT（支持COUNT台设备）"),
    PROP("prop_manage","网络配置管理系统v2.4.2","网络配置管理系统v2.4.2 NCM DLCOUNT（支持COUNT台设备）"),
    IP("ip_manage","IP地址管理&终端用户追踪系统v2.4.2","IP地址管理&终端用户追踪系统v2.4.2 IPAM&UDT IPULCOUNT（支持COUNT个IP地址+接口）"),
    ASSETS_MANAGE_SERVER("assets_manage_server","服务器&应用监控系统v4.4.2","服务器&应用监控系统v4.4.2 ALCOUNT (支持COUNT个服务器+应用)"),
    ASSETS_MANAGE_STORAGE("assets_manage_storage","存储监控系统v4.4.2","存储监控系统v4.4.2 SRMCOUNT (支持COUNT块物理磁盘)"),
    POLLING_MANAGE("polling_manage","分布式轮询引擎v4.4.2","分布式轮询引擎v4.4.2 MPECOUNT(支持COUNT个扩展轮询引擎)"),
    AUTO_MANAGE("auto-manage","运维自动化平台系统v3.0","运维自动化平台系统v3.0 AutoOpsCOUNT(支持COUNT个资产)"),
    /*ASSETS_MANAGE_YUN("assets_manage_yun","MonitorWay 混合云监控系统v2.4.2","MonitorWay 混合云监控系统v2.4.2 Mcloud"),
    ASSETS_MANAGE_VIR("assets_manage_vir","MonitorWay 虚拟化监控系统v4.4.2","MonitorWay 虚拟化监控系统v4.4.2 VMs-COUNT(支持COUNT颗物理CPU）"),*/
    MODEL("model_manage","智慧资产管理平台系统V1.0","智慧资产管理平台系统V1.0 MWCMDBCOUNT(支持COUNT个资产）- 包含一年原厂服务"),
    MWMONITOR("mw_monitor","智能运维平台系统V1.0","MonitorWay 智能运维平台系统 MWOPS SLCOUNT 含集中监控（支持网络设备、服务器、虚拟化、存储设备、WEB应用、操作系统、中间件、数据库、物联网、动力环境等资产）、告警、报表、可视化、用户管理模块（支持COUNT个资产） - 包含一年原厂服务"),
    ASSETS_MANAGE_WEB("assets_manage_web","WEB性能监测系统V4.4.2","WEB应用性能监控系统V4.4.2 WPMCOUNT(支持COUNT个网站URL监控)");
    /*HOME("home_manage","首页"),
    MODULE("module_manage","模块管理"),
    SCREEN("monitor_screen","监控大屏"),
    TOPO("topo_manage","拓扑管理"),
    MONITOR("mw_monitor","我的监控"),
    LOG("log_audit","日志监控"),
    REPORT("report_manage","报表管理"),
    KNOWLEDGE("knowledge_base","知识库"),
    ASSETS("assets_manage","资产管理"),
    SYS("sys_manage","系统管理"),
    USER("user_manage","用户管理"),
    MODEL("model_manage","模型管理"),
    AUTO("auto_discovery","自动化发现");*/
    private  String code;
    private  String name;
    private  String describe;
    ModuleDesc(String code, String name,String describe){
        this.code = code;
        this.name = name;
        this.describe = describe;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescribe(String describe) {
        this.describe = describe;
    }
    public String getCode() { return code; }
    public String getName() {
        return name;
    }
    public String getDescribe() {
        return describe;
    }


    public static String getModuleDescEnum(String code){
        for (ModuleDesc val : ModuleDesc.values()){
            if(val.getCode().equals(code)){
                return val.getName();
            }
        }
        return null;
    }
    public static String getModuleDesc(String code){
        for (ModuleDesc val : ModuleDesc.values()){
            if(val.getCode().equals(code)){
                return val.getDescribe();
            }
        }
        return null;
    }

}
