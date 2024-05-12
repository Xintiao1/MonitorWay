package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/3/3 11:04
 */
@Data
@ApiModel
public class QueryModelGroupParam {

    @ApiModelProperty("父节点id")
    private Integer modelGroupId;
    @ApiModelProperty("是否子节点：true，false")
    private Boolean isChild;
}
