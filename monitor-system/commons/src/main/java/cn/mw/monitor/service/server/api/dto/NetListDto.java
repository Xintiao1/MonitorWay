package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author xhy
 * @date 2020/4/29 9:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NetListDto {

    @ApiModelProperty("接口id")
    private Integer id;

    @ApiModelProperty("接口名称")
    private String interfaceName;

    @ApiModelProperty("状态")
    private String state;

    @ApiModelProperty("每秒接收流量")
    private String inBps;

    @ApiModelProperty("每秒接收流量")
    private Double sortInBps;

    @ApiModelProperty("每秒发送流量")
    private String outBps;

    @ApiModelProperty("每秒发送流量")
    private Double sortOutBps;

    @ApiModelProperty("每秒发送丢包")
    private String sendLoss;

    @ApiModelProperty("每秒发送丢包")
    private Double sortSendLoss;

    @ApiModelProperty("每秒接收丢包")
    private String acceptLoss;

    @ApiModelProperty("每秒接收丢包")
    private Double sortAcceptLoss;

    @ApiModelProperty("每秒发送流量%")
    private String inBpsRatio;

    @ApiModelProperty("每秒发送流量%")
    private Double sortInBpsRatio;

    @ApiModelProperty("每秒接收流量%")
    private String outBpsRatio;

    @ApiModelProperty("每秒接收流量%")
    private Double sortOutBpsRatio;

    @ApiModelProperty("接口描述")
    private String interfaceDescr;

    @ApiModelProperty("接口速率")
    private String rate;

    @ApiModelProperty("接口速率数值")
    private Double sortRate;

    private String macaddr;
    private Integer MTU;
    private Integer interfaceIndex;


    private String hostId;
    private String hostIp;
    private String assetsId;

    //是否告警
    private Boolean alertTag;

}
