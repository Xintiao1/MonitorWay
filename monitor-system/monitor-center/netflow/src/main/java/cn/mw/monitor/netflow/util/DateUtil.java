package cn.mw.monitor.netflow.util;

import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.DateUtils;
import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Date;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description 日期工具类
 */
@UtilityClass
public class DateUtil {


    public static long betweenDay(Date startTime, Date endTime, boolean isReset) {
        if (isReset) {
            startTime = beginOfDay(startTime);
            endTime = beginOfDay(endTime);
        }
        return DateUtils.between(startTime, endTime, DateUnitEnum.DAY);
    }


    public static long betweenWeek(Date startTime, Date endTime, boolean isReset) {
        if (isReset) {
            startTime = beginOfDay(startTime);
            endTime = beginOfDay(endTime);
        }
        return DateUtils.between(startTime, endTime, DateUnitEnum.WEEK);
    }

    public static long betweenMonth(Date startTime, Date endTime, boolean isReset) {
        if (isReset) {
            startTime = beginOfDay(startTime);
            endTime = beginOfDay(endTime);
        }
        return DateUtils.between(startTime, endTime, DateUnitEnum.MONTH);
    }

    public static long betweenYear(Date startTime, Date endTime, boolean isReset) {
        if (startTime.after(endTime)) {
            return 0;
        }
        Integer startYear = DateUtils.getYear(startTime);
        Integer endYear = DateUtils.getYear(endTime);
        return endYear - startYear;
    }



    public static String format(Date date, String format) {
        return DateUtils.format(date,format);
    }

    private static Date beginOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
