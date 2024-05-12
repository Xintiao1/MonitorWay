package cn.mw.monitor.user.state;

public enum LoginState {

    ONLINE(0,"ONLINE"),
    OFFLINE(1,"OFFLINE");

    private int code;

    private String name;

    public static LoginState DEFAULT;

    static {
        DEFAULT = OFFLINE;
    }

    LoginState(int code, String name){
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
