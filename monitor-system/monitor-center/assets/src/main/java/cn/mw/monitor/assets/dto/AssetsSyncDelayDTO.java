package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @Version 1.0
 */
@Data
public class AssetsSyncDelayDTO {
    @ApiModelProperty(value="库名")
    private String name;
    @ApiModelProperty(value="副本服务器名称")
    private String replicaServerName;
    @ApiModelProperty(value="可用性模式描述")
    private String availabilityModeDesc;
    @ApiModelProperty(value="当前时间")
    private Date now;
    @ApiModelProperty(value="相差时间")
    private String diffMS;
    @ApiModelProperty(value="角色描述")
    private String roleDesc;
    @ApiModelProperty(value="主服务上次提交时间")
    private String priLastCommitTime;
    @ApiModelProperty(value="副本服务上次提交时间")
    private String secLastCommitTime;

}
