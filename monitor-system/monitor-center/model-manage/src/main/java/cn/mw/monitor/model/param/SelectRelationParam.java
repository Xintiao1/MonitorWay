package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/3/8 12:11
 */
@Data
@ApiModel
public class SelectRelationParam  extends BaseParam {
    @ApiModelProperty("模型Id")
    private Integer modelId;
}
