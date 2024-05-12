package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.AssetsDto;
import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配查询")
public class QueryIpAddressDistributtionParam extends BaseParam{
    //主键
    @ApiModelProperty(value="ip地址id")
    private List<Integer> id;
    @ApiModelProperty(value="ip地址id")
    private List<String> ipList;

    @ApiModelProperty(value="产生ipv6接口")
    private String ip;

    @ApiModelProperty(value="接受ipgrop")
    private String keyValue;

    @ApiModelProperty(value="sourceCheck源ip选项")
    private boolean sourceCheck;


    @ApiModelProperty(value="sourceCheck源ip选项")
    private Integer keytype;

    @ApiModelProperty(value="ip地址类型  true表示是ipv4 false是ipv6 ")
    private boolean idType;

    @ApiModelProperty(value="IP查询是否为高级查询 ")
    private boolean labelLevel;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;
    @ApiModelProperty(value="结束时间")
    private Date updateDateEnd;
    @ApiModelProperty(value="开始时间")
    private Date updateDateStart;
    @ApiModelProperty(value="signId")
    private Integer signId;
}
