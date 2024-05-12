package cn.mw.monitor.api.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("删除模块数据")
public class DeleteModuleParam {
    @Valid
    @Size(min = 1, message = "模块id不能为空！")
    @ApiModelProperty("模块id集合")
    private List<Integer> ids;
}
