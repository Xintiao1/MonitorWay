package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.alert.dto.AssetsDto;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.link.param.AssetsParam;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.virtual.dto.VirtualizationMonitorInfo;
import cn.mwpaas.common.model.Reply;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author qzg
 * @date 2023/1/6
 */
public interface MwModelViewCommonService {
    static final String SEP = "-";

    //旧资产查询的id关键字
    static final String ID_KEY = "ids";
    static final String IPS_KEY = "ipList";
    static final String SCANSUCESS_ID_KEY = "scanSuccessIds";

    static final Set DATA_PERMINSSION_KEY = new HashSet<>();

    //位异或密钥
    static final int KEY = 5;
    static final String ESID = "esId";
    static final String ORG_IDS = "orgIds";
    static final String GROUP_IDS = "groupIds";
    static final String USER_IDS = "userIds";

    static final String ORG_NAMES = "orgNames";
    static final String GROUP_NAMES = "groupNames";
    static final String USER_NAMES = "userNames";

    static final String RJ45 = "RJ45";
    static final String LC_LC = "LC-LC";
    static final String MPO_MPO = "MPO-MPO";


    static final String SNMPV1V2 = "snmpv1v2";
    static final String SNMPV3 = "snmpv3";
    static final String ZABBIX_AGENT = "zabbixAgent";
    static final String ICMP = "icmp";
    static final String IMPI = "ipmi";
    static final String INSTANCE_ID_KEY = "modelInstanceId";
    static final String MULTI_NODE = "multiNode";
    static final String OWN_LINK_CABINET_ID = "ownLinkCabinetId";
    static final String OWN_LINK_DEVICE_ID = "ownLinkDeviceId";
//    static final String RELATION_FLOOR = "relationFloor";
    static final String INSTANCE_NAME_KEY = "instanceName";
    static final String RELATION_GROUP = "relationGroup";
    static final String INSTANCE_CODE = "instanceCode";
    static final String IN_BAND_IP = "inBandIp";

    static final String DESCRIPTION = "description";
    static final String ITEM_ASSETS_STATUS = "itemAssetsStatus";
    static final String INSTANCE_NAME_FIELD = "名称";
    static final String VIEW_SHOW_TYPE = "viewShowType";
    static final String DEFAULT_VIEW = "默认视图";
    static final String CHASSIS_VIEW = "刀箱视图";
    static final String BLADE_VIEW = "刀片视图";

    static final String BAY_COL = "bayCol";
    static final String BAY_ROW = "bayRow";
    static final String MODEL_ID_KEY = "modelId";
    static final String MODEL_INDEX = "modelIndex";
    static final String GROUP_NODES = "groupNodes";
    static final String ASSETTYPE_ID_KEY = "assetsTypeId";
    static final String ASSETTYPE_SUB_ID = "assetsTypeSubId";
    static final String ASSETTYPE_NAME = "assetsTypeName";
    static final String ASSETSUBTYPE_NAME = "assetsTypeSubName";
    static final String OPERATION_MONITOR = "operationMonitor";

    static final String WEB_STATE ="webState";
    static final String MONITOR_MODE = "monitorMode";
    static final String AUTO_MANAGE = "autoManage";
    static final String LOG_MANAGE = "logManage";
    static final String PROP_MANAGE = "propManage";
    static final String MODEL_AREA = "modelArea";
    static final String MODEL_SYSTEM = "modelSystem";
    static final String MANUFACTURER = "manufacturer";
    static final String SPECIFICATIONS = "specifications";
    static final String SERIAL_NUM = "serialNum";
    static final String MODEL_TAG = "modelTag";
    static final String ASSETS_ID = "assetsId";
    static final String MONITOR_SERVER_ID = "monitorServerId";
    static final String RELATION_INSTANCE_ID = "relationInstanceId";
    static final String POLLING_ENGINE = "pollingEngine";
    static final String MODEL_CLASSIFY = "modelClassify";
    static final String MONITOR_FLAG = "monitorFlag";
    static final String MWMACROS_DTO = "mwMacrosDTO";
    static final String HOST_GROUP_ID = "hostGroupId";
    static final String KEYWORD = ".keyword";

