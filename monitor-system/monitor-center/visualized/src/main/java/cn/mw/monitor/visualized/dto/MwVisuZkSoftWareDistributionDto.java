package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 中控配电柜返回数据实体
 * @Author gengjb
 * @Date 2023/3/16 15:15
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwVisuZkSoftWareDistributionDto {

    @ApiModelProperty("配电柜名称")
    private String distributionName;

    @ApiModelProperty("配电柜电压")
    private String voltage;

    @ApiModelProperty("配电柜电流")
    private String current;

    @ApiModelProperty("配电柜输出功率")
    private String curtailment;

    @ApiModelProperty("配电柜定时均冲充")
    private String distributionPdb;

    @ApiModelProperty("配电柜电池1")
    private String distributionBattery1;

    @ApiModelProperty("配电柜剩余")
    private String distributionRemainder;

    @ApiModelProperty("配电柜单相电压")
    private String distributionSinglephaseVoltage;

    @ApiModelProperty("配电柜剩余百分比")
    private String distributionPanelBatteryChareg;

    @ApiModelProperty("输入电压")
    private String importVoltage;
}
