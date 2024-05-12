package cn.mw.xiangtai.plugin.enums;

/**
 * @author gengjb
 * @description 祥泰时间对应间隔枚举
 * @date 2023/11/21 11:15
 */
public enum XiangtaiDateTypeEnum {

    YEAR(1,5,"年"),
    MONTH(2,12,"月"),
    DAY(3,30,"日"),
    ;


    private Integer dateType;

    private Integer interval;

    private String desc;

    XiangtaiDateTypeEnum(Integer dateType, Integer interval, String desc) {
        this.dateType = dateType;
        this.interval = interval;
        this.desc = desc;
    }

    public Integer getDateType() {
        return dateType;
    }

    public Integer getInterval() {
        return interval;
    }

    public String getDesc() {
        return desc;
    }

    public static XiangtaiDateTypeEnum getIntervalByType(int dateType) {
        for (XiangtaiDateTypeEnum dateTypeEnum : values()) {
            if (dateType == dateTypeEnum.getDateType()) {
                return dateTypeEnum;
            }
        }
        return null;
    }
}
