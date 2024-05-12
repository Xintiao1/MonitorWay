package cn.mw.monitor.service.assets.param;

import cn.mwpaas.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class MwAssetsMainTainZabbixParam {
    public static final int PERIOD_ONCE = 0;
    public static final int PERIOD_DAY = 2;
    public static final int PERIOD_WEEK = 3;
    public static final int PERIOD_MONTH = 4;

    private Integer period;
    private Integer timeperiod_type;

    private Long start_date;

    private Integer start_time;

    private Integer every;

    private Integer dayofweek;

    private Integer day;

    private Integer month;

    public void extractFrom(Object obj){
        if(obj instanceof MwAssetsMainTainOnceParam){
            extractFrom((MwAssetsMainTainOnceParam) obj);
        }

        if(obj instanceof MwAssetsMainTainDayParam){
            extractFrom((MwAssetsMainTainDayParam) obj);
        }

        if(obj instanceof MwAssetsMainTainWeekParam){
            extractFrom((MwAssetsMainTainWeekParam) obj);
        }

        if(obj instanceof MwAssetsMainTainMonthParam){
            extractFrom((MwAssetsMainTainMonthParam) obj);
        }
    }

    private void extractFrom(MwAssetsMainTainOnceParam onceParam){
        timeperiod_type = PERIOD_ONCE;
        start_date = onceParam.getMainDate().getTime() / 1000;
        period = caculatePeriod(onceParam.getDurationDay(), onceParam.getDurationHour(), onceParam.getDurationMin());
    }

    private void extractFrom(MwAssetsMainTainDayParam dayParam) {
        timeperiod_type = PERIOD_DAY;
        period = caculatePeriod(dayParam.getDurationDay(), dayParam.getDurationHour(), dayParam.getDurationMin());
        start_time = caclulateStartTime(dayParam.getStartHour(), dayParam.getStartMin());
        every = dayParam.getFreq();
    }

    private void extractFrom(MwAssetsMainTainWeekParam weekParam) {
        timeperiod_type = PERIOD_WEEK;
        period = caculatePeriod(weekParam.getDurationDay(), weekParam.getDurationHour(), weekParam.getDurationMin());
        start_time = caclulateStartTime(weekParam.getStartHour(), weekParam.getStartMin());
        every = weekParam.getFreq();

        if (null != weekParam.getWeekIndex()) {
            int day = 0;
            for (Integer index : weekParam.getWeekIndex()) {
                day = (byte) day | (byte) MaintainTransform.dayOfWeeks[index-1];
            }
            dayofweek = day;
        }
    }

    private void extractFrom(MwAssetsMainTainMonthParam monthParam) {
        timeperiod_type = PERIOD_MONTH;
        period = caculatePeriod(monthParam.getDurationDay(), monthParam.getDurationHour(), monthParam.getDurationMin());
        start_time = caclulateStartTime(monthParam.getStartHour(), monthParam.getStartMin());

        if(null != monthParam.getMonthIndex()){
            byte[] value = MaintainTransform.intToBytes(0);
            for(Integer index : monthParam.getMonthIndex()){
                byte[] data = MaintainTransform.intToBytes(MaintainTransform.months[index-1]);
                value[0] = (byte)(value[0] | data[0]);
                value[1] = (byte)(value[1] | data[1]);
            }

            int intValue = MaintainTransform.bytesToInt(value);
            if(intValue > 0){
                month = intValue;
            }
        }

        if(null != monthParam.getDayPerMonth()){
            day = monthParam.getDayPerMonth();
        }

        if(null != monthParam.getWeekIndexPerMonth()){
            every = monthParam.getWeekIndexPerMonth();
        }

        if (null != monthParam.getWeekIndex()) {
            int day = 0;
            for (Integer index : monthParam.getWeekIndex()) {
                day = (byte) day | (byte) MaintainTransform.dayOfWeeks[index-1];
            }
            dayofweek = day;
        }
    }

    private Integer caculatePeriod(Integer durationDay, Integer durationHour, Integer durationMin) {
        int period = 0;
        if (null != durationDay) {
            period += durationDay * 24 * 3600;
        }

        if (null != durationHour) {
            period += durationHour * 3600;
        }

        if (null != durationMin) {
            period += durationMin * 60;
        }

        if (period > 0) {
            return period;
        }

        return null;
    }

    private Integer caclulateStartTime(Integer startHour, Integer startMin) {
        int startTime = 0;
        if (null != startHour) {
            startTime += startHour * 3600;
        }

        if (null != startMin) {
            startTime += startMin * 60;
        }

        if (startTime > 0) {
            return startTime;
        }

        return null;
    }

    public HashMap getRequest(){
        HashMap map = new HashMap();
        String[] requestNames = {"period" ,"timeperiod_type" ,"start_date" ,"start_time" ,"every" ,"dayofweek"
        ,"day" ,"month"};

        for(String fieldName : requestNames){
            String methodName = StringUtils.genGetMethoName(fieldName);
            try{
                Method method = this.getClass().getMethod(methodName);
                Object object = method.invoke(this);
                if(null != object){
                    map.put(fieldName ,object);
                }

            }catch (Exception e){}

        }

        if(!map.isEmpty()){
            map.put("hasPeriod" ,true);
        }
        return map;
    }

    public void extractFrom(JsonNode resultData){
        if (resultData.size() > 0) {
            resultData.forEach(result -> {
                JsonNode timeperiods = result.get("timeperiods");
                if(null != timeperiods){
                    timeperiods.forEach(data -> {
                        String periodStr = data.get("period").asText();
                        period = Integer.parseInt(periodStr);

                        String timeperiod_typeStr = data.get("timeperiod_type").asText();
                        timeperiod_type = Integer.parseInt(timeperiod_typeStr);

                        String start_dateStr = data.get("start_date").asText();
                        start_date = Long.parseLong(start_dateStr);

                        String start_timeStr = data.get("start_time").asText();
                        start_time = Integer.parseInt(start_timeStr);

                        String everyStr = data.get("every").asText();
                        every = Integer.parseInt(everyStr);

                        String dayofweekStr = data.get("dayofweek").asText();
                        dayofweek = Integer.parseInt(dayofweekStr);

                        String dayStr = data.get("day").asText();
                        day = Integer.parseInt(dayStr);

                        String monthStr = data.get("month").asText();
                        month = Integer.parseInt(monthStr);
                    });
                }
            });
        }
    }
}
