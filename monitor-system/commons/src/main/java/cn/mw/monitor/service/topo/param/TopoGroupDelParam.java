package cn.mw.monitor.service.topo.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TopoGroupDelParam {
    @ApiModelProperty(value = "分组id")
    @NotEmpty(message = "id不能空")
    private List<String> ids;
}
