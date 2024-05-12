package cn.mw.monitor.activiti.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "流程列表查询")
public class SearchProcessParam extends BaseParam {
    @ApiModelProperty(value = "流程定义id即流程id")
    private String processDefinitionId;
    @ApiModelProperty(value = "当前流程定义的Id")
    private String processInstanceId;
    @ApiModelProperty(value = "流程名称")
    private String processName;
    @ApiModelProperty(value = "流程状态")
    private String processStatus;


    @ApiModelProperty(value = "大模块id")
    private Integer moudleId;
    @ApiModelProperty(value = "节点名称")
    private String taskName;
    @ApiModelProperty(value = "删除id集合")
    private List<Integer> ids;
    @ApiModelProperty(value = "工单名称")
    private String task_name;
    @ApiModelProperty(value = "工单提交人")
    private String task_submit_creator;
    @ApiModelProperty(value = "工单创建时间范围")
    private Date create_dateEnd;
    @ApiModelProperty(value = "工单创建时间范围")
    private Date create_dateStart;

    @ApiModelProperty(value = "工单状态")
    private Integer is_successful;

    @ApiModelProperty(value = "工单状态")
    private String task_type;

    @ApiModelProperty(value = "提交人名称")
    private String loginName;
}
