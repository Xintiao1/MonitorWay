package cn.mw.monitor.service.model.dto.rancher;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherDataInfoDTO {
    private String id;
    private String name;
    private String instanceName;
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
    private String clusterId;
    private Integer modelId;
    private Integer modelInstanceId;
    private String modelIndex;
    private String esId;
    private String relationModelSystem;
    private String relationModelClassify;
    private String relationName;
    private String relationIp;
    private List<Integer> groupIds;

    private List<Integer> userIds;

    private List<List<Integer>> orgIds;

    public void setName(String name){
        this.name = name;
        this.instanceName = name;
    }
}
