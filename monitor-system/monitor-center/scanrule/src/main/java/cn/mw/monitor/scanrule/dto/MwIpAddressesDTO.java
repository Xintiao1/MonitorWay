package cn.mw.monitor.scanrule.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baochengbin
 * @date 2020/4/13
 */
@ApiModel(value = "IP地址段")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwIpAddressesDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "扫描规则ID")
    private Integer ruleId;

    @ApiModelProperty(value = "IP地址段")
    private String ipAddresses;

    @ApiModelProperty(value = "是否ipv6")
    private Boolean ipType;
}
