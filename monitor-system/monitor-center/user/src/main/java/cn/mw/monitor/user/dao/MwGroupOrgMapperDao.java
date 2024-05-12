package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwGroupOrgTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwGroupOrgMapperDao {

    /**
     * 新增用户组和机构关联关系
     */
    int insertBatch(@Param("list")List<MwGroupOrgTable> list);
    /**
     * 根据用户组id删除用户组和机构关联关系
     */
    int deleteBatchByGroupId(@Param("groudIds")List<Integer> groudIds);
    /**
     * 根据机构id删除用户组和机构的关联关系
     */
    int deleteBatchByOrgId(@Param("orgIds")List<Integer> orgIds);
    /**
     * 根据机构id查询用户组和机构的关联关系数量
     */
    Integer countByOrgId(@Param("orgIds")Integer orgIds);
    /**
     * 根据用户组id删除用户组和资产关联关系
     */
    int deleteGroupMapper(@Param("groupIds")List<Integer> groupIds);

}
