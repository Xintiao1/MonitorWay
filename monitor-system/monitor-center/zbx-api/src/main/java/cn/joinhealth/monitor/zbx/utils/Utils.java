package cn.joinhealth.monitor.zbx.utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jiangwenjiang on 2019/6/29.
 */
public class Utils {
    public static Map<String,String> STATE = new HashMap<>();
    public static Map<String,String> SEVERITY = new HashMap<>();
    public static Map<String,String> GROUPNAME = new HashMap<>();
    static {
        STATE.put("1","AGENT");
        STATE.put("2","SNMP");
        STATE.put("3","IPMI");
        STATE.put("4","JMX");

        SEVERITY.put("0","未分类");
        SEVERITY.put("1","信息");
        SEVERITY.put("2","警告");
        SEVERITY.put("3","一般");
        SEVERITY.put("4","严重");
        SEVERITY.put("5","紧急");

        GROUPNAME.put("1","主机");
        GROUPNAME.put("2","网路设备");
        GROUPNAME.put("6","应用");
        GROUPNAME.put("7","中间件");
        GROUPNAME.put("8","数据库");
    }

    public static String getDate(Long times){
        Calendar c= Calendar.getInstance();
        long millions=times*1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    public static String getDates(Long times){
        Calendar c= Calendar.getInstance();
        long millions=times*1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateString = sdf.format(c.getTime());
        return dateString;
    }

    /**
     * list去重
     * @param list
     * @return
     */

    public static List removeDuplicate(List list){
        List listTemp = new ArrayList();
        for(int i=0;i<list.size();i++){
            if(!listTemp.contains(list.get(i))){
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }
    /**
     * 获取时间戳
     * @param times
     * @return
     */
    public static Long getLongTimes(int times){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -times);
        long mill = calendar.getTimeInMillis()/1000;
        return mill;
    }

    public static List<String> getTimes(int times){
        Calendar calendar= Calendar.getInstance();
        List<String> list = new ArrayList<>();
        for(int i=0;i<times;i--){
            calendar.add(Calendar.MINUTE, -times);
            long mill = calendar.getTimeInMillis()/1000;
            String time = getDates(mill);
            list.add(time);
        }
        return list;
    }

    public static void main(String[] args){
        String str = getDates(1561782008l);
        //List<String> list = getTimes(5);
        ////System.out.println(">>>>>>>>>>>>>>>"+str);
    }
}
