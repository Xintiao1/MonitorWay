package cn.mw.monitor.assets.dao;

import cn.mw.monitor.service.assets.param.AddUpdateOutbandAssetsParam;
import cn.mw.monitor.assets.api.param.assets.QueryLabelParam;
import cn.mw.monitor.service.assets.param.QueryOutbandAssetsParam;
import cn.mw.monitor.assets.api.param.assets.UpdateMonStateParam;
import cn.mw.monitor.assets.api.param.assets.UpdateSetStateParam;
import cn.mw.monitor.assets.dto.MwOutbandAssetsDTO;
import cn.mw.monitor.assets.dto.OutbandWithAssetsDTO;
import cn.mw.monitor.assets.model.MwOutbandAssetsTable;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/5/22 9:46
 * @Version 1.0
 */
public interface MwOutbandAssetsTableDao {
    /**
     * 新增资产
     *
     * @param record 资产信息
     * @return
     */
    int insert(AddUpdateOutbandAssetsParam record);

    /**
     * 批量新增资产
     *
     * @param assetsList 资产信息
     * @return
     */
    int insertBatch(List<AddUpdateOutbandAssetsParam> assetsList);

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
    int update(AddUpdateOutbandAssetsParam record);

    /**
     * 批量修改资产信息
     *
     * @param record
     * @return
     */
    int updateBatch(AddUpdateOutbandAssetsParam record);

    /**
     * 根据ID查询资产信息
     *
     * @param id 资产信息
     * @return
     */
    MwOutbandAssetsDTO selectById(String id);
    /**
     *私有角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<MwOutbandAssetsTable> selectPriList(Map criteria);
    /**
     *私有角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<MwOutbandAssetsTable> selectPubList(Map criteria);

    /**
     * 查询资产列表
     *
     * @param criteria 查询条件
     * @return
     */
//    List<MwOutbandAssetsDTO> selectList(Map criteria);

    /**
     * 新增重复检查
     *
     * @param  checkParam 查询条件
     * @return
     */
    List<MwOutbandAssetsDTO> check(QueryOutbandAssetsParam checkParam);

    List<MwAllLabelDTO> selectAllLabel(QueryLabelParam labelParam);

    int updateAssetsState(UpdateAssetsStateParam updateAssetsStateParam);

    int updateAssetsMonState(UpdateMonStateParam updateMonStateParam);

    int updateAssetsSetState(UpdateSetStateParam updateSetStateParam);

    List<String> selectIPDropdownList();

    /**
     * 查询所有资产信息
     *
     * @return List<MwTangibleassetsTable>
     */
    List<MwOutbandAssetsTable> selectTopoAssetsList();

    int updateTemplateId(@Param("templateId") String templateId, @Param("id") String id);

    void updateTemplateIdBatch(@Param("list") List<MwAssetsIdsDTO> list);

    OutbandWithAssetsDTO selectRelevanceByOBIds(String id);

    /**
     * 根据IP查询有形资产信息
     * @param outAssetsIps
     * @return
     */
    List<String> selectTangibleAssetsByIps(@Param("ips") List<String> outAssetsIps);
}
