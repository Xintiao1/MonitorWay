package cn.mw.monitor.user.state;

public enum UserControlState {

    IP(0,"IP"), MAC(1,"MAC"),LOGINTIME(1,"LOGINTIME");

    private int code;
    private String name;

    UserControlState(int code, String name){
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
