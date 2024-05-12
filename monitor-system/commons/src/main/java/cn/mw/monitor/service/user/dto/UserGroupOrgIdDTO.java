package cn.mw.monitor.service.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lstart
 * @date 2021/6/11 - 15:01
 */

@Data
@ApiModel("负责人组部门idDTO")
public class UserGroupOrgIdDTO {

    @ApiModelProperty("负责人id")
    private Integer userId;

    @ApiModelProperty("组id")
    private Integer groupId;

    @ApiModelProperty("部门id")
    private Integer orgId;

}
