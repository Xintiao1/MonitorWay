package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CpuAndMemorySyDto {
    @ExcelIgnore
    private String _XID;

    @ExcelIgnore
    private String hostId;

    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;

    @ExcelProperty(value = {"日期"},index = 2)
    private Date recordTime;

//    @ExcelProperty(value = {"CPU可用率"},index = 3)
//    private String cpuFreeRage;
//
//    @ExcelProperty(value = {"内存可用率"},index = 4)
//    private String memoryFreeRage;

    @ExcelProperty(value = {"CPU利用率","最大"},index = 3)
    private String cpuMaxValue;

    @ExcelProperty(value = {"CPU利用率","最小"},index = 4)
    private String cpuMinValue;

    @ExcelProperty(value = {"CPU利用率","平均"},index = 5)
    private String cpuAvgValue;

    @ExcelProperty(value = {"内存利用率","最大"},index = 6)
    private String memoryMaxValue;

    @ExcelProperty(value = {"内存利用率","最小"},index = 7)
    private String memoryMinValue;

    @ExcelProperty(value = {"内存利用率","平均"},index = 8)
    private String memoryAvgValue;

}
