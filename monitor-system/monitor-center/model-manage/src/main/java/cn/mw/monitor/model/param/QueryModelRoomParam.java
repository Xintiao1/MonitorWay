package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 机房布局参数
 * @author qzg
 * @date 2022/3/04 14:44
 */
@Data
@ApiModel
public class QueryModelRoomParam {
    @ApiModelProperty("该位置是否已选，true：已被选择，false：未被选择")
    private Integer isSelected;

}
