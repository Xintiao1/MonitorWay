package cn.mw.monitor.api.param.passwdPlan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("更新密码策略状态数据")
public class UpdatePasswdPlanStateParam {

    @ApiModelProperty("密码策略id")
    @NotNull(message = "密码策略id不能为空!")
    private Integer passwdId;

    @ApiModelProperty("密码策略状态信息")
    @NotEmpty(message = "状态信息不能为空!")
    private String passwdState;

    @ApiModelProperty("修改人")
    private String modifier;

}
