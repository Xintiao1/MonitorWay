package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/3/15 17:15
 */
@Data
@ApiModel
public class QueryModelTypeParam {
    @ApiModelProperty("分类名称")
    private String modelGroupName;
    @ApiModelProperty("分类Ids")
    private List<String> modelGroupIds;
}
