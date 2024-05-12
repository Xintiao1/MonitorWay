package cn.mw.monitor.user.advcontrol;

public enum ActionType {

    Permit(1,"Permit"), NotPermit(2,"NotPermit");

    private int code;
    private String name;

    ActionType(int code, String name){
        this.code = code;
        this.name = name;
    }
}
