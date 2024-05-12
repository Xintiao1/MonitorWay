package cn.mw.monitor.assets.dto;

import cn.mw.monitor.service.assets.model.OrgDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/31
 */
@Data
@ApiModel(value = "子页面使用的用户DTO")
public class MwSubUserDTO {
    @ApiModelProperty(value="用户ID")
    private Integer userId;
    @ApiModelProperty(value="用户名")
    private String loginName;
    @ApiModelProperty(value="姓名")
    private String userName;
    @ApiModelProperty(value="部门")
    private List<OrgDTO> department;
    @ApiModelProperty(value="用户状态")
    private String userState;
}
