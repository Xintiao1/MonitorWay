package cn.mw.monitor.user.dto;

import lombok.Data;

@Data
public class MwRoleModulePermMapDTO {
    private String id;
    private Integer roleId;
    private Integer moduleId;
    private String permName;
    private Boolean enable;
}
