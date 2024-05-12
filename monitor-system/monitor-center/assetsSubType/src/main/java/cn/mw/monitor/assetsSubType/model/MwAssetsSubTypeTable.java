package cn.mw.monitor.assetsSubType.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("子分类新增和修改对象类")
public class MwAssetsSubTypeTable {

    @ApiModelProperty(name = "ID")
    private Integer id;

    @ApiModelProperty(name = "分类名称")
    private String typeName;

    @ApiModelProperty(name = "父分类ID")
    private Integer pid;

    @ApiModelProperty(name = "分类描述")
    private String typeDesc;

    @ApiModelProperty(name = "节点信息")
    private String nodes;

    @ApiModelProperty(name = "状态")
    private String enable;

    //1有形 2无形
    @ApiModelProperty(name = "有形无形")
    private Integer classify;

    @ApiModelProperty(name = "主机组id")
    private Integer groupid;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    @ApiModelProperty(name = "类型图标")
    private String typeIcon;
}
