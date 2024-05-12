package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/22 17:10
 */
@Data
@ApiModel
public class ModelAssetsPropertiesValueDto {
    @ApiModelProperty("模型ID")
    private String modelId;
    @ApiModelProperty("模型索引INDEX")
    private String modelIndex;
    @ApiModelProperty("模型索引INDEX的Id")
    private String modelIndexId;
    @ApiModelProperty("属性名称")
    private String modelName;
    @ApiModelProperty("属性类型")
    private String modelPropertiesTypeId;
    @ApiModelProperty("属性名称")
    private String modelPropertiesName;
    @ApiModelProperty("属性值")
    private String modelPropertiesValue;
}
