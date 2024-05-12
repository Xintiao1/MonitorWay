package cn.mw.monitor.service.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ModelConnectRelationParam {

    @ApiModelProperty("模型Index")
    private String modelIndex;

    @ApiModelProperty("模型Index")
    private Integer modelId;

    @ApiModelProperty("instanceId")
    private Integer instanceId;

    @ApiModelProperty("属性Index")
    private String propertiesIndex;

    @ApiModelProperty("属性Value")
    private Object propertiesValue;

    @ApiModelProperty("关联模型Index")
    private String relationModelIndex;

    @ApiModelProperty("关联模型Id")
    private Integer relationModelId;

    @ApiModelProperty("关联属性Index")
    private String relationPropertiesIndex;


}
