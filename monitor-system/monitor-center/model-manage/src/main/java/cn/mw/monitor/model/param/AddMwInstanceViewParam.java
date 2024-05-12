package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AddMwInstanceViewParam {
    @ApiModelProperty("视图名称")
    @NotBlank
    private String viewName;

    @ApiModelProperty("关联实例id")
    @NotNull
    private Integer instanceId;
}
