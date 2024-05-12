package cn.mw.monitor.ipaddressmanage.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * bkc
 */
@ApiModel(value = "IP地址段")
@Data
public class MwIpAddresses1DTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "ip地址管理表主键")
    private Integer linkId;

    // 10.10.10.0/24
    @ApiModelProperty(value = "IP地址段")
    private String ipAddresses;

    //  10.10.10.0
    private String ip;

    //  24
    private String cidr;

    @ApiModelProperty(value = "是否ipv6")
    private Boolean ipType;
}
