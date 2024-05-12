package cn.mw.monitor.ipaddressmanage.param;

/**
 * @author bkc
 * @date 2020/7/25
 */

public enum TypeDESC {
    NOTUSER("0","未使用"),
    USERED("1","以使用"),
    RESERVED("2","预留");

    private String name;
    private String desc;

    TypeDESC(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
