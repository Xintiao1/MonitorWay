package cn.mw.monitor.report.param;


/**
 * @author xhy
 * @date 2020/12/25 11:08
 */
public enum ReportBase {
    CPUANDMEMORY(1, "性能报表"),
    DISK(2, "磁盘使用率报表"),
    NETWORK(3, "网络性能统计报表"),
    LINK(5, "ZABBIX线路流量报表"),
    ASSETS_COLLECTION(6, "资产统计报表(人行)"),
    RUNTIME_STATUS(7,"运行状态表");

    private Integer id;
    private String name;

    ReportBase(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }


    public String getName() {
        return name;
    }

}
