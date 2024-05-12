package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("流程管理关联模块下拉框")
public class ModuleDropDownParam {
    @ApiModelProperty("节点id")
    private String nodeId;

    @ApiModelProperty("下级节点查询协议")
    private String nodeProtocol;
}
