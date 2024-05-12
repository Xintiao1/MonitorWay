package cn.mw.monitor.hybridclouds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

;

/**
 * @author qzg
 * @Date 2021/6/6
 */
@Data
public class VirtualTableDto {
    private String hostId;
    private String hostName;
    @ApiModelProperty(value = "vpc私有地址")
    private VpcAttributes vpcAttributes;

    @ApiModelProperty(value = "vpc地址")
    private String vpcIp;

    @ApiModelProperty(value = "状态 Running:启用 其他：未启用")
    private String status;

    @ApiModelProperty(value = "内存")
    private String memory;

    @ApiModelProperty(value = "操作系统类型")
    private String oSType;

    @ApiModelProperty(value = "操作系统名称")
    private String oSName;

    @ApiModelProperty("区域")
    private String regionId;

    @ApiModelProperty(value = "开始时间")
    private String StartTime;

    @ApiModelProperty(value = "过期时间")
    private String expiredTime;

    @ApiModelProperty("网络连接类型")
    private String instanceNetworkType;

}
