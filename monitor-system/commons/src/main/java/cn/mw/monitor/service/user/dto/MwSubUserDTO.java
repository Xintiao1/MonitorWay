package cn.mw.monitor.service.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class MwSubUserDTO {

    // 用户ID
    private Integer userId;
    // 用户名
    private String loginName;
    // 姓名
    private String userName;
    // 部门
    private List<OrgDTO> department;
    // 用户状态
    private String userState;

}
