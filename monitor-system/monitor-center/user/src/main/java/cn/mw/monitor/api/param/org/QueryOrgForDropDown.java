package cn.mw.monitor.api.param.org;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("机构下拉框查询数据")
public class QueryOrgForDropDown {

    @ApiModelProperty("机构id")
    private Integer orgId;

    @ApiModelProperty("机构上级id")
    private Integer pid;

    @ApiModelProperty("类型-->机构/部门")
    private String type;

    @ApiModelProperty("子机构集合")
    private List<String> nodeList;

}
