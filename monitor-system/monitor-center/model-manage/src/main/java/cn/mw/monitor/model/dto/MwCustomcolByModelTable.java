package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class MwCustomcolByModelTable {
    @ApiModelProperty(value = "个性化id")
    private Integer customId;

    @ApiModelProperty(value = "自增序列")
    private Integer id;

    @ApiModelProperty(value = "列ID")
    private Integer colId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "是否排序")
    private Boolean sortable;

    @ApiModelProperty(value = "宽度")
    private Integer width;

    @ApiModelProperty(value = "是否显示")
    private Boolean visible;

    @ApiModelProperty(value = "顺序数")
    private Integer orderNumber;

    @ApiModelProperty(value = "还原标识  1-还原  0-默认")
    private Integer deleteFlag;

    @ApiModelProperty("模型属性Id")
    private Integer modelPropertiesId;

}