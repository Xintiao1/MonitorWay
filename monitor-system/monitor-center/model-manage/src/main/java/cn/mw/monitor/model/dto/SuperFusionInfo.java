package cn.mw.monitor.model.dto;

import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.superfusion.SuperFusionTreeParam;
import lombok.Data;

@Data
public class SuperFusionInfo {
    private AddAndUpdateModelInstanceParam instanceParam;
    private SuperFusionTreeParam dataInfoDTO;

    public SuperFusionInfo(AddAndUpdateModelInstanceParam instanceParam, SuperFusionTreeParam dataInfoDTO) {
        this.instanceParam = instanceParam;
        this.dataInfoDTO = dataInfoDTO;
    }
}
