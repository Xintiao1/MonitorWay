package cn.mw.monitor.model.dto;

import lombok.Data;

@Data
public class VirtualInstance {
    private String id;
    private Integer modelId;
    private String modelIndex;
    private Integer modelInstanceId;
    private String instanceName;
    private String Pid;
    private String type;
    private String assetsTypeName;

    private String specifications;
    private String assetsId;
    private String clusterId;
    private String datacenterId;
}
