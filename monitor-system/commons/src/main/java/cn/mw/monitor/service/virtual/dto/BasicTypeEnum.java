package cn.mw.monitor.service.virtual.dto;

/**
 * @author syt
 * @Date 2021/4/21 11:13
 * @Version 1.0
 */
public enum BasicTypeEnum {
    STRING("string"),
    SHORT("short"),
    BYTE("byte"),
    CHAR("char"),
    INT("int"),
    INTEGER("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    DATE("date"),
    TIMESTAMP("timestamp")
    ;

    private String typeName;


    BasicTypeEnum(String typeName) {
        this.typeName = typeName;
    }

    public String getItemName() {
        return typeName;
    }

    public void setItemName(String typeName) {
        this.typeName = typeName;
    }
}
