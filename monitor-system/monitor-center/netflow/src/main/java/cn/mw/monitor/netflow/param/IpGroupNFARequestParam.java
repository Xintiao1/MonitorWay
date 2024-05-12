package cn.mw.monitor.netflow.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className IpGroupNFARequestParam
 * @description NFA增加编辑请求参数
 * @date 2022/8/24
 */
@Data
public class IpGroupNFARequestParam {

    /**
     * IP地址组主键ID
     */
    @ApiModelProperty("IP地址组主键ID")
    private Integer ipGroupId;

    /**
     * IP类别（1：ipv4，2：ipv6）
     */
    @ApiModelProperty("IP类别（1：ipv4，2：ipv6）")
    private Integer ipType;

    /**
     * IP对象类别（1：ip范围，2：ip地址段：3：ip地址清单）
     */
    @ApiModelProperty("IP对象类别（1：ip范围，2：ip地址段：3：ip地址清单）")
    private Integer objectType;

    /**
     * IP地址范围，如1.0.0.1-1.0.0.19
     */
    @ApiModelProperty("IP地址范围，如1.0.0.1-1.0.0.19")
    private String ipRange;

    /**
     * IP地址段
     */
    @ApiModelProperty("IP地址段")
    private String ipPhase;

    /**
     * ip地址清单，多个用,拼接
     */
    @ApiModelProperty("ip地址清单，多个用,拼接")
    private String ipList;

}
