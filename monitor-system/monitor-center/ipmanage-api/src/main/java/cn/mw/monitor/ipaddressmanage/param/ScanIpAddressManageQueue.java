package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/12/11
 */
@Data
public class ScanIpAddressManageQueue {
    @ApiModelProperty(value="队列序号")
    Integer id;
    @ApiModelProperty(value="关联ip地址段")
    Integer linkId;
    @ApiModelProperty(value="用户id")
    Integer userId;
    @ApiModelProperty(value="参数内容")
    String param;
}
