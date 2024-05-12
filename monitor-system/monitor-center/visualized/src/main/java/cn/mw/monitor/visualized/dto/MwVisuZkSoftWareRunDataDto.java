package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 中控运行数据DTO
 * @Author gengjb
 * @Date 2023/3/16 16:10
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwVisuZkSoftWareRunDataDto {

    @ApiModelProperty("运行数据配电柜名称")
    private String runDataDistributionName;

    @ApiModelProperty("输出电压")
    private String voltage;

    @ApiModelProperty("输出电流")
    private String current;

    @ApiModelProperty("限流点")
    private String currentLimitingPoint;

    @ApiModelProperty("输入电压")
    private String importVoltage;

    @ApiModelProperty("交流状态")
    private String alternatingStatus;

    @ApiModelProperty("节能状态")
    private String energyStatus;

    @ApiModelProperty("交流限功率")
    private String curtailment;

    @ApiModelProperty("温度限功率")
    private String temperatureLimitPower;

}
