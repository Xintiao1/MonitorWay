package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/19
 */
@Data
@ApiModel
public class QueryHideModelToPo {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("当前进入的ownModelId")
    private Integer ownModelId;
    @ApiModelProperty("当前进入的ownInstanceId")
    private Integer ownInstanceId;

    @ApiModelProperty("选择的modelIds")
    private List<Integer> modelIds;
    @ApiModelProperty("1：显示，0：隐藏")
    private Integer type;
}
