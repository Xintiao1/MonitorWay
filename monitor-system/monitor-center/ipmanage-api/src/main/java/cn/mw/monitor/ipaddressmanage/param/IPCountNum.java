package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 20229-1616 0:53
 * @description
 */
@Data
public class IPCountNum {
    @ApiModelProperty(value="Ipv4使用")
    private Integer ipState;
    @ApiModelProperty(value="Ipv4未使用")
    private Integer ipUnState;
    @ApiModelProperty(value="Ipv4预留使用")
    private Integer ipReserveState;
    @ApiModelProperty(value="Ipv4分配")
    private Integer distributionStatus;
    @ApiModelProperty(value="Ipv4未分配")
    private Integer distributionUnStatus;
    @ApiModelProperty(value="Ipv4使用")
    private Integer ipv6State;
    @ApiModelProperty(value="Ipv6未使用")
    private Integer ipv6UnState;
    @ApiModelProperty(value="ipv6预留使用")
    private Integer ipv6ReserveState;
    @ApiModelProperty(value="ipv6分配")
    private Integer ipv6DistributionStatus;
    @ApiModelProperty(value="ipv6未分配")
    private Integer ipv6DistributionUnStatus;

    @ApiModelProperty(value="Ipv4纳管")
    private Integer haveManage;
    @ApiModelProperty(value="Ipv4未纳管")
    private Integer unHaveManage;

    @ApiModelProperty(value="Ipv6纳管")
    private Integer ipv6HaveManage;
    @ApiModelProperty(value="Ipv6未纳管")
    private Integer ipv6UnHaveManage;
}
