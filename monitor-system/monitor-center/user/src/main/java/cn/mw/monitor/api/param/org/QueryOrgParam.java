package cn.mw.monitor.api.param.org;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("查询机构列表数据")
public class QueryOrgParam extends BaseParam {

    @ApiModelProperty("机构id")
    private Integer orgId;

    @ApiModelProperty("机构上级id")
    private Integer pid;

    private List<Integer> orgIds;

    private String orgName;

}
