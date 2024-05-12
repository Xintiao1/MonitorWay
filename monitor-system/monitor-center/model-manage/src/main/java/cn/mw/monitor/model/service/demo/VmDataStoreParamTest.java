package cn.mw.monitor.model.service.demo;

import lombok.Data;

@Data
public class VmDataStoreParamTest {
    private String datastore;
    private String name;
    private String pId;
    private String id;
    private String type;
    private String instanceName;

    public void setDatastore(String datastore) {
        this.datastore = datastore;
        this.id = datastore;
    }

    public void setName(String name) {
        this.name = name;
        this.instanceName = name;
    }
}
