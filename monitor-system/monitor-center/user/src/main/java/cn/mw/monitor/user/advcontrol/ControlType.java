package cn.mw.monitor.user.advcontrol;

public enum ControlType {
    IP(1,"IP"),MAC(2,"MAC"),TIME(3,"TIME");

    private int code;
    private String name;

    ControlType(int code, String name){
        this.code = code;
        this.name = name;
    }
}
