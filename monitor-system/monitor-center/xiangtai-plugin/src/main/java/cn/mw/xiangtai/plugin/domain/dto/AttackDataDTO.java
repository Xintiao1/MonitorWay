package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("攻击数据")
@Data
public class AttackDataDTO {

    @ApiModelProperty("攻击名")
    private String name;

    @ApiModelProperty("次数")
    private Integer value;
}
