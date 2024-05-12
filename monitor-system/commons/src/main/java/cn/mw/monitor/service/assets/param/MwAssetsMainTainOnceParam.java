package cn.mw.monitor.service.assets.param;

import lombok.Data;

import java.util.Date;

@Data
public class MwAssetsMainTainOnceParam extends MaintainTransform{
    private Date mainDate;

    //持续天数
    private Integer durationDay;

    //持续小时
    private Integer durationHour;

    //持续分钟
    private Integer durationMin;

    @Override
    void tranform(MwAssetsMainTainZabbixParam param) {
        setCommon(param);
        long dataLong = param.getStart_date() * 1000;
        this.mainDate = new Date(dataLong);
    }

    @Override
    void setStartHour(Integer startHour) {

    }

    @Override
    void setStartMin(Integer startMin) {

    }
}
