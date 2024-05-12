package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/23 10:42
 */
@Data
@ApiModel
public class ModelAssetsParam extends BaseParam {
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型ID")
    private String modelId;
    @ApiModelProperty("模型实例ID")
    private String modelInstanceId;
    @ApiModelProperty("关联模型实例ID")
    private String relationInstanceId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
    @ApiModelProperty("模型属性值")
    private String modelPropertiesValue;
}
