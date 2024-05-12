package cn.mw.monitor.virtualization.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/30 11:16
 * @Version 1.0
 */
@Data
public class HostTableDto{
    private String hostId;
    private String hostName;
    private String ipAddress;
    @ApiModelProperty(value = "'0':启用 '1'：未启用")
    private String status;
//    群集
    private String cluster;
    private Double cpuUtilization;
    private Double memoryUtilization;

    private String duration;
    @ApiModelProperty("正常运行时间")
    private Double sortDuration;

    private String model;
    private String vendor;
    private Double sortMemoryTotal;
    private Double sortMemoryUsed;
    private String memoryTotal;
    private String memoryUsed;
    private Integer monitorServerId;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

}
