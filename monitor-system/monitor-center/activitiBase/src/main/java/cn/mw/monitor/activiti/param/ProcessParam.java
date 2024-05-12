package cn.mw.monitor.activiti.param;

import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "流程model")
public class ProcessParam extends BaseProcessParam {
    //流程定义
    @ApiModelProperty(value = "流程id")
    private String processId;


    @ApiModelProperty(value = "大模块id")
    private Integer moudleId;

    @ApiModelProperty(value = "表单对应位置")
    private String position;
    @ApiModelProperty(value = "流程名")
    private String processName;

    @ApiModelProperty(value = "流程路径")
    List<List<Integer>> customIdPath;

    @ApiModelProperty(value = "流程描述")
    private String processDesc;

    @ApiModelProperty(value = "模块的种类0.模型 1.知识库")
    private Integer modelType ;


    @ApiModelProperty(value = "流程定义")
    private ProcessNode processDefinition;

    @ApiModelProperty(value = "所属机构")
    private List<Integer> org;

    @ApiModelProperty(value = "所属用户组")
    private List<Integer> group;

    @ApiModelProperty(value = "所属责任人")
    private List<Integer> user;

    @ApiModelProperty(value = "是否需要批量修改责任人")
    private boolean principalcheckbox;

    @ApiModelProperty(value = "是否需要批量修改机构")
    private boolean orgIdscheckbox;

    @ApiModelProperty(value = "是否需要批量修改用户组")
    private boolean groupIdscheckbox;

}
