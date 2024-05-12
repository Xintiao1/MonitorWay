package cn.mw.monitor.util;


public enum ModuleNameEnum {
    ASSETS_MANAGE("assets_manage"),
    //服务器，应用，中间件，数据库，外带资产
    ASSETS_MANAGE_SERVER("assets_manage_server"),
    //网络设备，安全设备，线路
    ASSETS_MANAGE_NET("assets_manage_net"),
    //存储设备
    ASSETS_MANAGE_STORAGE("assets_manage_storage"),
    //配置管理
    PROP_MANAGE("prop_manage"),
    //运维监控
    MW_MONITOR("mw_monitor"),
    //日志管理
    LOG_SECURITY("log_security"),
    //自动化
    AUTOMANAGE("auto-manage"),
    //web
    ASSETS_MANAGE_WEB("assets_manage_web");


    private String moduleName;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    ModuleNameEnum(String moduleName) {
        this.moduleName = moduleName;
    }
}
