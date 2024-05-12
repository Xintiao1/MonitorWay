package cn.mw.monitor.activiti.entiy.OA;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lumingming
 * @createTime 202303 9:29
 * @description
 */
@Data
@ApiModel(value = "待办上下文")
@Accessors(chain = true)
public class NotifyTodoRemoveContext {
    @ApiModelProperty(value = "待办来源")
    String appName;
    @ApiModelProperty(value = "模块名")
    String modelName;
    @ApiModelProperty(value = "待办唯一标识")
    String modelId;
    @ApiModelProperty(value = "操作类型\t")
    Integer optType;
}
