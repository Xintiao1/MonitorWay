package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.AssetsDTO;
import cn.mw.monitor.assets.dto.AssetsTreeDTO;
import cn.mw.monitor.assets.param.MWTreeCustomClassifyParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName MWTreeCustomClassifyDao
 * @Description 树状结构自定义数据接口
 * @Author gengjb
 * @Date 2021/9/9 14:19
 * @Version 1.0
 **/
public interface MWTreeCustomClassifyDao {

    /**
     * 创建数据
     * @param classifyParam 树状结构自定义数据
     */
    void createTreeCustomClassify(MWTreeCustomClassifyParam classifyParam);


    /**
     * 删除数据
     * @param customIds 树状结构自定义ID集合
     */
    void deleteTreeCustomClassify(@Param("customIds") List<Integer> customIds);

    /**
     * 修改数据
     * @param classifyParam 树状结构自定义数据
     */
    void updateTreeCustomClassify(MWTreeCustomClassifyParam classifyParam);

    /**
     * 查询数据
     * @param classifyParam 树状结构自定义数据
     */
    List<MWTreeCustomClassifyParam> selectTreeCustomClassify(MWTreeCustomClassifyParam classifyParam);

    List<AssetsTreeDTO> selectAssetsVendorList(@Param("tableName") String tableName, @Param("settingEnable") int settingEnable,@Param("assetsIds") List<String> ids);

    List<AssetsTreeDTO> selectAssetsTypeList(@Param("moduleType") String moduleType, @Param("assetsSubTypeId") String assetsSubTypeId, @Param("tableName") String tableName, @Param("settingEnable") int settingEnable,@Param("assetsIds") List<String> ids);

    List<AssetsTreeDTO> selectAssetsLabelList(@Param("moduleType") String moduleType, @Param("tableName") String tableName, @Param("settingEnable") int settingEnable,@Param("assetsIds") List<String> ids);

    List<AssetsDTO> selectAllAssets(@Param("moduleType") String moduleType, @Param("tableName") String tableName, @Param("settingEnable") int settingEnable,@Param("assetsIds") List<String> ids);

    List<AssetsDTO> selectAssetsOrgList(@Param("moduleType") String moduleType, @Param("orgId") Integer orgId, @Param("tableName") String tableName, @Param("settingEnable") int settingEnable,@Param("assetsIds") List<String> ids);

    /**
     * 获取是否有重复名称
     * @param customName
     * @param customId
     * @return
     */
    Integer getCustomNameCount(@Param("customName") String customName,@Param("customId") Integer customId);
}
