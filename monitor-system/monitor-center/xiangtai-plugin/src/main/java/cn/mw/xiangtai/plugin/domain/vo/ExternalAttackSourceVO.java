package cn.mw.xiangtai.plugin.domain.vo;

import cn.mw.xiangtai.plugin.domain.dto.AttackSourceDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("外部攻击源")
public class ExternalAttackSourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("外部攻击源总数")
    private Long exAttackSourceCount;

    @ApiModelProperty("外部攻击源对象")
    private List<AttackSourceDTO> attackSourceList;

}
