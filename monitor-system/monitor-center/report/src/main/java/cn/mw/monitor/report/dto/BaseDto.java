package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/7/13 9:04
 */
@Data
public class BaseDto {
    @ExcelIgnore
    private String assetsId;
    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;
    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ipAddress;
}
