package cn.mw.monitor.api.param.role;

import cn.mw.monitor.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("修改角色状态数据")
public class UpdateRoleStateParam {
    @ApiModelProperty("角色id")
    @NotNull(message = "角色id不能为空!")
    private Integer roleId;

    @ApiModelProperty("角色状态信息")
    @NotEmpty(message = "状态信息不能为空!")
    private String enable;

    @ApiModelProperty("修改人")
    private String modifier;

}
