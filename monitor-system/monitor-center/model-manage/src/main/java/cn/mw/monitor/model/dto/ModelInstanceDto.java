package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/25 10:20
 */
@Data
@ApiModel
public class ModelInstanceDto {
    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型实例id")
    private Integer instanceId;

    @ApiModelProperty("模型实例名称")
    private String instanceName;

    @ApiModelProperty("实例拓扑关联信息")
    private ModelInstanceTopoInfo topoInfo;

    @ApiModelProperty("代理引擎id")
    private String proxyId;
}
