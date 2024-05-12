package cn.mw.monitor.state;

/**
 * @author xhy
 * @date 2020/5/21 10:12
 */

public enum DataType {
    SCREEN("SCREEN", "大屏"),
    ASSETS("ASSETS", "资产",1),
    INASSETS("INASSETS","无形资产",4),
    OUTBANDASSETS("OUTBANDASSETS","带外资产",5),
    REPORT("REPORT", "报表"),
    TOPO_GRAPH("TOPO_GRAPH", "拓扑图"),
    TOPO_GROUP("TOPO_GROUP", "拓扑分组"),
    LINK("LINK", "链路"),
    IP("IP", "IP地址管理"),
    IPV6("IPV6", "IPV6地址管理"),
    IPHIS("IPHIS", "IP历史记录基础信息"),
    ACCOUNT("ACCOUNT", "账号管理"),
    TEMPLATE("TEMPLATE", "模板管理"),
    INDEX("INDEX", "首页"),
    ENGINE("ENGINE", "引擎"),
    ACTION("ACTION", "告警通知"),
    MONITORING_SERVER("MONITORING_SERVER", "监控服务器"),
    RULE("RULE", "通知规则"),
    VIRTUAL("VIRTUAL","虚拟化"),
    WEB_MONITOR("WEB_MONITOR","WEB监测"),
    PROCESS("PROCESS","流程管理"),
    MODEL_MANAGE("MODEL_MANAGE","模型管理"),
    MODEL_VIRTUAL("MODEL_VIRTUAL","模型虚拟化"),
    INSTANCE_MANAGE("INSTANCE_MANAGE","实例管理"),
	CREDENTIAL("CREDENTIAL","系统凭据"),
    AUTO_MANAGE("AUTO_MANAGE","自动化运维"),
    ACCOUNT_MANAGE("ACCOUNT_MANAGE","账户管理"),
    DETECT_RULE_MANAGE("DETECT_RULE_MANAGE","合规检测----规则管理"),
    DETECT_POLICY_MANAGE("DETECT_POLICY_MANAGE","合规检测----策略管理"),
    DETECT_REPORT_MANAGE("DETECT_REPORT_MANAGE","合规检测----报告 管理")
    ;

    private String name;
    private String desc;
    private Integer moduleId;

    DataType(String name, String desc, Integer moduleId) {
        this.name = name;
        this.desc = desc;
        this.moduleId = moduleId;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    DataType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 根据名称获取类别数据
     *
     * @param name 名称
     * @return
     */
    public static DataType getDataTypeByName(String name) {
        for (DataType dataType : values()) {
            if (dataType.getName().equalsIgnoreCase(name)) {
                return dataType;
            }
        }
        return null;
    }
}
