package cn.mw.monitor.user.model;

import lombok.Data;

import java.util.Date;

@Data
public class MwRole {

    // 角色ID
    private Integer id;
    // 角色ID
    private Integer roleId;
    // 角色名称
    private String roleName;
    // 角色描述
    private String roleDesc;
    // 数据权限
    private String dataPerm;
    // 状态
    private String enable;
    // 版本
    private Integer version;
    // 创建人
    private String creator;
    // 创建时间
    private Date createDate;
    // 修改人
    private String modifier;
    // 修改时间
    private Date modifiactionDate;

    /**
     * 角色类别（1：超级管理员  2：自定义角色）
     */
    private Integer roleType;

    /**
     * 是否允许登录；0：不允许登录，1：允许登录
     */
    private Integer allowLogin;
}
