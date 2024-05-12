package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/3/8 12:11
 */
@Data
@ApiModel
public class MwModelFilterInterfaceParam {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("资产接口过滤名称")
    private String filterField;
    @ApiModelProperty("过滤以该字段开头的接口名称")
    private String noStartWith;
    @ApiModelProperty("以该字段开头的为光口接口")
    private String cableStartWith;

}