    static final String CREATE_DATE = "createDate";
    static final String TPSERVERHOSTNAME = "TPServerHostName";
    static final String CPUCHANGEALTERTAG = "CPU核数更改";
    static final String MEMORYCHANGEALTERTAG = "内存大小更改";


    List<Integer> getModelGroupIdByName(String name);

    List<Map<String, Object>> getModelListInfoByPerm(QueryModelAssetsParam param);

    List<Map<String, Object>> getModelListInfoByCommonQuery(QueryModelParam queryModelParam, QueryInstanceModelParam param);

    <T> List<T> getModelListInfoByCommonQuery(Class<T> type, QueryModelParam queryModelParam, QueryInstanceModelParam param);

    Reply findTopoModelAssetsBySNMP();

    <T> Reply findTopoModelAssets(Class<T> clazz, Map map);

    <T> Reply findTopoModelAssets(Class<T> clazz);

    <T> List<T> findModelAssets(Class<T> clazz, QueryModelAssetsParam param) throws Exception;

    //根据条件查询资产的实例数据
    List<MwTangibleassetsDTO> findModelAssetsByRelationIds(QueryModelInstanceByPropertyIndexParamList param) throws Exception;

    List<MwModelVirtualDataParam> getAllVirtualDeviceData();

    Reply batchShiftPowerByUser(InstanceShiftPowerParam shiftParam);

    MwTangibleassetsDTO findModelAssetsByInstanceId(Integer instanceId) throws Exception;

    Object selectByAssetsIdAndServerId(String assetsId, int monitorServerId) throws Exception;

    MwTangibleassetsDTO selectByIp(String ip);

    MwTangibleassetsDTO selectByHostIdAndIp(String id, String ip);

    List<String> getAssetsNameByIp(String ip);

    List<IpAssetsNameDTO> getAssetsNameByIps(List<String> ips);

    Reply selectById(String id);

    Reply selectById(String id, Boolean isQueryAssetsState);

    MwTangibleassetsByIdDTO doSelectById(MwTangibleassetsDTO mwTangAsset) throws Exception;

    List<MwTangibleassetsTable> fuzzySearch(String search, Boolean enableWildcard);

    Reply selectVXLanAssetsList();

    void addCacheAssetInfo(MwTangibleassetsDTO dto);

    void removeCacheAssetInfo(String id);

    void updateMonitorServerSet();

    Reply findAllMonitorServerId();

    Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam);

    Reply insertAssets(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable;

    Reply deleteNetworkLinkAsset(Object id, AssetsParam targetAssetsParam);

    AssetsDto getAssetsById(String assetsId, Integer monitorServerId) throws Exception;

    List<AssetsDto> getAssetsByIds(List<String> assetsIds) throws Exception;

    /**
     * 根据所有资产类型图标
     *
     * @return
     */
    Map<Integer, AssetTypeIconDTO> selectAllAssetsTypeIcon();

    //获取模型资产主机数据
    Reply getModelAssetsHostData(QueryInstanceModelParam param);

    List<MwModelInstanceCommonParam> getModelSystemIndexIdAndInstanceInfo(Integer modelId);

    Reply getModelSystemAndClassify();

    Reply getSystemAndClassifyInstanceInfo();

    List<MwRancherProjectUserListDTO> getAllRancherProjectUserInfo() throws Exception;

    List<VirtualizationMonitorInfo> getAllVirtualInfoByMonitorData() throws Exception;

    Reply getAllCitrixListRelationInfo() throws Exception;

    Reply getModelListInfoByView(QueryInstanceModelParam param);

    List<MwInstanceCommonParam> getNameListByIds(List<Integer> ids);
}
