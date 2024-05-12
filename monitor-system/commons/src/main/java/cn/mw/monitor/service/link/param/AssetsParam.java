package cn.mw.monitor.service.link.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/12/3 11:07
 */
@Data
public class AssetsParam {
    //    @ApiModelProperty("资产主键")
//    private String id;
    @ApiModelProperty("资产id")
    private String assetsId;
    @ApiModelProperty("资产名称")
    private String assetsName;

    //    @ApiModelProperty("资产非管理ip地址")
//    private String ipAddress;
//    @ApiModelProperty("源端口")
//    private String port;
    @ApiModelProperty("zabbix服务器id")
    private Integer monitorServerId;
}
