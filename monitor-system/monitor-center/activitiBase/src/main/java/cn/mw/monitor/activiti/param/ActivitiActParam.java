package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ActivitiActParam {
    @ApiModelProperty(value = "后端参数：流程id")
    private String processIds;
    @ApiModelProperty(value = "后端参数：流程激活：false.激活 ture.关闭")
    private boolean activitiSign;
}
