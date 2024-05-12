package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 2023814 1:03
 * @description 14
 */
@Data
@ApiModel(value = "组成流程有用节点")
public class ProcessNodeHaveful {
    @ApiModelProperty(value = "剥离后后端所需要的有用节点 1.审批节点 2.提交节点 3.条件节点 4.排他网关 5.并行网关")
    public Integer type;
    @ApiModelProperty(value = "箭头进入点")
    List<Integer> sourceId;
    @ApiModelProperty(value = "箭头出入点")
    List<Integer> nextId;
    @ApiModelProperty(value = "后端参数：流程类型（ 0.短信通知 1.人工审批 2.模块操作（拦截操作） 3.脚本执行命令）4.并行网关")
    private int infoType;
    @ApiModelProperty(value = "后端参数：流程关联模块操作 0.添加 1.删除 2.修改 3.全选")
    private List<Integer> operType;
    @ApiModelProperty(value = "后端参数：流程关联模型的Ids")
    private List<List<Integer>> modelId;
    @ApiModelProperty(value = "后端参数： 0.用户 1.用户组 2.角色")
    private int nodeType;
    @ApiModelProperty(value = "后端参数：用户组")
    private List<Integer> group;
    @ApiModelProperty(value = "后端参数：用户")
    private List<Integer> people;
    @ApiModelProperty(value = "后端参数：角色")
    private List<Integer> role;
    @ApiModelProperty(value = "条件节点参数")
    private String text;
    //流程定义
    @ApiModelProperty(value = "节点ID")
    private String nodeId;

    @ApiModelProperty(value = "绘制流程和后端参数：节点名称（流程名称）")
    private String nodeName;

    @ApiModelProperty(value = "当前节点路径参数")
    private Integer customId;
}
