package cn.mw.monitor.labelManage.dao;

import cn.mw.monitor.dropDown.api.param.AddDropDownParam;
import cn.mw.monitor.dropDown.model.MwDropdownTable;
import cn.mw.monitor.labelManage.api.param.AddUpdateLabelManageParam;
import cn.mw.monitor.labelManage.dto.*;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import cn.mw.monitor.labelManage.api.param.UpdateLabelStateParam;
import cn.mw.monitor.labelManage.model.MwLabelAssetsTypeMapper;
import cn.mw.monitor.labelManage.model.MwLabelModuleMapper;
import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.label.model.QueryLabelParam;
import cn.mw.monitor.service.label.model.MWCommonLabel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MwLabelManageTableDao {

    /**
     * 新增标签信息
     */
    int insert(AddUpdateLabelManageParam record);

    /**
     * 新增标签和资产类型关联关系
     */
    int createAssetsTypeLabelMapper(@Param("list") List<MwLabelAssetsTypeMapper> list);

    /**
     * 新增标签和模块类型关联关系
     */
    int createModuleLabelMapper(@Param("list") List<MwLabelModuleMapper> list);

    /**
     * 根据标签id查询标签名称
     */
    Map<String, Object> selectLabelMsgById(@Param("labelId") Integer labelId);

    /**
     * 删除标签信息
     */
    int delete(@Param("labelIds") List<Integer> labelIds, @Param("modifier") String modifier);

    /**
     * 删除标签和资产类型关联关系
     */
    int deleteAssetsTypeLableMapper(@Param("labelIds") List<Integer> labelIds);

    /**
     * 删除标签和模型关联关系
     */
    int deleteModelLabelapper(@Param("labelIds") List<Integer> labelIds);

    /**
     * 变更标签状态
     */
    int updateState(UpdateLabelStateParam stateParam);

    /**
     * 更新标签信息
     */
    int update(AddUpdateLabelManageParam record);

    /**
     * 分页查询标签列表信息
     */
    List<MwLabelManageDTO> selectList(Map record);

    /**
     * 根据标签ID查询标签信息
     */
    MwLabelManageDTO selectById(@Param("labelId") Integer labelId);

    /**
     * 根据标签id查询标签关联资产类型信息
     */
    List<MwAssetsTypeDTO> selectAssetsType(@Param("labelId") Integer labelId);

    List<MwModuleTypeDTO> selectModuleType(@Param("labelId") Integer labelId);

    List<MwLabelAssetsTypeDTO> getLabelListByAssetsTypeId(Integer assetsTypeId);


    /**
     * 根据标签查询模块id
     *
     * @param commonLabel
     * @return
     */
    List<String> getIdListByLabel(MWCommonLabel commonLabel);

    List<String> getAssetsIdByLabel(List<Integer> filterLabelIds,@Param("moduleType") String moduleType);

    int createLabelMapper(List<MwAssetsLabelDTO> list);

    int createLabelDateMapper(List<MwAssetsLabelDTO> list);

    int createLabelDropMapper(List<MwAssetsLabelDTO> list);

    List<MwAllLabelDTO> selectLabelBrowse(QueryLabelParam queryLabelParam);

    List<MwAssetsLabelDTO> selectLabelBoard(String typeId, String moduleType);

    List<MwAssetsLabelDTO> selectLabelBoards(@Param("typeIds") List<String> typeIds, @Param("moduleType")String moduleType);

    List<MwAssetsLabelDTO> selectLabelDateBoard(String typeId, String moduleType);

    List<MwAssetsLabelDTO> selectLabelDateBoards(@Param("typeIds") List<String> typeIds, @Param("moduleType")String moduleType);

    List<MwAssetsLabelDTO> selectLabelDropBoard(String typeId, String moduleType);

    List<MwAssetsLabelDTO> selectLabelDropBoards(@Param("typeIds") List<String> typeIds, @Param("moduleType")String moduleType);

    int deleteLabelBoard(String typeId, String moduleType);

    int deleteLabelDateBoard(String typeId, String moduleType);

    int deleteLabelDropBoard(String typeId, String moduleType);

    int deleteLabelBoards(@Param("list") List<String> typeIds, @Param("moduleType") String moduleType);

    int deleteLabelDateBoards(@Param("list") List<String> typeIds, @Param("moduleType") String moduleType);

    int deleteLabelDropBoards(@Param("list") List<String> typeIds, @Param("moduleType") String moduleType);

    List<String> getTypeIdsByTagboard(@Param("tagboardList") List<LogicalQueryLabelParam> tagboardList, @Param("operation") String operation);

    List<MwAllLabelDTO> selectLabelBrowseByList(QueryLabelParam queryLabelParam);

    int getCountByLabelId(@Param("labelId") Integer labelId, @Param("tableName") String tableName);

    List<String> getLabelIdsByAssetsId(@Param("typeId") String id, @Param("moduleType") String name);

    List<Map<String, String>> getLabelsByAssetsId(@Param("typeId") String id, @Param("moduleType") String name);


//    int insertBatch(List<AddUpdateLabelManageParam> record);

//    int updateBatch(List<AddUpdateLabelManageParam> record);

    /**
     * 查询修改之前的标签信息
     * @param labelId 标签ID
     * @return
     */
    AddUpdateLabelManageParam getLableData(Integer labelId);

    /**
     * 查询文本类型的标签关联的类型和模块
     * @param labelId 标签ID
     * @return
     */
    List<Map<String,String>> getTextTypeIdAndModule(Integer labelId);

    /**
     * 查询下拉类型的标签关联的类型和模块
     * @param labelId 标签ID
     * @return
     */
    List<Map<String,String>> getDropDownTypeIdAndModule(Integer labelId);

    /**
     * 查询日期类型的标签关联的类型和模块
     * @param labelId 标签ID
     * @return
     */
    List<Map<String,String>> getDateTypeIdAndModule(Integer labelId);

    /**
     * 插入下拉框类型标签的键值
     * @param dropdownTable 下拉框类型键值数据
     */
    void insertDropdownTables(List<LabelEditConvertDto> dropdownTable);

    /**
     * 删除原来下拉框的数据，重新添加
     * @param labelCode
     */
    void deleteDropdownTables(String labelCode);

    /**
     * 擦汗如下拉框类型的关联信息
     * @param editConvertDtos 下拉框类型的关联模块数据
     */
    void insetLabelDropMapper( List<LabelEditConvertDto> editConvertDtos);

    /**
     * 删除原来的关联信息
     * @param labelId 标签ID
     * @param tableName 表名
     */
    void deleteLabelTextType(Integer labelId,String tableName);

    /**
     * 查询关联关系
     * @param dropids 下拉框类型值的主键
     * @return
     */
    List<Integer> selectAssociatedDropId(List<Integer> dropids);

    /**
     * 根据编号查询下拉信息
     * @param labelCode 编号
     * @return
     */
    List<Map<String,Object>> selectDropDownId(String labelCode);

    /**
     *修改下拉框数据
     * @param updateDropDown 修改修改的数据
     */
    void updateDropDownData(List<AddDropDownParam> updateDropDown);

    /**
     *新增下拉框数据
     * @param insertDropDown 新增的数据
     */
    void insertDropDownData(List<AddDropDownParam> insertDropDown);

    /**
     *删除下拉框数据
     * @param deleteDropDown 删除的数据
     */
    void deleteDropDownData(Set<Integer> deleteDropDown);

    /**
     * 根据标签ID查询标签编码
     * @param labelId 标签ID
     * @return
     */
    String getLabelCode(Integer labelId);

    /**
     *   查询是否有重读数据
     * @param labelCode  标签Code
     * @param dropId  标签ID
     * @param value  标签值
     * @return
     */
    List<Integer> getDropLabelRepeatData(@Param("labelCode") String labelCode,@Param("dropId") Integer dropId,@Param("dropValue") String value);

    /**
     * 根据标签code查询下拉类型KEY最大值
     * @param labelCode
     * @return
     */
    Integer getDropDownKeyMaxValue(@Param("labelCode") String labelCode);


    List<Map<String, Object>> fuzzyQueryLabelAllFiledData();

    List<String> fuzzyQueryLabelNames();

    List<MwDropdownTable> selectOldLabel();

    void updateById(@Param("delete") List<Integer> delete, @Param("updateId") Integer updateId);

    void updateDeleteById(@Param("id") Integer updateId);

    /**
     * 获取资产对应的标签数据列表
     * @return
     */
    List<MwAssetsLabelDTO> getAssetsSimplifyLabelList();

    /**
     * 根据资产的标签列表数据获取对应的资产ID列表
     *
     * @param labelList 标签列表
     * @return
     */
    List<String> getAssetsIdByLabelList(@Param(value = "list") List<MwAssetsLabelDTO> labelList);
}
