package cn.mw.monitor.assets.dto;

import cn.mw.monitor.service.assets.param.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MainTainInfo {
    private List<MWMainTainHostParam> hostInfo;

    //"天维护周期"
    private MwAssetsMainTainDayParam mainTainDayParam;

    //"周维护周期"
    private MwAssetsMainTainWeekParam mainTainWeekParam;

    //"月维护周期"
    private MwAssetsMainTainMonthParam mainTainMonthParam;

    public void setMaintainTransform(MaintainTransform maintainTransform){
        if(maintainTransform instanceof MwAssetsMainTainDayParam){
            this.mainTainDayParam = (MwAssetsMainTainDayParam)maintainTransform;
        }

        if(maintainTransform instanceof MwAssetsMainTainWeekParam){
            this.mainTainWeekParam = (MwAssetsMainTainWeekParam)mainTainWeekParam;
        }

        if(maintainTransform instanceof MwAssetsMainTainMonthParam){
            this.mainTainMonthParam = (MwAssetsMainTainMonthParam)mainTainMonthParam;
        }
    }
}
