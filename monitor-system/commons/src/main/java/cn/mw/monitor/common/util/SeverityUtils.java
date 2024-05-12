package cn.mw.monitor.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
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
    public static Map<String,String> SEVERITY = new HashMap<>();
    public static Map<String,String> ALE_TYPE = new HashMap<>();
    public static Map<String,String> TYPE2ZHN = new HashMap<>();
    static {
        SEVERITY.put("0","未分类");
        SEVERITY.put("1","信息");
        SEVERITY.put("2","警告");
        SEVERITY.put("3","一般");
        SEVERITY.put("4","严重");
        SEVERITY.put("5","灾难");
        /*****************类型*******************/
        ALE_TYPE.put("服务器","1");
        ALE_TYPE.put("数据库","2");
        ALE_TYPE.put("存储设备","3");
        ALE_TYPE.put("网络设备","4");
        ALE_TYPE.put("安全设备","5");
        ALE_TYPE.put("中间件","6");
        ALE_TYPE.put("混合云","7");
        ALE_TYPE.put("虚拟化","8");
        ALE_TYPE.put("线路","9");
        ALE_TYPE.put("应用","10");
/*****************************类型数字转中文******************************************/
        TYPE2ZHN.put("1","服务器");
        TYPE2ZHN.put("2","数据库");
        TYPE2ZHN.put("3","存储设备");
        TYPE2ZHN.put("4","网络设备");
        TYPE2ZHN.put("5","安全设备");
        TYPE2ZHN.put("6","中间件");
        TYPE2ZHN.put("7","混合云");
        TYPE2ZHN.put("8","虚拟化");
        TYPE2ZHN.put("9","线路");
        TYPE2ZHN.put("10","应用");

    }

    public static String getDate(Long times){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c= Calendar.getInstance();
        long millions=times*1000;
        c.setTimeInMillis(millions);
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    public static Long str2Timestamp(String times)  {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(times);
        } catch (ParseException e) {
            log.error("错误返回 :{}",e);
        }
        long time = date.getTime();
        return time/1000;
    }
    /**
     * 根据传入的时间计算距离当前时间
     * @param time
     * @return
     */
    public static String CalculateTime(String time) {
        long nowTime = System.currentTimeMillis(); // 获取当前时间的毫秒数
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
            long dateTemp1 = dateDiff / 1000; // 秒
            long dateTemp2 = dateTemp1 / 60; // 分钟
            long dateTemp3 = dateTemp2 / 60; // 小时
            long dateTemp4 = dateTemp3 / 24; // 天数
            long dateTemp5 = dateTemp4 / 30; // 月数
            long dateTemp6 = dateTemp5 / 12; // 年数
            if (dateTemp6 > 0) {
                msg = dateTemp6 + "年前";
            } else if (dateTemp5 > 0) {
                msg = dateTemp5 + "个月前";
            } else if (dateTemp4 > 0) {
                msg += dateTemp4 + "天";
            } else if (dateTemp3 > 0) {
                msg += dateTemp3 + "时";
            } else if (dateTemp2 > 0) {
                msg += dateTemp2 + "分";
            } else if (dateTemp1 > 0) {
                msg = "刚刚";
            }
        }
        return msg;
    }

    /**
     * 计算两个时间差
     * @param time
     * @return
     */
    public static String getLastTime(Long time){
        long nd = 24 * 60 * 60;// 一天的秒数
        long nh =  60 * 60;// 一小时的秒数
        long nm = 60;// 一分钟的秒数


        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        // 获得两个时间的毫秒时间差异
        day = time / nd;// 计算差多少天
        hour = time % nd / nh + day * 24;// 计算差多少小时
        min = time % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
        sec = time % nd % nh % nm ;// 计算差多少秒
        String lastTime = "";
        // 输出结果
        if(0!=day){
            lastTime+=day + "天";
        }
        if(0!=hour){
            lastTime+=(hour - day * 24) + "时";
        }
        if(0!=min){
            lastTime+=(min - day * 24 * 60) + "分";
        }
        if(0!=sec){
            lastTime+= sec + "秒";
        }
       return lastTime;

    }

    /**
     * 当前时间减N分钟
     *
     * @return
     */
    public static long curTimeMinusN(int n) {
        Date date=new Date();
        Date date1=new Date(date.getTime()-n*60*1000);
        return date1.getTime();
    }

    public static void main (String[] args) throws ParseException {

//        String str = "2019-07-18 17:30:50";
//        Long teims = str2Timestamp(str);
//        String date = getDate(teims);
//        /*String str = getLastTime(240L);*/
//        ////System.out.println(">>>>>>>>>>>>>"+date);
      /*  ////System.out.println(getDate(1587285537344L/1000));
        ////System.out.println(getDate(1587285542345L/1000));
        ////System.out.println(getDate(1587285547346L/1000));*/

     //
        // ////System.out.println(CalculateTime(getDate(1587911557L)));
//        ////System.out.println(CalculateTime(getDate(1586708392L)));
//        ////System.out.println(CalculateTime(getDate(1586786653L)));
//        ////System.out.println(getDate(1586708392L));
//        ////System.out.println(getDate(1586621872L));
////        ////System.out.println(getDate(1586786653L));
//        ////System.out.println(getDate(1586621872L+666768297L/1000000));
//        ////System.out.println(getLastTime(666768297L/1000/1000));
//        ////System.out.println(getLastTime(353672944L/1000/1000));

    }
   /* public static String CalculateTime1(Long time) {
        long nowTime = System.currentTimeMillis(); // 获取当前时间的毫秒数
        ////System.out.println(nowTime);
        String msg = "";
        long reset = time; // 获取指定时间的毫秒数
        long dateDiff = nowTime - reset;
        if (dateDiff < 0) {
            msg = "--";
        } else {
            long dateTemp1 = dateDiff / 1000; // 秒
            long dateTemp2 = dateTemp1 / 60; // 分钟
            long dateTemp3 = dateTemp2 / 60; // 小时
            long dateTemp4 = dateTemp3 / 24; // 天数
            long dateTemp5 = dateTemp4 / 30; // 月数
            long dateTemp6 = dateTemp5 / 12; // 年数
            if (dateTemp6 > 0) {
                msg = dateTemp6 + "年前";
            } else if (dateTemp5 > 0) {
                msg = dateTemp5 + "个月前";
            } else if (dateTemp4 > 0) {
                msg += dateTemp4 + "天 ";
            } else if (dateTemp3 > 0) {
                msg += dateTemp3 + "时 ";
            } else if (dateTemp2 > 0) {
                msg += dateTemp2 + "分 ";
            } else if (dateTemp1 > 0) {
                msg = "刚刚";
            }
        }
        return msg;
    }*/
}
