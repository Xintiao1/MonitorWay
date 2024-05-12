package cn.mw.monitor.service.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 数字孪生接口监控信息DTO
 * @date 2023/8/3 10:13
 */
@Data
@ApiModel("数字孪生接口监控信息DTO")
public class MwDigitalTwinInterfaceDto {

    @ApiModelProperty("接口名称")
    private String interfaceName;

    @ApiModelProperty("接口IP地址")
    private String interfaceIp;

    @ApiModelProperty("接口MAC地址")
    private String interfaceMac;

    @ApiModelProperty("接口速率")
    private String interfaceSpeed;

    @ApiModelProperty("接口流量(入)")
    private String interfaceInTraffic;

    @ApiModelProperty("接口流量(出)")
    private String interfaceOutTraffic;

    @ApiModelProperty("接口丢包(入)")
    private String interfaceInDropRate;

    @ApiModelProperty("接口丢包(出)")
    private String interfaceOutDropRate;

    @ApiModelProperty("接口错误包(入)")
    private String interfaceInErrors;

    @ApiModelProperty("接口错误包(出)")
    private String interfaceOutErrors;

}
