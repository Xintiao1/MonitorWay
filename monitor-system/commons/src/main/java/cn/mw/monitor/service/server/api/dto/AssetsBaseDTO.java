package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/24 14:55
 * @Version 1.0
 */
@Data
public class AssetsBaseDTO {
    //    资产主键id
    @ApiModelProperty("资产主键id")
    private String id;
    //    主机id
    @ApiModelProperty("主机id")
    private String assetsId;
    //    第三方监控服务器id
    @ApiModelProperty("第三方监控服务器id")
    private int monitorServerId;
    //    资产关联带外ip
    @ApiModelProperty("资产关联带外ip")
    private String outBandIp;

    @ApiModelProperty("资产关联模板id")
    private String templateId;

    @ApiModelProperty("第三方监控项名称")
    private String itemName;

    private Integer modelId;

    private String ip;

}
