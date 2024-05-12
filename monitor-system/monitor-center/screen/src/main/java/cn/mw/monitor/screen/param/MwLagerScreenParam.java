package cn.mw.monitor.screen.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/9 10:53
 */
@Data
public class MwLagerScreenParam   {

    @ApiModelProperty("大屏id")
    private String screenId;

    @ApiModelProperty("投屏名称")
    private String screenName;

    @ApiModelProperty("投屏描述")
    private String screenDesc;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("修改人")
    private String modifier;

    @ApiModelProperty("布局id")
    private Integer layoutId;

    @ApiModelProperty("是否有用户id")
    private Integer isUserId;

    @ApiModelProperty("是否有用户组id")
    private Integer isGroupId;

    @ApiModelProperty("用户ids")
    private List<Integer> userIds;

    @ApiModelProperty("用户组ids")
    private List<Integer> groupIds;

    @ApiModelProperty("机构ids")
    private List<List<Integer>> orgIds;

    private Integer screenType;
}
