package cn.mw.monitor.customPage.dto;

import cn.mw.monitor.customPage.model.MwCustomcolTable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UpdateCustomColDTO extends MwCustomcolTable {

    @ApiModelProperty(value = "个性化id")
    private Integer customId;

    @ApiModelProperty(value = "字段类型")
    private Integer customFieldType;

}
