package cn.mw.monitor.model.dto.rancher;

import lombok.Data;

@Data
public class RancherInstance {
    private String id;
    private Integer modelId;
    private String modelIndex;
    private Integer modelInstanceId;
    private String instanceName;
    private String PId;
    private String type;
    private String clusterId;//namespace查询时使用
    private String assetsTypeName;

    private String specifications;
    private String assetsId;
}
