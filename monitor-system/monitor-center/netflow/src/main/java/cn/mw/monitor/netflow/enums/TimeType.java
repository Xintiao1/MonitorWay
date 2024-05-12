package cn.mw.monitor.netflow.enums;

import cn.mwpaas.common.utils.StringUtils;

import java.util.Calendar;

/**
 * @author gui.quanwang
 * @className TimeType
 * @description 时间类型
 * @date 2023/4/6
 */
public enum TimeType {
    /**
     * 秒
     */
    SECOND("s", Calendar.SECOND),
    /**
     * 分钟
     */
    MINUTE("m", Calendar.MINUTE),
    /**
     * 小时
     */
    HOUR("H", Calendar.HOUR_OF_DAY),
    /**
     * 天
     */
    DAY("d", Calendar.DAY_OF_YEAR),
    /**
     * 周
     */
    WEEK("W", Calendar.WEEK_OF_YEAR),
    /**
     * 月
     */
    MONTH("M", Calendar.MONTH),
    /**
     * 年
     */
    YEAR("y", Calendar.YEAR);

    /**
     * 时间类型
     */
    private String name;

    /**
     * 在calendar中的时间属性
     */
    private int calendarTimeType;

    TimeType(String name, int calendarTimeType) {
        this.name = name;
        this.calendarTimeType = calendarTimeType;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据时间值获取时间类型
     *
     * @param value 时间值
     * @return
     */
    public static TimeType getTimeType(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        for (TimeType type : values()) {
            if (value.contains(type.getName())) {
                return type;
            }
        }
        return null;
    }

}
