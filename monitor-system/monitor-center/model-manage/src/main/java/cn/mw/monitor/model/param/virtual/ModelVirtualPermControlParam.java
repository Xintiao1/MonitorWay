package cn.mw.monitor.model.param.virtual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/27
 */
@Data
public class ModelVirtualPermControlParam {
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    @ApiModelProperty("type类型")
    private String type;

    @ApiModelProperty("备注")
    private String desc;

}
