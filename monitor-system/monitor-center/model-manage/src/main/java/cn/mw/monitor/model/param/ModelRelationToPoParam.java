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
public class ModelRelationToPoParam {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("当前模型Id")
    private Integer ownModelId;
    @ApiModelProperty("当前实例Id")
    private Integer ownInstanceId;
    @ApiModelProperty("关联模型实例参数")
    private List<QueryInstanceRelationsParam> relationsInstanceList;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
}
