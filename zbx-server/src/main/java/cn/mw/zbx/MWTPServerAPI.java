package cn.mw.zbx;

import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.service.model.param.MwModelWebMonitorTriggerParam;
import cn.mw.monitor.service.model.param.MwSyncZabbixAssetsParam;
import cn.mw.monitor.service.zbx.model.HostCreateParam;
import cn.mw.monitor.service.zbx.model.HostProblemType;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.service.zbx.param.CloseDto;
import cn.mw.zbx.dto.MWAlertParamDto;
import cn.mw.zbx.dto.MWWebDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MWTPServerAPI extends Serializable {
    int getServerId();

    boolean init();

    void destroy();

    JsonNode call(MWRequestAbstract request);

    boolean login(String user, String password);

    String message();

    MWZabbixAPIResult alertGetByCurrent(int serverId, AlertParam dto);

    MWZabbixAPIResult triggerGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter);

    MWZabbixAPIResult eventGettByTriggers2(int serverId, List<String> eventids);

    MWZabbixAPIResult eventGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter);

    MWZabbixAPIResult eventGetByHistory(int serverId, AlertParam dto);

    MWZabbixAPIResult getHistAlarmByEventGet(int serverId, MWAlertParamDto dto);

    MWZabbixAPIResult eventGetByEventid(int serverId, String eventid);

    MWZabbixAPIResult eventGetByEventids(int serverId, List<String> eventid);

    MWZabbixAPIResult triggerClose(int serverId, CloseDto dto);

    MWZabbixAPIResult triggerGetByTriggerid(int serverId, String triggerid);

    MWZabbixAPIResult itemGetByTriggerid(int serverId, String triggerid);

    MWZabbixAPIResult itemGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter);

    MWZabbixAPIResult alertGetByEventid(int serverId, String eventid);

    MWZabbixAPIResult alertGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter);

    MWZabbixAPIResult eventgetByObjectid(int serverId, String objectid, HashMap<String, Object> filter, long time_from, long time_till);

    MWZabbixAPIResult eventgets(int serverId, HashMap<String, Object> filter, String acknowledged
            , String severity, Long start, Long end, String subject);

    MWZabbixAPIResult eventacknowledge(int serverId, String eventids, String msg, String act);

    MWZabbixAPIResult eventacknowledge(int serverId, String eventids, String act);

    MWZabbixAPIResult eventacknowledge(int serverId, List<String> eventids, String act);

    MWZabbixAPIResult alertGet(int serverId, List<String> hostids);

    MWZabbixAPIResult alertEventGet(int serverId, List<String> hostids);

    MWZabbixAPIResult getEventSeverity(int serverId, Object eventid);

    MWZabbixAPIResult getEventHistAlert(int serverId, String objectid);

    MWZabbixAPIResult itemgetbyhostidList(int serverId, List<String> hostidList);

    MWZabbixAPIResult triggerCreate(int serverId, String description, String expression, String priority);

    MWZabbixAPIResult triggerCreateByKey(int serverId, String description, String hostName, String webMonitorName, String key, String code, String priority);

    MWZabbixAPIResult triggerCreate(int serverId, String description, String expression, String rexpression, String priority);

    MWZabbixAPIResult triggerBatchCreate(int serverId, List<MwModelWebMonitorTriggerParam> triggerParams);

    MWZabbixAPIResult actioncreate(int serverId, String name, String shortdata, String longdata, Map filter, List operations, Integer status);

    MWZabbixAPIResult actioncreate(int serverId, String name, String def_longData, String def_shortData, String r_longData
            , String r_shortData, Integer status, Map<String, Object> filter, List operations, List recovery_operation);

    MWZabbixAPIResult actioncreate(int serverId, String name, String def_longData, String def_shortData
            , Map<String, Object> filter, List operations);

    MWZabbixAPIResult actionupdate(int serverId, String actionId, String name, String def_longData, String def_shortData
            , String r_longData, String r_shortData, Map<String, Object> filter, List operations, List recovery_operation);

    MWZabbixAPIResult actionDelete(int serverId, String actionIds);

    MWZabbixAPIResult actionDelete(int serverId, List<String> actionIds);

    MWZabbixAPIResult triggerDelete(int serverId, List<String> triggerIds);

    MWZabbixAPIResult hostGetById(int serverId, String hostid);

    MWZabbixAPIResult hostGetBySeverity(int serverId, List<HostProblemType> severities);

    MWZabbixAPIResult actionGetByEventSourceTrigger(int serverId, String name, String status);

    MWZabbixAPIResult templateGet(int serverId, String name, Boolean isFilter);

    MWZabbixAPIResult templateGet(int serverId, String name);

    MWZabbixAPIResult hostGroupGet(int serverId, String name, Boolean isFilter);

    MWZabbixAPIResult triggerGetItemid(int serverId, String triggerids);

    MWZabbixAPIResult triggerGetHostId(int serverId, String hostId);

    MWZabbixAPIResult hostListGetByHostName(int serverId, ArrayList<String> hostNameList);

    MWZabbixAPIResult alertGetByCurrent(int serverId, List<String> hostIds);

    MWZabbixAPIResult getHistEvent(int serverId, Integer count, List<String> hostids);

    MWZabbixAPIResult itemGetbyType(int serverId, String name, List<String> hostIdList);

    MWZabbixAPIResult itemGetbyTypeNames(int serverId, List<String> names, List<String> hostIdList);

    MWZabbixAPIResult HttpTestCreate(int serverId, MWWebDto webDto);

    MWZabbixAPIResult HttpTestBatchCreate(int serverId, List<MWWebDto> webDtoList);

    MWZabbixAPIResult HttpTestUpdate(int serverId, MWWebDto webDto);

    MWZabbixAPIResult HttpTestBatchUpdate(int serverId, List<MWWebDto> webDtoList);

    MWZabbixAPIResult HttpTestUpdate(int serverId, String httptestid, Integer status);

    MWZabbixAPIResult HttpTestDelete(int serverId, List<String> webids);

    MWZabbixAPIResult HttpTestGet(int serverId, String httptestid);

    MWZabbixAPIResult HttpTestGet(int serverId, List<String> hostids);

    MWZabbixAPIResult HistoryGetByItemid(int serverId, String itemids, Integer type);

    MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, String itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetByItemid(int serverId, String itemids, Integer type, int limit);

    MWZabbixAPIResult HistoryGetByItemids(int serverId, List<String> itemids, Integer type, int limit);

    MWZabbixAPIResult GetHistoryByTimeAndType(int serverId, String itemid, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, List<String> hostids, List<String> itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetByTimeAndTypeASC(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetByTime(int serverId, String itemids, Long timeFrom, Long timeTill);

    MWZabbixAPIResult proxyGetByServerIp(int serverId, String serverIp, String port, String dns);

    MWZabbixAPIResult createProxy(int serverId, String engineName, String serverIp, String status, String port, String dns, String proxyAddress);

    MWZabbixAPIResult updateProxy(int serverId, String proxyId, String engineName, String serverIp, String status, String port, String dns, String proxyAddress);

    MWZabbixAPIResult proxyDelete(int serverId, List<String> proxyIds);

    MWZabbixAPIResult itemGetbyType(int serverId, Object name, Object hostid, Boolean isFilter);

    MWZabbixAPIResult itemGetbyNameList(int serverId, List<String> nameList, Object hostid, Boolean isFilter);

    MWZabbixAPIResult itemGetbyNameList(int serverId, List<String> nameList, Object hostid, Boolean isFilter, boolean searchByAny);

    MWZabbixAPIResult itemGetbyItemidList(int serverId, List<String> nameList, Object hostid, Boolean isFilter);

    MWZabbixAPIResult itemGetbyFilter(int serverId, String name, List<String> hostids);

    MWZabbixAPIResult itemGetbySearch(int serverId, Object nameList, Object hostid);

    MWZabbixAPIResult itemGetbyHostIdsSearch(int serverId, Object nameList, List<String> hostids);

    MWZabbixAPIResult itemGetbySearchItemId(int serverId, Object itemList, Object hostid);

    MWZabbixAPIResult itemGetbySearchNames(int serverId, String applicationName, Object nameList, Object hostid);

    MWZabbixAPIResult HistoryGetByItemid(int serverId, List<String> itemids, Integer type);

    MWZabbixAPIResult getItemDataByAppName(int serverId, String hostid, String applicationName, String type);

    MWZabbixAPIResult getItemNameByAppName(int serverId, String hostid, String applicationName, String type);

    MWZabbixAPIResult getItemDataByAppNameList(int serverId, List<String> hostIdList, String applicationName, List<String> type);

    MWZabbixAPIResult getItemDataByAppNameList(int serverId, List<String> hostIdList, String applicationName, List<String> type, boolean searchByAny);

    MWZabbixAPIResult getItemDataByAllAssets(int serverId, List<String> hostIdList);

    MWZabbixAPIResult HistoryGetInfoByTimeAll(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult HistoryGetInfoForLimitOne(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type);

    MWZabbixAPIResult getApplication(int serverId, String hostid);

    MWZabbixAPIResult getApplicationName(int serverId, String hostid, String applicationName);

    MWZabbixAPIResult itemgetbyhostid(int serverId, String hostid, String name, Boolean isName);

    MWZabbixAPIResult itemgetbyhostid(int serverId, List<String> hostids, String name, Boolean isName);

    MWZabbixAPIResult itemGetbyHostId(int serverId, Object hostid);

    MWZabbixAPIResult itemGetbyHostIdList(int serverId, List<String> hostids);

    MWZabbixAPIResult problemget(int serverId, List<String> hostids);

    MWZabbixAPIResult problemget(int serverId, List<String> hostids, Boolean acknowledged);

    MWZabbixAPIResult problemget(int serverId, List<String> hostids, String timeTill);

    MWZabbixAPIResult itemUpdate(int serverId, String itemid, Integer status);

    MWZabbixAPIResult trendGet(int serverId, String itemid, Long timeFrom, Long timeTill);

    MWZabbixAPIResult hostBatchCreate(int serverId, List<HostCreateParam> params);

    MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, List<String> templates, List<Map> macro, Integer status);

    MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, List<String> templates, List<Map> macro, Integer status, String proxyID);

    MWZabbixAPIResult hostUpdateGroup(int serverId, String hostid, ArrayList<String> groupIdList);

    MWZabbixAPIResult hostDelete(int serverId, List<String> ids);

    MWZabbixAPIResult hostProxyUpdate(int serverId, Object hostids, String proxyId);

    MWZabbixAPIResult hostUpdate(int serverId, List<String> hostids, Integer status);

    MWZabbixAPIResult hostBatchUpdate(int serverId, List<String> hostids, Map<String, Object> updateParams);

    MWZabbixAPIResult hostUpdate(int serverId, List<String> hostids, List<String> templateids, List<Map<String, Object>> hostInterfaces);

    MWZabbixAPIResult hostMassUpdate(int serverId, String hostid, String templateid);

    MWZabbixAPIResult hostMassRemove(int serverId, String hostid, String templateid);

    MWZabbixAPIResult hostListDeleteById(int serverId, ArrayList<String> hostIdList);

    MWZabbixAPIResult hostgroupCreate(int serverId, String name);

    MWZabbixAPIResult batchCreateHostGroup(int serverId, List<Map> params);

    MWZabbixAPIResult hostgroupUpdate(int serverId, String groupid, String name);

    MWZabbixAPIResult hostgroupDelete(int serverId, ArrayList<String> groupids);

    MWZabbixAPIResult hostgroupGetById(int serverId, List<String> groupids);

    MWZabbixAPIResult ItemGetBykey(int serverId, String hostid, String key);

    MWZabbixAPIResult getValueMap(int serverId);

    MWZabbixAPIResult getValueMapById(int serverId, List<String> valuemapIds);

    MWZabbixAPIResult itemGet(int serverId, List<String> hostIds);

    MWZabbixAPIResult itemKeyGetByTriggerids(int serverId, List<String> triggerIds);

    MWZabbixAPIResult getDRuleByHostId(int serverId, String hostId);

    MWZabbixAPIResult getDRuleByHostIdList(int serverId, List<String> hostIds);

    MWZabbixAPIResult getHostprototype(int serverId, String discoveryId);

    MWZabbixAPIResult getHostGroup(int serverId);

    MWZabbixAPIResult getGroupHostsByGroupIds(int serverId, List<String> groupId);

    MWZabbixAPIResult getGroupHostsByHostIds(int serverId, List<String> hostId);

    MWZabbixAPIResult getGroupHosts(int serverId, String groupId);

    MWZabbixAPIResult getGroupHostByNames(int serverId, List<String> groupNames);

    MWZabbixAPIResult getHostInfosById(int serverId, String hostId, String hostName);

    MWZabbixAPIResult getHostInfoByName(int serverId, String hostName);

    MWZabbixAPIResult getTemplatesByHostId(int serverId, String hostId);
    MWZabbixAPIResult getGroupHostByName(int serverId, String groupName);

    MWZabbixAPIResult getHostByHostId(int serverId, String hostId, String hostName);

    MWZabbixAPIResult getHostByHostIdByFuzzy(int serverId, String hostId, String hostName);

    MWZabbixAPIResult getHostDetailsByHostIds(int serverId, List<String> hostIds);

    MWZabbixAPIResult itemGetbyFilter(int serverId, List<String> name, Object hostid);

    MWZabbixAPIResult itemGetbyVm(int serverId, List<String> name, Object hostid);

    MWZabbixAPIResult getItemsbyHostId(int serverId, String hostid);

    MWZabbixAPIResult hostGetbyFilterByUUID(int serverId, List<String> hostList);

    MWZabbixAPIResult getItemsbyHostIds(int serverId, Object hostids);

    MWZabbixAPIResult getItemDataByAppName(int serverId, List<String> hostid, String applicationName, String type);

    MWZabbixAPIResult itemsGet(int serverId, String hostid, String itemName);

    MWZabbixAPIResult getItemName(int serverId, List<String> itemids);

    //根据主机id,过滤信息value_type(监控项信息的类型)和valuemapid(关联映射值的id) 得到符合条件的监控项
    MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, List<String> value_types, String valuemapid, String units, String itemName);

    //查询web监测的itemid filter
    MWZabbixAPIResult getWebItemId(Integer monitorServerId, List<String> hostids, String key);

    MWZabbixAPIResult getWebItemByhostId(Integer monitorServerId, List<String> hostids);

    //查询web监测的itemid search
    MWZabbixAPIResult getWebValue(Integer monitorServerId, List<String> hostIds, String key);

    //根据参数不同创建不同的主机
    MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, List<String> templates, List<Map> macro, Map<String, Object> otherParam);

    MWZabbixAPIResult hostgetByTempalteid(Integer monitorServerId, String hostId, String templateid);

    //根据模板id获取模板中的宏值键值对
    MWZabbixAPIResult getMacrosByTemplateId(int serverId, String templateId);

    MWZabbixAPIResult getMacrosByTemplateIdList(int serverId, List<String> templateIdList);

    //根据主机id获取主机关联的模板id
    MWZabbixAPIResult getTemplateIdByHostId(int serverId, String hostId);

    //根据自动发现规则查询监控项,过滤条件
    MWZabbixAPIResult getItemPrototypeFilter(int serverId, String discoveryId, Map<String, Object> filter);

    //根据主机hostids获取templateId
    MWZabbixAPIResult getHostInfosById(int serverId, List<String> hostIds);

    //根据主机id,过滤信息filter 得到符合条件的监控项
    MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, Map<String, Object> filter);

    MWZabbixAPIResult getItemsByTemplateIdsFilter(int serverId, List<String> hostIds, Map<String, Object> filter);

    //根据主机id,过滤信息filter 得到符合条件的监控项
    MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, String templateid, Map<String, Object> filter);

    //根据主机ip信息以及端口号获取过滤后的主机信息
    MWZabbixAPIResult getHostsByIpPort(int serverId, String ip, String port);

    //资产列表维护时进行zabbix的维护
    MWZabbixAPIResult maintenanceCreate(int serverId, Map<String, Object> param, List<HashMap> times);

    //删除资产列表维护数据时同步u删除zabbix中的维护数据
    MWZabbixAPIResult maintenanceDelete(int serverId, List<String> maintenids);

    //查询Zabbix上的数据
    MWZabbixAPIResult maintenanceGet(int serverId, Map params);

    //资产列表维护时进行zabbix的维护
    MWZabbixAPIResult maintenanceUpdate(int serverId, Map<String, Object> param, List<HashMap> times);

    //根据proxyIds 查询关联上的主机数量
    MWZabbixAPIResult proxyGet(int serverId, Object proxyIds);

    MWZabbixAPIResult proxyInfoget(int serverId);

    //根据主机id获取主机对应的接口信息id
    MWZabbixAPIResult hostInterfaceGet(int serverId, Object hostIds);

    //根据接口信息id更新主机对应的接口信息
    MWZabbixAPIResult hostInterfaceUpdate(int serverId, Object interfaceIds, Map<String, Object> interfaceInfo);

    //创建立即执行的任务(包括监控项以及自动发现规则)
    MWZabbixAPIResult taskItems(int serverId, String type, List<String> itemIds);

    MWZabbixAPIResult HistoryGetByTimeAndHistoryListByitem(int serverId, List<String> itemids, long time_from, long time_till, Integer type);

    //批量查询趋势接口数据
    MWZabbixAPIResult trendBatchGet(int serverId, List<String> itemids, Long timeFrom, Long timeTill);

    //修改主机可见名称接口
    MWZabbixAPIResult hostUpdateSoName(Integer serverId, String hostid, String name);

    //批量修改主机可见名称
    MWZabbixAPIResult hostUpdateSoName(Integer serverId, List<MwSyncZabbixAssetsParam> params);

    //创建主机宏接口
    MWZabbixAPIResult hostCreateMacro(Integer serverId, String hostid, String macro, Double value);

    MWZabbixAPIResult executeScript(Integer serverId, String scriptid, String hostid);

    MWZabbixAPIResult getScript(Integer serverId);

    /**
     * 获取当前服务器类别
     *
     * @return
     */
    TPServerTypeEnum getServerType(int serverId);

    MWZabbixAPIResult getTriggeInfo(int serverId, List<String> triggerids);

    MWZabbixAPIResult getAcknowledgeidByObjectId(int serverId, List<String> triggerids);
}

