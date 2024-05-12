package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class MwAssetsMainTainMonthParam extends MaintainTransform{

    //几月, 1代表1月, 2代表2月
    private List<Integer> monthIndex;


    //每月第几日
    private Integer dayPerMonth;

    //每月第几个星期
    private Integer weekIndexPerMonth;

    //每周几,1代表周一,以此类推
    private List<Integer> weekIndex;

    //每天起始时间
    //每天起始时间
    private Integer startHour;
    private Integer startMin;

    //持续天数
    private Integer durationDay;

    //持续小时
    private Integer durationHour;

    //持续分钟
    private Integer durationMin;

    @Override
    public void tranform(MwAssetsMainTainZabbixParam param) {
        setCommon(param);
        this.dayPerMonth = param.getDay();
        if(this.dayPerMonth == 0){
            this.weekIndexPerMonth = param.getEvery();
        }

        int dayofweek = param.getDayofweek();
        if(dayofweek > 0){
            weekIndex = new ArrayList<>();
            for(int i=0;i<dayOfWeeks.length;i++){
                int value = dayOfWeeks[i];
                int data = (byte) value & (byte) dayofweek;
                if(data > 0){
                    weekIndex.add(i+ 1);
                }
            }
        }

        int month = param.getMonth();
        if(month > 0){
            monthIndex = new ArrayList<>();
            byte[] value = intToBytes(month);
            for(int i=0 ;i<months.length; i++){
                int data = months[i];
                byte[] dataByte = intToBytes(data);
                int[] result = new int[2];
                result[0] = value[0] & dataByte[0];
                result[1] = value[1] & dataByte[1];

                if(result[0] > 0 || result[1] > 0){
                    monthIndex.add(i + 1);
                }
            }
        }

    }
}
