package cn.mw.monitor.model.param;

import lombok.Data;

@Data
public class UpdateRelationIdParam {
    //实例Id
    private Integer instanceId;
    //关联实例Id
    private Integer relationInstanceId;

    private String instanceName;
}
