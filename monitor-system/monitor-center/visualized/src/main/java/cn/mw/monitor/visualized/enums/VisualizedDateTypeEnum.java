package cn.mw.monitor.visualized.enums;

/**
 * @author gengjb
 * @description 日期枚举
 * @date 2023/12/25 11:18
 */
public enum VisualizedDateTypeEnum {

    YESTERDAY(1,"昨天"),
    THIS_WEEK(2,"本周"),
    THIS_MONTH(3,"本月"),
    THIS_SEASON(4,"本季"),
    THIS_YEAR(5,"本年")
    ;

    private Integer dateType;

    private String desc;

    VisualizedDateTypeEnum(Integer dateType, String desc) {
        this.dateType = dateType;
        this.desc = desc;
    }

    public Integer getDateType() {
        return dateType;
    }

    public String getDesc() {
        return desc;
    }

    public static VisualizedDateTypeEnum getDateTypeEnumByType(Integer dateType) {
        for (VisualizedDateTypeEnum dateTypeEnum : values()) {
            if (dateType == dateTypeEnum.getDateType()) {
                return dateTypeEnum;
            }
        }
        return null;
    }
}
