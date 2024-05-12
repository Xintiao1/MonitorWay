package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/26 15:32
 */
@ApiModel
@Data
public class ModelChartDto {
    @ApiModelProperty("图谱id")
    private Integer chartId;
    @ApiModelProperty("图谱名称")
    private Integer chartName;

}
