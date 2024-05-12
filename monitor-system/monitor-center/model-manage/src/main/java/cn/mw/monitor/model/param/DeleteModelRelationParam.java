package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 12:00
 */
@Data
@ApiModel
public class DeleteModelRelationParam {
    @ApiModelProperty("模型关系主键id")
    private Integer id;
    @ApiModelProperty("批量删除模型关系ids")
    private List<Integer> ids;
    @ApiModelProperty("是否批量删除")
    private Boolean isBathDelete;
}
