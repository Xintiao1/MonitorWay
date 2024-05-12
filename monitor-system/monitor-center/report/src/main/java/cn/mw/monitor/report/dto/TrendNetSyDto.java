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
@NoArgsConstructor
@AllArgsConstructor
public class TrendNetSyDto {
    @ExcelIgnore
    private String assetsId;

    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;

    @ExcelIgnore
    private Integer assetsTypeId;

    @ExcelProperty(value = {"日期"},index = 2)
    private Date recordTime;

    @ExcelProperty(value = {"接口名称"},index = 3)
    private String netName;

    @ExcelProperty(value = {"接入流量(入向)","最大"},index = 4)
    private String netInBpsMaxValue;

    @ExcelProperty(value = {"接入流量(入向)","最小"},index = 5)
    private String netInBpsMinValue;

    @ExcelProperty(value = {"接入流量(入向)","平均"},index = 6)
    private String netInBpsAvgValue;

    @ExcelProperty(value = {"接入流量(出向)","最大"},index = 7)
    private String netOutBpsMaxValue;

    @ExcelProperty(value = {"接入流量(出向)","最小"},index = 8)
    private String netOutBpsMinValue;

    @ExcelProperty(value = {"接入流量(出向)","平均"},index = 9)
    private String netOutBpsAvgValue;

}
