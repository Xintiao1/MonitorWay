package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 中控统计信息
 * @Author gengjb
 * @Date 2023/3/16 16:00
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwVisuZkSoftWareStatisticsDto {

    @ApiModelProperty("监测点数")
    private Integer monitorNumber = 0;

    @ApiModelProperty("正常数量")
    private Integer normalNumber = 0;

    @ApiModelProperty("异常数量")
    private Integer abNormalNumber = 0;
}
