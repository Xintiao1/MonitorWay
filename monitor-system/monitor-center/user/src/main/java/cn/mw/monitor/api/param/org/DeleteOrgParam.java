package cn.mw.monitor.api.param.org;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("删除机构数据")
public class DeleteOrgParam {

    @Valid
    @Size(min = 1, message = "机构id不能为空！")
    @ApiModelProperty("机构id集合")
    private List<Integer> orgIds;

}
