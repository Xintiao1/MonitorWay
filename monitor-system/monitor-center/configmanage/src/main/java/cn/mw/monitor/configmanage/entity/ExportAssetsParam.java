package cn.mw.monitor.configmanage.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gui.quanwang
 * @className ExportAssetsParam
 * @description 资产表格导出数据
 * @date 2022/7/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportAssetsParam {

    /**
     * 资产名称
     */
    @ExcelProperty(value = "资产名称", index = 0)
    private String assetsName;

    /**
     * 模板名称
     */
    @ExcelProperty(value = "模板名称", index = 1)
    private String templateName;

    /**
     * 账户名称
     */
    @ExcelProperty(value = "账户名称", index = 2)
    private String accountName;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名", index = 3)
    private String userName;

    /**
     * 密码
     */
    @ExcelProperty(value = "密码", index = 4)
    private String password;

    /**
     * 协议
     */
    @ExcelProperty(value = "协议", index = 5)
    private String protocol;

    /**
     * 端口
     */
    @ExcelProperty(value = "端口", index = 6)
    private String port;

    /**
     * 错误信息，导入失败后返回
     */
    @ExcelProperty(value = "错误信息", index = 7)
    private String errorMsg;

    /**
     * 资产ID
     */
    @ExcelProperty(value = "资产ID", index = 34)
    private String assetsId;


}
