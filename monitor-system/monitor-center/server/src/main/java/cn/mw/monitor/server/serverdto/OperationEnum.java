package cn.mw.monitor.server.serverdto;

/**
 * @author syt
 * @Date 2021/4/14 10:42
 * @Version 1.0
 */
public enum OperationEnum {
    create("create"),
    delete("delete"),
    update("update"),
    select("select");
    private String name;

    OperationEnum(String name) {
        this.name = name;
    }

    OperationEnum() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
