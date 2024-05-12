package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/23 17:10
 */
@Data
@ApiModel
public class AssetsRelationsParam {
    @ApiModelProperty("左资产ID")
    private String leftTangibleId;
    @ApiModelProperty("右资产ID")
    private String rightTangibleId;
}
