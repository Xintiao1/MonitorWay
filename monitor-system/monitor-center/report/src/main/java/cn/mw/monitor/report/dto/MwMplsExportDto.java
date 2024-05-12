package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MwMplsExportDto
 * @Description MPLS导出参数
 * @Author gengjb
 * @Date 2022/1/6 11:26
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwMplsExportDto {

    @ExcelProperty(value = {"时间"},index = 0)
    private String time;

    @ExcelProperty(value = {"发送流量"},index = 1)
    private String sendLink;

    @ExcelProperty(value = {"接收流量"},index = 2)
    private String acceptLink;
}
