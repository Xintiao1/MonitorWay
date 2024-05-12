package cn.mw.monitor.service.model.dto.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherClusterDTO{
    private String id;
    private String name;
    private String type;
    //创建时间
    private String created;
    private String uuid;
    private String PId;
    private String state;
    //供应商
    private String provider;
    //Kubernetes 版本
    private String gitVersion;

    private String cpu;

    private String memory;

    private String pods;
    //已预留 Cpu
    private String requestedCpu;
    //已预留 Memory
    private String requestedMemory;
    //已使用 Pods
    private String requestedPods;
    public void setCreated(String created){
        created = created.replaceAll("T"," ").replaceAll("Z","");
        this.created = created;
    }
}
