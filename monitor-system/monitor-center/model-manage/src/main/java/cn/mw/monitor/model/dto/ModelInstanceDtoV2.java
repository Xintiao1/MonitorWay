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
public class ModelInstanceDtoV2 {
    @ApiModelProperty("模型Id")
    private Long modelId;

    @ApiModelProperty("模型实例id")
    private Long instanceId;

    @ApiModelProperty("模型实例名称")
    private String instanceName;

    @ApiModelProperty("实例拓扑关联信息")
    private ModelInstanceTopoInfo topoInfo;

}
