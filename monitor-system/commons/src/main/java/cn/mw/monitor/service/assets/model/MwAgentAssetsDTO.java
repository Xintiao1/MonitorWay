package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author baochengbin
 * @date 2020/4/13
 */
@ApiModel(value = "AGENT规则")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwAgentAssetsDTO implements ESStructData{
    @ApiModelProperty(value = "端口号")
    private Integer port;
    @ApiModelProperty(value = "资产ID")
    private String assetsId;
}
