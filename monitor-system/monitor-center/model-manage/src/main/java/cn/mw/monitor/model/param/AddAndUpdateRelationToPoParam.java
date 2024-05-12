package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2022/3/9
 */
@Data
@ApiModel
public class AddAndUpdateRelationToPoParam {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("当前模型Id")
    private Integer ownModelId;
    @ApiModelProperty("当前实例Id")
    private Integer ownInstanceId;
    @ApiModelProperty("当前实例Ids")
    private List<Integer> ownInstanceIds;
    @ApiModelProperty("关联模型id")
    private Integer oppositeModelId;
    @ApiModelProperty("关联实例id")
    private Integer oppositeInstanceId;
    @ApiModelProperty("关联实例ids")
    private List<Integer> oppositeInstanceIds;
    @ApiModelProperty("当前模型Name")
    private String ownModelName;
    @ApiModelProperty("当前实例Name")
    private String ownInstanceName;
    @ApiModelProperty("关联模型Name")
    private String oppositeModelName;
    @ApiModelProperty("关联实例Name")
    private String oppositeInstanceName;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;

    private List<AddAndUpdateRelationToPoParam> getRelationModels;
}
