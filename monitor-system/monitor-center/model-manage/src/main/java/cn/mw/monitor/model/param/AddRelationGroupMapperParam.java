package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2021/2/8 16:16

 */
@Data
@ApiModel
public class AddRelationGroupMapperParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("模型分组关系Id")
    private Integer relationGroupId;
    @ApiModelProperty("当前模型Id")
    private Integer ownModelId;
    @ApiModelProperty("关联模型id")
    private Integer oppositeModelId;

}
