package cn.mw.monitor.netflow.enums;

/**
 * @author gui.quanwang
 * @className KibanaType
 * @description kibanaType
 * @date 2023/3/15
 */
public enum KibanaType {
    /**
     * 关键字，column
     */
    KEY("key", "关键名字"),

    /**
     * 操作符
     */
    OPERATE("op", "操作类型，例如 > , <"),

    /**
     * 值
     */
    VALUE("value", "值"),

    /**
     * 关联符
     */
    RELATION("rela", "关联关系，and  or ");

    /**
     * 类别
     */
    private String type;

    /**
     * 描述
     */
    private String desc;

    KibanaType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * @return
     */
    public static KibanaType getKibanaType(String type){
        for (KibanaType kibanaType : values()) {
            if (kibanaType.getType().equalsIgnoreCase(type)){
                return kibanaType;
            }
        }
        return null;
    }
}
