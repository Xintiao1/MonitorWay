package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/3/15 17:15
 */
@Data
@ApiModel
public class SortFieldParam {
    @ApiModelProperty("排序字段")
    private String sortField;

    @ApiModelProperty("排序类型  0升序，1降序")
    private Integer sortType;
}
