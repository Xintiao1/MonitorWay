package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/25 10:31
 */
@Data
@ApiModel
public class AddModelInstanceRelationsParam {
    @ApiModelProperty("实例id")
    private Integer instanceRelationsId;
    @ApiModelProperty("左实例id")
    private Integer leftInstanceId;
    @ApiModelProperty("右实例id")
    private Integer rightInstanceId;
    @ApiModelProperty("右模型id")
    private Integer leftModelId;
    @ApiModelProperty("右模型id")
    private Integer rightModelId;

    @ApiModelProperty("图谱id")
    private Integer chartId;


    @ApiModelProperty("模型实例和模型实例之间的关系类型  1关联关系incidence  2父子关系 membership ")
    private String type;
}
