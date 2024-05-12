package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/6/24
 */
public interface MwAssetsDataExportDao {
    List<MwAssetsExportTable> selectAssetsByAssetsTypeId(@Param("assetsTypeId") Integer assetsTypeId, @Param("assetsTypeSubId") Integer assetsTypeSubId);

    /**
     * 私有角色查询资产列表
     *
     * @param criteria 查询条件
     * @return
     */
    List<MwAssetsExportTable> selectPriList(Map criteria);

    /**
     * public 角色查询资产列表
     *
     * @param criteria 查询条件
     * @return
     */
    List<MwAssetsExportTable> selectPubList(Map criteria);

    List<Map> exportComponentLayout(MwAssetsDataExportDto param);

    List<AssetsTemplateIdBySubTypeIdDTO> getTemplateIdByAssetsSubId(Integer assetsTypeSubId);

    void insertDataInfo(AssetsComponentLayoutDTO dto);

    void batchInsertDataInfo(List<AssetsComponentLayoutDTO> list);

    void updateDataInfo(AssetsComponentLayoutDTO dto);

    void batchInsertNavigationBar(List<AddNavigationBarDTO> list);

    void insertNavigationBar(AddNavigationBarDTO dto);

    Integer queryNavigationBarId(AddNavigationBarDTO dto);

    Integer check(@Param("barName") String barName, @Param("templateId") String templateId);

    List<Map> checkAll();

    Integer check2(AssetsComponentLayoutDTO dto);

    List<Map> checkAll2();

    List<Map> selectNavigationBarInfo();

    List<Map> getSubTypeNameMap();
}
