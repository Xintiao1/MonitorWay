package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("新回收参数")
public class RequestIpAddressReciveParam {

    @ApiModelProperty(value="回收变更前查询")
    List<ResponIpDistributtionNewParam> ipAndAddressList ;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;

    @ApiModelProperty(value="回收变更前查询")
    IsInput isInput ;

    Map<String,Object> requestIpAddressDistributtionSeniorParam;
}
