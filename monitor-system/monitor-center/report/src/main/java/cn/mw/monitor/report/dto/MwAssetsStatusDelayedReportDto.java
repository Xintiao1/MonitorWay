package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 资产信息与状态Dto
 * @Author gengjb
 * @Date 2023/6/16 11:56
 * @Version 1.0
 **/
@Data
@ApiModel("资产信息与状态Dto")
public class MwAssetsStatusDelayedReportDto {

    @ExcelProperty(value = {"时间"})
    private String time;

    @ExcelProperty(value = {"连通状态"})
    private String status;

    @ExcelProperty(value = {"延时值"})
    private String delayed;

    private Long sortDate;

}
