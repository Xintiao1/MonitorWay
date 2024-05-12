package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author lumingming
 * @createTime 20220916 0:22
 * @description 15
 */
@Data
@ApiModel("ip概览")
public class IPComprehensive {
    @ApiModelProperty(value="选择首标签值")
    private Map<String,Object> text;

    @ApiModelProperty(value="分配人")
    private String distributtioner;
    @ApiModelProperty(value="提交时间")
    private Date applicantDate;
    @ApiModelProperty(value="IP地址")
    private String ipAddresses;
}
