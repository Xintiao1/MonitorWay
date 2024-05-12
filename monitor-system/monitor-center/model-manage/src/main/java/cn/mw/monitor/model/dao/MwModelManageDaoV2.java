package cn.mw.monitor.model.dao;

import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.param.AddAndUpdateModelPropertiesParam;
import cn.mw.monitor.service.model.param.PropertiesValueParam;
import cn.mw.monitor.service.model.param.QueryModelGroupParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/2/5 15:12
 */
public interface MwModelManageDaoV2 {

    int creatModel(AddAndUpdateModelParamV2 addAndUpdateModelParam);

    int updateNodes(int modelId, String nodes);

    int updateModel(AddAndUpdateModelParamV2 addAndUpdateModelParam);

    int deleteModel(Long modelId);

    //新增 start
    //根据模型id查询模型信息
    ModelInfo selectBaseModelInfoByIndex(String modelIndex);
    ModelInfo selectBaseModelInfoById(Long modelId);
    ModelInfo selectModelInfoWithPropertyById(Long modelId);
    List<ModelInfo> selectModelInfoByGroupId(Integer groupId);
    List<ModelInfo> selectChildreModelInfoByGroupId(Integer groupId);
    List<ModelInfo> selectModelPropertiesListWithParent(Map priCriteria);
    List<ModelInfo> selectModelListWithParent(Long modelId);
    List<ModelInfo> selectAllModelListWithParent();
    List<ModelInfo> selectListWithParentAndGroup(Long modelId);
    List<ModelInfo> selectModelInfoByPids(String pids);
    List<ModelInfo> selectPropertiesByInstanceFuzzyQueryGroup(@Param("modelId") Long modelId);
    List<ModelInfo> selectPropertiesByInstanceFuzzyQuery(@Param("modelId") Long modelId);
    List<ModelInfo> selectAllModelInfo();
    List<ModelInfo> selectModelListByIds(@Param("modelIds") List<Integer> modelIds);
    List<ModelInfo> getBaseModelInfos();
    List<ModelInfo> getBaseModelInfosDisParent();

    int countInstanceIdsByModelId(Long modelId);
    //新增 end

    List<MwModelManageDtoV2> selectModelList(Map priCriteria);

    String selectModelNameById(Long modelId);

    int bathDeleteModelRelations(@Param("ids") List<Integer> ids);

    List<ModelRelationsDto> selectModelRelationsByModelId(Map criteria);

    List<ModelRelationsGroupDto> selectModelRelationsGroup(Map priCriteria);

    int creatModelProperties(AddAndUpdateModelPropertiesParam propertiesParam);

    void createModelPropertiesToPageField(AddAndUpdateModelPageFieldParam param);

    int insertPropertiesValueInfo(PropertiesValueParam param);

    void creatModelPropertiesStruct(List<ModelPropertiesStructDto> list);

    List<MwCustomcolTable> initInsertPropertiesToCol(List<MwCustomcolTable> records);

    int insertPropertiesToCol(List<MwCustomcolTable> records);

    int updateModelProperties(AddAndUpdateModelPropertiesParam propertiesParam);

    void deletePropertiesStruct(@Param("modelId") Long modelId, @Param("indexId") String indexId);

    void updateModelPropertiesSort(ModelPropertiesSortParam param);

    int deleteModelPropertiesByPropertiesId(Integer propertiesId);

    void updateModelPropertiesShowStatus(AddAndUpdateModelPropertiesParam propertiesParam);

    List<String> getPropertiesByManage(Long modelId);

    List<String> getPropertiesByBaseShow(@Param("showType") Integer showType, @Param("modelId") Long modelId);

    List<String> selectModelInstanceFiledBySecond(Long modelId);

    int deleteModelPropertiesByModelId(Long modelId);

    List<ModelPropertiesDto> selectModelPropertiesList(Map priCriteria);

    List<ModelPropertiesStructDto> getProperticesStructInfo(@Param("modelId") Long modelId, @Param("indexId") String indexId);

    String getProperticesArrObj(@Param("modelId") Long modelId, @Param("indexId") String indexId);

    void editorModelPropertiesType(AddAndUpdateModelPropertiesParam param);

    List<String> queryPropertiesTypeList(AddAndUpdateModelPropertiesParam param);

    List<AddAndUpdateModelParamV2> queryModelIndexList();

    List<MwModelInfoDTOV2> queryParentModelInfo();

    List<MwModelInfoDTOV2> queryOrdinaryModelInfo(AddAndUpdateModelGroupParamV2 groupParam);

