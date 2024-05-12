package cn.mw.monitor.api.param.usergroup;

import cn.mw.monitor.api.param.org.AddUpdateOrgParam;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
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
@ApiModel("添加或更新用户组数据")
@GroupSequence({Insert.class,Update.class, AddUpdateGroupParam.class})
public class AddUpdateGroupParam {

    // 用户组id
    @ApiModelProperty("用户组id")
    @Null(message = "新增用户组时用户组id必须为空!",groups = {Insert.class})
    @NotNull(message = "修改用户组时用户组id不能为空!",groups = {Update.class})
    private Integer groupId;
    // 用户组名称
    @ApiModelProperty("用户组名称")
    @NotEmpty(message = "用户组名称不能为空！",groups = {Insert.class, Update.class})
    @Size(max = 20,message = "用户组名称最大长度不能超过20字符!",groups = {Insert.class,Update.class})
    private String groupName;
    // 用户组关联用户
    @ApiModelProperty("用户组关联用户")
    private List<Integer> userIds;

}
