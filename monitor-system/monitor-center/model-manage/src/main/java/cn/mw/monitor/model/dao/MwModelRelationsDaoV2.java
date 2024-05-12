package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.ModelRelationGroupDTO;
import cn.mw.monitor.model.param.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/2/21
 */
public interface MwModelRelationsDaoV2 {
    List<ModelRelationGroupDTO> selectModelRelationGroup(ModelRelationGroupSelParam param);

    int selectCountByLeftAndRightModelId(AddAndUpdateModelRelationParam param);

    int creatModelRelations(AddAndUpdateModelRelationParam param);

    void creatModelRelationsMapper(AddRelationGroupMapperParam param);

    List<Map> selectAllModelByRelations(Integer ownModelId);

    Integer findDefaulGroupId(Integer ownModelId);

    void creatModelRelationsGroup(AddAndUpdateModelRelationGroupParam param);

    int updateModelRelationsGroup(AddAndUpdateModelRelationGroupParam param);

    AddAndUpdateModelRelationGroupParam modelRelationsGroupByUpdate(@Param("relationGroupId") Integer relationGroupId);

    int deleteModelRelationsGroup(DeleteModelRelationGroupParamV2 param);

    int selectModelRelationsNum(Integer relationGroupId);

    List<Map<String, Object>> selectGroupList(QueryGroupListParam param);

    List<Map> getRelationGroupListInfo(Integer ownModelId);

    List<Integer> getRelationByModelId(Integer ownModelId);

    List<Map> getRelationListInfoByGroup(@Param("relationGroupId") Integer relationGroupId, @Param("owmModelId") Integer owmModelId);

    Map getOwnModelInfo(Integer ownModelId);

    void editorModelRelationsByModelId(AddAndUpdateModelRelationParam param);

    void deleteModelRelations(AddAndUpdateModelRelationParam param);

    AddAndUpdateModelRelationParam selectModelRelationsByModelId(AddAndUpdateModelRelationParam param);

    List<Map> queryModelRelationGroupBySelect(Integer ownModelId);

    void updateModelRelationByGroup(AddAndUpdateModelRelationParam param);

    void addInstanceToPo(List<AddAndUpdateRelationToPoParam> list);

    List<AddAndUpdateRelationToPoParam> selectAllRelationsInstanceByToPo(ModelRelationToPoParam param);

    List<Map> getInstanceNameByIds(List<Integer> list);

    Map getRelationNumByModel(@Param("ownModelId") Integer ownModelId, @Param("oppositeModelId") Integer oppositeModelId);

    Map getRelationGroupName(@Param("ownModelId") Integer ownModelId, @Param("oppositeModelId") Integer oppositeModelId);

    void deleteTOPOByIds(@Param("list") List<Integer> list);

    List<AddAndUpdateRelationToPoParam> selectAllRelationsInfoIdByToPo(ModelRelationToPoParam param);

    List<AddAndUpdateRelationToPoParam> findOtherRelationsModelAll(ModelRelationToPoParam param);

    List<Integer> findHideModelList(ModelRelationToPoParam param);

    void setOwnModelInstance(Integer ownModelId);

    List<Map> getRelationyModelByTOPO(Integer ownModelId);

    void hideModelToPoDelete(QueryHideModelToPo param);

    void hideModelToPoSave(QueryHideModelToPo param);

    void hideModelToPoDeleteAll(QueryHideModelToPo param);
}
