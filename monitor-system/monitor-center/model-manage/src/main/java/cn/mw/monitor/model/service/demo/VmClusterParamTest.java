package cn.mw.monitor.model.service.demo;

import lombok.Data;

@Data
public class VmClusterParamTest {
    private String cluster;
    private String name;
    private String pId;
    private String id;
    private String type;
    private String instanceName;

    public void setCluster(String cluster) {
        this.cluster = cluster;
        this.id = cluster;
    }
    public void setName(String name) {
        this.name = name;
        this.instanceName = name;
    }
}
