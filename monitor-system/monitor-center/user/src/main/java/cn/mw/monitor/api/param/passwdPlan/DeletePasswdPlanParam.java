package cn.mw.monitor.api.param.passwdPlan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("删除密码策略数据")
public class DeletePasswdPlanParam {

    @ApiModelProperty("密码策略id集合")
    private List<Integer> passwdPlanIds;

}
