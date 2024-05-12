package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/26 14:59
 */
@Data
@ApiModel
public class AddAndUpdateModelChartParam {
    @ApiModelProperty("图谱Id")
    private Integer chartId;
    @ApiModelProperty("图谱名称")
    private Integer chartName;
}
