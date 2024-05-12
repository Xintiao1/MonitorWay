package cn.mw.monitor.netflow.enums;

/**
 * @author gui.quanwang
 * @className NetFlowType
 * @description 流量流入类型
 * @date 2022/8/11
 */
public enum NetFlowType {
    /**
     * 入流量监控
     */
    IN(1, "入"),

    /**
     * 出流量监控
     */
    OUT(2, "出"),

    /**
     * 入流量监控+出流量监控
     */
    BOTH(3, "入+出");

    /**
     * 流入类别
     */
    private Integer type;

    /**
     * 简介
     */
    private String desc;

    NetFlowType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    /**
     * 根据值获取类型
     *
     * @param type 值
     * @return
     */
    public static NetFlowType getType(int type) {
        for (NetFlowType value : values()) {
            if (value.getType() == type) {
                return value;
            }
        }
        return IN;
    }
}
