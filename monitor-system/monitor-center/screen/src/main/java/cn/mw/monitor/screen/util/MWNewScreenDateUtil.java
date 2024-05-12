package cn.mw.monitor.screen.util;

import cn.mwpaas.common.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName MWNewScreenDateUtil
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/29 14:33
 * @Version 1.0
 **/
public class MWNewScreenDateUtil {

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

    public static Date getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    //本周
    public static List<Date> getWeek(){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        Date dataStart = calendar.getTime();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
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
        return list;

    }
}
