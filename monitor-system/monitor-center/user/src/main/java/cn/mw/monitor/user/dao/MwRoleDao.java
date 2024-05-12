package cn.mw.monitor.user.dao;

import cn.mw.monitor.api.param.role.UpdateRoleStateParam;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.model.MwRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwRoleDao {

    /**
     * 新增角色信息
     */
    int insert(MwRole role);
    /**
     * 更新角色信息
     */
    int update(MwRole role);
    /**
     * 根据角色id查询角色名称
     */
    String selectRoleNameById(@Param("roleId")Integer roleId);
    /**
     * 删除角色信息
     */
    int delete(@Param("roleIds")List<Integer> roleIds, @Param("modifier")String modifier);
    /**
     * 更新角色状态信息
     */
    int updateUserState(UpdateRoleStateParam dParam);
    /**
     * 分页查询角色列表信息
     */
    List<MwRoleDTO> selectList(Map criteria);
    /**
     * 根据角色ID查询角色信息
     */
    MwRole selectByRoleId(@Param("roleId")Integer roleId);
    /**
     * 角色下拉框查询
     */
    List<MwRoleDTO> selectDropdownList();

    /**
     * 根据用户ID取角色信息
     */
    MwRole selectByUserId(@Param("userId")Integer userId);

    Integer selectByRoleName(@Param("roleName")String roleName);

    /**
     * 根据角色名称获取角色信息
     *
     * @param roleName 角色名称
     * @return 角色信息
     */
    MwRole selectRoleByRoleName(@Param("roleName") String roleName);

    /**
     * 根据角色ID获取用户信息
     *
     * @param roleId 角色ID
     * @return
     */
    List<MwSubUserDTO> selectUser(@Param(value = "id") int roleId);

    /**
     * 根据用户ID获取所属机构
     *
     * @param userId 用户ID
     * @return
     */
    List<OrgDTO> selectOrg(@Param(value = "userId") int userId);
}
