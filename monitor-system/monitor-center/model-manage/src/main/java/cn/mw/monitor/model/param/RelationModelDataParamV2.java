package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/8 12:11
 */
@Data
@ApiModel
public class RelationModelDataParamV2 {
    @ApiModelProperty("模型id")
    private Long modelId;
    @ApiModelProperty("是否关联模型关系")
    private Boolean isRelation;
}
