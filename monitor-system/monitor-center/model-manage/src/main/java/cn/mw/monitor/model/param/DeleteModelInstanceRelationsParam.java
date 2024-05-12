package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/2/25 11:13
 */
@Data
@ApiModel
public class DeleteModelInstanceRelationsParam {
    @ApiModelProperty("关系Id")
    private Integer instanceRelationsId;
    @ApiModelProperty("实例Id")
    private Integer instanceId;
}
