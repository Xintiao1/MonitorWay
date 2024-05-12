package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("攻击源对象")
public class AttackSourceDTO {

    /**
     * 攻击源Ip
     */
    @ApiModelProperty("攻击源地址")
    private String ip;

    /**
     * 国家
     */
    @ApiModelProperty("攻击源国家")
    private String country;

    /**
     * 本次攻击的值
     */
    @ApiModelProperty("攻击数量")
    private Integer value;
}
