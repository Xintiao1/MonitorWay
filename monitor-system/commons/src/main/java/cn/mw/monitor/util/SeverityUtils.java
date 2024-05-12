package cn.mw.monitor.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangwenjiang on 2019/6/12.
 */
@Slf4j
public class SeverityUtils {
    public static Map<String, String> SEVERITY = new HashMap<>();
    public static Map<String, String> ALE_TYPE = new HashMap<>();
    public static Map<Integer, String> TYPE2ZHN = new HashMap<>();

    static {
        SEVERITY.put("0", "未分类");
        SEVERITY.put("1", "信息");
        SEVERITY.put("2", "警告");
        SEVERITY.put("3", "一般");
        SEVERITY.put("4", "严重");
        SEVERITY.put("5", "紧急");
        /*****************类型*******************/
        ALE_TYPE.put("服务器", "1");
        ALE_TYPE.put("数据库", "2");
        ALE_TYPE.put("存储设备", "3");
        ALE_TYPE.put("网络设备", "4");
        ALE_TYPE.put("安全设备", "5");
        ALE_TYPE.put("中间件", "6");
        ALE_TYPE.put("混合云", "7");
        ALE_TYPE.put("虚拟化", "8");
        ALE_TYPE.put("线路", "9");
        ALE_TYPE.put("应用", "10");
/*****************************类型数字转中文******************************************/
        TYPE2ZHN.put(1, "服务器");
        TYPE2ZHN.put(2, "网络设备");
        TYPE2ZHN.put(6, "应用");
        TYPE2ZHN.put(7, "中间件");
        TYPE2ZHN.put(8, "数据库");
    }

