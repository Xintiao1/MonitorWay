package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.Date;

@Data
public class MwAssetsMainTainDayParam extends MaintainTransform{

    //每天执行频率
    private Integer freq;

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
    }
}
