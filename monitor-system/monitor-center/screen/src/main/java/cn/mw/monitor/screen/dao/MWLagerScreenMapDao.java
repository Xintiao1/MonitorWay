package cn.mw.monitor.screen.dao;

import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.screen.dto.LargeScreenAssetsInterfaceDto;
import cn.mw.monitor.screen.dto.LargeScreenMapDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MWLagerScreenMapDao
 * @Author gengjb
 * @Date 2022/9/2 9:21
 * @Version 1.0
 **/
public interface MWLagerScreenMapDao {

    //查询资产及资产接口
    List<LargeScreenAssetsInterfaceDto> selectAseetsAndInterface();

    //创建地图展示信息
    void insertScreenMapShowInformation(LargeScreenMapDto screenMapDto);

    //删除地图展示信息
    void deleteScreenMapShowInformation(@Param("id")Integer id,@Param("userId") Integer userId);

    //创建地图展示信息
    List<LargeScreenMapDto> selectScreenMapShowInformation(@Param("userId") Integer userId,@Param("orgId") Integer id);


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

    /**
     * 根据用户权限数据查询所有的资产数据列表
     *
     * @param criteria 查询参数
     * @return 用户可查看的所有资产数据
     */
    List<AssetsDTO> selectAllAssets(Map criteria);

    /**
     * 查询监控项中文名称
     * @param itemNames
     * @return
     */
    List<Map<String,String>> getItemNameByName(@Param("itemNames") List<String> itemNames);

    /**
     * 查询所有监控项
     * @return
     */
    List<Map<String,String>> selectScreenAllMonitorItem();

}
