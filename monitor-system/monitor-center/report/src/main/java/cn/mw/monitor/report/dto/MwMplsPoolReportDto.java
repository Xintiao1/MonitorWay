package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MwMplsPoolReportDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/26 15:57
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwMplsPoolReportDto {

    //线路名称
    private String lineName;

    //接收流量最大
    private String acceptFlowMax;

    //接收流量平均
    private String acceptFlowAvg;

    //发送流量最大
    private String sendingFlowMax;

    //发送流量平均
    private String sendingFlowAvg;

    private String unit;
}
