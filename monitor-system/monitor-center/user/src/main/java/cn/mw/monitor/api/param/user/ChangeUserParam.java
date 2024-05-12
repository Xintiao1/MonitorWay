package cn.mw.monitor.api.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className ChangeUserParam
 * @description 更换负责人参数
 * @date 2021/3/30
 */
@Data
@ApiModel("更换负责人数据参数")
public class ChangeUserParam {

    /**
     * 数据类型
     */
    @ApiModelProperty(value = "数据类型")
    private String dataType;

    /**
     * 待更改用户ID列表
     */
    @ApiModelProperty(value = "待更改用户ID列表")
    private List<Integer> userList;

    /**
     * 更改的用户ID列表
     */
    @ApiModelProperty(value = "更改的用户ID列表")
    private List<Integer> changedUserList;

    /**
     * 待更改的用户ID
     */
    @ApiModelProperty(value = "待更改的用户ID")
    private Integer userId;

    /**
     * 更改的用户ID
     */
    @ApiModelProperty(value = "更改的用户ID")
    private Integer changedUserId;
}
