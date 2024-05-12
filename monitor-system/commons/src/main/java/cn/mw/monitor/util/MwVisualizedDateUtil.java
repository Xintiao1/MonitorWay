package cn.mw.monitor.util;

import cn.mwpaas.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MwVisualizedDateUtil
 * @Author gengjb
 * @Date 2022/4/24 14:18
 * @Version 1.0
 **/
@Slf4j
public class MwVisualizedDateUtil {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据类型与时间类型获取开始时间与结束时间
     * @param type
     * @param dateType
     * @return
     */
    public static List<Date> getDates(Integer type,Integer dateType){
        if(type == null || dateType == null) return new ArrayList<>();
        try {
            switch (type){
                case 1://上周日期
                    return getLastWeek(dateType);
                case 2://昨天 前天  大前天
                    return getDay(dateType);
                case 3://以前到现在时间
                    return getBeforeUpToNow(dateType);
                case 4://最近天、月、年数据
                    return getLatelyTime(dateType);
                case 5://最近分钟，小时数据
                    return getLatelyMinuteAndHour(dateType);
            }
        }catch (Throwable e){
            log.error("日期转换失败，失败信息:"+e);
        }
        return new ArrayList<>();
    }

    /**
     * 根据时间类型判断取上周几数据
     * @param dateType
     */
    public static List<Date> getLastWeek(Integer dateType) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday());
        cal.add(Calendar.DATE, dateType*-1);
        Date time = cal.getTime();
        String start = format.format(time)+" 00:00:00";
        String end = format.format(time)+" 23:59:59";
        List<Date> dates = new ArrayList<>();
        dates.add(format.parse(start));
        dates.add(format.parse(end));
        return dates;
    }

    public static Date getThisWeekMonday() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    /**
     * 获取昨天、前天、大前天数据
     * @param dateType
     */
    private static List<Date> getDay(Integer dateType) throws ParseException {
        Date today = new Date();
        //获取三十天前日期
        Calendar calendar = Calendar. getInstance ();
        calendar .setTime(today);
        calendar .add(calendar.DATE , dateType*-1);
        Date time = calendar.getTime();
        String start = format.format(time)+" 00:00:00";
        String end = format.format(time)+" 23:59:59";
        List<Date> dates = new ArrayList<>();
        dates.add(format.parse(start));
        dates.add(format.parse(end));
        return dates;
    }


    /**
     * 获取今天到现在、周一到现在、月初到现在、今年到现在的时间
     * @param dateType
     * @return
     * @throws ParseException
     */
    private static List<Date> getBeforeUpToNow(Integer dateType) throws ParseException {
        Date today = new Date();
        List<Date> dates = new ArrayList<>();
        String startTime = "";
        Calendar cal=Calendar.getInstance();
        switch (dateType){
            case 1:
                startTime = format.format(new Date()) + " 00:00:00";
                break;
            case 2:
                cal.add(Calendar.WEEK_OF_MONTH, 0);
                cal.set(Calendar.DAY_OF_WEEK, 2);
                startTime = format.format(cal.getTime()) + " 00:00:00";
                break;
            case 3:
                cal.add(Calendar.MONTH, 0);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startTime = format.format(cal.getTime()) + " 00:00:00";
                break;
            case 4:
                startTime = new SimpleDateFormat("yyyy").format(new Date())+"-01-01 00:00:00";
        }
        dates.add(format.parse(startTime));
        dates.add(today);
        return dates;
    }


    /**
     * 获取最近一天、最近七天、最近30天，最近90天、最近6个月、最近一年的时间
     * @param dateType
     * @return
     * @throws ParseException
     */
    private static List<Date> getLatelyTime(Integer dateType) throws ParseException {
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = new Date();
        List<Date> dates = new ArrayList<>();
        String startTime = "";
        Calendar cal=Calendar.getInstance();
        if(dateType == 1 || dateType == 7 || dateType == 30 || dateType == 90){
            cal.setTime(new Date());
            cal.add(Calendar.DATE, - dateType);
            startTime = format2.format(cal.getTime());
        }
        if(dateType == 2){
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, - 6);
            startTime = format2.format(cal.getTime());
        }
        if(dateType == 3){
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, - 1);
            startTime = format2.format(cal.getTime());
        }
        dates.add(format2.parse(startTime));
        dates.add(today);
        return dates;
    }

    /**
     * 获取最近小时与分钟的时间
     * @param dateType
     * @return
     * @throws ParseException
     */
    private static List<Date> getLatelyMinuteAndHour(Integer dateType) throws ParseException {
        Date today = new Date();
        long time = today.getTime();
        long l = time - (dateType * 60 * 60 * 1000);
        Date startTime = new Date();
        startTime.setTime(l);
        List<Date> dates = new ArrayList<>();
        dates.add(startTime);
        dates.add(today);
        return dates;
    }

    /**
     * 获取过去几天内的日期数组
     * @param intervals      intervals天内
     * @return              日期数组
     */
    public static List<String> getDays(int intervals){
        List<String> pastDaysList = new ArrayList<>();
        for (int i = intervals -1; i >= 0; i--) {
            pastDaysList.add(getPastDate(i));
        }
        return pastDaysList;
    }

    /**
     * 获取过去第几天的日期
     * @param past
     * @return
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today); return result;
    }

    //今天00：00：00-到当前
    public static List<Date> getToday(){
        Date dataStart = getTimesMorning();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateEnd = Calendar.getInstance().getTime();
        List<Date> list=new ArrayList<>();
        list.add(dataStart);
        list.add(dateEnd);
        return list;
    }

    public static Date getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static List<Date> getYesterday(){
        Date dateStart;
        Date dateEnd;
        Calendar cal2=new GregorianCalendar();
        cal2.setTime(DateUtils.getTimesMorning());
        cal2.add(Calendar.DAY_OF_MONTH,-1);
        dateStart=cal2.getTime();
        Calendar cal3=new GregorianCalendar();
        cal3.setTime(DateUtils.getTimesNight());
        cal3.add(Calendar.DAY_OF_MONTH,-1);
        dateEnd=cal3.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dateStart);
        list.add(dateEnd);
        return list;
    }
}
