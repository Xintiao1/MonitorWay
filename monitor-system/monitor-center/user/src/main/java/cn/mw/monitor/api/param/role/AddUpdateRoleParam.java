package cn.mw.monitor.api.param.role;

import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import cn.mw.monitor.service.user.model.PageAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("新增或更新角色数据")
@GroupSequence({Insert.class,Update.class, AddUpdateRoleParam.class})
public class AddUpdateRoleParam {

    // 角色ID
    @ApiModelProperty("角色id")
    @Null(message = "新增角色时角色id必须为空!",groups = {Insert.class})
    @NotNull(message = "修改角色时角色id不能为空!",groups = {Update.class})
    private Integer roleId;
    // 角色名称
    @ApiModelProperty("角色名称")
    @NotEmpty(message = "角色名称不能为空!",groups = {Insert.class,Update.class})
    @Size(max = 20,message = "角色名称不能超过20字符!",groups = {Insert.class,Update.class})
    private String roleName;
    // 角色描述
    @ApiModelProperty("角色描述")
    @Size(max = 100,message = "角色描述不能超过100字符!",groups = {Insert.class,Update.class})
    private String roleDesc;
    // 数据权限
    @ApiModelProperty("数据权限")
    private String dataPerm;
    // 功能权限集合
    @ApiModelProperty("功能权限集合")
    private List<PageAuth> pageAuth;

    /**
     * 是否允许登录；0：不允许登录，1：允许登录
     */
    @ApiModelProperty("是否允许登录")
    private Integer allowLogin;
}
