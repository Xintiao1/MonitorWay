package cn.mw.monitor.user.dao;

import org.apache.ibatis.annotations.Param;

public interface MwOrgMonitorMapperDao {

    /**
     * 根据机构id查询机构和监控关联关系
     */
    @Deprecated
    Integer countMonitorByOrgId(@Param("orgId") Integer orgId);

}
