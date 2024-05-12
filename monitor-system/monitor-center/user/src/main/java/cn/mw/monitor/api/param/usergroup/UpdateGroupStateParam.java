package cn.mw.monitor.api.param.usergroup;

import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("更新用户组状态")
public class UpdateGroupStateParam {

    @ApiModelProperty("用户组id")
    @NotNull(message = "用户组id不能为空")
    private Integer groupId;

    @ApiModelProperty("用户组状态参数")
    @NotEmpty(message = "状态参数不能为空")
    private String enable;

}
