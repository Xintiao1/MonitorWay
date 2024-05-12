package cn.mw.monitor.netflow.enums;

/**
 * @author gui.quanwang
 * @className ProtocolType
 * @description 协议类型
 * @date 2022/9/6
 */
public enum ProtocolType {
    /**
     * 全部
     */
    ALL(0, "全部", "ALL"),

    /**
     * TCP
     */
    TCP(1, "TCP协议", "TCP"),

    /**
     * UDP
     */
    UDP(2, "UDP协议", "UDP");


    /**
     * 类别ID
     */
    private Integer type;

    /**
     * 备注
     */
    private String desc;

    /**
     * 名称
     */
    private String name;


    ProtocolType(Integer type, String desc, String name) {
        this.type = type;
        this.desc = desc;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ProtocolType getByType(int type) {
        for (ProtocolType protocolType : values()) {
            if (protocolType.getType().equals(type)) {
                return protocolType;
            }
        }
        return null;
    }

    public static ProtocolType getByName(String name) {
        for (ProtocolType protocolType : values()) {
            if (protocolType.getName().equalsIgnoreCase(name)) {
                return protocolType;
            }
        }
        return null;
    }
}
