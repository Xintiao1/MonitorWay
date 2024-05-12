package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 */
@ApiModel
@Data
public class UpdateModelInstanceStateParam {

    private List<MwModelInstanceParam> paramList;

    @ApiModelProperty("状态类型")
    private String stateType;

    @ApiModelProperty("enable")
    private String enable;
}
