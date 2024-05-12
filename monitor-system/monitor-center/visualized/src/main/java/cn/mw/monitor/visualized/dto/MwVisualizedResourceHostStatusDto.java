package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 主机状态统计DTO
 * @Author gengjb
 * @Date 2023/5/17 11:24
 * @Version 1.0
 **/
@Data
@ApiModel("主机状态统计DTO")
public class MwVisualizedResourceHostStatusDto {

    @ApiModelProperty("正常数")
    private Double normalValue;

    @ApiModelProperty("单位")
    private String normalValueUnits;

    @ApiModelProperty("异常数")
    private Double abnormalValue;

    @ApiModelProperty("单位")
    private String abnormalValueUnits;

    @ApiModelProperty("总数")
    private Double sumValue;

    @ApiModelProperty("总数单位")
    private String sumUnits;
}
