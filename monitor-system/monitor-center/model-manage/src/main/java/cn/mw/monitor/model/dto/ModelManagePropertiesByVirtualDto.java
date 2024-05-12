package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/9/7 9:51
 */
@Data
public class ModelManagePropertiesByVirtualDto {
    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型描述")
    private String modelDesc;

    @ApiModelProperty("模型索引")
    private String modelIndex;

}
