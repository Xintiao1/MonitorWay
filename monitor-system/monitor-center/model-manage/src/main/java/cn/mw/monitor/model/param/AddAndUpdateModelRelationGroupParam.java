package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2021/2/8 16:16

 */
@Data
@ApiModel
public class AddAndUpdateModelRelationGroupParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("模型分组关系Id")
    private Integer relationGroupId;
    @ApiModelProperty("当前模型Id")
    private Integer ownModelId;
    @ApiModelProperty("模型分组关系名称")
    private String relationGroupName;
    @ApiModelProperty("模型分组关系描述")
    private String relationGroupDesc;

    @ApiModelProperty("是否系统默认分组")
    private boolean defautGroupFlag = false;

    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

}
