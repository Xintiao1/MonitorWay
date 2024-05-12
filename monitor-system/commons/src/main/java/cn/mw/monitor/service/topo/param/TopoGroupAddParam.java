package cn.mw.monitor.service.topo.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TopoGroupAddParam {

    @ApiModelProperty(value = "父分组id")
    @NotEmpty(message = "父分组id不能为空!")
    private String parentId;

    @ApiModelProperty(value = "分组名称")
    @NotEmpty(message = "分组名称不能为空!")
    private String name;

    @ApiModelProperty(value = "责任人")
    @NotEmpty(message = "责任人不能为空!")
    private List<Integer> respPerson;

    @ApiModelProperty(value = "机构")
    @NotEmpty(message = "机构不能为空!")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
}
