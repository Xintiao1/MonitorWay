package cn.mw.monitor.service.model.dto.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherNodesDTO {
    private String id;
    private String name;
    private String type;
    //创建时间
    private String created;
    private String uuid;
    private String PId;
    private String state;
    private String ipAddress;

    private String dockerVersion;
    private String kubeProxyVersion;
    private String cpu;

    private String cpuUnit;

    private String memory;

    private String memoryUnit;

    private String pods;
    //已预留 Cpu
    private String requestedCpu;
    //已预留 Memory
    private String requestedMemory;
    //已使用 Pods
    private String requestedPods;
    //CPU使用率
    private String cpuUtilization;
    //内存使用率
    private String memoryUtilization;
    //pods使用率
    private String podsUtilization;


    public void setCreated(String created){
        created = created.replaceAll("T"," ").replaceAll("Z","");
        this.created = created;
    }
}
