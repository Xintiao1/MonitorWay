package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwModulePermMapper {

    // id
    private Integer id;
    // 模块id
    private Integer moduleId;
    // 权限id
    private Integer permId;
    // 模块url
    private String url;
    // 权限名称
    private String permName;

}
