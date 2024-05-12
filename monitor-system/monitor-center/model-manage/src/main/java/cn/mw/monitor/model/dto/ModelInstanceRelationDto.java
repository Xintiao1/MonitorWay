package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/26 16:41
 */
@Data
@ApiModel
public class ModelInstanceRelationDto {
    @ApiModelProperty("实例关系id")
    private Integer instanceRelationsId;
    @ApiModelProperty("左实例id")
    private Integer leftInstanceId;
    @ApiModelProperty("右实例id")
    private Integer rightInstanceId;
    @ApiModelProperty("左实例名称")
    private String  leftInstanceName;
    @ApiModelProperty("右实例名称")
    private String  rightInstanceName;
    @ApiModelProperty("关联类型")
    private String type;
}
