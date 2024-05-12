package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MwAssetsMainTainWeekParam extends MaintainTransform{
    //每天执行频率
    private Integer freq;

    //星期几
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
        this.freq = param.getEvery();

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

    }
}
