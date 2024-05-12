package cn.mw.monitor.model.dao;

import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/2/5 15:12
 */
public interface MwModelManageDao {

    int creatModel(AddAndUpdateModelParam addAndUpdateModelParam);

    int getModelMaxSort(Integer modelGroupId);

    int updateNodes(int modelId, String nodes);

    int updateModel(AddAndUpdateModelParam addAndUpdateModelParam);

    void batchUpdateModel(List<AddAndUpdateModelParam> list);

    int deleteModel(Integer modelId);

    Integer getBaseModelMaxId();

    List<ModelInfo> selectModelInfoWithParent(String modelName);

    //新增 start
    //根据模型id查询模型信息
    ModelInfo selectBaseModelInfoByIndex(String modelIndex);

    ModelInfo selectBaseModelInfoById(Integer modelId);

    ModelInfo selectModelInfoWithPropertyById(Integer modelId);

    List<ModelInfo> selectModelInfoByGroupId(Integer groupId);

    List<ModelInfo> selectChildreModelInfoByGroupId(Integer groupId);

    List<ModelInfo> selectModelPropertiesListWithParent(Map priCriteria);

    List<ModelInfo> selectModelListWithParent(Integer modelId);

    List<ModelInfo> selectModelInfoByIds(@Param("modelIdList") List<Integer> modelIdList);

    List<ModelInfo> selectAllParentModelInfo();

    List<ModelInfo> selectAllModelListWithParent();

    List<ModelInfo> selectListWithParentAndGroup(Integer modelId);

    List<ModelInfo> selectModelInfoByPids(String pids);

    List<ModelInfo> selectPropertiesByInstanceFuzzyQueryGroup(@Param("modelId") Integer modelId);

    List<ModelInfo> selectPropertiesByInstanceFuzzyQuery(@Param("modelId") Integer modelId);

    List<ModelInfo> selectAllModelInfo();

    List<ModelInfo> selectModelListByIds(@Param("modelIds") List<Integer> modelIds);

    List<ModelInfo> getBaseModelInfos();

    List<ModelInfo> getBaseModelInfosDisParent();

    int countInstanceIdsByModelId(Integer modelId);
    //新增 end

    List<MwModelManageDto> selectModelList(Map priCriteria);

    String selectModelNameById(Integer modelId);

    List<ModelInfo> selectModelListWithParentShow(Integer modelId);

    int bathDeleteModelRelations(@Param("ids") List<Integer> ids);

    List<ModelRelationsDto> selectModelRelationsByModelId(Map criteria);

    List<ModelRelationsGroupDto> selectModelRelationsGroup(Map priCriteria);

    int creatModelProperties(AddAndUpdateModelPropertiesParam propertiesParam);

    void createModelPropertiesToPageField(AddAndUpdateModelPageFieldParam param);

    void batchInsertPropertiesPageField(List<AddAndUpdateModelPageFieldParam> list);

    int insertPropertiesValueInfo(PropertiesValueParam param);

    void creatModelPropertiesStruct(List<ModelPropertiesStructDto> list);

    void increaseCustomcoldIdSeq(Integer size);

    Integer getCustomcolIdSeq();

    void increasePageFieldIdSeq(Integer size);

    Integer getPageFieldIdSeq();

    Integer getManageIdSeq();

    int insertPropertiesToCol(List<MwCustomcolTable> records);

    int updateModelProperties(AddAndUpdateModelPropertiesParam propertiesParam);

    void deletePropertiesStruct(@Param("modelId") Integer modelId, @Param("indexId") String indexId);

    void updateModelPropertiesSort(ModelPropertiesSortParam param);

    int deleteModelPropertiesByPropertiesId(Integer propertiesId);

    void updateModelPropertiesShowStatus(AddAndUpdateModelPropertiesParam propertiesParam);

    List<String> getPropertiesByManage(Integer modelId);

    List<String> getPropertiesByBaseShow(@Param("showType") Integer showType, @Param("modelId") Integer modelId);

    List<String> selectModelInstanceFiledBySecond(Integer modelId);

    int deleteModelPropertiesByModelId(Integer modelId);

    List<ModelPropertiesDto> selectModelPropertiesList(Map priCriteria);

    List<ModelPropertiesStructDto> getProperticesStructInfo(@Param("modelId") Integer modelId, @Param("indexId") String indexId);

    String getProperticesArrObj(@Param("modelId") Integer modelId, @Param("indexId") String indexId);

    void editorModelPropertiesType(AddAndUpdateModelPropertiesParam param);

    List<String> queryPropertiesTypeList(AddAndUpdateModelPropertiesParam param);

    List<AddAndUpdateModelParam> queryModelIndexList();

    List<MwModelInfoDTO> queryParentModelInfo();

    List<MwModelInfoDTO> queryOrdinaryModelInfo(AddAndUpdateModelGroupParam groupParam);

    List<MwModelInfoDTO> selectOrdinaryModel();

    List<MwModelInfoDTO> selectOrdinaryModelByOwnModelId(@Param("modelId") Integer modelId);

    List<MwModelManageTypeDto> selectModelTypeList(QueryModelTypeParam param);

    List<MwModelManageTypeDto> selectModelInstanceTree(@Param("instanceIds") List<String> instanceIds, @Param("modelIds") List<String> modelIds);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstanceFuzzyQuery(@Param("modelId") Integer modelId);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstanceFuzzyQueryGroup(@Param("modelId") Integer modelId);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstance(@Param("modelId") Integer modelId);

    int creatModelGroup(AddAndUpdateModelGroupParam groupParam);

