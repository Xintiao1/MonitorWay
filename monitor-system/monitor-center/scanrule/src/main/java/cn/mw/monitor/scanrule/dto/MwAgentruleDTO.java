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
@ApiModel(value = "AGENT规则")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwAgentruleDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "端口号")
    private Integer port;
    @ApiModelProperty(value = "规则ID")
    private Integer ruleId;
}
