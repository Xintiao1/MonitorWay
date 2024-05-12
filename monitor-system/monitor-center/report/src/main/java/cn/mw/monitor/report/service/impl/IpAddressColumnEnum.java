package cn.mw.monitor.report.service.impl;

/**
 * IP地址报表列名枚举
 */
public enum IpAddressColumnEnum {
    TITLE_CENSUS("地址段使用率统计"),
    TITLE_UTILIZATION_TOPN("网段IP利用率TOPN"),
    TITLE_OPERATECLASSOFT_CENSUS("IP管理操作分类统计"),
    TITLE_UPDATE_TOPN("分配/变更次数TOPN"),
    TITLE_UPDATE("分配变更次数"),
    NAME("名称"),
    IP_SEGMENT_AMOUNT("地址段总数"),
    UTILIZATION_LTEQUALTOFIFTY("≤50%使用率"),
    UTILIZATION_FIFTYTOEIGHTY("50%~80%使用率"),
    UTILIZATION_GTEQUALTOEIGHTY("≥80%使用率"),
    DISTRIBUTION("分配"),
    UPDATE("变更"),
    RETRIEVE("回收"),
    ;

    private String name;

    IpAddressColumnEnum( String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
