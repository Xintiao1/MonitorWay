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
@ApiModel(value = "规则SNMPV1数据表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwRulesnmpv1DTO {
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "主机ID")
    private String hostId;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "规则ID")
    private Integer ruleId;

    @ApiModelProperty(value = "团体名")
    private String community;

}
