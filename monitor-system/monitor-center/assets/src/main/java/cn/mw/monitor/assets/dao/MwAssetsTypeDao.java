package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.service.assets.model.AssetTypeIconDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/4/16 9:18
 * @Version 1.0
 */
public interface MwAssetsTypeDao {

    /**
     * 获取资产的资产类型数据列表
     *
     * @param criteria 查询参数
     * @return 资产的资产类型数据
     */
    List<AssetsTreeDTO> selectAssetsTypeList(Map criteria);

    /**
     * 获取资产的品牌列表
     *
     * @param criteria 查询参数
     * @return 资产的品牌列表数据
     */
    List<AssetsTreeDTO> selectAssetsVendorList(Map criteria);

    /**
     * 获取资产的标签数据列表（同一个资产会有多个标签）
     *
     * @param criteria 查询参数
     * @return 资产的标签列表数据
     */
    List<AssetsTreeDTO> selectAssetsLabelList(Map criteria);

    /**
     * 获取机构下的资产数据信息
     *
     * @param criteria 查询参数
     * @return 当前机构下的所有资产数据
     */
    List<AssetsDTO> selectAssetsOrgList(Map criteria);

    List<Integer> selectTPServerIds();

    /**
     * 根据用户权限数据查询所有的资产数据列表
     *
     * @param criteria 查询参数
     * @return 用户可查看的所有资产数据
     */
    List<AssetsDTO> selectAllAssets(Map criteria);

    List<AssetsTreeDTO> selectAssetsType(@Param("ids") List<String> ids);

    List<String> selectAssetsMonitorStatus(@Param("assetsIds") List<String> assetsIds);

    /**
     * 根据组织ID获取资产数据信息
     *
     * @param criteria 查询参数
     * @return 资产数据
     */
    List<AssetsDTO> selectAssetsByGroupIds(Map criteria);

    /**
     * 根据用户ID获取资产数据信息
     *
     * @param criteria 查询参数
     * @return 资产数据
     */
    List<AssetsDTO> selectAssetsByUserId(Map criteria);

    /**
     * 获取资产的用户组数据列表
     *
     * @param criteria 查询参数
     * @return 资产的用户组数据
     */
    List<AssetsTreeDTO> selectAssetsUserGroupList(Map criteria);

    List<AssetTypeIconDTO> selectAllAssetsTypeIcon();
}
