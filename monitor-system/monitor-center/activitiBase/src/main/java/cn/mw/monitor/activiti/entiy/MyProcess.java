package cn.mw.monitor.activiti.entiy;

import cn.mw.monitor.common.bean.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 10 13:21
 * @description
 */
@Data
@ApiModel(value = "流程历史")
public class MyProcess extends BaseDTO {
    @ApiModelProperty(value = "显示参数/查询参数:关联流程")
    String acctivitiName;
    @ApiModelProperty(value = "显示参数/查询参数:名称")
    String submitBandModelName;
    @ApiModelProperty(value = "显示参数/查询参数:提交时间")
    Date submitTime;
    @ApiModelProperty(value = "显示参数/:流程发起人")
    String submitBandUser;
    @ApiModelProperty(value = "/查询参数:查询提交时间结束时间")
    Date submitTimeEnd;
    @ApiModelProperty(value = "/查询参数:查询提交时间开始时间")
    Date submitTimeStart;
    @ApiModelProperty(value = "显示参数/:流程结束时间")
    Date processEndTime;
    @ApiModelProperty(value = "节点的名称")
    String taskName;
    @ApiModelProperty(value = "流程名称")
    String processName;
    @ApiModelProperty(value = "流程定义id")
    String processDefinitionId;
    @ApiModelProperty(value = "流程id")
    String processInstanceId;
    @ApiModelProperty(value = "流程ids")
    List<String> processInstanceIds;
    @ApiModelProperty(value = "批量审批参数 0.单个审批 1.批量审批")
    Integer compeleteType=0;
    @ApiModelProperty(value = "后端参数:type:0.已审批 1.未通过 2.审批中")
    Integer type;
    @ApiModelProperty(value = "批注")
    String comment;
    @ApiModelProperty(value = "流程大模块Id")
    Integer moudleId;
    @ApiModelProperty(value = "大模块名称")
    String moudleName;
    @ApiModelProperty(value = "流程大模块Id")
    Integer modelId;
}
