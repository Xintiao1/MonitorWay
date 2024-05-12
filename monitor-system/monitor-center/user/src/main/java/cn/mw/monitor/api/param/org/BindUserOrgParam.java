package cn.mw.monitor.api.param.org;

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
public class BindUserOrgParam {

    // 关联用户id集合
    @ApiModelProperty("关联用户id集合")
    private List<Integer> userIds;
    // 关联结构id集合
    @ApiModelProperty("关联机构id集合")
    private List<Integer> orgIds;
    // 关联机构id
    @ApiModelProperty("关联机构id")
    private  Integer orgId;
    //1:用戶新增 2：用戶修改 3：机构新增 4：机构修改
    @ApiModelProperty("1:用戶新增 2：用戶修改 3：机构新增 4：机构修改")
    private Integer flag;

}
