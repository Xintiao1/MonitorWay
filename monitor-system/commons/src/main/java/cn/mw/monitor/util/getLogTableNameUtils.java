package cn.mw.monitor.util;

import java.util.Calendar;

public class getLogTableNameUtils {
    //根据类别获取定时任务创建的表名
    public static String getTableNameByType(Calendar calendar,String tableNameType) {
        String str = "_";
        StringBuffer sb = new StringBuffer().append(tableNameType);
        Integer month = calendar.get(Calendar.MONTH);
        switch (month) {
            case 0:
            case 1:
            case 2:
                sb.append(str)
                        .append(calendar.get(Calendar.YEAR))
                        .append(str)
                        .append("q1th");
                break;
            case 3:
            case 4:
            case 5:
                sb.append(str)
                        .append(calendar.get(Calendar.YEAR))
                        .append(str)
                        .append("q2nd");
                break;
            case 6:
            case 7:
            case 8:
                sb.append(str)
                        .append(calendar.get(Calendar.YEAR))
                        .append(str)
                        .append("q3rd");
                break;
            case 9:
            case 10:
            case 11:
                sb.append(str)
                        .append(calendar.get(Calendar.YEAR))
                        .append(str)
                        .append("q4th");
                break;
            default:
                break;
        }
        return sb.toString();
    }
}
