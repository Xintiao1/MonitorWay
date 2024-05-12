package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName LineFlowReportParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/24 9:35
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineFlowReportParam {

    @ExcelProperty(value = {"时间"})
    private String time;

    @ExcelProperty(value = {"资产名称"})
    private String assetsName;

    @ExcelProperty(value = {"接口名称"})
    private String interfaceName;

    @ExcelProperty(value = {"接收流量最大"})
    private String acceptFlowMax;

    @ExcelProperty(value = {"接收流量平均"})
    private String acceptFlowAvg;

    @ExcelProperty(value = {"接收流量最小"})
    private String acceptFlowMin;

    @ExcelProperty(value = {"接收总流量"})
    private String acceptTotalFlow;

    @ExcelProperty(value = {"发送流量最大"})
    private String sendingFlowMax;

    @ExcelProperty(value = {"发送流量平均"})
    private String sendingFlowAvg;

    @ExcelProperty(value = {"发送流量最小"})
    private String sendingFlowMin;

    @ExcelProperty(value = {"发送总流量"})
    private String sendTotalFlow;

    @ExcelProperty(value = {"发送最大值时间"})
    private String sendMaxValueTime;

    @ExcelProperty(value = {"发送最小值时间"})
    private String sendMinValueTime;

    @ExcelProperty(value = {"接收最大值时间"})
    private String acceptMaxValueTime;

    @ExcelProperty(value = {"接收最小值时间"})
    private String acceptMinValueTime;


    private String assetsId;

    private Integer type;

    private Date maxTime;

    private Date saveTime;

    //自动进程成功标识
    private boolean updateSuccess;
}
