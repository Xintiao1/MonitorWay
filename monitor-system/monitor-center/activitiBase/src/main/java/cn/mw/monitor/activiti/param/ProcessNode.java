package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(value = "流程节点")
public class ProcessNode {


    @ApiModelProperty(value = "后端参数组")
    private NodeInfo nodeInfo;
    @ApiModelProperty(value = "后端参数：关联子节点")
    private ProcessNode childNode;
    @ApiModelProperty(value = "后端参数：关联子节点")
    private List<ProcessNode> childNodes;
    @ApiModelProperty(value = "绘制流程参数：模型")
    private Object modelInfo;
    @ApiModelProperty(value = "绘制流程参数：分组")
    private Object groupInfo;
    @ApiModelProperty(value = "绘制流程参数：用户")
    private Object userInfo;
    @ApiModelProperty(value = "绘制流程参数：角色")
    private Object roleInfo;
    @ApiModelProperty(value = "绘制流程参数：节点颜色和身份")
    private int type;
    @ApiModelProperty(value = "绘制流程参数:提示拼接 写死1")
    private int settype;
    @ApiModelProperty(value = "绘制流程和后端参数：节点名称（流程名称）")
    private String nodeName;
    @ApiModelProperty(value = "绘制流程参数:directorLevel")
    private String directorLevel;
    @ApiModelProperty(value = "绘制流程参数:error")
    private boolean error;
    @ApiModelProperty(value = "绘制流程参数:examineEndDirectorLevel")
    private int examineEndDirectorLevel;
    @ApiModelProperty(value = "绘制流程参数:examineMode")
    private int examineMode;
    @ApiModelProperty(value = "绘制流程参数:noHanderAction")
    private int noHanderAction;
    @ApiModelProperty(value = "绘制流程参数:priorityLevel")
    private String priorityLevel;
    @ApiModelProperty(value = "绘制流程参数:selectMode")
    private int selectMode;
    @ApiModelProperty(value = "绘制流程参数:selectRange")
    private int selectRange;
    @ApiModelProperty(value = "绘制流程参数:conditionList")
    private List<Object> conditionList;
    @ApiModelProperty(value = "绘制流程参数:conditionNodes")
    private List<ProcessNode> conditionNodes;

    @ApiModelProperty(value = "当前节点路径参数")
    private Integer customId;

    private String condition;

}
