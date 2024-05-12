package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.service.user.model.PageAuth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MwRoleDTO implements Serializable {

    // 角色ID
    private Integer id;
    // 角色ID
    private Integer roleId;
    // 角色名称
    private String roleName;
    // 角色描述
    private String roleDesc;
    // 功能权限集合
    private List<PageAuth> pageAuth;
    // 数据权限
    private String dataPerm;
    // 状态
    private String enable;
    // 版本
    private Integer version;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date createDate;
    // 修改时间
    private Date modificationDate;
    // 用户列表
    private List<MwSubUserDTO> userDTOS;
    //
    private List<MwModuleDTO> mwModuleDTOList;

    /**
     * 角色类别（1：超级管理员  2：自定义角色）
     */
    private Integer roleType;

    /**
     * 是否允许登录；0：不允许登录，1：允许登录
     */
    private Integer allowLogin;
}
