package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.AssetsLabelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/7/22 18:28
 * @Version 1.0
 */
public interface MwAssetsLabelDao {
    /**
     *私有角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<String> selectPriList(Map criteria);
    /**
     *public 角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<String> selectPubList(Map criteria);

    List<AssetsLabelDTO> selectAssetsLabels(@Param("moduleType") String moduleType, @Param("assetsIds") List<String> assetsIds);
}
