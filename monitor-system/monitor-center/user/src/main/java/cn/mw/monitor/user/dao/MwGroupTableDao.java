package cn.mw.monitor.user.dao;

import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.model.MwGroupTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwGroupTableDao {

    /**
     * 新增用户组信息
     */
    int insert(MwGroupTable mwGroupTable);
    /**
     * 根据用户组id查询用户组名称
     */
    String selectGroupNameById(@Param("groupId")Integer groupId);
    /**
     * 根据用户组id查询用户组和资产关联关系
     */
    Integer countGroupAssetsByGroupId(@Param("groupId")Integer groupId);
    /**
     * 根据用户组id查询用户组和监控关联关系
     */
    Integer countGroupMonitorByGroupId(@Param("groupId")Integer groupId);
    /**
     * 删除用户组信息
     */
    int delete(@Param("groupIds")List<Integer> groupIds, @Param("modifier")String modifier);
    /**
     * 更新用户组状态信息
     */
    int updateGroupState(MwGroupTable mwGroupTable);
    /**
     * 更新用户组信息
     */
    int update(MwGroupTable mwGroupTable);
    /**
     * 分页查询用户组列表信息
     */
    List<MwGroupDTO> selectList(Map record);
    /**
     * 根据用户组id获取用户组信息
     */
    MwGroupDTO selectById(@Param("groupId")Integer groupId);

    /**
     * 用户组下拉框查询
     */
    List<MwGroupDTO> selectDropdown(@Param("loginName")String loginName);
    /**
     * 用户组关联用户查询
     */
    List<GroupUserDTO> selectGroupUser(@Param("groupId")Integer groupId);

    List<GroupUserDTO> selectGroupUsers(List<Integer> groupIds);
    /**
     * 根据用户名查询用户组信息
     */
    List<MwGroupDTO> selectListByLoginName(String loginName);

    Integer selectByLoginName(String groupName);

    /**
     * 根据用户组名称获取用户组数据
     *
     * @param groupNames 用户组名称
     * @return 用户组
     */
    List<Integer> selectGroupIdsByGroupNames(List<String> groupNames);

    /**
     * 根据机构ID获取机构信息
     * @param groupId 机构ID
     * @return
     */
    List<OrgDTO> selectOrg(@Param("groupId") int groupId);

    /**
     * 根据用户ID获取用户所属的机构信息
     *
     * @param userId 用户ID
     * @return
     */
    List<OrgDTO> selectUserOrg(@Param(value = "userId") int userId);
}
