package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName DiskUseAbleReportExportParam
 * @Description 磁盘可用率参数
 * @Author gengjb
 * @Date 2021/10/18 11:33
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiskUseAbleReportExportParam {

    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;

    @ExcelProperty(value = {"分区名称"},index = 2)
    private String typeName;

    @ExcelProperty(value = {"分区容量"},index = 3)
    private String diskTotal;

    @ExcelProperty(value = {"可用率"},index = 4)
    private String diskUsable;

    @ExcelProperty(value = {"可用容量"},index = 5)
    private String diskFree;

}
