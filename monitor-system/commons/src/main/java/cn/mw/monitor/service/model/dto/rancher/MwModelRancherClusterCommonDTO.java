package cn.mw.monitor.service.model.dto.rancher;

import cn.mwpaas.common.utils.CollectionUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherClusterCommonDTO {
    private String id;
    @ApiModelProperty("集群名称")
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

    @ApiModelProperty("总CPU")
    private String cpu;

    @ApiModelProperty("CPU单位")
    private String cpuUnit;

    @ApiModelProperty("总内存")
    private String memory;

    @ApiModelProperty("内存单位")
    private String memoryUnit;

    @ApiModelProperty("总pods")
    private String pods;
    //已预留 Cpu
    @ApiModelProperty("已使用CPU")
    private String requestedCpu;
    //已预留 Memory
    @ApiModelProperty("已使用内存")
    private String requestedMemory;
    //已使用 Pods
    @ApiModelProperty("已使用PODS")
    private String requestedPods;
    //CPU使用率
    @ApiModelProperty("CPU已使用")
    private String cpuUtilization;
    //内存使用率
    @ApiModelProperty("内存已使用")
    private String memoryUtilization;
    //pods使用率
    @ApiModelProperty("pods已使用")
    private String podsUtilization;
    public void setCreated(String created){
        created = created.replaceAll("T"," ").replaceAll("Z","");
        this.created = created;
    }

    @ApiModelProperty("集群下项目数据")
    private List<MwModelRancherProjectCommonDTO> projectList;

    @ApiModelProperty("集群详情数据")
    private List<MwModelRancherNodesDTO> nodeList;

    @ApiModelProperty("集群节点CPU排行")
    private List<MwModelRancherNodesRankingDTO> nodesCpus;

    @ApiModelProperty("集群节点内存排行")
    private List<MwModelRancherNodesRankingDTO> nodesMemorys;
}
