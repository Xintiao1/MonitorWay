package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.model.dto.MwPropertiesValueDTO;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherCommonDTO;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.server.param.AssetsIdsPageInfoParam;
import cn.mwpaas.common.model.Reply;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2023/5/20 9:03
 */
public interface MwModelCommonService {
    List<MwModeRommCommonParam> getAllRoomAndCabinetInfo(QueryModelInstanceByPropertyIndexParamList paramList);

    List<MwModelFromUserParam> getInstanceInfoByModelIndex();

    //根据实例数据获取Rancher关联信息
    List<MwModelRancherCommonDTO> findRancherInfoByModelAssets(QueryModelInstanceByPropertyIndexParamList params);

    Reply getAllAssetsInterfaceByCriteria(AssetsIdsPageInfoParam param);

    Map<Integer, List<PropertyInfo>> getAllModelPropertyInfo();

    Reply getAllAssetsInterface(AssetsIdsPageInfoParam param);

    Reply getAllInterfaceNameAndHostId(List<String> hostIds);

    List<MwModelInterfaceCommonParam> queryInterfaceInfoAlertTag(MwModelInterfaceCommonParam param);

    List<MwPropertiesValueDTO> getAllAlertModelProperties();

    //获取资产CPU、内存change历史数据
    void getModelAssetsChangeMessage(HashMap<String, String> map);

    void testConnectServer();

    void insertInterface(List<AssetsInterfaceDTO> list);

    Reply getInstanceNameByModelId();

    Reply getAllInstanceInfoByCabinet();

    List<Map<String, Object>> getAllInstanceInfoByQueryParam(QueryEsParam param);

    Reply getCabinetRelationDevice() throws Exception;

    Reply getInterfaceInfosByAssetsIds(MwModelAssetsInterfaceParam param);

    List<MwModelPropertyInfo> getModelPropertyInfoByModelId(Integer modelId);
}
