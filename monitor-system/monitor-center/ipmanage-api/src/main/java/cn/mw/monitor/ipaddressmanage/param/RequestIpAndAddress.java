package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("RequestIpAndAddress")
public class RequestIpAndAddress {
    //主键
    @ApiModelProperty(value="Id")
    Integer Id;
    @ApiModelProperty(value="地址是否为IPv4")
    private Boolean idType;
}
