package cn.mw.monitor.service.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupUserDTO {

    private Integer userId;

    private String loginName;

    private String userName;

    private String userState;
    // 机构/部门
    private List<OrgDTO> userDepartment;

    private Integer sort;
}
