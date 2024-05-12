package cn.mw.monitor.report.service.impl;

import cn.joinhealth.zbx.enums.action.EvalTypeEnum;

import java.util.Calendar;
import java.util.Date;

public enum DateTypeEnum {
    SELF_DEFINE(0)
    ,YESTERDAY(1)    //昨天
    ,TODAY(2)       //今天
    ,LATEST_7DAY(3) //最近7天
    ,THIS_WEEK(4)   //本周
    ,LAST_WEEK(5)   //上周
    ,LATEST_30DAY(6)//最近30天
    ,THIS_MONTH(7)  //本月
    ,LAST_MONTH(8)  //上月
    ,LAST_12MONTH(9)//近12个月
    ,THIS_YEAR(10)  //今年
    ,SET_DATE(12)  //自定义
    ;

    private int type;

    DateTypeEnum(int type){
        this.type = type;
    }

    //是否能获取历史数据
    //每周第一天，或每月第一天可以获取数据
    public boolean enableCollectHisData(){
        DateTypeEnum dateTypeEnum = getDateTypeEnum(this.type);
        boolean enable =false;
        switch (dateTypeEnum){
            case YESTERDAY: case LATEST_7DAY: case THIS_WEEK:case LATEST_30DAY:case THIS_MONTH:
                case LAST_12MONTH:case THIS_YEAR:
                enable = true;
                break;
            case LAST_WEEK:
                //每周一才获取上周数据
                //判断当前时间是否是周一
                Calendar cal=Calendar.getInstance();
                cal.setTime(new Date());
                int week = cal.get(Calendar.DAY_OF_WEEK)-1;
                if(1 == week){
                    enable = true;
                }
                break;
            case LAST_MONTH:
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                if(1 == calendar.get(Calendar.DAY_OF_MONTH)){
                    enable = true;
                }
            default:
        }
        return enable;
    }

    //按指定时间间隔获取数据
    public boolean enableByLastDate(Date lastDate){
        DateTypeEnum dateTypeEnum = getDateTypeEnum(this.type);
        Date today = new Date();
        int days = (int) ((today.getTime() - lastDate.getTime()) / (1000*3600*24));

        Calendar bef = Calendar.getInstance();
        bef.setTime(lastDate);

        Calendar aft = Calendar.getInstance();
        aft.setTime(today);
        int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
        int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR))*12;
        int betweenMonth = month+result;

        boolean enable =false;
        switch (dateTypeEnum){
            case YESTERDAY:
                enable = true;
                break;
            case LAST_WEEK:
            case LATEST_7DAY:
                if(days > 7 ){
                    enable = true;
                    break;
                }
            case LATEST_30DAY:
                if(days > 30 ){
                    enable = true;
                    break;
                }
            case LAST_12MONTH:
                if(betweenMonth > 12){
                    enable = true;
                    break;
                }
            case LAST_MONTH:
                if(betweenMonth >= 1){
                    enable = true;
                    break;
                }
            default:

        }
        return enable;
    }

    public int getType() {
        return type;
    }

    public static DateTypeEnum getDateTypeEnum(int type){
        for (DateTypeEnum dateTypeEnum : DateTypeEnum.values()){
            if(dateTypeEnum.getType() == type){
                return dateTypeEnum;
            }
        }
        DateTypeEnum dateTypeEnum = DateTypeEnum.getDateTypeEnum(12);

        return dateTypeEnum;
    }
}
