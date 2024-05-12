package cn.mw.monitor.util;

/**
 * @author
 * @date
 */
public enum SqlUpdateEnum {
    ASSETS("assets"),
    LABEL("label");

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    SqlUpdateEnum(String name) {
        this.name = name;
    }
}
