package cn.mw.monitor.scanrule.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MwIcmpruleDTO
 * @Author gengjb
 * @Date 2022/6/21 15:21
 * @Version 1.0
 **/
@ApiModel(value = "ICMP扫描表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwIcmpruleDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "规则ID")
    private Integer ruleId;
}
