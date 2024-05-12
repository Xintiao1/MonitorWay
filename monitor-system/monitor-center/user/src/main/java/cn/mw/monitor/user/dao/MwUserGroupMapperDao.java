package cn.mw.monitor.user.dao;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.user.dto.UserGroupDTO;
import cn.mw.monitor.user.model.MwUserGroupTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwUserGroupMapperDao {

    /**
     * 根据用户组id删除用户组和用户关联关系
     */
    int deleteBatchByGroupId(@Param("groupIds") List<Integer> groupIds);
    /**
     * 根据用户id删除用户组和用户关联关系
     */
    int deleteBatchByUserId(@Param("userIds") List<Integer> userIds);
    /**
     * 新增用户和用户组的关联关系
     */
    int insertBatch(@Param("list") List<MwUserGroupTable> list);
    /**
     * 根据用户组id查询关联用户数量
     */
    Integer countByGroupId(@Param("groupId") Integer groupId);
    /**
     * 根据用户id查询用户组和用户关联关系
     */
    @Deprecated
    List<MwUserGroupTable> selectByUserId(Integer userId);
    /**
     * 根据用户登录名称查询用户组id
     */
    List<Integer> getGroupIdByLoginName(String loginName);

    @Deprecated
    List<Integer> selectGroupUserIdByUserId(Integer userId);

    String getUserIdIdByLoginName(String loginName);

    List<String> getWxOpenId(String loginName);

    /**
     * 获取所有的用户组信息
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 所有的用户组数据
     */
    List<GroupDTO> getAllGroupList(@Param(value = "typeId") int typeId,
                                   @Param(value = "dataType") String dataType);

    List<String> getGroupnamesByids(@Param("groupIds") List<Integer> groupIds);

    List<UserGroupDTO> getUserListByGroupIds(@Param("groupIds") List<Integer> groupIds);
}
