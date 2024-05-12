package cn.mw.monitor.service.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 模型属性结构体
 *
 * @author qzg
 * @date 2021/10/11
 */
@Data
public class ModelPropertiesStructDto {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("结构项名称")
    private String structName;
    @ApiModelProperty("结构项Id")
    private String structId;
    @ApiModelProperty("结构项类型")
    private Integer structType;
    @ApiModelProperty("结构项值")
    private String structStrValue;
    @ApiModelProperty("结构项值数组")
    private List<String> structListValue;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型属性Id")
    private String propertiesIndexId;

}
