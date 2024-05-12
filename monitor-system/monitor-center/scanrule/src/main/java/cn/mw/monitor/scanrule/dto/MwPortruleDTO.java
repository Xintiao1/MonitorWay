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
@ApiModel(value = "端口扫描表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwPortruleDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "协议类型")
    private String protocolType;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "规则ID")
    private Integer ruleId;
}
