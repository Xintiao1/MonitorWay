package cn.mw.monitor.api.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author baochengbin
 * @date 2020/3/31
 */
@Data
@ApiModel(description = "更新用户状态数据")
public class UpdateUserStateParam {
    @ApiModelProperty("用户id")
    @NotNull(message = "用户id不能为空!")
    private Integer id;

    @ApiModelProperty("用户状态参数")
    @NotEmpty(message = "状态参数不能为空！")
    private String enable;
}
