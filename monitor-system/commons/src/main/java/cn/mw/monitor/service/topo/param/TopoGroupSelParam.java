package cn.mw.monitor.service.topo.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class TopoGroupSelParam {
    @ApiModelProperty(value = "分组id")
    private String id;

    @ApiModelProperty(value = "父分组id")
    private String parentId;
}
