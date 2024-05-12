package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 02 11:23
 * @description
 */
@Data
@ApiModel(value = "中转模块")
public class Tree {
/*    @ApiModelProperty("当前节点类型：0.是文档节点 1.是对象 3.即使文档节点又是对象")
    private Integer treeType;*/
    @ApiModelProperty("当前节点id")
    private String treeId;
    @ApiModelProperty("当前节点模块Id")
    private Integer treeModelId;
    @ApiModelProperty("当前节点名称")
    private String treeName;
    @ApiModelProperty("当前节点的modelName")
    private String modelName;
    @ApiModelProperty("当前节点是否选中")
    private boolean treeChoice;
/*    @ApiModelProperty("当前节点子节点")
    private List<Tree> tree;*/
}
