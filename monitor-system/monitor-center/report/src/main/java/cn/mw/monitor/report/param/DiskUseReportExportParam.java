package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName DiskUseReportExportParam
 * @Description 磁盘使用率参数
 * @Author gengjb
 * @Date 2021/10/18 11:25
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiskUseReportExportParam {

    @ExcelProperty(value = {"时间区域"},index = 0)
    private String time;

    @ExcelProperty(value = {"资产名称"},index = 1)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 2)
    private String ipAddress;

    @ExcelProperty(value = {"分区名称"},index = 3)
    private String typeName;

    @ExcelProperty(value = {"分区容量"},index = 4)
    private String diskTotal;

    @ExcelProperty(value = {"使用率"},index = 5)
    private String diskAvgValue;

    @ExcelProperty(value = {"已使用"},index = 6)
    private String diskUse;

    @ExcelProperty(value = {"可用容量"},index = 7)
    private String diskFree;

    @ExcelProperty(value = {"可用率"},index = 8)
    private String diskUsable;

}
