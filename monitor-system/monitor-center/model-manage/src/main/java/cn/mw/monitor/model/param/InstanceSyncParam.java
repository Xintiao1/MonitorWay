package cn.mw.monitor.model.param;

import lombok.Data;

@Data
public class InstanceSyncParam {
    private Integer serverId;
    private Integer instanceId;
    private String modelId;
    private String modelIndex;
}
