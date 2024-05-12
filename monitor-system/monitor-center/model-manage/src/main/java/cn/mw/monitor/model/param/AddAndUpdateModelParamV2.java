package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelInfoV2;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/5 14:24
 *
 */
@Data
@ApiModel
public class AddAndUpdateModelParamV2 extends ModelInfoV2 {
    @ApiModelProperty("模型分组子ID ")
    private List<Long> modelGroupSubId;
}
