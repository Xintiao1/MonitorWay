package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author baochengbin
 * @date 2020/4/13
 */
@ApiModel(value = "端口扫描表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwPortAssetsDTO implements ESStructData{
    @ApiModelProperty(value = "协议类型")
    private String protocolType;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "资产ID")
    private String assetsId;
}
