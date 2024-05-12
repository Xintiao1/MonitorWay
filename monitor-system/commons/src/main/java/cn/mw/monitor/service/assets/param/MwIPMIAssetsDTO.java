package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author syt
 * @Date 2020/5/21 16:25
 * @Version 1.0
 */
@ApiModel(value = "IPMI协议")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwIPMIAssetsDTO {
    @ApiModelProperty(value = "端口号")
    private Integer port;
    @ApiModelProperty(value = "资产ID")
    private String assetsId;
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "密码")
    private String password;
}

