package cn.mw.monitor.hybridclouds.dto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @Date 2021/6/6
 */
@Data
public class VirtualBaseInfoDto {
    @ApiModelProperty(value = "IOPS读取")
    private String IOPSRead;
    @ApiModelProperty(value = "IOPS写入")
    private String IOPSWrite;
    @ApiModelProperty(value = "内网带宽")
    private String  IntranetBandwidth;
    @ApiModelProperty(value = "外网带宽")
    private String InternetBandwidth;
    @ApiModelProperty(value = "BPS读取")
    private String BPSRead;
    @ApiModelProperty(value = "BPS写入")
    private String BPSWrite;
    @ApiModelProperty(value = "内网发送速率")
    private String IntranetTX;
    @ApiModelProperty(value = "内网接收速率")
    private String  IntranetRX;
    @ApiModelProperty(value = "外网发送速率")
    private String InternetTX;
    @ApiModelProperty(value = "外网接收速率")
    private String  InternetRX;
}
