package cn.mw.monitor.api.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("删除角色数据")
public class DeleteRoleParam {
    @ApiModelProperty("角色id集合")
    private List<Integer> roleIdList;

}
