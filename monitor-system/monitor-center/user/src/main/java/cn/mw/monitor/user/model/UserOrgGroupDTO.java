package cn.mw.monitor.user.model;

import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.dto.MwUserDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserOrgGroupDTO {
    List<MwUserDTO> users;
    List<MWOrgDTO> orgs;
    List<MwGroupDTO> groups;
    List<MwRoleDTO> roles;
}
