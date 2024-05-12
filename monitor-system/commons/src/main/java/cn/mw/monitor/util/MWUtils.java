package cn.mw.monitor.util;

import cn.mwpaas.common.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jiangwenjiang on 2019/6/29.
 */
@Slf4j
public class MWUtils {
    public static final String ORG_NAME_TOP = "系统管理";
    public static final String ROLE_TOP_ID = "0";//超级管理员角色id
    public static final String MODEL_ALARM_COUNT_TYPE = "分类统计告警";
    public static final String REDIS_SECURITY_TYPE = "security:type";
    public static Map<String, String> STATE = new HashMap<>();
    public static Map<String, String> SEVERITY = new HashMap<>();
    public static Map<String, Integer> RANK = new HashMap<>();
    public static Map<String, String> LINK = new HashMap<>();
    public static Map<String, Integer> ASSETS_TYPE = new HashMap<>();
    public static final Map<Integer, String> TIMETYPEVALUE = new HashMap<>();


    static {
        STATE.put("1", "AGENT");
        STATE.put("2", "SNMP");
        STATE.put("3", "IPMI");
        STATE.put("4", "JMX");

        SEVERITY.put("0", "未分类");
        SEVERITY.put("1", "一般");
        SEVERITY.put("2", "信息");
        SEVERITY.put("3", "警告");
        SEVERITY.put("4", "严重");
        SEVERITY.put("5", "紧急");

        RANK.put("CPU_UTILIZATION", 7);
        RANK.put("MEMORY_UTILIZATION", 8);
        RANK.put("DISK_UTILIZATION", 9);
        RANK.put("ICMP_LOSS", 10);
        RANK.put("INTERFACE_OUT_TRAFFIC", 15);
        RANK.put("INTERFACE_IN_TRAFFIC", 16);
        RANK.put("ICMP_RESPONSE_TIME", 17);

//        RANK.put("1", "CPU_UTILIZATION");
//        RANK.put("2", "MEMORY_UTILIZATION");
//        RANK.put("3", "DISK_UTILIZATION");
//        RANK.put("4", "ICMP_RESPONSE_TIME");

        LINK.put("1", "ICMP_PING");
        LINK.put("2", "ICPM_LOSS");
        LINK.put("3", "ICMP_RESPONSE_TIME");


        ASSETS_TYPE.put("服务器", 1);
        ASSETS_TYPE.put("网络设备", 2);
        ASSETS_TYPE.put("应用", 6);
        ASSETS_TYPE.put("中间件", 7);

        TIMETYPEVALUE.put(1, "15mins:");
        TIMETYPEVALUE.put(2, "60mins:");
    }

    public static String getDate(Long times) {
        Calendar c = Calendar.getInstance();
        long millions = times * 1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    public static Date getLongToDate(Long times) {
        Date date = new Date(times*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(date);
        Date newDate = strToDateLong(format);
        return newDate;
    }

    public static String getDate(Date date, String formatType) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatType);
        String dateString = sdf.format(date);
        return dateString;
    }


    @SneakyThrows
    public static Long getDate(String date, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat(type);
        Date parse = sdf.parse(date);
        long time = parse.getTime() / 1000L;
        return time;
    }

    public static String getDates(Long times) {
        Calendar c = Calendar.getInstance();
        long millions = times * 1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    /**
     * list去重
     *
     * @param list
     * @return
     */

    public static List removeDuplicate(List list) {
        List listTemp = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            if (!listTemp.contains(list.get(i))) {
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    /**
     * 获取时间戳
     *
     * @param times
     * @return
     */
    public static Long getLongTimes(int times) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -times);
        long mill = calendar.getTimeInMillis() / 1000;
        return mill;
    }

    public static List<String> getTimes(int times) {
        Calendar calendar = Calendar.getInstance();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < times; i--) {
            calendar.add(Calendar.MINUTE, -times);
            long mill = calendar.getTimeInMillis() / 1000;
            String time = getDates(mill);
            list.add(time);
        }
        return list;
    }

