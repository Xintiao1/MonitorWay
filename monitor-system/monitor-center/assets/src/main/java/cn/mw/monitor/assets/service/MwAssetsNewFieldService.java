package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.dto.MwAssetsCustomFieldDto;
import cn.mw.monitor.customPage.dto.UpdateCustomColDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwAssetsNewFieldService
 * @Description 资产自定义字段接口
 * @Author gengjb
 * @Date 2022/7/5 10:28
 * @Version 1.0
 **/
public interface MwAssetsNewFieldService {

    /**
     * 新增资产自定义字段
     * @return
     */
    Reply addAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 删资产自定义字段
     * @return
     */
    Reply deleteAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 修改产自定义字段
     * @return
     */
    Reply updateAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 查询产自定义字段
     * @return
     */
    Reply selectAssetsCustomField(MwAssetsCustomFieldDto customFieldDto);

    /**
     * 获取资产自定义字段的值
     * @param mwTangAssetses
     */
    void getAssetsCustomFieldValue(List<MwTangibleassetsTable> mwTangAssetses);

    /**
     * 获取资产所有标签
     */
    Map<String,String> getAssetsAllLabel();

    /**
     * 资产字段排序
     * @param customColDTOS
     * @return
     */
    Reply assetsFieldSort(List<UpdateCustomColDTO> customColDTOS);

}
