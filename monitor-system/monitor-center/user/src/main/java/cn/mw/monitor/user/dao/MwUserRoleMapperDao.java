package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwUserRoleMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwUserRoleMapperDao {

    /**
     * 根据角色id查询关联用户数量
     */
    Integer countByRoleId(@Param("roleId")Integer roleId);
    /**
     * 新增用户角色映射
     */
    int insertUserRoleMapper(MwUserRoleMap userRoleMap);

    int deleteUserRoleByUserId(@Param("userIds")List<Integer> userIds);
}
