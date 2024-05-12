package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 触发器阈值信息
 * @author qzg
 * @date 2023/5/15 17:15
 */
@Data
@ApiModel
public class QueryModelTriggerInfoParam {
    @ApiModelProperty("触发器名称")
    private String triggerName;
    @ApiModelProperty("告警阈值")
    private String expressionParameter;
    @ApiModelProperty("恢复阈值")
    private String recoveryParameter;
    @ApiModelProperty("触发器状态")
    private String triggerStatus;
    @ApiModelProperty("告警等级")
    private String triggerLevel;
}
