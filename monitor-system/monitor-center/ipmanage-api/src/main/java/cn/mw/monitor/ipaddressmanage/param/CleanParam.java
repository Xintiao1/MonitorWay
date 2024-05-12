package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("历史分类接口")
public class CleanParam  extends  Check{

    @ApiModelProperty(value="分组id")
    private Integer GraparentId;

    @ApiModelProperty(value="分组名称")
    private String GraparentName;

    @ApiModelProperty(value="分组名称")
    private Integer id;
}
