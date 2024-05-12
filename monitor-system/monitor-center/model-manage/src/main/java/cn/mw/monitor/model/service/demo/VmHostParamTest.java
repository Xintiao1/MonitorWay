package cn.mw.monitor.model.service.demo;

import lombok.Data;

@Data
public class VmHostParamTest {
    private String host;
    private String name;
    private String pId;
    private String id;
    private String type;
    private String instanceName;
    public void setHost(String host) {
        this.host = host;
        this.id = host;
    }
    public void setName(String name) {
        this.name = name;
        this.instanceName = name;
    }
}
