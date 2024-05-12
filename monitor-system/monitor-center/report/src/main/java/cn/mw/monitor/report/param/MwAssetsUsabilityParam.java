package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName MwAssetsUsabilityParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/14 16:20
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwAssetsUsabilityParam {

    @ExcelProperty(value = {"资产名称"},index = 1)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 2)
    private String ip;

    @ExcelProperty(value = {"可用性"},index = 3)
    private String assetsUsability;

    private String assetsId;

    private Integer type;


    private Date belongTime;

    private Date saveTime;

    //自动进程成功标识
    private boolean updateSuccess;

    @ExcelProperty(value = {"时间区域"},index = 0)
    private String time;
}
