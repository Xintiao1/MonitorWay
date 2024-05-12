package cn.mw.monitor.util;

public enum IDModelType {
    Model("模型管理")
    ,Topo("拓扑管理")
    ,IpManage("ip管理")
    ,AutoManage("自动化")
    ,ConfigManage("配置管理")
    ,LargeScreen("大屏")
    ,MyMonitor("我的监控")
    ,Alert("告警管理")
    ,Log("日志管理")
    ,Report("报表管理")
    ,System("系统管理")
    ,Netflow("流量管理")
    ,User("用户管理")
    ,DataSource("数据源管理")
    ,Visualized("可视化")
    ;

    private String desc;

    IDModelType(String desc){
        this.desc = desc;
    }
}
