package cn.mw.monitor.service.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ModelInstanceBaseInfoDTO {
    @ApiModelProperty("es的索引")
    private String modelIndex;

    @ApiModelProperty("es的实例id")
    private String modelInstanceId;

    @ApiModelProperty("mw_cmdbmd_instance表id")
    private Integer instanceId;

    @ApiModelProperty("mw_cmdbmd_manage表id")
    private Integer modelId;

    private String esId;

    @ApiModelProperty("模型名称")
    private String modelName;
}
