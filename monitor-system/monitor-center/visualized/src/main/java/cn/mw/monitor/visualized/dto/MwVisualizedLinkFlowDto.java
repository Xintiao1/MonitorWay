package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 线路DTO
 * @date 2023/10/9 16:11
 */
@Data
@ApiModel("线路DTO")
public class MwVisualizedLinkFlowDto {

    @ApiModelProperty("线路名称")
    private String linkName;

    @ApiModelProperty("线路流量入")
    private String flowIn;

    @ApiModelProperty("线路流量出")
    private String flowOut;

    @ApiModelProperty("线路流量百分比入")
    private String flowPercentageIn;

    @ApiModelProperty("线路流量百分比出")
    private String flowPercentageOut;
}
