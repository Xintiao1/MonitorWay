package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 2022916 16:10
 * @description
 */
@Data
public class IPCountPrictureDetails {
    @ApiModelProperty(value = "分组")
    private Integer createTime;
    @ApiModelProperty(value = "分组数据")
    private Integer counts=0;
}
