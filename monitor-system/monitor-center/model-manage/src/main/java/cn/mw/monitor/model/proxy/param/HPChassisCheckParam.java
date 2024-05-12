package cn.mw.monitor.model.proxy.param;

import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import lombok.Data;

import java.util.List;

@Data
public class HPChassisCheckParam {
    List<MwModelMacrosValInfoParam> connectParam;

    public HPChassisCheckParam(List<MwModelMacrosValInfoParam> connectParam){
        this.connectParam = connectParam;
    }
}
