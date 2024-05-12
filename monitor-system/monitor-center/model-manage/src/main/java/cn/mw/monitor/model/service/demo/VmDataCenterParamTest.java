package cn.mw.monitor.model.service.demo;

import lombok.Data;

@Data
public class VmDataCenterParamTest {
    private String datacenter;
    private String name;
    private String instanceName;
    private String pId;
    private String id;
    private String type;

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
        this.id = datacenter;
    }
    public void setName(String name) {
        this.name = name;
        this.instanceName = name;
    }
}
