package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/20 9:02
 */
@Data
@ApiModel
public class QueryModelCustomPageParam {

    @ApiModelProperty(value = "用户id")
    private Integer userId;

    @ApiModelProperty(value = "页面id")
    private List<Integer> pageId;

    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型类型id")
    private Integer modelTypeId;

    @ApiModelProperty("父类型id")
    private Integer FatherModelId;
}
