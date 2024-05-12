package cn.mw.monitor.webMonitor.api.param.webMonitor;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author syt
 * @Date 2021/7/15 20:42
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "导入webmonitor数据")
public class ImportWebMonitorParam {
    @ApiModelProperty("网站名称")
    @ExcelProperty(value="网站名称",index = 0)
    private String webName;

    @ApiModelProperty("网站url")
    @ExcelProperty(value="网站url",index =1)
    private String webUrl;

    @ApiModelProperty("调用的服务器Ip")
    @ExcelProperty(value="调用的服务器Ip",index =2)
    private String hostIp;

    @ApiModelProperty("更新间隔")
    @Max(value=86400,message = "更新间隔不能大于86400s")
    @ExcelProperty(value="更新间隔",index =3)
    private Integer updateInterval;

    @ApiModelProperty("尝试次数1-10")
    @ExcelProperty(value="尝试次数",index =4)
    private Integer attempts;

    @ApiModelProperty("启用状态")
    @ExcelProperty(value="启用状态",index =5)
    private String enable;

    @ApiModelProperty("超时")
    @Max(value=3600,message = "超时最大时间不能大于3600s")
    @Min(value=1,message = "超时最小时间不能小于1s")
    @ExcelProperty(value="超时",index =6)
    private Integer timeOut;

    @ApiModelProperty("必要状态码")
    @Max(value=86400,message = "状态码不能大于86400")
    @ExcelProperty(value="必要状态码",index =7)
    private String statusCode;

    @ApiModelProperty(value = "责任人")
    @ExcelProperty(value = "负责人",index =8)
    private String principalName;

    @ApiModelProperty(value = "所属机构/部门")
    @ExcelProperty(value = "所属机构/部门",index =9)
    private String orgs;

    @ApiModelProperty(value = "用户组")
    @ExcelProperty(value = "用户组",index =10)
    private String groups;

    @ApiModelProperty(value = "监控服务器名称")
    @ExcelProperty(value = "监控服务器名称",index =11)
    private String monitorServer;

    @ApiModelProperty(value = "错误信息")
    @ExcelProperty(value = "错误信息",index =12)
    private String errorMsg;
}
