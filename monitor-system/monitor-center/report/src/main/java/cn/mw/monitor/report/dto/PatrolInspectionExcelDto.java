package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PatrolInspectionExcelDto
 * @Description 巡检报告导出excel类
 * @Author gengjb
 * @Date 2022/11/1 9:10
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolInspectionExcelDto {

    @ExcelProperty(value = {"资产名称"})
    private String assetsName;

    @ExcelProperty(value = {"IP地址"})
    private String ipAddress;

    @ExcelProperty(value = {"设备状态"})
    private String deviceStatus;

    @ExcelProperty(value = {"CPU利用率"})
    private String cpuUtilzation;

    @ExcelProperty(value = {"内存利用率"})
    private String memoryUtilzation;

    @ExcelProperty(value = {"电源状态"})
    private String powerSupplyStatus;

    @ExcelProperty(value = {"风扇状态"})
    private String fanStatus;

    @ExcelProperty(value = {"接口状态"})
    private String interfaceStatus;

    @ExcelProperty(value = {"BGP状态"})
    private String bgpStatus;

    @ExcelProperty(value = {"OSPF状态"})
    private String ospfStatus;

    @ExcelProperty(value = {"系统稳定性"})
    private String systemStability;

    @ExcelProperty(value = {"防火墙堆叠状态"})
    private String irfStatus;

    @ExcelProperty(value = {"drni链路状态"})
    private String drniLinkStatus;

    @ExcelProperty(value = {"drniIPP口状态"})
    private String ippStatus;

    @ExcelProperty(value = {"drni组角色"})
    private String drniRole;

    @ExcelProperty(value = {"vrrp状态"})
    private String vrrpStatus;

    @ExcelProperty(value = {"路由器稳定性"})
    private String routerStability;

    @ExcelProperty(value = {"数据产生时间"})
    private String time;

    @ExcelProperty(value = {"接口利用率(in)"})
    private String interfaceInUtilization;

    @ExcelProperty(value = {"接口利用率(out)"})
    private String interfaceOutUtilization;

    @ExcelProperty(value = {"检测结果"})
    private String testingResult;

}
