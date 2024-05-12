package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PatrolInspectionLinkDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/12 11:19
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolInspectionLinkDto {

    @ExcelProperty(value = {"资产名称"})
    private String assetsName;

    @ExcelProperty(value = {"IP地址"})
    private String ipAddress;

    @ExcelProperty(value = {"接口名称"})
    private String interfaceName;

    @ExcelProperty(value = {"接口IN最大利用率"})
    private String maxInterfaceInUtilization;

    @ExcelProperty(value = {"接口IN平均利用率"})
    private String avgInterfaceInUtilization;

    @ExcelProperty(value = {"接口IN最小利用率"})
    private String minInterfaceInUtilization;

    @ExcelProperty(value = {"接口OUT最大利用率"})
    private String maxInterfaceOutUtilization;

    @ExcelProperty(value = {"接口OUT平均利用率"})
    private String avgInterfaceOutUtilization;

    @ExcelProperty(value = {"接口OUT最小利用率"})
    private String minInterfaceOutUtilization;
}
