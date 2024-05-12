package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("攻击类型返回对象")
public class AttackTypeDTO {

    @ApiModelProperty("攻击类型")
    private String type;

    @ApiModelProperty("攻击类型总数")
    private Integer value;
}
