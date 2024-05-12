package cn.mw.monitor.user.dao;


import cn.mw.monitor.service.user.model.MwRoleModulePermMapper;
import cn.mw.monitor.service.user.model.PageAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwRoleModulePermMapperDao {

    /**
     * 根据角色ID重置角色模块权限信息
     */
    int updateEnableByRoleId(@Param("roleId") Integer roleId);
    /**
     * 查询角色模块权限映射信息
     */
    List<PageAuth> selectModulePermByRoleId(@Param("roleId") Integer roleId);

    /**
     * 查询模块信息
     */
    @Deprecated
    List<PageAuth> selectModule(Map criteria);
    /**
     * 新增角色模块权限映射信息
     */
    int insert(@Param("list") List<MwRoleModulePermMapper> list);
    /**
     * 根据角色ID删除角色模块权限信息
     */
    int deleteByRoleId(@Param("roleId") Integer roleId);
    /**
     * 根据用户ID查询权限信息
     */
    List<MwRoleModulePermMapper> selectByUserId(Integer userId);

}