package cn.mw.monitor.model.service;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mwpaas.common.model.Reply;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;
import cn.mw.monitor.service.model.param.MwSyncZabbixAssetsParam;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @author xhy
 * @date 2021/2/25 9:10
 */
public interface MwModelInstanceService {

    Reply lookModelInstance(AddAndUpdateModelInstanceParam instanceParam);

    Reply updateSyncZabbixName(MwSyncZabbixAssetsParam param);

    Reply batchUpdateSyncZabbixName(List<MwSyncZabbixAssetsParam> paramList);

    Reply batchUpdatePowerByEs();

    Reply batchUpdatePower(BatchUpdatePowerParam param);

    Reply updatePollingEngine(UpdatePollingEngineParam param);

    Reply lookModelInstanceByAction(AddAndUpdateModelInstanceParam instanceParam);

    Reply lookModelInstanceByActionDelete(DeleteModelInstanceParam instanceParam);

    Reply selectModelInstance(QueryInstanceModelParam param);

    Reply modelInstanceFieldUnique(QueryModelInstanceParam param);

    Reply getInstanceInfoByFuzzyQuery(QueryModelInstanceParam param);

    Reply selectModelInstanceFiled(QueryCustomModelparam queryCustomModelparam);

    Reply queryModelListInfo(QueryInstanceModelParam param);

    Reply selectModelInstanceTree();

    Reply selectModelInstanceProperties(QueryModelInstanceParam param) throws IOException;

    Reply selectModelInstanceFiledByInsert(QueryCustomModelparam queryCustomModelparam);

    Reply imageUpload(MultipartFile multipartFile, Integer instanceId);

    Reply instaceChangeHistory(SystemLogParam qParam);

    Reply selectInstanceProperties(QueryModelInstanceParam param) throws Exception;

    Reply shiftInstanceCheck(List<AddAndUpdateModelInstanceParam> param);

    Reply getInstanceInfoById(QueryModelInstanceParam param);

    Reply getInstanceStructInfo(QueryModelInstanceParam param);

    Reply getTimeOutInfo();

    TimeTaskRresult getTimeOutInfoByTimeTask();

    Reply getSelectDataInfo(List<QueryRelationInstanceInfo> paramList);

    Reply updateRoomLayout(QueryBatchSelectDataParam param);

    UpdateRequest getUpdateRequestToES(MwModelInstanceParam param);

    Reply cleanFieldValueToEs(List<MwModelInstanceParam> paramList);

    Reply updateCabinetLayout(QueryCabinetLayoutListParam param);

    Reply getRoomAndCabinetLayout(QueryInstanceModelParam param);

    Reply getAllCabinetInfoByRoom(QueryInstanceModelParam param);

    Reply getModelRelationInfo(QueryInstanceRelationToPoParam param);

    Reply instanceRelationBrowse(QueryInstanceRelationToPoParam param);

    Reply instanceRelationLink(QueryInstanceRelationToPoParam param);

    Reply getInstanceListByModelId(QueryInstanceModelParam param);

    Reply batchUpdateModelInstance(List<AddAndUpdateModelInstanceParam> instanceParams);

    Reply editorData(List<AddAndUpdateModelInstanceParam> instanceParams);

    Integer selectCountInstances();

    List<Map<String, Object>> getInfoByInstanceId(QueryModelInstanceParam param);

    List<Map<String, Object>> getInstanceInfoByExport(QueryModelInstanceParam param);

    List<Map<String, Object>> getInstanceInfoByModelIndexs(QueryInstanceModelParam param);

    List<Map<String, Object>> getInstanceInfoByMoreIndexs(QueryInstanceModelParam param);

    List<Map<String, Object>> getInstanceInfoByIndexs(QueryModelInstanceParam param);

    void saveData(List instanceParams, Boolean isLicense, Boolean isPower) throws Exception;


    Reply batchDeleteInstanceInfo(DeleteModelInstanceParam param);

    List<Map<String, Object>> getInstanceInfoByProperties(QueryInstanceModelParam param) throws Exception;

    Reply batchUpdateHostState(Integer serverId, List<String> hostIds, Integer status);

    List<Map<String, Object>> getInstanceInfoByPropertiesValue(QueryInstanceModelParam param) throws Exception;

    List<QueryInstanceParam> getCabinetInfoByRelationCabinedId(List<Integer> instanceIds);

    List<QueryInstanceParam> getAllInstanceNameById(List<Integer> instanceIds);

    void esDataRefresh(ModelParam modelParams);

    Reply getInstanceIdByLinkRelation(QueryInstanceTopoInfoParam param) throws Exception;

    Reply syncAllInstanceLinkRelation();

    Reply setModelAreaDataToEs();

    List<AddAndUpdateModelInstanceParam> batchInsertWebMonitorInstance(List<AddAndUpdateModelInstanceParam> paramList) throws Exception;

    List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId);

    List<AddAndUpdateModelInstanceParam> convertInstanceList(List dataList, Integer modelId,Integer relationId);
    Reply getModelPropertiesById(Integer modelId);

    Reply selectAllModelMonitorItem();

    Reply settingConfigPowerByIp(SettingConfigPowerParam param);

    Reply getSettingConfigPowerByIp();

    Reply getModelInfoParamById(Integer modelId);

    Map<String, Object> selectInfoByInstanceId(Integer instanceId) throws Exception;

    List<Map<String, Object>> getAllInstanceInfoByBase();

    List<Map<String, Object>> selectInfosByModelId(Integer modelId) throws Exception;
}
