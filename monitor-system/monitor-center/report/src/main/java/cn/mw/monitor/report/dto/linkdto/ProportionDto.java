package cn.mw.monitor.report.dto.linkdto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/12/22 15:59
 */
@Data
public class ProportionDto {
    @ExcelProperty(value = "接口流量时间占比<10%")
    private Double proportionTen;
    @ExcelProperty(value = "接口流量时间占比10%-50%")
    private Double proportionFifty;
    @ExcelProperty(value = "接口流量时间占比50%-80%")
    private Double proportionEighty;
    @ExcelProperty(value = "接口流量时间占比>80%")
    private Double proportionHundred;
}
