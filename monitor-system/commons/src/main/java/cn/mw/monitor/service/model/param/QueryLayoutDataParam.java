package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 机房布局参数
 *
 * @author qzg
 * @date 2022/3/04 14:44
 */
@Data
@ApiModel
public class QueryLayoutDataParam {
    @ApiModelProperty("该位置是否已选，true：已被选择，false：未被选择")
    private Boolean isSelected;
    @ApiModelProperty("该位置是否可选，true：不可选（表示外部原因，该位置不存在或者已撤销），false：可以选择")
    private Boolean isBan;
}
