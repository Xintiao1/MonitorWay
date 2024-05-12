package cn.mw.zbx;

import cn.mw.zbx.dto.MWAlertParamDto;
import cn.mw.zbx.dto.MWWebDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MWZabbixAPIInterface {
  void init();

  void destroy();

  JsonNode call(MWRequestAbstract request);

  boolean login(String user, String password);

  MWZabbixAPIResult alertGetByCurrent(MWAlertParamDto dto);

  MWZabbixAPIResult triggerGet(HashMap<String, Object> param, HashMap<String, Object> filter);

  MWZabbixAPIResult eventGettByTriggers2(ArrayList<String> clocks);

  MWZabbixAPIResult eventGet(HashMap<String, Object> param, HashMap<String, Object> filter);

  MWZabbixAPIResult eventGetByHistory(MWAlertParamDto dto);

  MWZabbixAPIResult getHistAlarmByEventGet(MWAlertParamDto dto);

  MWZabbixAPIResult eventGetByEventid(String eventid);

  MWZabbixAPIResult triggerGetByTriggerid(String triggerid);

  MWZabbixAPIResult itemGetByTriggerid(String triggerid);

  MWZabbixAPIResult itemGet(HashMap<String, Object> param, HashMap<String, Object> filter);

  MWZabbixAPIResult alertGetByEventid(String eventid);

  MWZabbixAPIResult alertGet(HashMap<String, Object> param, HashMap<String, Object> filter);

  MWZabbixAPIResult eventgetByObjectid(String objectid, HashMap<String, Object> filter, long time_from, long time_till);

  MWZabbixAPIResult eventgets(HashMap<String, Object> filter, String acknowledged
          , String severity, Long start, Long end, String subject);

  MWZabbixAPIResult eventacknowledge(String eventids, String msg, String act);

  MWZabbixAPIResult eventacknowledge(String eventids, String act);

  MWZabbixAPIResult alertGet(List<String> hostids);

  MWZabbixAPIResult alertEventGet(List<String> hostids);

  MWZabbixAPIResult getEventSeverity(String eventid);

  MWZabbixAPIResult getEventHistAlert(String objectid);

  MWZabbixAPIResult itemgetbyhostidList(List<String> hostidList);

  MWZabbixAPIResult triggerCreate(String description, String expression, String priority);

  MWZabbixAPIResult triggerCreate(String description, String expression, String rexpression, String priority);

  MWZabbixAPIResult actioncreate(String name, String shortdata, String longdata, Map filter, List operations, Integer status);

  MWZabbixAPIResult actioncreate(String name, String def_longData, String def_shortData, String r_longData
          , String r_shortData, Integer status, Map<String, Object> filter, List operations, List recovery_operation);

  MWZabbixAPIResult actioncreate(String name, String def_longData, String def_shortData
          , Map<String, Object> filter, List operations);

  MWZabbixAPIResult actionupdate(String actionId, String name, String def_longData, String def_shortData
          , String r_longData, String r_shortData, Map<String, Object> filter, List operations, List recovery_operation);

  MWZabbixAPIResult actionDelete(String actionIds);

  MWZabbixAPIResult actionDelete(List<String> actionIds);

  MWZabbixAPIResult triggerDelete(List<String> triggerIds);

  MWZabbixAPIResult hostGetById(String hostid);

  MWZabbixAPIResult actionGetByEventSourceTrigger(String name, String status);

  MWZabbixAPIResult templateGet(String name, Boolean isFilter);

  MWZabbixAPIResult templateGet(String name);

  MWZabbixAPIResult hostGroupGet(String name, Boolean isFilter);

  MWZabbixAPIResult triggerGetItemid(String triggerids);

  MWZabbixAPIResult hostListGetByHostName(ArrayList<String> hostNameList);

  MWZabbixAPIResult alertGetByCurrent(List<String> hostIds);

  MWZabbixAPIResult getHistEvent(Integer count, List<String> hostids);

  MWZabbixAPIResult itemGetbyType(String name, List<String> hostIdList);

  MWZabbixAPIResult HttpTestCreate(MWWebDto webDto);

  MWZabbixAPIResult HttpTestUpdate(MWWebDto webDto);

  MWZabbixAPIResult HttpTestUpdate(String httptestid, Integer status);

  MWZabbixAPIResult HttpTestDelete(List<String> webids);

  MWZabbixAPIResult HttpTestGet(String httptestid);

  MWZabbixAPIResult HttpTestGet(List<String> hostids);

  MWZabbixAPIResult HistoryGetByItemid(String itemids, Integer type);

  MWZabbixAPIResult HistoryGetByTimeAndType(String itemids, Long timeFrom, Long timeTill, Integer type);

  MWZabbixAPIResult HistoryGetByTimeAndType(List<String> itemids, Long timeFrom, Long timeTill, Integer type);

  MWZabbixAPIResult HistoryGetByTime(String itemids, Long timeFrom, Long timeTill);

  MWZabbixAPIResult proxyGetByServerIp(String serverIp, String port, String dns);

  MWZabbixAPIResult createProxy(String engineName, String serverIp, String status, String port, String dns);

  MWZabbixAPIResult updateProxy(String proxyId, String engineName, String serverIp, String status, String port, String dns);

  MWZabbixAPIResult proxyDelete(List<String> proxyIds);

  MWZabbixAPIResult itemGetbyType(String name, String hostid, Boolean isFilter);

  MWZabbixAPIResult itemGetbyFilter(String name, List<String> hostids);

  MWZabbixAPIResult itemGetbySearch(List<String> nameList, String hostid);

  MWZabbixAPIResult HistoryGetByItemid(List<String> itemids, Integer type);

  MWZabbixAPIResult getItemDataByAppName(String hostid, String applicationName, String type);

  MWZabbixAPIResult getApplication(String hostid);

  MWZabbixAPIResult getApplicationName(String hostid, String applicationName);

  MWZabbixAPIResult itemgetbyhostid(String hostid, String name, Boolean isName);

  MWZabbixAPIResult itemGetbyHostId(String hostid);

  MWZabbixAPIResult problemget(List<String> hostids);

  MWZabbixAPIResult problemget(List<String> hostids, Boolean acknowledged);

  MWZabbixAPIResult problemget(List<String> hostids,String timeTill);

  MWZabbixAPIResult itemUpdate(String itemid, Integer status);

  MWZabbixAPIResult trendGet(String itemid, Long timeFrom, Long timeTill);

  MWZabbixAPIResult hostCreate(String host, ArrayList<String> groupIdList
          , List<Map<String, Object>> hostInterfaces, ArrayList<String> templates,
                               List<Map> macro, Integer status);

  MWZabbixAPIResult hostCreate(String host, ArrayList<String> groupIdList
          , List<Map<String, Object>> hostInterfaces, ArrayList<String> templates,
                               List<Map> macro, Integer status,String proxyId);

  MWZabbixAPIResult hostDelete(List<String> ids);

  MWZabbixAPIResult hostUpdate(String hostids, String status);

  MWZabbixAPIResult hostUpdate(List<String> hostids, Integer status);

  MWZabbixAPIResult hostUpdate(List<String> hostids, List<String> templateids, List<Map<String, Object>> hostInterfaces);

  MWZabbixAPIResult hostMassUpdate(String hostid, String templateid);

  MWZabbixAPIResult hostMassRemove(String hostid, String templateid);

  MWZabbixAPIResult hostListDeleteById(ArrayList<String> hostIdList);

  MWZabbixAPIResult hostgroupCreate(String name);

  MWZabbixAPIResult hostgroupUpdate(String groupid, String name);

  MWZabbixAPIResult hostgroupDelete(ArrayList<String> groupids);

  MWZabbixAPIResult hostgroupGetById(List<String> groupids);

  MWZabbixAPIResult ItemGetBykey(String hostid, String key);

  MWZabbixAPIResult getValueMap();

  MWZabbixAPIResult getValueMapById(List<String> valuemapIds);

  MWZabbixAPIResult itemGet(List<String> hostIds);

  MWZabbixAPIResult getDRuleByHostId(String hostId);

  MWZabbixAPIResult getHostprototype(String discoveryId);

  MWZabbixAPIResult getHostGroup();

  MWZabbixAPIResult getGroupHostsByGroupIds(List<String> groupId);

  MWZabbixAPIResult getGroupHosts(String groupId);

  MWZabbixAPIResult getGroupHostByNames(List<String> groupNames);

  MWZabbixAPIResult getGroupHostByName(String groupName);

  MWZabbixAPIResult getHostByHostId(String hostId, String hostName);

  MWZabbixAPIResult itemGetbyFilter(List<String> name, String hostid);

  MWZabbixAPIResult getItemsbyHostId(String hostid);

  MWZabbixAPIResult getItemDataByAppName(List<String> hostid, String applicationName, String type);

  MWZabbixAPIResult itemsGet(String hostid, String itemName);

  MWZabbixAPIResult getItemName(List<String> itemids);
}

