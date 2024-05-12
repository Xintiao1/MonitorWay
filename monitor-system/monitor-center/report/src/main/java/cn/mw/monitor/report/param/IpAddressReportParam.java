package cn.mw.monitor.report.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description IP地址报表查询参数
 * @Author gengjb
 * @Date 2023/3/7 10:01
 * @Version 1.0
 **/
@Data
public class IpAddressReportParam  extends BaseParam {

    @ApiModelProperty("日期类型")
    private Integer dateType;

    @ApiModelProperty("内置id")
    private Integer id;


    @ApiModelProperty("日期范围")
    private List<String> chooseTime;

}
