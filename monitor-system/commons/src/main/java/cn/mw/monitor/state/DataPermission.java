package cn.mw.monitor.state;

public enum DataPermission {

    PUBLIC(0,"PUBLIC"),
    PRIVATE(1,"PRIVATE");

    private int code;

    private String name;

    public static DataPermission DEFAULT;

    static {
        DEFAULT = PRIVATE;
    }

    DataPermission(int code, String name) {
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
