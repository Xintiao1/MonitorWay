package cn.mw.monitor.visualized.util;

import cn.mw.monitor.visualized.enums.VisualizedDateTypeEnum;
import cn.mwpaas.common.utils.DateUtils;

import java.util.*;

/**
 * @description 获取日期
 * @date 2023/12/25 14:45
 */
public class VisualizedDateTypeUtils {

    public static List<String> getTime(VisualizedDateTypeEnum typeEnum){
        switch (typeEnum){
            case YESTERDAY:
                return getYesterday();
            case THIS_WEEK:
                return getWeek();
            case THIS_MONTH:
                return getMonth();
            case THIS_SEASON:
                return getSeason();
            case THIS_YEAR:
                return getYear();
        }
        return null;
    }


    public static List<String> getYesterday(){
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
        List<String> list=new ArrayList<>();
        list.add(DateUtils.formatDate(dateStart));
        list.add(DateUtils.formatDate(dateEnd));
        return list;
    }

    public static List<String> getWeek(){
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DATE,-week);
        Date dateStart = calendar.getTime();
        calendar.add(Calendar.DATE,8-week);
        Date dateEnd = calendar.getTime();
        List<String> list=new ArrayList<>();
        list.add(DateUtils.formatDate(dateStart));
        list.add(DateUtils.formatDate(dateEnd));
        return list;
    }

    public static List<String> getMonth(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,0);
        int actualMinimum = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int actualMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMinimum,00,00,00);
        Date dataStart = calendar.getTime();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMaximum,23,59,59);
        Date dateEnd = calendar.getTime();
        List<String> list=new ArrayList<>();
        list.add(DateUtils.formatDate(dataStart));
        list.add(DateUtils.formatDate(dateEnd));
        return list;
    }

    public static List<String> getSeason(){
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        // 计算当前季度
        int quarter = (calendar.get(Calendar.MONTH) / 3) + 1;
        // 设置开始时间
        calendar.set(Calendar.MONTH, (quarter - 1) * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateStart = calendar.getTime();
        // 设置结束时间
        calendar.set(Calendar.MONTH, quarter * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);
        Date dateEnd = calendar.getTime();
        List<String> list=new ArrayList<>();
        list.add(DateUtils.formatDate(dateStart));
        list.add(DateUtils.formatDate(dateEnd));
        return list;
    }

    public static List<String> getYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
        Date dateStart= cal.getTime();
        List<String> list=new ArrayList<>();
        list.add(DateUtils.formatDate(dateStart));
        list.add(DateUtils.formatDate(Calendar.getInstance().getTime()));
        return list;
    }
}
