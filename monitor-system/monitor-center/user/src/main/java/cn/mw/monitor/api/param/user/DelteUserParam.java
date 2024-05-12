package cn.mw.monitor.api.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "删除用户数据")
public class DelteUserParam {
    @ApiModelProperty("用户id集合")
    private List<Integer> userIdList;

}
