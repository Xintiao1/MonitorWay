package cn.mw.monitor.api.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("绑定用户数据")
public class BindUserGroupParam {

    // 关联用户id集合
    @ApiModelProperty("关联用户id集合")
    private List<Integer> userIds;
    // 关联用户组id集合
    @ApiModelProperty("关联用户组id集合")
    private List<Integer> groupIds;
    // 关联用户组id
    @ApiModelProperty("关联用户组id")
    private Integer groupId;
    //1:用戶新增 2：用戶修改 3：用戶组新增 4：用户组修改
    @ApiModelProperty("1:用戶新增 2：用戶修改 3：用戶组新增 4：用户组修改")
    private Integer flag;

}
