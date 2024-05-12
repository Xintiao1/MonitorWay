package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.dto.MwAssetsCustomFieldDto;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwAssetsNewFieldDao
 * @Author gengjb
 * @Date 2022/7/5 10:47
 * @Version 1.0
 **/
public interface MwAssetsNewFieldDao {

    /**
     * 添加资产自定义字段
     * @return
     */
    int insertAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 修改资产自定义字段
     * @return
     */
    int updateAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 删除资产自定义字段
     * @return
     */
    int deleteAssetsCustomField(@Param("ids") List<Integer> ids);

    /**
     * 查询资产自定义字段
     * @return
     */
    List<MwAssetsCustomFieldDto> selectAssetsCustomField();

    /**
     * 查询所有资产的标签
     * @return
     */
    List<Map<String,Object>> selectAllAssetsLabel();

    /**
     * 根据名称查询数据库是否已经有该字段
     * @param itemName
     * @return
     */
    int selectCustomFieldCount(@Param("itemName") String itemName,@Param("id") Integer id);

    /**
     * 根据标签名称查询标签
     * @return
     */
    List<Map<String,Object>> selectAssetsLabelByLabelName(@Param("labelNames") List<String> labelNames);

    /**
     * 修改资产系统字段顺序
     * @return
     */
    int updateAssetsSysFieldOrder(@Param("dtos") List<UpdateCustomColDTO> customColDTO);

    /**
     * 修改资产自定义字段顺序
     * @return
     */
    int updateAssetsCustomFieldOrder(@Param("dtos") List<MwAssetsCustomFieldDto> customFieldDtos);

}
