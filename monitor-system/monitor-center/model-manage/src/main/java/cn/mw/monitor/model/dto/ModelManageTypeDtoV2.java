package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2021/2/19 14:33
 */
@Data
public class ModelManageTypeDtoV2 {
    @ApiModelProperty("模型分类id")
    private Long modelGroupId;

    @ApiModelProperty("模型分类名称")
    private String modelGroupName;

    @ApiModelProperty("节点深度")
    private Integer deep=1;

    @ApiModelProperty("是否显示")
    private Boolean isShow;

    @ApiModelProperty("节点")
    private String nodes;

    @ApiModelProperty("父节点id ")
    private Long pid;

    @ApiModelProperty("是否根节点根是true ")
    private Boolean isNode;

    @ApiModelProperty("模型分组图标 ")
    private String url;

    //是否是虚拟化分组下的设备
    private Boolean isVim;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    private String type;

    private String modelGroupIdStr;

    private String pidStr;

    private String modelIndex;

    private String icon;

    private String instanceNum;

    private String pids;

    private Boolean disabled;

    private Boolean isFlag;

    private Integer groupLevel;

    //是否是基础数据
    private Boolean isBase;
}
