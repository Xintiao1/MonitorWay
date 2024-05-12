package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/7/13 10:53
 */
@Data
public class CpuAndMemoryDtos extends BaseDto {
    @ExcelProperty(value = {"CPU可用率"},index = 2)
    private double cpuFreeRage;
    @ExcelProperty(value = {"内存可用率"},index = 3)
    private double memoryFreeRage;
    @ExcelProperty(value = {"CPU利用率","最大"},index = 4)
    private double cpuMaxValue;
    @ExcelProperty(value = {"CPU利用率","最小"},index = 5)
    private double cpuMinValue;
    @ExcelProperty(value = {"CPU利用率","平均"},index = 6)
    private double cpuAvgValue;
    @ExcelProperty(value = {"内存利用率","最大"},index = 7)
    private double memoryMaxValue;
    @ExcelProperty(value = {"内存利用率","最小"},index = 8)
    private double memoryMinValue;
    @ExcelProperty(value = {"内存利用率","平均"},index = 9)
    private double memoryAvgValue;

}
