package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lumingming
 * @createTime 2023615 10:59
 * @description
 */
@Data
@ApiModel(value = "父节点")
@Accessors(chain = true)
public class ChildrenOption {
    @ApiModelProperty("ID")
    public String ChildId;

    @ApiModelProperty("name")
    public String name;
}
