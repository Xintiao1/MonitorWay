package cn.mw.monitor.activiti.entiy.OA;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 202303 9:20
 * @description
 */
@Data
@ApiModel(value = "待办发送上下文")
@Accessors(chain = true)
public class NotifyTodoSendContext {
    @ApiModelProperty(value = "待办来源")
    String appName;
    @ApiModelProperty(value = "模块名")
    String modelName;
    @ApiModelProperty(value = "待办唯一标识")
    String modelId;
    @ApiModelProperty(value = "标题")
    String subject;
    @ApiModelProperty(value = "链接")
    String link;
    @ApiModelProperty(value = "移动端链接")
    String mobileLink;
    @ApiModelProperty(value = "pad端链接")
    String padLink;
    @ApiModelProperty(value = "待办类型")
    Integer type;
   /* @ApiModelProperty(value = "流程名称")
    Targets targets;*/
    @ApiModelProperty(value = "创建时间 格式为:yyyy-MM-dd HH:mm:ss")
    String createTime;

}
