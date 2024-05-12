package cn.mw.monitor.user.model;

import lombok.Data;

@Data
public class MwPermission {

    // 权限id
    private Integer id;
    // 权限名称
    private String permName;
    // 权限描述
    private String permDesc;
    // 权限版本
    private Integer version;

}