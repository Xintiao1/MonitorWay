package cn.mw.monitor.model.dto;

import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class UpdateCustomPageByModelParam {
    @ApiModelProperty(value = "个性化设置参数")
    private List<MwCustomcolByModelTable> models;

    @ApiModelProperty(value = "当前模型id")
    private Integer modelId;

    @ApiModelProperty(value = "当前用户id")
    private Integer userId;

}
