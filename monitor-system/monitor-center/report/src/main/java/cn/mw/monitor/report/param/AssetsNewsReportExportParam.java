package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName AssetsNewsReportExportParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/18 10:21
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsNewsReportExportParam {

    @ExcelProperty(value = {"机构"},index = 0)
    private String orgName;

    @ExcelProperty(value = {"资产类型"},index = 1)
    private String assetsTypeName;

    @ExcelProperty(value = {"资产名称"},index = 2)
    private String assetsName;

    @ExcelProperty(value = {"IP地址"},index = 3)
    private String inBandIp;

    @ExcelProperty(value = {"厂商"},index = 4)
    private String manufacturer;

    @ExcelProperty(value = {"规格型号"},index = 5)
    private String specifications;
}
