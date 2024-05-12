package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配新建")
public class RequestIpAddressDistributtionParam {
    //主键
    @ApiModelProperty(value="ip地址分配回填")
    List<ResponIpDistributtionParam> responIpDistributtionParams;

    @ApiModelProperty(value="高级配置")
    RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;
    @ApiModelProperty(value="是否为修改 true修改 false 不修改")
    boolean submitStatus = false;
    @ApiModelProperty(value="signId")
    Integer signId;


}
