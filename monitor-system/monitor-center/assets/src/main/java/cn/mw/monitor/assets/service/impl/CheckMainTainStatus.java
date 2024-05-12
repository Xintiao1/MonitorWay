package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.service.assets.param.*;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @ClassName
 * @Description 校验维护计划的维护时间是否是屏蔽中
 * @Author gengjb
 * @Date 2023/6/21 10:05
 * @Version 1.0
 **/
@Service
@Slf4j
public class CheckMainTainStatus {

    public boolean checkStatus(MwAssetsMainTainParamV1 mwAssetsMainTainParamV1){
        List<MwAssetsMainTainV1Period> periods = mwAssetsMainTainParamV1.getPeriods();
        MwAssetsMainTainV1Once onceParam = mwAssetsMainTainParamV1.getOnceParam();
        Date currTime = new Date();
        Calendar calendar = Calendar.getInstance();
        if(onceParam != null){
            return checkOnceMainTain(onceParam,periods);
        }
        MwAssetsMainTainV1Day dayParam = mwAssetsMainTainParamV1.getDayParam();
        if(dayParam != null){
            return checkDayMainTain(periods);
        }
        MwAssetsMainTainV1Week weekParam = mwAssetsMainTainParamV1.getWeekParam();
        if(weekParam != null){
           return checkWeekMainTain(weekParam,periods,calendar,currTime);
        }
        MwAssetsMainTainV1Month monthParam = mwAssetsMainTainParamV1.getMonthParam();
        if(monthParam != null){
            return checkMonthMainTain(monthParam,periods,calendar,currTime);

        }
        return false;
    }

    /**
     * 校验一次性维护是否是屏蔽中
     * @param onceParam
     * @param periods
     * @return
     */
    private boolean checkOnceMainTain(MwAssetsMainTainV1Once onceParam,List<MwAssetsMainTainV1Period> periods){
        for (MwAssetsMainTainV1Period period : periods) {
            boolean flag = dateCpmpare(DateUtils.parse(period.getStart()), DateUtils.parse(period.getEnd()));
            if(flag){return true;}
        }
        return false;
    }

    /**
     * 校验每天维护是否是屏蔽中
     * @param periods
     * @return
     */
    private boolean checkDayMainTain(List<MwAssetsMainTainV1Period> periods){
        for (MwAssetsMainTainV1Period period : periods) {
            boolean flag = dateCpmpare(DateUtils.parse( DateUtils.formatDate(new Date()) + " " + period.getStart()), DateUtils.parse(DateUtils.formatDate(new Date()) + " " + period.getEnd()));
            if(flag){return true;}
        }
        return false;
    }

    /**
     * 校验每周维护是否是屏蔽中
     * @param periods
     * @return
     */
    private boolean checkWeekMainTain(MwAssetsMainTainV1Week weekParam,List<MwAssetsMainTainV1Period> periods,Calendar calendar,Date currTime){
        List<Integer> weekIndexes = weekParam.getWeekIndexes();
        //获取今天是周几
        int dayOfWeek = DateUtils.getDayOfWeek(calendar,currTime);
        if(!weekIndexes.contains(dayOfWeek)){return false;}
        for (MwAssetsMainTainV1Period period : periods) {
            boolean flag = dateCpmpare(DateUtils.parse( DateUtils.formatDate(new Date()) + " " + period.getStart()), DateUtils.parse(DateUtils.formatDate(new Date()) + " " + period.getEnd()));
            if(flag){return true;}
        }
        return false;
    }

    /**
     * 校验每月维护是否是屏蔽中
     * @param periods
     * @return
     */
    private boolean checkMonthMainTain( MwAssetsMainTainV1Month monthParam,List<MwAssetsMainTainV1Period> periods,Calendar calendar,Date currTime){
        List<Integer> monthIndexes = monthParam.getMonthIndexes();//维护的月份
        //获取当前月份
        int month = DateUtils.getMonth(currTime);
        if(!monthIndexes.contains(month)){return false;}
        List<Integer> weekIndexPerMonth = monthParam.getWeekIndexPerMonth();//第几周
        List<Integer> weekIndexes = monthParam.getWeekIndexes();//第几周周几
        if(CollectionUtils.isNotEmpty(weekIndexPerMonth) && CollectionUtils.isNotEmpty(weekIndexes)){
            //获取今天是第几周周几
            int weekOfMonth = DateUtils.getWeekOfMonth(calendar,currTime);
            if(!weekIndexPerMonth.contains(weekOfMonth)){return false;}
            int dayOfWeek = DateUtils.getDayOfWeek(calendar,currTime);
            if(!weekIndexes.contains(dayOfWeek)){return false;}
            for (MwAssetsMainTainV1Period period : periods) {
                boolean flag = dateCpmpare(DateUtils.parse( DateUtils.formatDate(new Date()) + " " + period.getStart()), DateUtils.parse(DateUtils.formatDate(new Date()) + " " + period.getEnd()));
                if(flag){return true;}
            }
        }
        List<Integer> dayIndexes = monthParam.getDayIndexes();
        //获取今天是几号
        int dayOfMonth = DateUtils.getDayOfMonth(calendar,currTime);
        if(dayIndexes.contains(dayOfMonth)){
            for (MwAssetsMainTainV1Period period : periods) {
                boolean flag = dateCpmpare(DateUtils.parse( DateUtils.formatDate(new Date()) + " " + period.getStart()), DateUtils.parse(DateUtils.formatDate(new Date()) + " " + period.getEnd()));
                if(flag){return true;}
            }
        }
        return false;
    }


    private boolean dateCpmpare(Date startTime,Date endTime){
        Date curTime = new Date();
        if(curTime.after(startTime) && endTime.after(curTime)){
            return true;
        }
        return false;
    }
}
