package cn.mw.syslog.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GenerationTableName {

    public static final String TABLENAME = "mw_sys_log";

    //系统日志
    public static String getTableName(Calendar calendar) {
        String str = "_";
        StringBuffer sb = new StringBuffer().append(TABLENAME);
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

    //根据类别获取表名
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

    public static List<String> getTableNameKey(List<?> list) {
        List<String> strList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                continue;
            } else {
                if (list.get(i).toString().contains(TABLENAME)) {
                    String str = list.get(i).toString().replace(new StringBuffer().append(TABLENAME).append("_").toString(), "");
                    strList.add(str);
                }
            }
        }
        return strList;
    }

    public static List<String> getTableNameKey(List<?> list,String tableType) {
        List<String> strList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).toString().contains(tableType)) {
                String str = list.get(i).toString().replace(new StringBuffer().append(tableType).append("_").toString(), "");
                strList.add(str);
            }
        }
        return strList;
    }

    public static String getTablename(List<?> list, String tableNameKey) {
        List<String> strlist = getTableNameKey(list);
        if (strlist.contains(tableNameKey)) {
            return new StringBuffer().append(TABLENAME).append("_").append(tableNameKey).toString();
        }
        return getTableName(Calendar.getInstance());
    }

    public static String getTablename(List<?> list, String tableNameKey,String tableType) {
        List<String> strlist = getTableNameKey(list,tableType);
        if (strlist.contains(tableNameKey)) {
            return new StringBuffer().append(tableType).append("_").append(tableNameKey).toString();
        }
        return getTableName(Calendar.getInstance());
    }

}
