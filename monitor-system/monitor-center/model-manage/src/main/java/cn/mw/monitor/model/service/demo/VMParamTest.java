package cn.mw.monitor.model.service.demo;

import lombok.Data;

@Data
public class VMParamTest {
    private String vm;
    private String name;
    private String pId;
    private String id;
    private String type;
    private String instanceName;
    private String clusterId;
    private String datacenterId;
    public void setVm(String vm) {
        this.vm = vm;
        this.id = vm;
    }
    public void setName(String name) {
        this.name = name;
        this.instanceName = name;
    }
}
