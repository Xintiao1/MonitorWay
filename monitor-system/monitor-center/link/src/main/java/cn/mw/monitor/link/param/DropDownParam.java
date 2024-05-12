package cn.mw.monitor.link.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/7/21 9:28
 */
@Data
public class DropDownParam {
    @ApiModelProperty("资产主键id")
    private String id;
    private String hostid;
    private String itemName;
    private String assetsName;
    private String ipAddress;
    //是否从zabbix中获取ip地址
    private Boolean isGetByZabbix;
    private Integer monitorServerId;

    private String monitorModeName;//监控方式


    private String valuePort;

}
