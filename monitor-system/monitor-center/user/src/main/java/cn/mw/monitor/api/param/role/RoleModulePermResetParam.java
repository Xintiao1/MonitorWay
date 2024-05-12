package cn.mw.monitor.api.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("获取或重置角色模块权限数据")
public class RoleModulePermResetParam {
    @ApiModelProperty("角色id")
    private Integer roleId;

}
