package cn.mw.monitor.model.proxy.param;

import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import lombok.Data;

import java.util.List;

@Data
public class SuperFusionCheckParam {
    List<MwModelMacrosValInfoParam> connectParam;

    public SuperFusionCheckParam(List<MwModelMacrosValInfoParam> connectParam){
        this.connectParam = connectParam;
    }
}
