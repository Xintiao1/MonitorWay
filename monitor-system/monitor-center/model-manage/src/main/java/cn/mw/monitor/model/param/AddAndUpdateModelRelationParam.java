package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 10:18
 *
 *
 */
@Data
@ApiModel
public class AddAndUpdateModelRelationParam {
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("关系组id")
    private Integer relationGroupId;

    @ApiModelProperty("当前模型id")
    private Integer ownModelId;
    @ApiModelProperty("当前模型名称")
    private String ownModelName;

    @ApiModelProperty("关联模型id")
    private Integer oppositeModelId;
    @ApiModelProperty("关联模型名称")
    private String oppositeModelName;

    @ApiModelProperty("当前模型描述关系名称")
    private String ownRelationName;

    @ApiModelProperty("当前模型描述关系Id")
    private String ownRelationId;

    @ApiModelProperty("当前模型关联个数(0-1或者0-n)")
    private String ownRelationNum;

    @ApiModelProperty("关联模型描述关系名称")
    private String oppositeRelationName;

    @ApiModelProperty("关联模型描述关系Id")
    private String oppositeRelationId;

    @ApiModelProperty("关联模型关联个数((0-1或者0-n))")
    private String oppositeRelationNum;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("修改人")
    private String modifier;

    @ApiModelProperty("修改时间")
    private Date modificationDate;

}
