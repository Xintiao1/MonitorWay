package cn.mw.monitor.model.dao;


import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.util.entity.EmailFrom;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/10/21
 */
public interface MwModelInstanceDao {
    List<MwCustomColByModelDTO> selectByModelUserIdList(QueryCustomModelparam queryCustomModelparam);

    List<MwCustomColByModelDTO> selectFiledsByGroupList(QueryCustomModelparam queryCustomModelparam);

    List<ModelListInfoDTO> queryModelListInfo(@Param("groupId") Integer groupId, @Param("modelIdList") List<String> modelIdList
            , @Param("instanceIdList") List<String> instanceIdList);

    List<MwModelInstanceParam> selectAllInstanceInfo();

    List<MwModelInstanceParam> selectRelationInstanceInfo(List<String> list);

    List<String> getModelPropertiesType(@Param("modelId") Integer modelId);

    List<String> getModelPropertiesTypeByGroup(@Param("modelId") Integer modelId);

    List<Map> getPropertiesInfoByModelId(@Param("modelId") Integer modelId);

    List<ModelPropertiesDto> getModelPropertiesByType(@Param("modelId") Integer modelId, @Param("type") String type);

    List<Map> getPropertiesNameByModelId(@Param("modelId") Integer modelId);

    List<Map> getPropertiesNameByGrouyModelId(@Param("modelGroupId") Integer modelGroupId);

    MwModelInfoDTO getModelIndexInfo(@Param("modelId") Integer modelId);

    List<Map> getMonitorModeInfo();

    List<Map> getAllModelIndexInfo();

    List<MwModelManageTypeDto> selectModelInstanceTreeByRedirect(@Param("modelGroupId") Integer modelGroupId, @Param("modelId") Integer modelId);

    List<Map> getTimeOutPropertiesInfo();

    List<MWUser> selectAllUserList();

    List<Map> getModelRelationInfo(@Param("lastModelId") Integer lastModelId, @Param("modelId") Integer modelId);

    Integer selectCountInstances();

    void updataInstanceName(@Param("instanceName") String instanceName, @Param("instanceId") Integer instanceId);

    void increaseInstanceIdSeq(Integer size);

    Integer getInstanceIdSeq();

    int insertInstanceName(List<AddAndUpdateModelInstanceParam> paramList);

    void deleteBatchInstanceById(@Param("instanceIds") List<Integer> instanceIds);

    void deleteBatchInstanceByRelationIds(@Param("DeleteModelInstanceParam") DeleteModelInstanceParam param);

    void deleteBatchInstanceByIndex(@Param("indexs") List<String> indexs, @Param("relationInstanceId") Integer relationInstanceId);

    List<Integer> getInstanceIdsByModelIndex(@Param("list") List<String> list, @Param("relationInstanceId") Integer relationInstanceId);

    List<Integer> getInstanceIdsByRelationIds(@Param("DeleteModelInstanceParam") DeleteModelInstanceParam param);

    List<Integer> getInstanceIdsByRelationIdAndModelId(@Param("list") List<Integer> relationInstanceIds);

    void deleteInstanceIdsByRelationIdAndModelId(@Param("list") List<Integer> relationInstanceIds);

    List<QueryInstanceParam> getInstanceNameByIds(List<Integer> instanceIds);

    List<MwInstanceCommonParam> getInstanceNameListByIds(List<Integer> instanceIds);


    List<MwModelInstanceTypeDto> getInstanceTypeById();

    String getInstanceNameById(Integer instanceId);

    Integer getModelIdByInstanceId(Integer instanceId);

    List<String> getModelIndexByInstanceIds(List<Integer> instanceIds);

    String getModelIndexByModelId(Integer modelId);

    MwModelEngineDTO selectProxyIdById(String engineId);

    List<MwModelInstanceCommonParam> selectModelInstanceInfoById(Integer modelId);

    List<MwModelInstanceCommonParam> selectModelInstanceInfoByIds(List<Integer> modelIds);

    void batchEditorInstanceName(List<AddAndUpdateModelInstanceParam> paramList);

    List<MwModelInfoDTO> getAllModelInfoByPids(String modelId);

    List<MwModelInstanceCommonParam> getInstanNameAndRelationNameById(List<Integer> instanceIds);

    List<Map<String, String>> selectAllMonitorItem();

    List<MWUser> selectEmailAll();

    EmailFrom selectEmailFrom(String ruleName);

    MwModelInfoDTO getModelNameAndGroupName(Integer modelId);

    void updateCabinetRelationId(List<UpdateRelationIdParam> list);

    List<String> getInstanceNameByModelId(Integer modelId);

    List<MwModelInstanceCommonParam> getAllCabinetInfo();

    List<MwModelInstanceCommonParam> getCabinetInfoByRoomId(Integer roomId);

    List<MwModelInstanceCommonParam> getAllLinkDeviceInfo();

    List<MwModelInstanceCommonParam> getAllRoomInfo(MwModelInstanceCommonParam param);

    MwModelInfoParam getModelInfoParamById(Integer modelId);

    String getModelTypeById(String instanceId);

    List<MwModelInstanceCommonParam> getSystemAndClassifyInfo();

}
