package cn.mw.monitor.service.user.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LoginInfo {

    private UserDTO user;

    private Set<String> moduleIds;

    private List<MWOrgDTO> orgs;

    private String dataPerm;

    private SettingDTO settings;

    private boolean modelAssetEnable;

}
