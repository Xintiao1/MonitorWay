package cn.mw.monitor.user.state;

public enum PermType {

    browse(1,"browse"),
    create(2,"create"),
    editor(3,"editor"),
    delete(4,"delete"),
    perform(5,"perform"),
    secopassword(6,"secopassword");

    private int code;

    private String name;

    public static PermType DEFAULT;

    static {
        DEFAULT = browse;
    }

    PermType(int code, String name) {
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
