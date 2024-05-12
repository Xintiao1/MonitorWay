package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author baochengbin
 * @date 2020/4/13
 */
@ApiModel(value = "资产SNMPv3数据表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwSnmpAssetsDTO implements ESStructData{
    @ApiModelProperty(value = "资产ID")
    private String assetsId;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "安全名称")
    private String secName;

    @ApiModelProperty(value = "安全级别(0:noauthnopri 1:authnopri 2:authpri)")
    private Integer secLevel;

    private String secLevelName;

    @ApiModelProperty(value = "验证协议(1:MD5 2:SHA)")
    private Integer authAlg;

    private String authAlgName;

    @ApiModelProperty(value = "验证口令")
    private String authValue;

    @ApiModelProperty(value = "隐私协议(1:AES 2:DES)")
    private Integer privAlg;

    private String privAlgName;

    @ApiModelProperty(value = "私钥")
    private String priValue;

    @ApiModelProperty(value = "上下文名称")
    private String contextName;
}
