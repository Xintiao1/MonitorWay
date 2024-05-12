package cn.mw.monitor.assets.utils;

import java.util.Calendar;
import java.util.Date;

public class SimpleDateUtil {

    public static Long curTimeMinusN(int m){
        Date date=new Date();
        Date date1=new Date(date.getTime()-m*60*1000);
        return date1.getTime();
    }
}
