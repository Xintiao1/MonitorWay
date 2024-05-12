package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 攻击类型编码映射DTO
 * @date 2023/10/19 9:51
 */
@Data
@ApiModel("攻击类型编码映射DTO")
public class AttackTypeCodeMappingDto {

    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("攻击类型编码")
    private String attackCode;

    @ApiModelProperty("攻击类型名称")
    private String attackName;
}
