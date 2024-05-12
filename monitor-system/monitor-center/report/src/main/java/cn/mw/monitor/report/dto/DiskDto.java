package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/7/13 9:02
 */
@Data
public class DiskDto extends BaseDto {

    @ApiModelProperty("分区名称")
    private String typeName;
    @ApiModelProperty("磁盘总容量")
    private double diskTotal;
    @ApiModelProperty("剩余磁盘容量")
    private double diskFree;

    private double diskMaxValue;
    private double diskMinValue;
    private double diskAvgValue;
}
