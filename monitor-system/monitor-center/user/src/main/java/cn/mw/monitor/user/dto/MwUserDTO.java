package cn.mw.monitor.user.dto;

import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.user.dto.OrgDTO;
import lombok.Data;

import java.util.List;

@Data
public class MwUserDTO extends MWUser {

    // 机构/部门
    private List<OrgDTO> department;
    // 用户组
    private List<GroupDTO> userGroup;
    // 角色
    private RoleDTO role;
    // 角色id
    private String roleId;
    // 角色名称
    private String roleName;
    //
    private String userExpireStateName;
    //密码策略名称
    private String activePasswdPlanName;
    //
    private String loginStateName;

    /**
     * 用户组数据
     */
    private String userGroupString;

    /**
     * 部门组数据
     */
    private String departmentString;

    private List<String> subscribeRuleIds;
    private List<String> subscribeModelSystem;
}
