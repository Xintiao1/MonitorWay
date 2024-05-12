package cn.mw.monitor.service.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwRoleModulePermMapper {

    private String id;

    private Integer roleId;

    private Integer moduleId;

    private String permName;

    private Boolean enable;

}