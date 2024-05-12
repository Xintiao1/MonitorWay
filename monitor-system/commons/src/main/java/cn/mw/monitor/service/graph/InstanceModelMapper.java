package cn.mw.monitor.service.graph;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstanceModelMapper {
    private Integer instanceId;
    private String instanceName;
    private Integer modelId;
}
