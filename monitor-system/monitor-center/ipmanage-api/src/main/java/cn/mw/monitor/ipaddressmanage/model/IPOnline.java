package cn.mw.monitor.ipaddressmanage.model;

public enum IPOnline {
    OFFLINE(0,"offline", "离线")
    ,ONLINE(1,"online", "在线");

    private int code;
    private String name;
    private String chName;

    IPOnline(int code, String name, String chName){
        this.code = code;
        this.name = name;
        this.chName = chName;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getChName() {
        return chName;
    }
}
