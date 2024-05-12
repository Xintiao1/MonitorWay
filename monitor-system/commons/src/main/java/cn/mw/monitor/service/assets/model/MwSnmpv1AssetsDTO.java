package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author baochengbin
 * @date 2020/4/13
 */
@ApiModel(value = "资产SNMPV12数据表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwSnmpv1AssetsDTO implements ESStructData{
    @ApiModelProperty(value = "资产ID")
    private String assetsId;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "团体名")
    private String community;

    @ApiModelProperty(value = "snmp版本")
    private Integer snmpVersion;
}
