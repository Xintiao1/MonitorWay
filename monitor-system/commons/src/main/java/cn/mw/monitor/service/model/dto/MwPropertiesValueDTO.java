package cn.mw.monitor.service.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/6/06 17:10
 */
@Data
@ApiModel
public class MwPropertiesValueDTO {
    @ApiModelProperty("属性名称")
    private String modelPropertiesName;
    @ApiModelProperty("属性字段名")
    private String modelPropertiesIndexId;
}
