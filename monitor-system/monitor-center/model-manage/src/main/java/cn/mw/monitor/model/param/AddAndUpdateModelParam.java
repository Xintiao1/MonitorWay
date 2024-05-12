package cn.mw.monitor.model.param;

import cn.mw.monitor.service.activiti.param.BaseProcessParam;
import cn.mw.monitor.service.model.dto.ModelInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/5 14:24
 *
 */
@Data
@ApiModel
public class AddAndUpdateModelParam extends ModelInfo {
    @ApiModelProperty("模型分组子ID ")
    private List<Integer> modelGroupSubId;
}
