package cn.mw.monitor.assets.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName ZabbixMainTenTimesUtils
 * @Description 翻译星期和月份
 * @Author gengjb
 * @Date 2021/7/30 14:40
 * @Version 1.0
 **/
public class ZabbixMainTenTimesUtils {

    /**
     * 返回星期的int数据
     * @param dayOfWeek 星期的字符串，可能多个星期，按,分割
     * @return
     */
    public static Integer dayOfWeekUtils(String dayOfWeek){
        Integer count = 0;
        if(StringUtils.isNotBlank(dayOfWeek)){
            String[] weeks = dayOfWeek.split(",");
            if(weeks != null && weeks.length > 0){
                for (String week : weeks) {
                    switch (week){
                        case "星期一":
                            count += 1;
                            break;
                        case "星期二":
                            count += 1 << 1;
                            break;
                        case "星期三":
                            count += 1 << 2;
                            break;
                        case "星期四":
                            count += 1 << 3;
                            break;
                        case "星期五":
                            count += 1 << 4;
                            break;
                        case "星期六":
                            count += 1 << 5;
                            break;
                        case "星期日":
                            count += 1 << 6;
                            break;
                        default: count += 0;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 返回月份的int数据
     * @param sinoGramMonth 月份的字符串，可能多个月份，按,分割
     * @return
     */
    public static Integer monthUtils(String sinoGramMonth){
        Integer count = 0;
        if(StringUtils.isNotBlank(sinoGramMonth)){
            String[] months = sinoGramMonth.split(",");
            if(months != null && months.length > 0){
                for (String month : months) {
                    switch (month){
                        case "一月":
                            count += 1;
                            break;
                        case "二月":
                            count += 1 << 1;
                            break;
                        case "三月":
                            count += 1 << 2;
                            break;
                        case "四月":
                            count += 1 << 3;
                            break;
                        case "五月":
                            count += 1 << 4;
                            break;
                        case "六月":
                            count += 1 << 5;
                            break;
                        case "七月":
                            count += 1 << 6;
                            break;
                        case "八月":
                            count += 1 << 7;
                            break;
                        case "九月":
                            count += 1 << 8;
                            break;
                        case "十月":
                            count += 1 << 9;
                            break;
                        case "十一月":
                            count += 1 << 10;
                            break;
                        case "十二月":
                            count += 1 << 11;
                            break;
                        default: count += 0;
                    }
                }
            }
        }
        return count;
    }

}

