package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 17:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class DeleteModelRelationGroupParam {
    @ApiModelProperty("模型关系分组id")
    private Integer relationGroupId;
    @ApiModelProperty("当前模型id")
    private Integer ownModelId;
    @ApiModelProperty("批量删除模型关系分组id")
    private List<Integer> relationGroupIds;
}
