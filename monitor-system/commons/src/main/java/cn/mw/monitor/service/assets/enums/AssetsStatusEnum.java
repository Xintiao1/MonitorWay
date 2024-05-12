package cn.mw.monitor.service.assets.enums;

/**
 * 资产状态枚举
 */
public enum AssetsStatusEnum {

    NORMAL(0,"NORMAL"),
    ABNORMAL(1,"ABNORMAL"),
    SHUTDOWN(2,"SHUTDOWN");

    private int code;

    private String name;

    AssetsStatusEnum(int code, String name) {
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