    public static String getDate(Long times) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        long millions = times * 1000;
        c.setTimeInMillis(millions);
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    public static Long str2Timestamp(String times) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(times);
        } catch (ParseException e) {
            log.error("错误返回 :{}",e);
        }
        long time = date.getTime();
        return time / 1000;
    }

    public static String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }


    /**
     * 根据传入的时间计算距离当前时间
     *
     * @param time
     * @return
     */
    public static String CalculateTime(String time) {
        long nowTime = System.currentTimeMillis(); // 获取当前时间的毫秒数
        Date date = new Date();
        String msg = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 指定时间格式
        Date setTime = null; // 指定时间
        try {
            setTime = sdf.parse(time); // 将字符串转换为指定的时间格式
        } catch (ParseException e) {
            log.error("错误返回 :{}",e);
        }
        long reset = setTime.getTime(); // 获取指定时间的毫秒数
        long dateDiff = nowTime - reset;
        if (dateDiff < 0) {
            msg = "--";
        } else {
            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(date);
            Calendar previousDate = Calendar.getInstance();
            previousDate.setTime(setTime);
            previousDate.add(Calendar.SECOND, -60);
            int year = nextDate.get(Calendar.YEAR) - previousDate.get(Calendar.YEAR);
            int month = nextDate.get(Calendar.MONTH) - previousDate.get(Calendar.MONTH);
            int day = nextDate.get(Calendar.DAY_OF_MONTH) - previousDate.get(Calendar.DAY_OF_MONTH);
            int hour = nextDate.get(Calendar.HOUR_OF_DAY) - previousDate.get(Calendar.HOUR_OF_DAY);// 24小时制
            int min = nextDate.get(Calendar.MINUTE) - previousDate.get(Calendar.MINUTE);
            int second = nextDate.get(Calendar.SECOND) - previousDate.get(Calendar.SECOND);
            boolean hasBorrowDay = false;// "时"是否向"天"借过一位
            int count = 0;
            if (second < 0) {
                second += 60;
                min--;
            }
            if (min < 0) {
                min += 60;
                hour--;
            }
            if (hour < 0) {
                hour += 24;
                day--;
                hasBorrowDay = true;
            }
            if (day < 0) {
                // 计算截止日期的上一个月有多少天，补上去
                Calendar tempDate = (Calendar) nextDate.clone();
                tempDate.add(Calendar.MONTH, -1);// 获取截止日期的上一个月
                day += tempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                // nextDate是月底最后一天，且day=这个月的天数，即是刚好一整个月，比如20160131~20160229，day=29，实则为1个月
                if (!hasBorrowDay
                        && nextDate.get(Calendar.DAY_OF_MONTH) == nextDate.getActualMaximum(Calendar.DAY_OF_MONTH)// 日期为月底最后一天
                        && day >= nextDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {// day刚好是nextDate一个月的天数，或大于nextDate月的天数（比如2月可能只有28天）
                    day = 0;// 因为这样判断是相当于刚好是整月了，那么不用向 month 借位，只需将 day 置 0
                } else {// 向month借一位
                    month--;
                }
            }
            if (month < 0) {
                month += 12;
                year--;
            }
            if(month != 0){
                msg += month + "个月";
                count += 1;
            }
            if(day != 0){
                msg += day + "天";
                count += 1;
            }
            if(hour != 0){
                msg += hour + "时";
                count += 1;
            }
            if(min != 0){
                if(count > 2){
                    return msg;
                }
                msg += min + "分";
                count += 1;
            }
            if(second != 0){
                if(count > 2){
                    return msg;
                }
                msg += second + "秒";
            }
           // msg = getLastTime(dateDiff/1000);
        }
        return msg;
    }

    /**
     * 计算两个时间差
     *
     * @param time
     * @return
     */
    public static String getLastTime(Long time) {
        long nd = 24 * 60 * 60;// 一天的秒数
        long nh = 60 * 60;// 一小时的秒数
        long nm = 60;// 一分钟的秒数


        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long mon = 0;
        // 获得两个时间的毫秒时间差异
        day = time / nd;// 计算差多少天
        hour = time % nd / nh + day * 24;// 计算差多少小时
        min = time % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
        sec = time % nd % nh % nm;// 计算差多少秒
        String lastTime = "";
        // 输出结果
        if (0 != day) {
            lastTime += day + "天";
        }
        if (0 != hour) {
            lastTime += (hour - day * 24) + "时";
        }
        if (0 != min) {
            lastTime += (min - day * 24 * 60) + "分";
        }
        lastTime += sec + "秒";

        return lastTime;

    }

    /**
     * 获取时间戳
     *
     * @param time
     * @return
     */
    public static long getTime(String time) {
        long nd = 24 * 60 * 60;// 一天的秒数
        long nh = 60 * 60;// 一小时的秒数
        long nm = 60;// 一分钟的秒数
        long result = 0;
        int num = 0;
                //
        /*String str = time.substring(time.length()-1,time.length());
        try{
            num = Integer.parseInt(time.substring(0,time.length()-1));
        }catch (Exception e){
            throw e;
        }*/
        if(time.contains("天") || time.contains("月")){
            return result;
        }
        if(time.contains("时")){
            int begin = time.indexOf("天");
            int end = time.indexOf("时");
            String msg = time.substring(begin+1,end);
            result += Integer.parseInt(msg) * nh;
        }
        if(time.contains("分")){
            int begin = time.indexOf("时");
            int end = time.indexOf("分");
            String msg = time.substring(begin+1,end);
            result += Integer.parseInt(msg) * nm;
        }
        if(time.contains("秒")){
            int begin = -1;
            if(time.contains("分")){
                begin = time.indexOf("分");
            }else{
                begin = time.indexOf("时");
            }
            int end = time.indexOf("秒");
            String msg = time.substring(begin+1,end);
            result += Integer.parseInt(msg);
        }
        return result;

    }

//    public static void main(String[] args) {
//        String str = getLastTime(300L);
//        ////System.out.println(str);
//    }

    /**
     * 当前时间减N分钟
     *
     * @return
     */
    public static long curTimeMinusN(int n) {
        Date date = new Date();
        Date date1 = new Date(date.getTime() - n * 60 * 1000);
        return date1.getTime();
    }

}
