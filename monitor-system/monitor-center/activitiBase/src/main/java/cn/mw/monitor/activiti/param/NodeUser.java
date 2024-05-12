package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "流程节点对应用户")
public class NodeUser {
    @ApiModelProperty(value = "节点名称（流程名称）")
    private String name;
    @ApiModelProperty(value = "选择用户范围方式 0.")
    private int type;
    @ApiModelProperty(value = "节点名称（流程名称）")
    private String targetId;
}
