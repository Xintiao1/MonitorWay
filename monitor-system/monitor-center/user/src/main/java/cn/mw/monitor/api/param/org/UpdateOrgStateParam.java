package cn.mw.monitor.api.param.org;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("更新机构状态数据")
public class UpdateOrgStateParam {

    @ApiModelProperty("机构id")
    @NotNull(message = "机构Id不能为空！")
    private Integer orgId;

    @ApiModelProperty("机构状态参数")
    @NotEmpty(message = "状态参数不能为空！")
    private String enable;

}
