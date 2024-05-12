package cn.mw.monitor.model.type;

/**
 * @author guiquanwnag
 * @datetime 2023/7/2
 * @Description ARP状态
 */
public enum ARPType {
    /**
     * none of the following
     */
    OTHER(1, "other", "其他"),
    /**
     * an invalidated mapping
     */
    INVALID(2, "invalid", "无效"),
    DYNAMIC(3, "dynamic", "动态"),
    STATIC(4, "static", "静态");

    private int type;

    private String name;

    private String cnName;

    ARPType(int type, String name, String cnName) {
        this.type = type;
        this.name = name;
        this.cnName = cnName;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ARPType getByType(int type) {
        for (ARPType arpType : values()) {
            if (arpType.getType() == type) {
                return arpType;
            }
        }
        return OTHER;
    }

    public static String getNameByType(int type) {
        ARPType arpType = getByType(type);
        if (arpType == null) {
            return String.valueOf(type);
        } else {
            return arpType.getName();
        }
    }
}
