package cn.mw.monitor.netflow.enums;

/**
 * @author guiquanwnag
 * @datetime 2023/7/25
 * @Description clickhouse的属性转换
 */
public enum CKFieldType {
    STRING("String", String.class, ""),
    LONG("Int64", Long.class, ""),
    INT("Int32", Integer.class, "");


    private String ckType;

    private Class clazz;

    private String desc;


    CKFieldType(String ckType, Class clazz, String desc) {
        this.ckType = ckType;
        this.clazz = clazz;
        this.desc = desc;
    }

    public static CKFieldType getByType(String ckType) {
        for (CKFieldType ckFieldType : values()) {
            if (ckFieldType.getCkType().equals(ckType)) {
                return ckFieldType;
            }
        }
        return STRING;
    }

    public String getCkType() {
        return ckType;
    }

    public Class getClazz() {
        return clazz;
    }
}
