package cn.mw.monitor.ipaddressmanage.model;

/*
 *  已使用：资产表内有/PING通/ARP\MAC地址表内有过
 */

public enum IPState {
    Used(1,"used", "已使用")
    , NotUsed(0, "notUsed", "未使用")
    , Reserved(2,"reserved", "预留");

    private int code;
    private String name;
    private String chName;

    IPState(int code, String name, String chName){
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
