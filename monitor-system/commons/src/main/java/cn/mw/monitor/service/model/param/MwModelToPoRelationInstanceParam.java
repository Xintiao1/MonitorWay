package cn.mw.monitor.service.model.param;

import lombok.Data;

@Data
public class MwModelToPoRelationInstanceParam {
    private Integer ownModelId;
    private Integer ownInstanceId;
    private String ownModelIndex;
    private Integer relationModelId;
    private Integer relationInstanceId;
}