    List<MwModelInfoDTOV2> selectOrdinaryModel();

    List<MwModelInfoDTOV2> selectOrdinaryModelByOwnModelId(@Param("modelId") Long modelId);

    List<MwModelManageTypeDto> selectModelTypeList(QueryModelTypeParam param);

    List<MwModelManageTypeDto> selectModelInstanceTree(@Param("instanceIds") List<String> instanceIds, @Param("modelIds") List<String> modelIds);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstanceFuzzyQuery(@Param("modelId") Long modelId);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstanceFuzzyQueryGroup(@Param("modelId") Long modelId);

    List<MWModelPropertiesInfoDto> queryPropertiesByInstance(@Param("modelId") Long modelId);

    int creatModelGroup(AddAndUpdateModelGroupParamV2 groupParam);

    ModelManageTypeDtoV2 getModelGroupByPid(Long pid);

    int updateModelGroupNodes(int groupId, String nodes);

    int updateModelGroup(AddAndUpdateModelGroupParam groupParam);

    AddAndUpdateModelGroupParam queryModelGroupById(@Param("modelGroupId") Integer modelGroupId);

   List<MwModelGroupDTO> getAllModelGroupInfo();

    List<Map> getZabbixGroupIdByMdoelGroupId(Integer modelGroupId);

    int deleteModelGroup(Integer modelGroupId);

    List<ModelManageStructDto> queryModelListInfo();

    List<ModelPropertiesStructDto> queryProperticesInfoByModelId(ModelManageStructDto dto);

    List<ModelPropertiesExportDto> queryProperticesInfoByExport(Long modelId);

    List<MWModelPropertiesInfoDto> getPropertiesInfoByModelId(@Param("modelId") Long modelId);

    List<ModelPropertiesExportDto> queryProperticesInfoByGroupExport(Long modelId);

    int selectModelCount(@Param("modelGroupId") Integer modelGroupId);

    List<Map<String, Object>> selectSonAndFatherModelList(String nodes);

    int selectModelIndexIdCount(AddAndUpdateModelPropertiesParam propertiesParam);

    int selectModelPropertiesSort(AddAndUpdateModelPropertiesParam propertiesParam);

    int selectModelPropertiesNameCount(AddAndUpdateModelPropertiesParam propertiesParam);

    int creatModelInstance(AddAndUpdateModelInstanceParam instanceParam);

    int updateModelInstance(AddAndUpdateModelInstanceParam instanceParam);

    int deleteModelInstances(List<Integer> deleteModelInstanceParam);

    int checkBaseDevice(Long modelId);

    List<ModelInstanceDto> selectModelInstance(Map priCriteria);

    List<ModelInstanceDto> selectModelInstanceByTimeOut(@Param("modelIndexs") List<String> modelIndexs, @Param("allTypeIdList") List<String> allTypeIdList);

    List<ModelInstanceDto> selectModelInstanceBySystemIsFlag(Map priCriteria);

    List<String> getAllModelIndexByGroup(Map priCriteria);

    List<ModelInstanceDto> selectModelInstanceByUniqueCheck(Map priCriteria);

    int deleteModelInstanceByModelId(Long modelId);

    List<Long> selectInstanceIdsByModelId(Long modelId);

    String selectModelIndexById(Long modelId);

    List<ModelInstanceDto> selectModelInstanceByModelId(String nodes);

    List<String> selectModelNodes(Integer leftModelId);

    String getCountRelationsByModelId(@Param("leftModelId") Integer leftModelId, @Param("rightModelId") Integer rightModelId);

    Integer getFatherModelIdBySonModelId(Long modelId);

    List<MwCustomColDTO> selectModelPropertiesByModelId(QueryModelInstanceParam param);

    List<Map<String, Object>> selectModelGroupList(QueryModelGroupParam param);

    List<Map<String, Object>> selectPropertiesList();

    int selectCountInstanceBymodelId(Long modelId);

    List<MwModelInfoDTOV2> selectFatherModelList();

    String selectGroupNodes(Long groupId);

    List<MwModelManageTypeDto> getAllModelList();

    Integer selectModelIdByIndexs(String modelIndex);

    List<MwModelAssetsGroupTable> selectGroupServerMap(Integer assetsSubTypeId);

   List<String> queryPropertiesGanged(QueryModelGangedFieldParam param);

    List<MWModelPropertiesInfoDto> selectPropertiesFieldByGanged(Long modelId);

   List<Map> getModelGroupNodesAll();

   List<MwModelManageDTO> getModelGroupNodes();
}
