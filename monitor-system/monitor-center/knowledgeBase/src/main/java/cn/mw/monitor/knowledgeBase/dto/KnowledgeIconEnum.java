package cn.mw.monitor.knowledgeBase.dto;

/**
 * @author syt
 * @Date 2020/8/31 14:22
 * @Version 1.0
 */
public enum KnowledgeIconEnum {
    generalContents("总目录","generalContents"),
    server("服务器","server"),
    dataBase("数据库","dataBase"),
    storageDevice("存储设备","storageDevice"),
    networkEquipment("网络设备","networkEquipment"),
    safetyEquipment("安全设备","safetyEquipment"),
    middleware("中间件","middleware"),
    virtual("虚拟化","virtual"),
    line("线路","line"),
    application("应用","application"),
    defaultUrl("默认","defaultUrl");



    private String typeName;
    private String icon;

    public static KnowledgeIconEnum getInfoByTypeName(String typeName) {
        for(KnowledgeIconEnum u : KnowledgeIconEnum.values()) {
            if(u.getTypeName().equals(typeName)) {
                return u;
            }
        }
        return KnowledgeIconEnum.valueOf("defaultUrl");
    }

    KnowledgeIconEnum(String typeName, String icon) {
        this.typeName = typeName;
        this.icon = icon;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
