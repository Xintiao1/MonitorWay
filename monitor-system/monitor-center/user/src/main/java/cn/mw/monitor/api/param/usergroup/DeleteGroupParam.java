package cn.mw.monitor.api.param.usergroup;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("删除用户组数据")
public class DeleteGroupParam {
    @ApiModelProperty("用户组id集合")
    private List<Integer> groupIds;

}
