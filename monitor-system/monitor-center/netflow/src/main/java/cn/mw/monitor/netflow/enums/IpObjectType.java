package cn.mw.monitor.netflow.enums;

/**
 * @author gui.quanwang
 * @className IpObjectType
 * @description IP类型
 * @date 2022/8/30
 */
public enum IpObjectType {
    /**
     * ip范围
     */
    IP_RANGE(1, "ip范围"),

    /**
     * ip地址段
     */
    IP_PHASE(2, "ip地址段"),

    /**
     * ip地址清单
     */
    IP_LIST(3, "ip地址清单");

    private Integer type;

    private String desc;

    IpObjectType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }


}
