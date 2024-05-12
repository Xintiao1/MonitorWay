package cn.mw.monitor.report.util;

import cn.mwpaas.common.utils.DateUtils;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReportDateUtil {
    //上周精确到 00：00：00 - 23：59：59
    public static List<Date> getLastWeek(){
        LocalDate localDate=LocalDate.now();
        int value = localDate.getDayOfWeek().getValue();
        LocalDate s1 = localDate.minus(value+6, ChronoUnit.DAYS);
        LocalDateTime localDateTime=s1.atTime(0,0,0);
        LocalDate s2 = localDate.minus(value, ChronoUnit.DAYS);
        LocalDateTime localDateTime1 = s2.atTime(23, 59, 59);
        List<Date> list=new ArrayList<>();
        list.add(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        list.add(Date.from(localDateTime1.atZone(ZoneId.systemDefault()).toInstant()));
        return list;
    }
    //上个月精确到 00：00：00 - 23：59：59
    public static List<Date> getLastMonth(){
        SimpleDateFormat stf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.MONTH, -1);
        int lastMonthMaxDay=calendar1.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar1.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), lastMonthMaxDay, 23, 59, 59);
        List<Date> list=new ArrayList<>();
        list.add(calendar.getTime());
        list.add(calendar1.getTime());

        String format = stf.format(calendar.getTime());
        String format1 = stf.format(calendar1.getTime());
        return list;

    }
    //本季度精确到 00：00：00 - 23：59：59
    public static List<Date> getQuarter(){
        LocalDate today = LocalDate.now();
        Month startMonth = today.getMonth();
        Month firstMonthOfQuarter = startMonth.firstMonthOfQuarter();
        Month endMonthOfQuarter = Month.of(firstMonthOfQuarter.getValue() + 2);
        LocalDate ofStart = LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
        LocalDate ofEnd = LocalDate.of(today.getYear(), endMonthOfQuarter, endMonthOfQuarter.length(today.isLeapYear()));
        LocalDateTime localDateTime = ofStart.atTime(0, 0, 0);
        LocalDateTime localDateTime1 = ofEnd.atTime(23, 59, 59);
        List<Date> list=new ArrayList<>();
        list.add(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        list.add(Date.from(localDateTime1.atZone(ZoneId.systemDefault()).toInstant()));
        return list;
    }
    //本年00:00:00 到当前
    public static List<Date> getYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
        Date dateStart= cal.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dateStart);
        list.add(Calendar.getInstance().getTime());
        return list;
    }
    //昨天00；00；00--23；59；59
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

    //本周
    public static List<Date> getWeek(){
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DATE,-week);
        Date dataStart = calendar.getTime();
        calendar.add(Calendar.DATE,8-week);
        Date dateEnd = calendar.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dataStart);
        list.add(dateEnd);
        return list;
    }

    //本月
    public static List<Date> getMonth(){

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,0);
        int actualMinimum = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int actualMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMinimum,00,00,00);
        Date dataStart = calendar.getTime();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMaximum,23,59,59);
        Date dateEnd = calendar.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dataStart);
        list.add(dateEnd);
        return list;
    }

    //近12个月
    public static List<Date> getFront12Month(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH,-12);
        Date dataStart = calendar.getTime();
        Date dateEnd = new Date();
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

    public static Date getTimesNight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @SneakyThrows
    public static Date getDate(String date, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat(type);
        Date parse = sdf.parse(date);
        return parse;
    }
}
