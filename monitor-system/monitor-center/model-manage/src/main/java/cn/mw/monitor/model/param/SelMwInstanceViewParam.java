package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SelMwInstanceViewParam {
    @ApiModelProperty("视图ID")
    private Long id;

    @ApiModelProperty("视图名")
    private String viewName;

    @ApiModelProperty("关联实例id")
    private Integer instanceId;
}
