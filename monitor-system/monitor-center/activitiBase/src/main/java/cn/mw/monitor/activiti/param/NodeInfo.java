package cn.mw.monitor.activiti.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class NodeInfo {
    @ApiModelProperty(value = "后端参数：流程类型（ 0.短信通知 1.人工审批 2.模块操作（拦截操作） 3.条件）4.并行网关 5.排他网关 6.子流程")
    private Integer infoType;
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

    @ApiModelProperty(value = "通知用户信息")
    private List<Integer> notifier;
    @ApiModelProperty(value = "条件节点参数")
    private String text;


    @ApiModelProperty(value = "审批过（同意/拒绝）流程所带参数：审批人")
    private String assign;
    @ApiModelProperty(value = "审批过（同意/拒绝）流程所带参数：审批状态")
    private Integer accpet;
    @ApiModelProperty(value = "审批过    流程所带参数：审批开始时间")
    private Date processStartTime;
    @ApiModelProperty(value = "审批过    流程所带参数：审批结束时间")
    private Date processEndTime;
    @ApiModelProperty(value = "审批所带参数类型 0.绑定模块参数 1.审批批注")
    private Integer type;
    @ApiModelProperty(value = "type:0.绑定模块参数 type:1.审批批注")
    private Object data;
//    private int nowType;
//    private Artificial artificial;
//    private Tools tools;
//    private CheckedInstanceResult checkedInstanceResult;
    @ApiModelProperty(value = "对象参数")
    private Object customNodeType;
}
