package cn.mw.monitor.report.util;

import cn.mw.monitor.report.service.impl.DateTypeEnum;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/3/13 16:32
 * @Version 1.0
 **/
@Slf4j
public class MwReportDateUtil {

    public static List<Long> calculitionTime(Integer dateType, List<String> chooseTime){
        DateTypeEnum dateTypeEnum = DateTypeEnum.getDateTypeEnum(dateType);
        if(dateTypeEnum == null && CollectionUtils.isEmpty(chooseTime)){
            dateTypeEnum = DateTypeEnum.YESTERDAY;
        }
        return calculitionTime(dateTypeEnum, chooseTime);
    }

    public static List<Long> calculitionTime(DateTypeEnum dateTypeEnum, List<String> chooseTime){
        long start = 0;
        long end = 0;
        if(CollectionUtils.isEmpty(chooseTime)||dateTypeEnum.getType()<11){
            switch (dateTypeEnum){
                case YESTERDAY:
                    //昨天
                    List<Date> yesterday = ReportDateUtil.getYesterday();
                    start = yesterday.get(0).getTime() / 1000;
                    end = yesterday.get(1).getTime() / 1000;
                    break;
                case TODAY:
                    //本日
                    List<Date> today = ReportDateUtil.getToday();
                    start = today.get(0).getTime() / 1000;
                    end = today.get(1).getTime() / 1000;
                    break;
                case LATEST_7DAY:
                    //最近7天
                    Long curr = System.currentTimeMillis();
                    end = System.currentTimeMillis() / 1000;
                    long l2 = 7*24*60*60;
                    start = (curr-(l2*1000)) / 1000;
                    break;
                case THIS_WEEK:
                    //本周
                    List<Date> week = ReportDateUtil.getWeek();
                    start = week.get(0).getTime() / 1000;
                    end = week.get(1).getTime() / 1000;
                    break;
                case LAST_WEEK:
                    //上周
                    List<Date> lastWeek = ReportDateUtil.getLastWeek();
                    start = lastWeek.get(0).getTime() / 1000;
                    end = lastWeek.get(1).getTime() / 1000;
                    break;
                case LATEST_30DAY:
                    //最近30天
                    Long currDate = System.currentTimeMillis();
                    end = System.currentTimeMillis() / 1000;
                    long l3 = 30*24*60*60;
                    start = (currDate-(l3*1000)) / 1000;
                    break;
                case THIS_MONTH:
                    //本月
                    List<Date> month = ReportDateUtil.getMonth();
                    start = month.get(0).getTime() / 1000;
                    end = month.get(1).getTime() / 1000;
                    break;
                case LAST_MONTH:
                    //上月
                    List<Date> lastMonth = ReportDateUtil.getLastMonth();
                    start = lastMonth.get(0).getTime() / 1000;
                    end = lastMonth.get(1).getTime() / 1000;
                    break;
                case LAST_12MONTH:
                    //近12个月
                    List<Date> front12Month = ReportDateUtil.getFront12Month();
                    start = front12Month.get(0).getTime() / 1000;
                    end = front12Month.get(1).getTime() / 1000;
                    break;
                case THIS_YEAR:
                    //今年
                    List<Date> year = ReportDateUtil.getYear();
                    start = year.get(0).getTime() / 1000;
                    end = year.get(1).getTime() / 1000;
                    break;
                default:
                    break;
            }
        }
        if(!CollectionUtils.isEmpty(chooseTime)||dateTypeEnum.getType()== DateTypeEnum.SET_DATE.getType()){
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if (dateTypeEnum.getType()==DateTypeEnum.SET_DATE.getType()){
                    start = format.parse(chooseTime.get(0)).getTime() / 1000;
                    end = format.parse(chooseTime.get(1)).getTime() / 1000;
                }
            } catch (Exception e) {
                try{
                    SimpleDateFormat formatTwo = new SimpleDateFormat("yyyy-MM-dd");
                    start = formatTwo.parse(chooseTime.get(0)).getTime() / 1000;
                    end = formatTwo.parse(chooseTime.get(1)).getTime() / 1000;
                }catch (Exception f){
                    log.error("时间转换失败1",f);
                }
                log.error("时间转换失败2",e);
            }
        }
        List<Long> times = new ArrayList<>();
        times.add(start);
        times.add(end);
        return times;
    }

    /**
     * 获取日期相隔天数
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean getDateDay(Long startTime,Long endTime){
        long hour = DateUtils.between(new Date(startTime*1000), new Date(endTime*1000), DateUnitEnum.HOUR);
        if(hour > 5){return true;}
        return false;
    }
}
