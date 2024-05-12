package cn.mw.zbx;

import cn.mw.monitor.service.model.param.MwModelWebMonitorTriggerParam;
import cn.mwpaas.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MWZabbixApiV6 extends MWZabbixImpl {

    /**
     * zabbix高于5.2版本，无应用集后的获取应用集方式
     */
    private final static String HIGH_ZABBIX_VERSION_TAG = "Application";
    private static final String WEB_TEST_FAIL = "web.test.fail";
    private static final String WEB_TEST_TIME = "web.test.time";
    private static final String WEB_TEST_RSPCODE = "web.test.rspcode";
    private static final String WEB_TEST_ERROR = "web.test.error";
    /**
     * 查询5.2之后,ZABBIX无应用集数据查询
     *
     * @param serverId zabbix服务器ID
     * @param hostId   主机ID(资产ID)
     * @return
     */
    @Override
    public MWZabbixAPIResult getApplication(int serverId, String hostId) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid"});
        if (null != hostId && StringUtils.isNotEmpty(hostId)) {
            param.put("hostids", hostId);
        }
        param.put("selectTags", "extend");
        return callApi(method, param);
    }


    @Override
    public MWZabbixAPIResult getItemNameByAppName(int serverId, String hostid, String applicationName, String type) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != type) {
            HashMap search = new HashMap<>();
            //分区类型
            search.put("name", type);
            param.put("search", search);
        }
        if (null != hostid && StringUtils.isNotEmpty(hostid)) {
            param.put("hostids", hostid);
        }
        param.put("tags", getTags(applicationName));
        param.put("output", new String[]{"name"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemDataByAppName(int serverId, String hostid, String applicationName, String type) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != type) {
            HashMap search = new HashMap<>();
            search.put("name", type);//分区类型
            param.put("search", search);
        }
        if (null != hostid && StringUtils.isNotEmpty(hostid)) {
            param.put("hostids", hostid);
        }
        param.put("tags", getTags(applicationName));
        param.put("output", new String[]{"itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemDataByAppName(int serverId, List<String> hostid, String applicationName, String type) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != type) {
            HashMap search = new HashMap<>();
            //分区类型
            search.put("name", type);
            param.put("search", search);
        }
        if (hostid.size() > 0) {
            param.put("hostids", hostid);
        }
        param.put("tags", getTags(applicationName));
        param.put("output", new String[]{"hostid", "itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemDataByAppNameList(int serverId, List<String> hostIdList,
                                                      String applicationName, List<String> type,
                                                      boolean searchByAny) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != type) {
            HashMap search = new HashMap<>();
            search.put("name", type);//分区类型
            param.put("search", search);
        }
        param.put("searchByAny", searchByAny);
        if (null != hostIdList) {
            param.put("hostids", hostIdList);
        }
        param.put("tags", getTags(applicationName));
        param.put("output", new String[]{"hostid", "itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }


    @Override
    public MWZabbixAPIResult itemGetbySearchNames(int serverId, String applicationName, Object nameList, Object hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", nameList);
        params.put("search", search);
        params.put("searchByAny", true);
        params.put("hostids", hostid);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        output.add("units");
        output.add("lastvalue");
        output.add("value_type");
        output.add("valuemapid");
        output.add("hostid");
        output.add("name");
        output.add("application");
        params.put("tags", getTags(applicationName));
        params.put("output", output);
        return callApi(method, params);
    }

    /**
     * 5.4及以上版本无应用集获取应用集数据
     * @param applicationName 应用集名称
     * @return
     */
    private List<Map> getTags(String applicationName){
        Map map  = new HashMap<>();
        map.put("tag",HIGH_ZABBIX_VERSION_TAG);
        map.put("value",applicationName);
        map.put("operator",1);
        return Arrays.asList(map);
    }

    /**
     * 6.0及以上版本维护方式有所改变
     * @param serverId
     * @param param
     * @param times
     * @return
     */
    @Override
    public MWZabbixAPIResult maintenanceCreate(int serverId, Map<String, Object> param, List<HashMap> times) {
        String method = "maintenance.create";
        HashMap params = new HashMap();
        params.put("maintenance_type", param.get("maintenanceType"));
        params.put("name", param.get("name"));
        params.put("active_since", param.get("activeSince"));
        params.put("active_till", param.get("activeTill"));
        params.put("hosts", param.get("hosts"));
        if(CollectionUtils.isNotEmpty(times)){
            for (HashMap time : times) {
                //检查是否设置了时间周期
                Object hasPeriod = time.get("hasPeriod");
                if(null != hasPeriod){
                    time.remove("hasPeriod");
                    continue;
                }

                Object period = time.get("period");
                time.clear();
                time.put("period",period);
                time.put("timeperiod_type",0);
            }
        }
        params.put("timeperiods", times);
        return callApi(method, params);
    }

    /**
     * 6.0及以上版本维护方式有所改变
     * @param serverId
     * @param param
     * @param times
     * @return
     */
    @Override
    public MWZabbixAPIResult maintenanceUpdate(int serverId, Map<String, Object> param, List<HashMap> times) {
        String method = "maintenance.update";
        HashMap params = new HashMap();
        params.put("maintenance_type", param.get("maintenanceType"));
        params.put("name", param.get("name"));
        params.put("active_since", param.get("activeSince"));
        params.put("active_till", param.get("activeTill"));
        params.put("hosts", param.get("hosts"));
        params.put("maintenanceid", param.get("maintenanceid"));
        if(CollectionUtils.isNotEmpty(times)){
            for (HashMap time : times) {
                //检查是否设置了时间周期
                Object hasPeriod = time.get("hasPeriod");
                if(null != hasPeriod){
                    time.remove("hasPeriod");
                    continue;
                }

                Object period = time.get("period");
                time.clear();
                time.put("period",period);
                time.put("timeperiod_type",0);
            }
        }
        params.put("timeperiods", times);
        return callApi(method, params);
    }


    //创建带有恢复条件的触发器
    public MWZabbixAPIResult triggerCreate(int serverId, String description, String expression, String rexpression, String priority) {
        String method = "trigger.create";
        HashMap params = new HashMap();
        params.put("description", description);
        params.put("expression", expression);
        params.put("recovery_mode", 1);//恢复模式为恢复表达式
        params.put("recovery_expression", rexpression);
        params.put("priority", priority);
        params.put("manual_close", "1");
        return callApi(method, params);
    }

    //创建带有恢复条件的触发器
    public MWZabbixAPIResult triggerCreateByKey(int serverId, String description, String hostName,String webMonitorName,String key,String code, String priority) {
        String webFailExpression = getWebExpression(hostName, webMonitorName, key,code);
        String webFailRexExpression = getWebRexExpression(hostName, webMonitorName, key,code);
        return triggerCreate(serverId, description,webFailExpression,webFailRexExpression,priority);
    }

    //批量创建带有恢复条件的触发器
    public MWZabbixAPIResult triggerBatchCreate(int serverId, List<MwModelWebMonitorTriggerParam> triggerParams) {
        String method = "trigger.create";
        List<HashMap> paramList = new ArrayList<>();
        for (MwModelWebMonitorTriggerParam param : triggerParams) {
            String webFailExpression = getWebExpression(param.getHostName(), param.getWebName(), param.getKey(),param.getCode());
            String webFailRexExpression = getWebRexExpression(param.getHostName(), param.getWebName(), param.getKey(),param.getCode());
            HashMap params = new HashMap();
            params.put("description", param.getDescription());
            params.put("expression", webFailExpression);
            params.put("recovery_mode", 1);//恢复模式为恢复表达式
            params.put("recovery_expression", webFailRexExpression);
            params.put("priority", param.getPriority());
            params.put("manual_close", "1");
            paramList.add(params);
        }
        return callApi(method, paramList);
    }


    private String getWebRexExpression(String hostName, String webName, String key,String code) {
        StringBuffer sb = new StringBuffer("last(/");
        if(key.equals(WEB_TEST_ERROR)){
            sb = new StringBuffer("nodata(/");
        }
        sb.append(hostName);
        sb.append("/");
        sb.append(key);
        if (key.equals(WEB_TEST_RSPCODE)) {
            sb.append("[" + webName + "," + webName +"]");
            sb.append(")=");
            sb.append(code);
        }else if (key.equals(WEB_TEST_TIME)) {
//            sb.append("[" + webName + "," + webName + "," + "resp]");
//            sb.append(".last(,360s)}>200");
        }else if (key.equals(WEB_TEST_ERROR)) {
            sb.append("[" + webName +"]");
            sb.append(",2m)=1");
        } else {
            sb.append("[" + webName + "]");
            sb.append(",#3)=0");
        }
        return sb.toString();
    }

    private String getWebExpression(String hostName, String webName, String key,String code) {
        StringBuffer sb = new StringBuffer("last(/");
        if(key.equals(WEB_TEST_ERROR)){
            sb = new StringBuffer("nodata(/");
        }
        sb.append(hostName);
        sb.append("/");
        sb.append(key);
        if (key.equals(WEB_TEST_RSPCODE)) {
            sb.append("[" + webName + "," + webName +"]");
            sb.append(")<>");
            sb.append(code);
        }else if (key.equals(WEB_TEST_TIME)) {
            sb.append("[" + webName + "," + webName + "," + "resp]");
            sb.append(",360s)>200");
        }else if (key.equals(WEB_TEST_ERROR)) {
            sb.append("[" + webName +"]");
            sb.append(",2m)=0");
        }else {
            sb.append("[" + webName + "]");
            sb.append(",#3)=1");
        }
        return sb.toString();
    }
}