    /**
     * @param hour     hour
     * @param dataType 日期格式
     * @param day      昨天 -1，今天0 明天 1
     * @return
     */
    public static String getSolarData(Integer hour, Integer minute, Integer second, String dataType, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, day);
        GregorianCalendar gregorianCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hour, minute, second);
        Date time = gregorianCalendar.getTime();
        String date = MWUtils.getDate(time, dataType);
        return date;
    }

    /**
     * @param hour     hour
     * @param dataType 日期格式
     * @param day      昨天 -1，今天0 明天 1
     * @return 根据传入的时间转换
     */
    public static String getSolarData(Integer hour, Integer minute, Integer second, String dataType, int year, int mouth, int day) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(year, mouth, day, hour, minute, second);
        Date time = gregorianCalendar.getTime();
        String date = MWUtils.getDate(time, dataType);
        return date;
    }

    /**
     * 获取指定年月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最小天数
        int firstDay = cal.getMinimum(Calendar.DATE);
        //设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 获取指定年月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 获取指定年月的最后一天和第一天
     *
     * @return
     */
    public static List<String> getLastDayOfMonth() {
        List<String> strings= new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        //设置月份
        cal.add(Calendar.MONTH, 0);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String s1 = sdf.format(cal.getTime());

        strings.add(s1);
        strings.add(s);
        return strings;
    }


    /**
     * 传入String类型的时间
     * 返回Date
     *
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date date = format.parse(strDate, pos);
        return date;
    }

    /**
     * 传入String类型的时间
     * 返回Date
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        ParsePosition pos = new ParsePosition(0);
        Date date = format.parse(strDate, pos);
        return date;
    }

    /**
     * @param str
     * @return
     * @throws ParseException 转成UTC时间格式
     */
    public static String getUtcTime(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //UTC时间有8个小时的时差
//        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 8);
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        long timeInMillis = calendar.getTimeInMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(timeInMillis);
    }

    /**
     * @param str
     * @return
     * @throws ParseException 转成UTC装北京时间时间格式
     */
    public static String getUTCToCST(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
       Date date =  df.parse(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 8);
        Date date2 =calendar.getTime();
        return sdf.format(date2);
    }


    /**
     * @param str
     * @return
     * @throws ParseException 转成UTC时间格式
     */
    public static String getUtcTime2(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(str);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(date);
    }

    /**
     * @param str
     * @return
     * @throws ParseException 转成UTC时间格式
     */
    public static String getUtcTimeByNew(String str) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //UTC时间有8个小时的时差
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 8);
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        long timeInMillis = calendar.getTimeInMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(timeInMillis);
    }

    /**
     * @param date
     * @return
     * @throws ParseException 转成UTC时间格式
     */
    public static String getUtcTimeByDate(Date date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //UTC时间有8个小时的时差
//        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 8);
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        long timeInMillis = calendar.getTimeInMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(timeInMillis);
    }

    public static Date getStartOrEndTime(Date date,Integer type) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (type==0){
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
        }else {
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,999);
        }
        Date dayStart = calendar.getTime();
        return dayStart;
    }


    public static List<String > getStartOrEndDate(Date start,Date end) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String starttime = sdf.format(start);
        String endtime = sdf.format(end);
        List<String> strings = new ArrayList<>();
        strings.add(starttime);
        strings.add(endtime);
        return strings;
    }

    //获取当前时间第一天和最后一天
    public static List<String > getStartOrEndDateByweek() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cld = Calendar.getInstance(Locale.CHINA);
        cld.setFirstDayOfWeek(Calendar.MONDAY);
        cld.setTimeInMillis(System.currentTimeMillis());

        List<String> strings = new ArrayList<>();

        cld.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        String s1 = sdf.format(cld.getTime());

        cld.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        String s2 = sdf.format(cld.getTime());
        strings.add(s1);
        strings.add(s2);
        return strings;
    }
    //获取当前时间本周各个日期
    public static List<Integer > getStartOrEndDateByweekGetDay()  {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Calendar cld = Calendar.getInstance(Locale.CHINA);
        cld.setFirstDayOfWeek(Calendar.MONDAY);
        cld.setTimeInMillis(System.currentTimeMillis());

        List<Integer> strings = new ArrayList<>();

        cld.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        String s1 = sdf.format(cld.getTime());

        cld.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
        String s2 = sdf.format(cld.getTime());

        cld.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
        String s3 = sdf.format(cld.getTime());


        cld.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
        String s4 = sdf.format(cld.getTime());


        cld.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
        String s5 = sdf.format(cld.getTime());


        cld.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        String s6 = sdf.format(cld.getTime());

        cld.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        String s7 = sdf.format(cld.getTime());

        strings.add(Integer.parseInt(s1));
        strings.add(Integer.parseInt(s2));
        strings.add(Integer.parseInt(s3));
        strings.add(Integer.parseInt(s4));
        strings.add(Integer.parseInt(s5));
        strings.add(Integer.parseInt(s6));
        strings.add(Integer.parseInt(s7));
        return strings;
    }


    public static List<Integer> getStartOrEndDateByMonthGetDay() {
        Calendar cal = Calendar.getInstance();
        //设置月份
        cal.add(Calendar.MONTH, 0);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);

        List<Integer> integers = new ArrayList<>();
        for (int i = 0; i < lastDay; i++) {
            integers.add(i+1);
        }
        return integers;
    }

    public static void emptyObjectField(Object object){
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName() ,object.getClass());
                Method method = pd.getWriteMethod();
                Object obj = null;
                method.invoke(object ,obj);
            }catch (Exception e){
                log.warn("emptyObjectField" ,e.getMessage());
            }
        }
    }
}
