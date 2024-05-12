package cn.mw.monitor.api.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "解锁用户数据")
public class UnlockParam {
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("登录名")
    private String loginName;
}
