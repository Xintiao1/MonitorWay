package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @date 2022/5/12
 */
@Data
public class QueryAssetsInterfaceParam {
    private Integer id;
    @ApiModelProperty("接口编号")
    private Integer ifIndex;
    @ApiModelProperty("接口名称")
    private String name;
    @ApiModelProperty("接口类型")
    private String type;
    @ApiModelProperty("接口状态")
    private String state;
    @ApiModelProperty("接口描述")
    private String description;
    @ApiModelProperty("Mac地址")
    private String mac;
    @ApiModelProperty("MTU")
    private Integer mtu;
    @ApiModelProperty("IP")
    private String ip;
    @ApiModelProperty("子网掩码")
    private String subnetMask;
    @ApiModelProperty("接口模式")
    private String ifMode;
    @ApiModelProperty("vlan")
    private String vlan;
    @ApiModelProperty("vlan标识")
    private Boolean vlanFlag;
    @ApiModelProperty("端口标识")
    private String portType;
    @ApiModelProperty("vrf")
    private String vrf;
    @ApiModelProperty("资产Id")
    private String assetsId;

    @ApiModelProperty("zabbix资产Id")
    private Integer zabbixAssetsId;

    @ApiModelProperty("接口设置状态")
    private Boolean interfaceSetState;

    private Integer monitorServerId;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    @ApiModelProperty("是否列表显示")
    private Boolean showFlag;
    private Boolean alertTag;

    private String hostIp;
    private String hostId;
    private Boolean editorDesc;

}
