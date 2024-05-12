package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/5/3 12:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CpuAndMemoryDto {

    @ExcelIgnore
    private String _XID;

    @ExcelIgnore
    private String hostId;

    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;

    @ExcelIgnore
    private String cpuName;

    @ExcelIgnore
    private String memoryName;

//    @ExcelProperty(value = {"CPU可用率"},index = 2)
    private String cpuFreeRage;

//    @ExcelProperty(value = {"内存可用率"},index = 3)
    private String memoryFreeRage;

    @ExcelProperty(value = {"CPU利用率","最大"},index = 2)
    private String cpuMaxValue;

    @ExcelProperty(value = {"CPU利用率","最小"},index = 3)
    private String cpuMinValue;

    @ExcelProperty(value = {"CPU利用率","平均"},index = 4)
    private String cpuAvgValue;

    @ExcelProperty(value = {"内存利用率","最大"},index = 5)
    private String memoryMaxValue;

    @ExcelProperty(value = {"内存利用率","最小"},index = 6)
    private String memoryMinValue;

    @ExcelProperty(value = {"内存利用率","平均"},index = 7)
    private String memoryAvgValue;
}
