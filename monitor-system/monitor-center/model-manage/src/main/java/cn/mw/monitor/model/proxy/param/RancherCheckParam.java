package cn.mw.monitor.model.proxy.param;

import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import lombok.Data;

import java.util.List;

@Data
public class RancherCheckParam {
    List<MwModelMacrosValInfoParam> connectParam;

    public RancherCheckParam(List<MwModelMacrosValInfoParam> connectParam){
        this.connectParam = connectParam;
    }
}
