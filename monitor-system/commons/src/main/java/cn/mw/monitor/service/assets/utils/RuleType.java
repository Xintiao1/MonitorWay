package cn.mw.monitor.service.assets.utils;

public enum RuleType {
    Port(0,"Port", "0", 3, 1)
    , SNMPv1v2(1, "SNMPv1v2","161",2,2)
    , SNMPv3(2, "SNMPv3", "161",2,2)
    , ZabbixAgent(3,"ZabbixAgent", "10050",1,1)
    , ICMP(4,"ICMP", "10050",4,1)
    , IPMI(5,"IPMI", "623",5,3)
    , IOT(6,"IOT", "10050",6,1)
    , VCenter(7,"VCenter", "443",7,1)
    , Middleware(8,"Middleware", "10050",8,1)
    , Application(9,"Application", "10050",9,1)
    , Database(10,"Database", "10050",10,1)
    , Logger(11,"Logger","",11,0)
    , HybridCloud(12,"HybridCloud","10050",12,1)
    , NetWorkDevice(99,"NetWorkDevice","99999",99,99)
    ;

    private int code;
    //监控名称
    private String name;
    //默认监控端口
    private String port;
    //监控方式
    private int monitorMode;
    //对应zabbix接口类型
    private int interfaceType;
    public static final String SNMP = "SNMP";

    RuleType(int code, String name, String port, int monitorMode, int interfaceType){
        this.code = code;
        this.name = name;
        this.port = port;
        this.monitorMode = monitorMode;
        this.interfaceType = interfaceType;
    }

    public String getPort(){
        return this.port;
    }

    public String getName() { return this.name; }

    public int geInterfaceType() { return this.interfaceType; }

    public int getMonitorMode(){ return this.monitorMode; }

    public static RuleType getInfoByMonitorMode(int monitorMode) {
        for(RuleType r : RuleType.values()) {
            if(r.getMonitorMode() == monitorMode) {
                return r;
            }
        }
        return null;
    }

    public static RuleType getInfoByName(String name) {
        for(RuleType r : RuleType.values()) {
            if(r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }
}
