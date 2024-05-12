package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 18:02
 * 模型属性
 *
 */
@Data
@ApiModel
public class ModelPropertiesSortParam {

    @ApiModelProperty("主键id")
    private Integer propertiesId;

    private String indexId;

    @ApiModelProperty("模型id")
    private Integer modelId;

    @ApiModelProperty("属性字段排序")
    private Integer sort;

}
