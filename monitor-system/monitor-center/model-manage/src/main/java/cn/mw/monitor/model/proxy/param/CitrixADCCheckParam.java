package cn.mw.monitor.model.proxy.param;

import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import lombok.Data;

import java.util.List;

@Data
public class CitrixADCCheckParam {
    private List<MwModelMacrosValInfoParam> connectParam;

    public CitrixADCCheckParam(List<MwModelMacrosValInfoParam> connectParam){
        this.connectParam = connectParam;
    }
}
