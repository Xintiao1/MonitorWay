package cn.mw.monitor.service.user.dto;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className UserOrg
 * @description 包含用户信息的机构数据
 * @date 2023/3/7
 */
@Data
public class UserOrgDto extends OrgDTO{

    /**
     * 用户ID
     */
    private Integer userId;


    /**
     * 登录名
     */
    private String loginName;

}
