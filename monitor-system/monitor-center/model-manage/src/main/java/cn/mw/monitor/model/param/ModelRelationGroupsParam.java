package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/3/9
 */
@Data
@ApiModel
public class ModelRelationGroupsParam {
    @ApiModelProperty("当前模型Id")
    private Integer ownModelId;
    @ApiModelProperty("关联模型id")
    private Integer oppositeModelId;
    @ApiModelProperty("关系分组名称")
    private String modelGroupName;
    @ApiModelProperty("关系分组Id")
    private String modelGroupId;
}
