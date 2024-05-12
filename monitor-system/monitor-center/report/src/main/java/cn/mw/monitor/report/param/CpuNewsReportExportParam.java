package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName CpuNewsReportExportParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/18 10:57
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CpuNewsReportExportParam {

    //品牌
    @ExcelProperty(value = {"品牌"})
    private String brand;

    //位置
    @ExcelProperty(value = {"位置"})
    private String location;

    @ExcelProperty(value = {"时间区域"})
    private String time;

    @ExcelProperty(value = {"主机名称"})
    private String assetName;

    @ExcelProperty(value = {"IP地址"})
    private String ip;

    @ExcelProperty(value = {"平均内存利用率"})
    private String diskUserRate;

    @ExcelProperty(value = {"最大内存利用率"})
    private String maxMemoryUtilizationRate;

    @ExcelProperty(value = {"最小内存利用率"})
    private String minMemoryUtilizationRate;

    @ExcelProperty(value = {"已用内存"})
    private String diskUser;

    @ExcelProperty(value = {"总内存"})
    private String diskTotal;

    @ExcelProperty(value = {"CPU最大利用率"})
    private String maxValue;

    @ExcelProperty(value = {"CPU平均利用率"})
    private String avgValue;

    @ExcelProperty(value = {"CPU最小利用率"})
    private String minValue;

    //ICMP响应时间
    @ExcelProperty(value = {"平均响应时间"})
    private String icmpResponseTime;

    //ping延迟
    @ExcelProperty(value = {"设备状态"})
    private String icmpPing;

    //Cpu利用率
    @ExcelProperty(value = {"CPU利用率"})
    private String cpuUtilizationRate;

    //内存利用率
    @ExcelProperty(value = {"内存利用率"})
    private String memoryUtilizationRate;

    //CPU是否需要显示颜色
    private Boolean isCpuColor;

    //内存是否需要显示颜色
    private Boolean isMemoryColor;

}
