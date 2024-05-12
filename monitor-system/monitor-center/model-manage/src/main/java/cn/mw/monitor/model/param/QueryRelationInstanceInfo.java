package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/3/31 12:11
 */
@Data
@ApiModel
public class QueryRelationInstanceInfo {
    @ApiModelProperty("模型Index")
    private String modelIndex;
    @ApiModelProperty("属性Index")
    private String propertiesIndex;
    //单前属性字段id
    private String prop;
}
