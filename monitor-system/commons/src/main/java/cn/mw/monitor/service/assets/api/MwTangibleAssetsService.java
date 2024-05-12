package cn.mw.monitor.service.assets.api;

import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.state.DataPermission;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;


/**
 * Created by baochengbin on 2020/3/12.
 */
public interface MwTangibleAssetsService {
    Reply getAssetsTypesTree(QueryAssetsTypeParam param);

    Reply selectById(String id);

    Reply selectListWithExtend();

    Reply selectListWithExtend(Map map);

    Reply selectTopoAssetsList();

    Reply selectTopoAssetsList(Map map);

    Reply selectVXLanAssetsList();

    Reply selectList(QueryTangAssetsParam qParam);

    Reply updateAssets(UpdateTangAssetsParam uParam) throws Throwable;

    Reply deleteAssets(List<DeleteTangAssetsID> ids);

//    Reply insertAssets(AddUpdateTangAssetsParam aParam) throws Throwable;

    Reply insertAssets(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable;

    Reply getTemplateList(AddUpdateTangAssetsParam aParam) throws Throwable;

    Reply doInsertAssets(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable;

    Reply selectAllLabel(QueryTangAssetsParam queryTangAssetsParam);

    Reply updateState(UpdateAssetsStateParam updateAssetsStateParam);

    String createAndGetZabbixHostId(AddUpdateTangAssetsParam aParam) throws Exception;

    String createAndGetOutAssetsZabbixHostId(AddUpdateTangAssetsParam aParam);

    void batchCreateAndGetZabbixHostId(List<AddUpdateTangAssetsParam> aParam) throws Exception;

    //根据ip获取资产信息
    MwTangibleassetsDTO selectByIp(String ip);

    //根据模板id 获取宏值以及宏值的中文名称
    Reply getTemplateMacrosByTemplateId(int monitorServerId, String templateId);

    //根据监控方式获取相应模板
    Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam);

    Reply updateAssetsTemplateIds();

    List<MwTangibleassetsTable> selectBySrecah(String search, Boolean disableWildcard);

    MwTangibleassetsDTO selectByAssetsIdAndServerId(String assetsId, int monitorServerId);

    Reply selectAssetsSearchTermData();

    Reply fuzzSeachAllFiledData(String value, boolean assetsIOTFlag);

    /**
     * 批量编辑资产时查询选中资产标签的交集
     *
     * @param updateTangAssetsParam 资产数据
     * @return
     */
    Reply batchEditAssetsGetLabel(UpdateTangAssetsParam updateTangAssetsParam);

    /**
     * 批量编辑资产时删除选中资产标签的交集
     *
     * @param labelDTO 标签数据
     * @return
     */
    Reply batchEditAssetsDeleteLabel(List<MwAssetsLabelDTO> labelDTO);

    /**
     * 查询所有监控项信息
     *
     * @return
     */
    Reply selectAllMonitorItem();

    /**
     * 查询所有资产关联的监控服务器id
     *
     * @return
     */
    Reply findAllMonitorServerId();

    /**
     * 根据ip查询资产信息
     *
     * @return
     */
    List<IpAssetsNameDTO> getAssetsNameByIps(List<String> list);

    List<String> getAssetsNameByIp(String ip);

    /**
     * 根据所有资产类型图标
     *
     * @return
     */
    Map<Integer, AssetTypeIconDTO> selectAllAssetsTypeIcon();

    /**
     * 根据权限查询资产信息
     *
     * @return
     */
    List<MwTangibleassetsTable> doSelectAssets(Object qParam, DataPermission dataPermission
            , String loginName, Integer userId);

    void insertDeviceInfo(AddUpdateTangAssetsParam record);

    void deleteDeviceInfo(List<String> list);

    void batchDeleteAssetsSnmpv12ByAssetsId(List<String> idList);

    void batchDeleteAssetsSnmpv3ByAssetsId(List<String> idList);

    void batchDeleteAssetsAgentByAssetsId(List<String> idList);

    void batchDeleteAssetsPortByAssetsId(List<String> idList);

    void deleteAssetsActionMapper(List<String> assetsIds);

    List<String> deleteAssetsCheckLinkRelation(List<DeleteTangAssetsID> ids);

    List<MwTangibleassetsTable> selectAssetsListByTypeIds(List<Integer> assetsTypeIds);

    //修改资产轮询引擎
    void updateAssetsPollingEngineInfo(String pollingEngine,Integer monitorServerId,String hostid);

    void tangibleAssetsPushConvert(List<String> ids);

}
