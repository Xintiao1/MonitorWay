package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import lombok.Data;

@Data
public class MwPermissionDTO extends BaseDTO {

    // 权限id
    private Integer id;
    // 权限名称
    private String permName;
    // 权限描述
    private String permDesc;
    // 权限版本
    private Integer version;
    // 权限状态
    private Boolean enable;

}