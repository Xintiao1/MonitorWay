package cn.mw.monitor.user.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwOrgAssetsMapperDao {

    /**
     * 根据机构id查询机构和资产关联关系
     */
    @Deprecated
    Integer countAssetsByOrgId(@Param("orgId") Integer orgId);
    /**
     * 删除机构和资产关联关系表
     */
    int deleteOrgMapper(List<Integer> orgIds);

}
