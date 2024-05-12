package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class HostIfPerformanceInfo {
    @ApiModelProperty("接口名")
    private String ifName;

    @ApiModelProperty("每秒接收流量")
    private String inBps;

    @ApiModelProperty("接收丢包率")
    private String pickDropped;

    @ApiModelProperty("每秒发送流量")
    private String outBps;

    @ApiModelProperty("发送丢包率")
    private String sendDropped;

    @ApiModelProperty("接收利用率")
    private String inUtilization;

    @ApiModelProperty("发送利用率")
    private String outUtilization;
}
