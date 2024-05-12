package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/2/19
 */
@Data
@ApiModel
public class MwModelAuthNameParam {
    @ApiModelProperty("凭证名称")
    private String authName;
    private List<MwModelMacrosValInfoParam> macrosParam;

}
