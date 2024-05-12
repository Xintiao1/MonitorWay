package cn.mwpaas.common.utils;

import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.enums.DateUnitEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.mwpaas.common.enums.DateUnitEnum.YEAR;

/**
 * @author phzhou
 * @ClassName DateUtils
 * @CreateDate 2019/2/22
 * @Description
 */
public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
    private static String[] DAY_OF_WEEK = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    /**
     * @return 当前时间
     */
    public static Date date() {
        return new Date();
    }

    /**
     * 当前时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return formatDateTime(new Date());
    }

    /**
     * 当前时间，格式 yyyy-MM-dd
     *
     * @return 当前时间的日期形式字符串
     */
    public static String nowDate() {
        return formatDate(new Date());
    }

    /**
     * 格式化日期时间<br>
     * 格式 yyyy年MM月dd日
     *
     * @param date 被格式化的日期
     * @return 格式化后的日期
     */
    public static String formatChineseDate(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.CHINESE_DATE);
        return formatter.format(date);
    }


    /**
     * 格式化日期时间<br>
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的日期
     */
    public static String formatDateTime(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_DATETIME);
        return formatter.format(date);
    }

    /**
     * 格式化日期部分（不包括时间）<br>
     * 格式 yyyy-MM-dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_DATE);
        return formatter.format(date);
    }

    /**
     * 格式化日期部分（不包括时间）<br>
     * 格式 yyyy.MM.dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDatePoint(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_DATE_POINT);
        return formatter.format(date);
    }

    /**
     * 格式化时间<br>
     * 格式 HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     * @since 3.0.1
     */
    public static String formatTime(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_TIME);
        return formatter.format(date);
    }

    /**
     * 格式化时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * timestamp转String
     *
     * @param timestamp
     * @return
     */
    public static String formatTimestamp(Timestamp timestamp) {
        if (null == timestamp) {
            return null;
        }
        return format(timestamp, DateConstant.NORM_DATETIME);
    }

    /**
     * timestamp转String
     *
     * @param timestamp
     * @param format
     * @return
     */
    public static String format(Timestamp timestamp, String format) {
        if (null == timestamp) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(timestamp);
    }

    /**
     * 获取当前系统年份
     */
    public static Integer getSysYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取日期年份
     *
     * @param date
     * @return
     */
    public static Integer getYear(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取当前系统月份
     */
    public static int getSysMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日期月份
     *
     * @param date
     * @return
     */
    public static Integer getMonth(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }


    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static Date parse(String dateStr, String format) {
        if (null == dateStr) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            logger.error(e.toString());
        }
        return null;
    }

    /**
     * 将字符串转化为Date(可以识别yyyy-MM-dd/yyyy-MM-dd HH:mm:ss/yyyy-MM-dd'T'HH:mm:ss'Z'/yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     *
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date parse(String str) {
        if (null == str) {
            return null;
        }
        String pattern;
        if (str.length() == 10) {
            pattern = DateConstant.NORM_DATE;
        }else if(str.length() == 16){
            pattern = DateConstant. NORM_DATETIME_MINUTE;
        } else if (str.length() == 7) {
            pattern = DateConstant.NORM_YEAR_MONTH;
        } else {
            if (str.contains("T")) {
                pattern = DateConstant.UTC;
                if (str.contains(".")) {
                    pattern = DateConstant.UTC2;
                }
            } else {
                pattern = DateConstant.NORM_DATETIME;
            }
        }
        return parse(str, pattern);
    }


    public static Date parseByTime(String str) {
        String pattern = DateConstant.UTC2;
        logger.info("进入新的时间转换函数：pattern" + pattern + "；str：" + str);
        if (null == str) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return formatter.parse(str);
        } catch (ParseException e) {
            logger.error(e.toString());
        }
        return null;
    }

    /**
     * String 转timestamp
     *
     * @param str
     * @return
     */
    public static Timestamp parseTimestamp(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        Date date = parse(str);
        return Timestamp.valueOf(formatDateTime(date));
    }

    /**
     * 判断两个日期相差的时长
     *
     * @param beginDate    开始时间
     * @param endDate      结束时间
     * @param dateUnitEnum 相差的单位
     * @return
     */
    public static long between(Date beginDate, Date endDate, DateUnitEnum dateUnitEnum) {
        if (null == beginDate || null == endDate) {
            return 0L;
        }
        long diff = endDate.getTime() - beginDate.getTime();
        return diff / dateUnitEnum.getMillis();
    }

    /**
     * 判断两个日期相差的时长
     *
     * @param beginDate    开始时间
     * @param endDate      结束时间
     * @param dateUnitEnum 相差的单位
     * @return
     */
    public static long between(String beginDate, String endDate, DateUnitEnum dateUnitEnum) {
        if (null == beginDate || null == endDate) {
            return 0L;
        }
        return between(parse(beginDate), parse(endDate), dateUnitEnum);
    }

    /**
     * 计算月份差
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int betweenMonth(String beginDate, String endDate) {
        if (null == beginDate || null == endDate) {
            return 0;
        }
        return betweenMonth(parse(beginDate), parse(endDate));
    }

    /**
     * 计算月份差
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int betweenMonth(Date beginDate, Date endDate) {
        if (null == beginDate || null == endDate) {
            return 0;
        }
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(beginDate);
        c2.setTime(endDate);
        int result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        return result == 0 ? 1 : Math.abs(result);
    }

    /**
     * 增加年
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addYears(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.YEAR, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * 增加月份
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMonths(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 增加月份
     *
     * @param date
     * @param amount
     * @return
     */
    public static String addMonths(final String date, final int amount) {
        if (null == date) {
            return null;
        }
        return formatDate(add(parse(date), Calendar.MONTH, amount));
    }

    /**
     * 增加周
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addWeeks(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    /**
     * 增加天
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addDays(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 日期加减天数返回计算后的日期 yyyy-MM-dd
     *
     * @param date
     * @param addDay
     * @return
     */
    public static String addDays(String date, int addDay) {
        if (null == date) {
            return null;
        }
        Date newDate = addDays(parse(date), addDay);
        return formatDate(newDate);
    }


    /**
     * 增加小时
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addHours(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 增加小时
     *
     * @param date
     * @param amount
     * @return
     */
    public static String addHours(final String date, final int amount) {
        if (null == date) {
            return null;
        }
        Date dateNew = add(parse(date), Calendar.HOUR_OF_DAY, amount);
        return formatDateTime(dateNew);
    }

    /**
     * 增加分
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMinutes(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.MINUTE, amount);
    }

    /**
     * 增加分
     *
     * @param date
     * @param amount
     * @return
     */
    public static String addMinutes(final String date, final int amount) {
        if (null == date) {
            return null;
        }
        Date dateNew = add(parse(date), Calendar.MINUTE, amount);
        return formatDateTime(dateNew);
    }

    /**
     * 增加秒
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addSeconds(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.SECOND, amount);
    }

    /**
     * 增加秒
     *
     * @param date
     * @param amount
     * @return
     */
    public static String addSeconds(final String date, final int amount) {
        if (null == date) {
            return null;
        }
        return formatDateTime(add(parse(date), Calendar.SECOND, amount));
    }

    /**
     * 增加毫秒
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMilliseconds(final Date date, final int amount) {
        if (null == date) {
            return null;
        }
        return add(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 日期增加
     *
     * @param date
     * @param calendarField
     * @param amount
     * @return
     */
    private static Date add(final Date date, final int calendarField, final int amount) {
        if (null == date) {
            return null;
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }


    /**
     * 判断2个date类型时间是否相等（年月日）
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * 判断2个Calendar类型时间是否相等（年月日）
     *
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 根据生日获取年龄 支持yyyy-MM-dd/yyyy-MM-dd HH:mm:ss/yyyy-MM-dd'T'HH:mm:ss'Z'
     *
     * @param birthday
     * @return
     */
    public static int getAgeByBirth(String birthday) {
        if (null == birthday) {
            return 0;
        }
        return getAgeByBirth(parse(birthday));
    }

    /**
     * 根据生日获取年龄
     *
     * @param birthday
     * @return
     */
    public static int getAgeByBirth(Date birthday) {
        if (null == birthday) {
            return 0;
        }
        int age;
        try {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());// 当前时间

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthday);

            if (birth.after(now)) {//如果传入的时间，在当前时间的后面，返回0岁
                age = 0;
            } else {
                age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
                    age += 1;
                }
            }
            return age;
        } catch (Exception e) {//兼容性更强,异常后返回数据
            return 0;
        }
    }

    /**
     * 获取某个日期是星期几
     *
     * @param calendar
     * @return
     */
    public static String getDayOfWeek(Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return DAY_OF_WEEK[day - 1];
    }

    /**
     * 获取某个日期是星期几
     *
     * @param date
     * @return
     */
    public static String getDayOfWeek(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getDayOfWeek(calendar);
    }

    /**
     * 判断是否周末
     *
     * @param calendar
     * @return
     */
    public static boolean isWeekend(Calendar calendar) {
        if (null == calendar) {
            return false;
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 补上时分秒 00:00:00
     *
     * @param time
     * @return
     */
    public static String addBeginTime(String time) {
        if (null == time) {
            return null;
        }
        Date data = parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR, 0);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 补上时分秒 23:59:59
     *
     * @param time
     * @return
     */
    public static String addEndTime(String time) {
        if (null == time) {
            return null;
        }
        Date data = parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 获取两个日期之间所有的日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static List<String> daysBetween(String date1, String date2) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(date1) || StringUtils.isBlank(date2)) {
            return list;
        }
        String tmp;
        if (date1.compareTo(date2) > 0) { // 确保 date1的日期不晚于date2
            tmp = date1;
            date1 = date2;
            date2 = tmp;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DateConstant.NORM_DATE);
        tmp = sdf.format(parse(date1).getTime());
        while (tmp.compareTo(date2) <= 0) {
            list.add(tmp);
            tmp = sdf.format(parse(tmp).getTime() + 3600 * 24 * 1000);
        }
        return list;
    }

    /**
     * 获得当天0点时间
     *
     * @return
     */
    public static Date getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得当天23:59:59时间
     *
     * @return
     */
    public static Date getTimesNight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得本周一0点时间
     *
     * @return
     */
    public static Date getTimesWeekMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    /**
     * 获得本周日23:59:59时间
     *
     * @return
     */
    public static Date getTimesWeekNight() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTimesWeekMorning());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        return cal.getTime();
    }

    /**
     * 获得本月第一天0点时间
     *
     * @return
     */
    public static Date getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
     * 获得本月最后一天23:59:59时间
     *
     * @return
     */
    public static Date getTimesMonthNight() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        return cal.getTime();
    }

    /**
     * 获取某月的最后一刻时间
     *
     * @param year
     * @param month
     * @return
     */
    public static String getTimesMonthNight(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        DateFormat format = new SimpleDateFormat(DateConstant.NORM_DATETIME);
        return format.format(calendar.getTime());
    }

    /**
     * 获得本年初0点时间
     *
     * @return
     */
    public static Date getTimesYearMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
        return cal.getTime();
    }

    /**
     * 获得本年末23:59:59时间
     *
     * @return
     */
    public static Date getTimesYearNight() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        return cal.getTime();
    }

    /**
     * 获取距离某时间多少月之前的最早一刻时间
     *
     * @param date
     * @param months
     * @return
     */
    public static String getMonthsBeforeTimeMorning(Date date, int months) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DateConstant.NORM_DATETIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -(months - 1));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return format.format(calendar.getTime());
    }

    /**
     * 两个时间相差距离 多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为： xx小时xx分xx秒
     */
    public static String getDateDiff(String str1, String str2) {
        if (null == str1 || null == str2) {
            return null;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            long time1 = df.parse(str1).getTime();
            long time2 = df.parse(str2).getTime();
            long diff;
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            hour = (diff / (60 * 60 * 1000));
            min = ((diff / (60 * 1000)) - hour * 60);
            sec = (diff / 1000 - hour * 60 * 60 - min * 60);
        } catch (ParseException e) {
            logger.error(e.toString());
        }
        return hour + "小时" + min + "分" + sec + "秒";
    }

    /**
     * 获取某年某月 周详情，跨月周：周四在哪个月本周就属于哪个月
     *
     * @param year
     * @param month
     * @return
     */
    public static List<Map<String, Object>> getMonthWeek(Integer year, Integer month) {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.YEAR, year);
        ca.set(Calendar.MONTH, month - 1);
        ca.setFirstDayOfWeek(Calendar.MONDAY);

        int days = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
        int count = 0;

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        for (int i = 1; i <= days; i++) {
            ca.set(Calendar.DAY_OF_MONTH, i);
            int k = ca.get(Calendar.DAY_OF_WEEK);
            //说明周一在上个月
            if (k == 1 && i >= 4 && i < 7) {
                map = new HashMap<>(10);
                count++;
                Calendar begin = Calendar.getInstance();
                begin.setTime(ca.getTime());
                begin.add(Calendar.DAY_OF_MONTH, -6);
                map.put("week", count);
                map.put("beginDate", formatDate(begin.getTime()));
                map.put("endDate", formatDate(ca.getTime()));
                map.put("monday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("tuesday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("wednesday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("thursday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("friday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("saturday", formatDate(begin.getTime()));
                begin.add(Calendar.DAY_OF_MONTH, 1);
                map.put("sunday", formatDate(begin.getTime()));
                list.add(map);
            }
            // 若当天是周一
            if (k == 2) {
                map = new HashMap<>(10);
                count++;
                Calendar compare = Calendar.getInstance();
                compare.setTime(ca.getTime());
                compare.add(Calendar.DAY_OF_MONTH, 3);
                if (compare.get(Calendar.MONTH) > (month - 1)) {
                    continue;
                }
                Calendar end = Calendar.getInstance();
                end.setTime(ca.getTime());
                end.add(Calendar.DAY_OF_MONTH, 6);

                map.put("week", count);
                map.put("beginDate", formatDate(ca.getTime()));
                map.put("endDate", formatDate(end.getTime()));
                map.put("monday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("tuesday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("wednesday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("thursday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("friday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("saturday", formatDate(ca.getTime()));
                ca.add(Calendar.DAY_OF_MONTH, 1);
                map.put("sunday", formatDate(ca.getTime()));
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 获取某年某月某日 所在周详情，包含第几月，第几周
     *
     * @param year
     * @param month
     * @return
     */
    public static List<Map<String, Object>> getMonthWeekByDay(Integer year, Integer month, Integer day) {
        List<Map<String, Object>> monthWeekList;
        List<Map<String, Object>> currentMonthWeekList = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        Calendar cache = Calendar.getInstance();
        cache.setTime(c.getTime());

        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(c.getTime());


        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6);
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(c.getTime());

        Calendar thursday = Calendar.getInstance();
        thursday.setTime(cache.getTime());
        thursday.setFirstDayOfWeek(Calendar.MONDAY);
        thursday.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 3);

        monthWeekList = getMonthWeek(thursday.get(Calendar.YEAR), thursday.get(Calendar.MONTH) + 1);
        String currentDay = year + "-" + (month < 10 ? ("0" + month) : month) + "-" + (day < 10 ? ("0" + day) : day);
        for (Map<String, Object> weekMap : monthWeekList) {
            if (currentDay.equals(weekMap.get("monday")) || currentDay.equals(weekMap.get("tuesday"))
                    || currentDay.equals(weekMap.get("thursday")) || currentDay.equals(weekMap.get("wednesday"))
                    || currentDay.equals(weekMap.get("friday")) || currentDay.equals(weekMap.get("saturday"))
                    || currentDay.equals(weekMap.get("sunday"))) {

                weekMap.put("month", thursday.get(Calendar.MONTH) + 1);
                weekMap.put("year", thursday.get(Calendar.YEAR));
                currentMonthWeekList.add(weekMap);
            }
        }
        return currentMonthWeekList;
    }

    /**
     * 功能描述：返回小时
     *
     * @param date 日期
     * @return 返回小时
     */
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 功能描述：返回分
     *
     * @param date 日期
     * @return 返回分钟
     */
    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 返回秒钟
     *
     * @param date Date 日期
     * @return 返回秒钟
     */
    public static int getSecond(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.SECOND);
    }

    /**
     * 获取指定时间是周几的数字
     * @param calendar
     * @param date
     * @return
     */
    public static int getDayOfWeek(Calendar calendar, Date date){
        calendar.setTime(date);
        int weekIdx = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if(weekIdx == 0){
            weekIdx = 7;
        }
        return weekIdx;
    }


    /**
     * 获取指定时间是第几周数字
     * @param calendar
     * @param date
     * @return
     */
    public static int getWeekOfMonth(Calendar calendar, Date date){
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setMinimalDaysInFirstWeek(4);
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * 获取指定时间是月中第几天数字
     * @param calendar
     * @param date
     * @return
     */
    public static int getDayOfMonth(Calendar calendar, Date date){
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String getFormatDate(Long time){
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat formatter = new SimpleDateFormat(DateConstant.NORM_DATETIME_MS);
        return formatter.format(date);
    }

}
