package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 08 15:34
 * @description
 */
@Data
@ApiModel("作业与方案变量")
public class MwConfigManageTreeGroup {
    @ApiModelProperty(value="主键")
    private Integer id;
    @ApiModelProperty(value="分组名称")
    private String name;

    @ApiModelProperty(value="涉及分组数量")
    private Integer num;

    @ApiModelProperty(value="父节点")
    private Integer parentId;

    @ApiModelProperty(value="父节点类型")
    private String type;

    @ApiModelProperty(value="子节点")
    private List<MwConfigManageTreeGroup> mwConfigManageTreeGroups;

}
