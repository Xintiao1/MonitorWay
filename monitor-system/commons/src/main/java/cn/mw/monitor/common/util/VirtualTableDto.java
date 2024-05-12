package cn.mw.monitor.common.util;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.OrgDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/30 12:01
 * @Version 1.0
 */
@Data
public class VirtualTableDto {
    private String hostId;
    private String hostName;
    private String ipAddress;
    private String pId;
    private Integer monitorServerId;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "0:关闭 1：启用 2：暂停")
    private String status;

    @ApiModelProperty(value = "是否跳转连接 0:不跳转 1：跳转")
    private Integer isConnect;

    @ApiModelProperty(value = "磁盘空间")
    private String diskSpace;
    @ApiModelProperty("磁盘空间")
    private Double sortDiskSpace;

    @ApiModelProperty(value = "磁盘使用率")
    private Double diskUtilization;

    @ApiModelProperty(value = "已用cpu")
    private String cpuUsage;
    @ApiModelProperty("已用cpu")
    private Double sortCpuUsage;

    @ApiModelProperty(value = "已用内存")
    private String memoryUsed;
    @ApiModelProperty("已用内存")
    private Double sortMemoryUsed;

    @ApiModelProperty(value = "安装（版本号） ；未安装")
    private String VMTools;

    private List<OrgDTO> department;

    private List<GroupDTO> group;

}
