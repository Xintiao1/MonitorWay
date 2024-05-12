package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2021/2/8 12:16
 */
@Data
@ApiModel
public class ModelRelationsDto {
    @ApiModelProperty("关系ID")
    private Integer relationId;
    @ApiModelProperty("关系名称")
    private String relationName;
    @ApiModelProperty("左关系模型id")
    private String leftModelId;
    @ApiModelProperty("右关系模型id")
    private String rightModelId;
    @ApiModelProperty("左映射关系")
    private String leftRelation;
    @ApiModelProperty("右映射关系")
    private String rightRelation;
    @ApiModelProperty("左关系模型名称")
    private String leftModelName;
    @ApiModelProperty("右关系模型名称")
    private String rightModelName;
    @ApiModelProperty("左关系模型索引")
    private String leftModelIndex;
    @ApiModelProperty("右关系模型索引")
    private String rightModelIndex;
    @ApiModelProperty("关系分组名称")
    private String relationGroupName;

    @ApiModelProperty("左关系分组id")
    private Integer leftRelationGroupId;
    @ApiModelProperty("右关系分组id")
    private Integer rightRelationGroupId;

    @ApiModelProperty("左关系分组名称")
    private String leftRelationGroupName;
    @ApiModelProperty("右关系分组名称")
    private String rightRelationGroupName;

    @ApiModelProperty("左关系图标")
    private String leftModelIcon;
    @ApiModelProperty("右关系图标")
    private String rightModelIcon;

    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

}
