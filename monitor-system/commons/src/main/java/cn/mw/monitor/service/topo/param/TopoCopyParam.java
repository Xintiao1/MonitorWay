package cn.mw.monitor.service.topo.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TopoCopyParam {
    @ApiModelProperty(value = "拓扑id")
    @NotEmpty(message = "id不能空")
    private String id;

    /**
     * 类别  1：拓扑网图，2：拓扑标签  3：机构分组
     */
    @ApiModelProperty(value = "类别")
    private int type = 1;

}
