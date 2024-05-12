package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/3/15 17:15
 */
@Data
@ApiModel
public class DeleteModelMacrosParam {
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("凭证名称")
    private String authName;

}
