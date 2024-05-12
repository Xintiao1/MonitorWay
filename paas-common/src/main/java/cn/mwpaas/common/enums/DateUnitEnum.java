package cn.mwpaas.common.enums;

/**
 * 日期时间单位，每个单位都是以毫秒为基数
 *
 * @author Looly
 */
public enum DateUnitEnum {
    /**
     * 一毫秒
     */
    MS(1),
    /**
     * 一秒的毫秒数
     */
    SECOND(1000),
    /**
     * 一分钟的毫秒数
     */
    MINUTE(SECOND.getMillis() * 60),
    /**
     * 一小时的毫秒数
     */
    HOUR(MINUTE.getMillis() * 60),
    /**
     * 一天的毫秒数
     */
    DAY(HOUR.getMillis() * 24),
    /**
     * 一周的毫秒数
     */
    WEEK(DAY.getMillis() * 7),
    /**
     * 一月的毫秒数
     */
    MONTH(DAY.getMillis() * 30),

    /**
     * 一年的毫秒数
     */
    YEAR(DAY.getMillis() * 365);

    private long millis;

    DateUnitEnum(long millis) {
        this.millis = millis;
    }

    /**
     * @return 单位对应的毫秒数
     */
    public long getMillis() {
        return this.millis;
    }

    /**
     * @param time
     * @param unit
     * @return
     */
    public static long getMillis(int time, DateUnitEnum unit) {
        return unit.getMillis() * time;
    }

    /**
     * @param time
     * @param unit
     * @return
     */
    public static int getSeconds(int time, DateUnitEnum unit) {
        if (unit == MS) {
            return 0;
        }
        return (int) (unit.getMillis() * time / 1000);
    }
}
