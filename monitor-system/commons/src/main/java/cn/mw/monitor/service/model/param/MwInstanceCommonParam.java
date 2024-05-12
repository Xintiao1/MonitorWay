package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class MwInstanceCommonParam {
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
}
