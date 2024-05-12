package cn.mw.monitor.model.dto;

import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.service.virtual.dto.VirtualizationDataInfo;
import lombok.Data;

@Data
public class VCenterInfo {
    private AddAndUpdateModelInstanceParam instanceParam;
    private VirtualizationDataInfo virtualizationDataInfo;

    public VCenterInfo(AddAndUpdateModelInstanceParam instanceParam ,VirtualizationDataInfo virtualizationDataInfo){
        this.instanceParam = instanceParam;
        this.virtualizationDataInfo = virtualizationDataInfo;
    }
}
