package cn.mw.monitor.logManage.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "字典详细信息")
public class DictionaryDetailDTO {

    @ApiModelProperty(value = "源值")
    private String srcValue;

    @ApiModelProperty(value = "映射值")
    private String mappingValue;
}
