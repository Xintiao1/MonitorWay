package cn.mw.monitor.TPServer.model;

public enum TPServerTypeEnum {
    Zabbix3_0(1,"Zabbix3_0")
    , Zabbix4_0(2,"Zabbix4_0")
    , Zabbix5_0(3,"Zabbix5_0")
    , Zabbix6_0(4,"Zabbix6_0")
    ;

    private int code;
    private String type;

    TPServerTypeEnum(int code, String type){
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
