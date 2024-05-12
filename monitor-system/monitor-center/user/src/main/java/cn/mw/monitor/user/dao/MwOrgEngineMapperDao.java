package cn.mw.monitor.user.dao;

import org.apache.ibatis.annotations.Param;

public interface MwOrgEngineMapperDao {

    /**
     * 根据机构id查询机构和引擎关联关系
     */
    Integer countEngineByOrgId(@Param("orgId")Integer orgId);

}
