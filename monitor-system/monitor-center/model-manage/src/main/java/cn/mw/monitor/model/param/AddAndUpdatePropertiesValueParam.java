package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2021/2/22 15:11
 */
@Data
@ApiModel
public class AddAndUpdatePropertiesValueParam {
    @ApiModelProperty("模型实例的Id")
    private Integer modelInstanceId;
    @ApiModelProperty("模型的索引")
    private String modelIndex;
    @ApiModelProperty("模型的索引的Id")
    @Size(max=32,message = "最大长度不能超过32")
    private String modelIndexId;
    @ApiModelProperty("模型的Id")
    private Integer modelId;
    @ApiModelProperty("模型属性Id")
    private Integer modelPropertiesId;
    @ApiModelProperty("模型属性的值")
    private String modelPropertiesValue;
    @ApiModelProperty("资产的主键Id")
    private String tangibleId;
}
