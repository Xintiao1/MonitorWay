package cn.mw.monitor.customPage.api.param;

import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class UpdateCustomPageParam {
    @ApiModelProperty(value = "个性化设置参数")
    private List<UpdateCustomColDTO> models;

    @ApiModelProperty(value = "当前页面id")
    private Integer pageId;

    @ApiModelProperty(value = "当前用户id")
    private Integer userId;

}
