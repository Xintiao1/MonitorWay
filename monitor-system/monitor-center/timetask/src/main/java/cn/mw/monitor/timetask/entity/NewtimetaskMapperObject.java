package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lumingming
 * @createTime 03 14:36
 * @description
 */
@Data
@ApiModel("绑定模块的id")
public class NewtimetaskMapperObject extends BaseParam implements Serializable {
    private static final long serialVersionUID = 1L;
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
    @ApiModelProperty("当前时间计划Id（添加不传，编辑传）")
    private String newtimetaskId ;

}
