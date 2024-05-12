package cn.mw.monitor.wireless.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2021/6/17
 */
@Data
public class WirelessClientDTO {
    @ApiModelProperty("发送流量")
    private String MW_CLIENTS_TXBYTES;
    @ApiModelProperty("MAC地址")
    private String MW_CLIENTS_MACADDR;
    @ApiModelProperty("接收流量")
    private String MW_CLIENTS_RXBYTES;
    @ApiModelProperty("IP地址")
    private String clientsIp;
    @ApiModelProperty("连接通道")
    private String MW_CLIENTS_CHANNELS;
    @ApiModelProperty("信号强度")
    private String MW_CLIENTS_RSSI;
    @ApiModelProperty("Ap端名称")
    private String accessPoint;
    @ApiModelProperty("SSID名称")
    private String clientsSSID;

    //排序使用
    private Double sortMW_CLIENTS_TXBYTES;
    private Double sortMW_CLIENTS_RXBYTES;
    private Double sortMW_CLIENTS_CHANNELS;
    private Double sortMW_CLIENTS_RSSI;

}
