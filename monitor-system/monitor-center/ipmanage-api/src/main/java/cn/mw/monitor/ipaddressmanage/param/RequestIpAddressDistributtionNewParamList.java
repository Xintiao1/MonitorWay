package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("新ip地址分配新建")
public class RequestIpAddressDistributtionNewParamList {
    //主键
    @ApiModelProperty(value="主ip的id")
    Integer primaryIp;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;

    @ApiModelProperty(value="ip的种类 0.ipv4 1.ipv6")
    Integer primaryType;
    @ApiModelProperty(value="主ip的地址")
    String primaryIdress;

}