    ModelManageTypeDto getModelGroupByPid(Integer pid);

    int updateModelGroupNodes(int groupId, String nodes);

    int updateModelGroup(AddAndUpdateModelGroupParam groupParam);

    AddAndUpdateModelGroupParam queryModelGroupById(@Param("modelGroupId") Integer modelGroupId);

    List<MwModelGroupDTO> getAllModelGroupInfo();

    List<Integer> getModelGroupIdByName(String name);

    List<Map> getZabbixGroupIdByMdoelGroupId(Integer modelGroupId);

    int deleteModelGroup(Integer modelGroupId);

    List<ModelManageStructDto> queryModelListInfo();

    List<ModelPropertiesStructDto> queryProperticesInfoByModelId(ModelManageStructDto dto);

    List<ModelPropertiesExportDto> queryProperticesInfoByExport(Integer modelId);

    List<MWModelPropertiesInfoDto> getPropertiesInfoByModelId(@Param("modelId") Integer modelId);

    List<ModelPropertiesExportDto> queryProperticesInfoByGroupExport(Integer modelId);

    int selectModelCount(@Param("modelGroupId") Integer modelGroupId);

    List<Map<String, Object>> selectSonAndFatherModelList(String nodes);

    int selectModelIndexIdCount(AddAndUpdateModelPropertiesParam propertiesParam);

    int selectModelPropertiesSort(AddAndUpdateModelPropertiesParam propertiesParam);

    int selectModelPropertiesNameCount(AddAndUpdateModelPropertiesParam propertiesParam);

    int creatModelInstance(AddAndUpdateModelInstanceParam instanceParam);

    int updateModelInstance(AddAndUpdateModelInstanceParam instanceParam);

    int updateModelInstanceTopoInfo(AddAndUpdateInstanceTopoInfoParam param);

    int insertModelInstanceTopoInfo(AddAndUpdateInstanceTopoInfoParam param);

    //批量更新实例信息
    int batchUpdModelInstance(BatchUpdateInstanceParam batchUpdateInstanceParam);

    int deleteModelInstances(List<Integer> deleteModelInstanceParam);

    int checkBaseDevice(Integer modelId);

    List<ModelInstanceDto> selectModelInstance(Map priCriteria);

    List<ModelInstanceDto> selectModelInstanceByTimeOut(@Param("modelIndexs") List<String> modelIndexs, @Param("allTypeIdList") List<String> allTypeIdList);

    List<ModelInstanceDto> selectModelInstanceBySystemIsFlag(Map priCriteria);

    List<String> getAllModelIndexByGroup(Map priCriteria);

    List<ModelInstanceDto> selectModelInstanceByUniqueCheck(Map priCriteria);

    int deleteModelInstanceByModelId(Integer modelId);

    List<Integer> selectInstanceIdsByModelId(Integer modelId);

    String selectModelIndexById(Integer modelId);

    List<ModelInstanceDto> selectModelInstanceByModelId(String nodes);

    List<String> selectModelNodes(Integer leftModelId);

    String getCountRelationsByModelId(@Param("leftModelId") Integer leftModelId, @Param("rightModelId") Integer rightModelId);

    Integer getFatherModelIdBySonModelId(Integer modelId);

    List<MwCustomColDTO> selectModelPropertiesByModelId(QueryModelInstanceParam param);

    List<Map<String, Object>> selectModelGroupList(QueryModelGroupParam param);

    List<Map<String, Object>> selectPropertiesList();

    int selectCountInstanceBymodelId(Integer modelId);

    List<MwModelInfoDTO> selectFatherModelList();

    String selectGroupNodes(Integer groupId);

    List<MwModelManageTypeDto> getAllModelList();

    Integer selectModelIdByIndexs(String modelIndex);

    List<MwModelAssetsGroupTable> selectGroupServerMap(Integer assetsSubTypeId);

    List<String> queryPropertiesGanged(QueryModelGangedFieldParam param);

    List<MWModelPropertiesInfoDto> selectPropertiesFieldByGanged(Integer modelId);

    List<Map> getModelGroupNodesAll();

    List<MwModelManageDTO> getModelGroupNodes();

    List<MwCustomColByModelDTO> selectCustomColList(@Param("propertyList") List<PropertyInfo> propertyList,
                                                    @Param("userId") Integer userId);

    void updatePropertiesToPageField(AddAndUpdateModelPropertiesParam propertiesParam);

    void batchUpdatePageFieldStatus(List<BatchUpdatePropertiesStatusParam> list);

    void batchUpdatePageFieldSort(List<BatchUpdatePropertiesStatusParam> list);

    void batchUpdateCustomcolSort(List<BatchUpdatePropertiesStatusParam> list);

    void deletePagefieldTable(@Param("modelId") Integer modelId, @Param("propertyIds") List<String> propertyIds);

    void deleteCustomcolTable(@Param("modelId") Integer modelId, @Param("propertyIds") List<String> propertyIds);

    void deletePropertiesPageFieldByModelId(Integer modelId);

    void deleteCustomColByModelId(Integer modelId);

    void deleteAllPageField();

    void deleteAllCustomCol();

    List<QueryInstanceParam> selectModelSysClassById(@Param("modelIds") List<Integer> modelIds);

    List<String> selectModelIndexsByModelIds(@Param("modelIds") List<Integer> modelIds);

    List<MwModelInstanceCommonParam> getAllModelInstanceInfo();

    List<ModelParam> getModelInfoDisParent();

    List<String> getAllCabinetModelIndex();

    List<String> getAllRoomModelIndex();
}
