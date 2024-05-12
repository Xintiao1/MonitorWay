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
@ApiModel(value = "IP范围表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwIpRangDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "扫描规则ID")
    private Integer ruleId;

    @ApiModelProperty(value = "IP范围开始")
    private String ipRangStart;

    @ApiModelProperty(value = "IP范围结束")
    private String ipRangEnd;

    @ApiModelProperty(value = "是否ipv6")
    private Boolean ipType;

    private String ranges;
}
