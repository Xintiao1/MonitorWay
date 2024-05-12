package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 容器使用情况DTO
 * @Author gengjb
 * @Date 2023/6/7 16:23
 * @Version 1.0
 **/
@Data
@ApiModel("容器概览DTO")
public class MwVisualizedContaineUsageDto {

    @ApiModelProperty("使用率")
    private String usageRate;

    @ApiModelProperty("使用率单位")
    private String usageRateUnits;

    @ApiModelProperty("已使用")
    private String usage;

    @ApiModelProperty("已使用单位")
    private String usageUnits;

    @ApiModelProperty("已预留")
    private String reserved;

    @ApiModelProperty("已预留单位")
    private String reservedUnits;


}
