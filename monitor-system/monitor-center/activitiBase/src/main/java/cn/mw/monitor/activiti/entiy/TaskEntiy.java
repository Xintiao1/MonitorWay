package cn.mw.monitor.activiti.entiy;

import cn.mw.monitor.common.bean.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author lumingming
 * @createTime 04 17:05
 * @description
 */
@Data
@ApiModel(value = "代办任务")
public class TaskEntiy extends BaseDTO {
    @ApiModelProperty(value = "显示参数/查询参数:关联流程")
    String acctivitiName;
    @ApiModelProperty(value = "显示参数/查询参数:名称")
    String submitBandModelName;
    @ApiModelProperty(value = "显示参数/查询参数:提交时间")
    Date submitTime;
    @ApiModelProperty(value = "/查询参数:查询提交时间结束时间")
    Date submitTimeEnd;
    @ApiModelProperty(value = "/查询参数:查询提交时间开始时间")
    Date submitTimeStart;
    @ApiModelProperty(value = "显示参数/:节点名称")
    String taskName;
    @ApiModelProperty(value = "显示参数/:流程发起人")
    String submitBandUser;
    @ApiModelProperty(value = "显示参数/:上一节点流程的完成人")
    String beforeSubmitUser;
    @ApiModelProperty(value = "显示参数/:上一节点的完成时间")
    Date beforTaskEndTime;
    @ApiModelProperty(value = "后端参数:上一节点的名称")
    String beforTaskName;
    @ApiModelProperty(value = "后端参数:流程定义id")
    String processDefinitionId;
    @ApiModelProperty(value = "后端参数:流程id")
    String processInstanceId;
    @ApiModelProperty(value = "流程大模块Id")
    Integer moudleId;
    @ApiModelProperty(value = "大模块名称")
    String moudleName;
    @ApiModelProperty(value = "流程大模块Id")
    Integer modelId;

    @ApiModelProperty(value = "批注")
    String comment;
}
