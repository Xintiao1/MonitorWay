package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2023615 10:57
 * @description
 */
@Data
@ApiModel(value = "父节点")
@Accessors(chain = true)
public class FatherOption {
    @ApiModelProperty("ID")
    public String fatherId;

    @ApiModelProperty("name")
    public String name;

    @ApiModelProperty("对应选中子节点")
    List<ChildrenOption> childrenOptions;
}
