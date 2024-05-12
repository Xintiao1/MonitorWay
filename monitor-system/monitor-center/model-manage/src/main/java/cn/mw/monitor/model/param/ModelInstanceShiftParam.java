package cn.mw.monitor.model.param;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 9:11
 */
@Data
@ApiModel
public class ModelInstanceShiftParam {
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型名称")
    private String modelName;

}
