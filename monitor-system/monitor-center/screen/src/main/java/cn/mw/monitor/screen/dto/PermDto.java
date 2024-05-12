package cn.mw.monitor.screen.dto;

/**
 * @author xhy
 * @date 2020/5/21 16:06
 */

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PermDto extends BaseParam {
    private String enable;
    private String screenId;
    private String perm;
    private Integer userId;
    private List<Integer> groupIds;
    private List<Integer> orgIds;
    private Boolean isAdmin;
//    @ApiModelProperty(value="机构")
//    private List<List<Integer>> orgIds;
//
//    @ApiModelProperty(value="负责人")
//    private List<Integer> userIds;
//
//    @ApiModelProperty(value="用户组")
//    private List<Integer> groupIds;

}
