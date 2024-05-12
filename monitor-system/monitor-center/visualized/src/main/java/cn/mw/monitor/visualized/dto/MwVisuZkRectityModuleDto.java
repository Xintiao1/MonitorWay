package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 整流模块DTO
 * @date 2023/7/17 10:39
 */
@Data
public class MwVisuZkRectityModuleDto {

    @ApiModelProperty("配电柜名称")
    private String distributionName;

    @ApiModelProperty("交流限功率")
    private String curtailment;

    @ApiModelProperty("交流状态")
    private String alternatingStatus;

    @ApiModelProperty("限流点")
    private String currentLimitingPoint;

    @ApiModelProperty("配电柜定时均冲充")
    private String distributionPdb;

    @ApiModelProperty("输入电压")
    private String importVoltage;

    @ApiModelProperty("输出电流")
    private String current;

    @ApiModelProperty("输出电压")
    private String voltage;
}
