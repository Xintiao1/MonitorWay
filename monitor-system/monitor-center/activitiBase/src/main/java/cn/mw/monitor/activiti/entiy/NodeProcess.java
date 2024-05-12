package cn.mw.monitor.activiti.entiy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author lumingming
 * @createTime 12 14:08
 * @description
 */
@Data
@ApiModel(value = "流程历史节点")
public class NodeProcess {
    @ApiModelProperty(value = "节点的名称")
    String nodeName;
    @ApiModelProperty(value = "开始时间")
    Date StartTime;
    @ApiModelProperty(value = "结束时间")
    Date EndTime;
    @ApiModelProperty(value = "审批过（同意/拒绝）流程所带参数：审批人")
    private String assign;
    @ApiModelProperty(value = "审批过（1同意/0拒绝/2审批中）流程所带参数：审批状态")
    private Integer accpet=2;
    @ApiModelProperty(value = "审批所带参数类型 0.绑定模块参数 1.审批批注")
    private Integer type;
    @ApiModelProperty(value = "type:0.绑定模块参数 type:1.审批批注")
    private Object data;
    @ApiModelProperty(value = "type:0.已审批 1.未通过 2.审批中")
    Integer processType=2;
    @ApiModelProperty(value = "0.短信通知 1.人工审批 2.模块操作（拦截操作） 3.脚本执行命令")
    Integer nodeType;
}
