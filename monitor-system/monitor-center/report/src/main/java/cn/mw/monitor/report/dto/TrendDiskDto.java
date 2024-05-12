package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author xhy
 * @date 2020/5/4 12:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDiskDto {
    @ExcelProperty(value = {"资产名称"},index = 1)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 2)
    private String ipAddress;

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

    /**
     * 已使用磁盘容量
     */
    private String diskUse;

    /**
     * 磁盘可用率
     */
    private String diskUsable;

    private String assetsId;

    private Integer type;

    //自动进程更新时间
    private Date updateTime;

    //自动进程成功标识
    private boolean updateSuccess;

    private Date saveTime;

    @ExcelProperty(value = {"时间区域"},index = 0)
    private String time;

}
