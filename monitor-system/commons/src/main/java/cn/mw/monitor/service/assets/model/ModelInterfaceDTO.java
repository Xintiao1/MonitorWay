package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ModelInterfaceDTO {
    private String name;
    private String hostId;//主机监控Id
    @ApiModelProperty("接口状态")
    private String state;
    @ApiModelProperty("资产Id")
    private String assetsId;
    private String hostIp;
}
