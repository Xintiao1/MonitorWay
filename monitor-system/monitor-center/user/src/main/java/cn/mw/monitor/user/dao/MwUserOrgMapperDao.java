package cn.mw.monitor.user.dao;

import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.service.user.dto.UserOrgDto;
import cn.mw.monitor.user.dto.UserGroupDTO;
import cn.mw.monitor.user.dto.UserOrgDTO;
import cn.mw.monitor.user.model.MwUserOrgTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwUserOrgMapperDao {

    /**
     * 根据机构id删除机构和用户关联关系
     */
    int deleteBatchByOrgId(@Param("orgIds") List<Integer> orgIds);

    /**
     * 根据用户id删除机构和用户关联关系
     */
    int deleteBatchByUserId(@Param("userIds") List<Integer> userIds);

    /**
     * 新增用户和机构的关联关系
     */
    int insertBatch(@Param("list") List<MwUserOrgTable> list);

    /**
     * 根据机构id查询关联用户数量
     */
    Integer countByOrgId(@Param("orgId") Integer orgId);

    /**
     * 根据登录名查询当前用户机构的节点id
     */
    List<String> getOrgNodesByLoginName(@Param("loginName") String loginName);

    /**
     * 根据登录名查询当前用户机构的最小深度
     */
    Integer getMinDeepByLoginName(@Param("loginName") String loginName);

    /**
     * 根据用户id查询机构关联用户信息
     */
    List<MwUserOrgTable> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据机构id查询关联用户id
     */
    @Deprecated
    List<Integer> selectUserIdByOrgId(@Param("orgIds") List<Integer> orgIds);

    /**
     * 根据登录名查询当前用户机构的名称
     */
    List<String> getOrgNameByLoginName(@Param("nodes") String nodes);

    /**
     * 根据用户登录名称查询机构id和子机构
     */
    List<Integer> getOrgIdByUserId(@Param("nodes") String nodes);

    /**
     * 查詢所有机构
     */
    @Deprecated
    List<Integer> getAllOrgIdByUserId(@Param("loginName") String loginName);

    /**
     * 根据用户名查询关联机构id
     */
    List<Integer> getOrgIds(@Param("loginName") String loginName);

    /**
     * 根据登录名称查询角色id
     */
    String getRoleIdByLoginName(@Param("loginName") String loginName);

    @Deprecated
    String getRoleIdByUserId(Integer uid);

    @Deprecated
    List<Integer> getUserIdByOrgId(@Param("nodes") List<String> nodes, @Param("userId") Integer userId);

    //根据用户id查询用户权限（共有还是私有）
    String getRolePermByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户id查询用户关联机构
     */
    List<OrgDTO> selectOrgNameByUserId(@Param("userId") Integer userId);

    /**
     * 存入系统设置信息
     *
     * @param settingDTO
     * @return
     */
    int insertSettings(SettingDTO settingDTO);

    /**
     * 查询系统设置信息
     *
     * @return
     */
    SettingDTO selectSettingsInfo();

    /**
     * 查询用户关联机构
     */
    List<UserOrgDto> selectAllOrgWithUserInfo();

    List<UserOrgDTO> getUserListByOrgIds(@Param("orgIds") List<Integer> orgIds);
}
