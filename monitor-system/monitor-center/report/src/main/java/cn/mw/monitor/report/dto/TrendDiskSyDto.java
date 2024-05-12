package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDiskSyDto {
    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;

    @ExcelProperty(value = {"日期"},index = 2)
    private Date recordTime;

    @ExcelProperty(value = {"分区名称"},index = 3)
    @ApiModelProperty("分区名称")
    private String typeName;

    @ExcelProperty(value = {"分区容量"},index = 4)
    @ApiModelProperty("磁盘总容量")
    private String diskTotal;

    @ExcelProperty(value = {"剩余容量(平均)"},index = 5)
    @ApiModelProperty("剩余磁盘容量")
    private String diskFree;

    @ExcelProperty(value = {"分区使用率","最大"},index = 6)
    private String diskMaxValue;

    @ExcelProperty(value = {"分区使用率","最小"},index = 7)
    private String diskMinValue;

    @ExcelProperty(value = {"分区使用率","平均"},index = 8)
    private String diskAvgValue;

}
