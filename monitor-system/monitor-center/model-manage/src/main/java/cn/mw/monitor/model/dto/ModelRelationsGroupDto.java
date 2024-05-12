package cn.mw.monitor.model.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2021/2/8 17:48
 */
@Data
@ApiModel
public class ModelRelationsGroupDto  {
    @ApiModelProperty("关系分组ID")
    private Integer relationGroupId;
    @ApiModelProperty("关系分组名称")
    private String relationGroupName;
    @ApiModelProperty("关系分组描述")
    private String relationGroupDesc;

    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

}
