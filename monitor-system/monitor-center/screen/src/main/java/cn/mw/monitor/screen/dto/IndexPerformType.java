package cn.mw.monitor.screen.dto;

/**
 * 首页模块化操作类型
 */
public enum IndexPerformType {

    MOVE(0, "移动"),
    ADD(1, "添加"),
    DELETE(2, "删除");
    private int code;
    private String name;

    IndexPerformType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static IndexPerformType getByValue(int value) {
        for (IndexPerformType code : IndexPerformType.values()) {
            if (code.getCode() == value) {
                return code;
            }
        }
        return null;
    }
}
