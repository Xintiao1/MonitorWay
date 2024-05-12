package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import lombok.Data;

@Data
public class RancherInfo {
    private AddAndUpdateModelInstanceParam instanceParam;
    private MwModelRancherDataInfoDTO rancherDataInfoDTO;

    public RancherInfo(AddAndUpdateModelInstanceParam instanceParam , MwModelRancherDataInfoDTO rancherDataInfoDTO){
        this.instanceParam = instanceParam;
        this.rancherDataInfoDTO = rancherDataInfoDTO;
    }
}
