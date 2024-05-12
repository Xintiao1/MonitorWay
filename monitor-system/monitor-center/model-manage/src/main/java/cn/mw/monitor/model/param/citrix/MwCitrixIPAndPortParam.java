package cn.mw.monitor.model.param.citrix;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * @author qzg
 * @date 2022/10/13
 */
@Data
public class MwCitrixIPAndPortParam extends BaseParam {
    @ApiModelProperty("IP地址")
    private String ip;
    @ApiModelProperty("端口号")
    private String port;
    private boolean isIPV6;

    //Ipv6转为BigInteger
    private BigInteger ipv6IntVal;

}
