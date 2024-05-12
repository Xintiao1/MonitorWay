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
@ApiModel(value = "规则SNMP数据表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwRulesnmpDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "snmp等级")
    private Integer snmpLev;

    @ApiModelProperty(value = "主机ID")
    private String hostId;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "团体名")
    private String community;

    @ApiModelProperty(value = "规则ID")
    private Integer ruleId;

    @ApiModelProperty(value = "安全名称")
    private String secName;

    @ApiModelProperty(value = "安全级别")
    private String secLevel;

    @ApiModelProperty(value = "验证协议")
    private String authAlg;

    private String authAlgName;

    @ApiModelProperty(value = "验证口令")
    private String authValue;

    @ApiModelProperty(value = "隐私协议")
    private String privAlg;

    private String privAlgName;

    @ApiModelProperty(value = "私钥")
    private String priValue;

    @ApiModelProperty(value = "上下文名称")
    private String contextName;
}
