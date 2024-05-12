package cn.mw.monitor.service.assets.param;

import cn.mwpaas.common.constant.DateConstant;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;

public class MwAssetsMainTainV1Adapter {
    private static final String timeSep = ":";
    public void apply(MwAssetsMainTainOnceParam onceParam ,MwAssetsMainTainV1Once v1Once ,MwAssetsMainTainV1Period period){

        PeriodStartCallback periodStartCallback = (startHour ,startMin) -> {
//            String startHourStr = (startHour > 9)?startHour.toString():"0"+startHour.toString();
//            String startMinStr = (startMin > 9)?startMin.toString():"0"+startMin.toString();
//            String start = v1Once.getStartDay() + " " + startHourStr + timeSep + startMinStr + timeSep + "00";
            onceParam.setMainDate(DateUtils.parse(v1Once.getStartDay(), DateConstant.NORM_DATETIME));

        };
        //计算时间差
        long minute = (DateUtils.parse(period.getEnd()).getTime() - DateUtils.parse(period.getStart()).getTime()) / (1000 * 60);
        period.setMinute(minute);
        period.setStart(period.getStart().split(" ")[1]);
        tranform(onceParam ,period ,periodStartCallback);
    }

    public void apply(MwAssetsMainTainDayParam dayParam ,MwAssetsMainTainV1Day v1Day ,MwAssetsMainTainV1Period period){
        dayParam.setFreq(1);
        tranform(dayParam ,period ,null);
    }

    public void apply(MwAssetsMainTainWeekParam weekParam ,MwAssetsMainTainV1Week v1Week ,MwAssetsMainTainV1Period period){
        weekParam.setFreq(1);
        weekParam.setWeekIndex(v1Week.getWeekIndexes());
        tranform(weekParam ,period ,null);
    }

    public void apply(MwAssetsMainTainMonthParam monthParam ,MwAssetsMainTainV1Month v1Month ,MwAssetsMainTainV1Period period){
        monthParam.setWeekIndex(v1Month.getWeekIndexes());
        monthParam.setMonthIndex(v1Month.getMonthIndexes());
        tranform(monthParam ,period ,null);
    }

    private void tranform(MaintainTransform maintainTransform ,MwAssetsMainTainV1Period period ,PeriodStartCallback periodStartCallback){
        String[] startStr = period.getStart().split(MwAssetsMainTainV1Period.SEP);
        Integer startHour = Integer.parseInt(startStr[0]);
        Integer startMin = Integer.parseInt(startStr[1]);

        if(null != periodStartCallback){
            periodStartCallback.setMainDate(startHour ,startMin);
        }

        Integer startMinNum = startHour * 60 + startMin;

        Integer endMinNum;
        Integer hour;
        Integer min;
        if(StringUtils.isNotBlank(period.getEnd()) && period.getMinute() == null){
            String[] endStr = period.getEnd().split(MwAssetsMainTainV1Period.SEP);
            endMinNum = Integer.parseInt(endStr[0]) * 60 + Integer.parseInt(endStr[1]);
            Integer interval = endMinNum - startMinNum;
            hour = interval / 60;
            min = interval % 60;
        }else{
            endMinNum = Integer.parseInt(String.valueOf(period.getMinute()));
            hour = endMinNum / 60;
            min = endMinNum % 60;
        }
        maintainTransform.setStartHour(startHour);
        maintainTransform.setStartMin(startMin);
        maintainTransform.setDurationDay(0);
        maintainTransform.setDurationHour(hour);
        maintainTransform.setDurationMin(min);
    }

    @FunctionalInterface
    private interface PeriodStartCallback{
        void setMainDate(Integer startHour ,Integer startMin);
    }
}
