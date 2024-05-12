package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.api.param.assets.AddUpdateIntangAssetsParam;
import cn.mw.monitor.assets.api.param.assets.QueryIntangAssetsParam;
import cn.mw.monitor.assets.dto.MwIntangibleassetsDTO;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;

import java.util.List;
import java.util.Map;

/**
 * Created by baochengbin on 2020/3/12.
 */
public interface MwIntangibleassetsTableDao {

    /**
     * 新增资产
     *
     * @param record 资产信息
     * @return
     */
    int insert(AddUpdateIntangAssetsParam record);

    /**
     * 批量新增资产
     *
     * @param assetsList 资产信息
     * @return
     */
    int insertBatch(List<AddUpdateIntangAssetsParam> assetsList);

    /**
     * 根据ID删除资产信息
     *
     * @param idList 资产id
     * @return
     */
    int delete(List<String> idList);

    /**
     * 修改资产信息
     *
     * @param record
     * @return
     */
    int update(AddUpdateIntangAssetsParam record);

    /**
     * 批量修改资产信息
     *
     * @param updateList
     * @return
     */
    int updateBatch(List<AddUpdateIntangAssetsParam> updateList);

    /**
     * 根据ID查询资产信息
     *
     * @param id 资产信息
     * @return
     */
    MwIntangibleassetsDTO selectById(String id);

    /**
     * @param criteria
     * @return 私有角色查询资产
     */
    List<MwIntangibleassetsDTO> selectPriList(Map criteria);

    /**
     *
     * @param criteria
     * @return 公有角色查询资产
     */
    List<MwIntangibleassetsDTO> selectPubList(Map criteria);

    /**
     * 新增重复检查
     *
     * @param  checkParam 查询条件
     * @return
     */
    List<MwIntangibleassetsDTO> checkAdd(QueryIntangAssetsParam checkParam);

    List<MwIntangibleassetsDTO> selectLabelList(QueryIntangAssetsParam qParam);

    int updateAssetsState(UpdateAssetsStateParam updateAssetsStateParam);

//    List<AssetsStatesParam> getDropdown();
}
