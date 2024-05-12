package cn.mw.monitor.logManage.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "模型明细")
public class ModelDetailDTO {
    @ApiModelProperty(value = "模型字段名")
    private String fieldName;

    @ApiModelProperty(value = "模型字段类型")
    private String fieldType;

}
