package cn.mw.zbx;

import cn.joinhealth.zbx.enums.action.EventSourceEnum;
import cn.mw.zbx.dto.MWAlertParamDto;
import cn.mw.zbx.dto.MWStep;
import cn.mw.zbx.dto.MWWebDto;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.DateUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/*
 * 默认zabbix api基于zabbix5.0版本
 */
@Data
@Slf4j
//@Component
public class MWZabbixApi implements MWZabbixAPIInterface {

    @Value("${monitor.zabbix.debug}")
    private boolean debug;

    private URI uri;
    private volatile String auth;
    private CloseableHttpClient httpClient;

//    @Value("${zabbix.url}")
    private String zabbixUrl;

//    @Value("${zabbix.user}")
    private String zabbixUser;

//    @Value("${zabbix.password}")
    private String zabbixPassword;

    @Autowired
    private Environment env;


//    public MWZabbixApi(String url) {
//        try {
//            uri = new URI(url.trim());
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("url invalid", e);
//        }
//    }
//
//    public MWZabbixApi() {
//
//    }
//
//    public MWZabbixApi(URI uri) {
//        this.uri = uri;
//    }
//
//    public MWZabbixApi(String url, CloseableHttpClient httpClient) {
//        this(url);
//        this.httpClient = httpClient;
//    }
//
//    public MWZabbixApi(URI uri, CloseableHttpClient httpClient) {
//        this(uri);
//        this.httpClient = httpClient;
//    }
//
    @PostConstruct
    public void init() {
        if ((null == zabbixUrl) || "".equals(zabbixUrl)) {
            log.error("zabbixUrl is null or empty");
        }
        try {
            uri = new URI(zabbixUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException("url invalid", e);
        }

        if (httpClient == null) {
            httpClient = HttpClients.custom().build();
        }
        login(zabbixUser, zabbixPassword);
    }

    @PreDestroy
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error("close httpclient error!", e);
            }
            httpClient = null;
        }
    }

    public boolean login(String user, String password) {
        this.auth = null;
        String method = "user.login";
        HashMap<String, String> params = new HashMap();
        params.put("user", user);
        params.put("password", password);

        MWZabbixAPIResult MWZabbixAPIResult = callApi(method, params);
        if (MWZabbixAPIResult.isFail()) {
            log.info("User {} login failure. Error Info:{}", user, MWZabbixAPIResult.getData());
            return false;
        } else {
            String auth = ((TextNode) MWZabbixAPIResult.getData()).asText();
            if (auth != null && !auth.isEmpty()) {
                this.auth = auth;
                log.info("User:{} login success.", user);
                return true;
            }
            return false;
        }
    }


    public JsonNode call(MWRequestAbstract request) {
        if (request.getAuth() == null) {
            request.setAuth(this.auth);
        }
        try {
            HttpUriRequest httpRequest = org.apache.http.client.methods.RequestBuilder.post().setUri(uri)
                    .addHeader("Content-Type", "application/json")
                    .setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON)).build();
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            return new ObjectMapper().readTree(entity.getContent());
        } catch (IOException e) {
            throw new RuntimeException("DefaultZabbixApi call exception!", e);
        }
    }

    public MWZabbixAPIResult callApi(String method) {
        return callApi(method, Collections.emptyList());
    }

    public MWZabbixAPIResult callApi(String method, Object params) {
        MWZabbixAPIResult MWZabbixAPIResult = new MWZabbixAPIResult();
        Date start = new Date();
        MWRequestBuilder requestBuilder = MWRequestBuilder.newBuilder().initRequest(params).method(method);
        if(debug) {
            log.info("callApi ,method:{},params:{}", method, JSON.toJSON(params));
        }
        JsonNode response = call(requestBuilder.build());
        if (response.has("error")) {
            MWZabbixAPIResult.setCode(response.get("error").get("code").asInt());
            MWZabbixAPIResult.setMessage(response.get("error").get("message").asText());
            MWZabbixAPIResult.setData(response.get("error").get("data").asText());
        } else {
            MWZabbixAPIResult.setMessage("Call Zabbix API Success.");
            MWZabbixAPIResult.setCode(MWZabbixAPIResultCode.SUCCESS.code());
            MWZabbixAPIResult.setData(response.get("result"));
        }
         // printAPIResult(MWZabbixAPIResult);
        long interval = DateUtils.between(start ,new Date() , DateUnitEnum.SECOND);
        log.info("callApi cost {}s" ,interval);
        return MWZabbixAPIResult;
    }

    private void printAPIResult(MWZabbixAPIResult MWZabbixAPIResult) {
        try {
            log.info("Call API. Result is :{}", new ObjectMapper().
                    writerWithDefaultPrettyPrinter().writeValueAsString(MWZabbixAPIResult));
        } catch (Exception exception) {
            log.error("call api error:", exception);
        }
    }

    /**
     * 当前告警
     *
     * @return
     */
    public MWZabbixAPIResult alertGetByCurrent(MWAlertParamDto dto) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        filter.put("value", 1);
        //filter.put("status", 0);
        ArrayList<String> output = new ArrayList<>();
        output.add("triggerid");
        output.add("description");
        output.add("lastchange");
        output.add("priority");

        param.put("sortfield", "priority");
        param.put("limit", 1000);
        param.put("sortorder", "DESC");
        param.put("output", output);
        if (null != dto.getHostids() && dto.getHostids().size() > 0) {
            param.put("hostids", dto.getHostids());
        }
        if (null != dto.getAcknowledged() && StringUtils.isNotEmpty(dto.getAcknowledged())) {
            if ("0".equals(dto.getAcknowledged())) {
                param.put("acknowledged", false);
            }
            param.put("acknowledged", true);
        }

        return triggerGet(param, filter);
    }

    public MWZabbixAPIResult triggerGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "trigger.get";
        param.put("filter", filter);
        return callApi(method, param);
    }

    /**
     * 得到当前告警的事件2
     *
     * @param clocks
     * @return
     */
    public MWZabbixAPIResult eventGettByTriggers2(ArrayList<String> clocks) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        //排序
        ArrayList<String> sortfield = new ArrayList<>();
        sortfield.add("clock");
        filter.put("clock", clocks);
        param.put("output", "extend");
        param.put("select_acknowledges", "extend");
        param.put("selectTags", "extend");
        param.put("selectHosts", "extend");
        param.put("select_alerts", "extend");
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        return eventGet(param, filter);
    }


    public MWZabbixAPIResult eventGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "event.get";
        param.put("filter", filter);
        return callApi(method, param);
    }

    public MWZabbixAPIResult eventGetByHistory(MWAlertParamDto dto) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        //排序
        ArrayList<String> sortfield = new ArrayList<>();
        sortfield.add("clock");
        sortfield.add("eventid");
        filter.put("value", 1);
        if (null != dto.getHostids() && dto.getHostids().size() > 0) {
            param.put("hostids", dto.getHostids());
        }
        if (!"".equals(dto.getEventid()) && null != dto.getEventid()) {
            param.put("eventids", dto.getEventid());
        }
        long endTime = 0;
        long beginTime = 0;
        if (StringUtils.isNotEmpty(dto.getStartTime()) && StringUtils.isNotEmpty(dto.getEndTime())) {
            endTime = Long.parseLong(dto.getEndTime());
            beginTime = Long.parseLong(dto.getStartTime());
        } else {
            endTime = new Date().getTime() / 1000;
            beginTime = 0;
            int m = 7;
            if (StringUtils.isNotEmpty(dto.getDays())) {
                m = Integer.parseInt(dto.getDays());
            }
            beginTime = curTimeMinusN(m) / 1000;
        }

        param.put("output", "extend");
        param.put("selectHosts", "extend");
        param.put("select_alerts", "extend");
        param.put("select_acknowledges", "extend");
        param.put("selectTags", "extend");
        param.put("time_from", beginTime);
        param.put("time_till", endTime);
        param.put("limit", 1000);
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        return eventGet(param, filter);
    }

    public MWZabbixAPIResult getHistAlarmByEventGet(MWAlertParamDto dto) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        //排序
        ArrayList<String> sortfield = new ArrayList<>();
        sortfield.add("clock");
        sortfield.add("eventid");
        param.put("hostids", dto.getHostids());
        long endTime = 0;
        long beginTime = 0;
        if (StringUtils.isNotEmpty(dto.getStartTime()) && StringUtils.isNotEmpty(dto.getEndTime())) {
            endTime = Long.parseLong(dto.getEndTime());
            beginTime = Long.parseLong(dto.getStartTime());
        } else {
            endTime = new Date().getTime() / 1000;
            beginTime = 0;
            int m = 7;
            if (StringUtils.isNotEmpty(dto.getDays())) {
                m = Integer.parseInt(dto.getDays());
            }
            beginTime = curTimeMinusN(m) / 1000;
        }
        param.put("output", "extend");
        List selectHosts = new ArrayList();
        selectHosts.add("hostid");
        param.put("selectHosts", selectHosts);
        param.put("time_from", beginTime);
        param.put("time_till", endTime);
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        return eventGet(param, filter);
    }


    private static Long curTimeMinusN(int m) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -m);
        return c.getTime().getTime();
    }

    /**
     * 得到事件详情
     *
     * @param eventid
     * @return
     */
    public MWZabbixAPIResult eventGetByEventid(String eventid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        param.put("eventids", eventid);
        param.put("selectTags", "extend");
        param.put("selectHosts", "extend");
        param.put("output", "extend");
        return eventGet(param, filter);
    }

    /**
     * 根据事件ID得到触发器
     *
     * @param triggerid
     * @return
     */
    public MWZabbixAPIResult triggerGetByTriggerid(String triggerid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("triggerids", triggerid);
        param.put("output", "extend");
        return triggerGet(param, filter);
    }

    /**
     * 根据事件ID得到触发器
     *
     * @param
     * @return
     */
    public MWZabbixAPIResult itemGetByTriggerid(String triggerid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("triggerids", triggerid);
        param.put("output", "extend");
        return itemGet(param, filter);
    }

    public MWZabbixAPIResult itemGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "item.get";
        param.put("filter", filter);
        return callApi(method, param);
    }

    /**
     * 活动告警信息
     *
     * @param eventid
     * @return
     */
    public MWZabbixAPIResult alertGetByEventid(String eventid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("eventids", eventid);
        param.put("output", "extend");
        return alertGet(param, filter);
    }

    public MWZabbixAPIResult alertGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "alert.get";
        param.put("filter", filter);
        return callApi(method, param);
    }

    /**
     * 获取触发器事件
     *
     * @param objectid
     * @return
     */
    public MWZabbixAPIResult eventgetByObjectid(String objectid, HashMap<String, Object> filter, long time_from, long time_till) {
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("filter", filter);
        if (time_from > 0) {
            params.put("time_from", time_from);
        }
        if (time_till > 0) {
            params.put("time_till", time_till);
        }
        params.put("output", new String[]{"severity", "acknowledged", "eventid", "objectid", "clock", "ns", "name"});
        params.put("objectids", objectid);
        params.put("select_acknowledges", new String[]{"userid", "alias", "name", "message", "clock"});
        params.put("selectHosts", new String[]{"hosts"});
        params.put("sortfield", new String[]{"clock"});
        params.put("only_true", true);
        params.put("sortorder", "DESC");
        return callApi(method, params);
    }

    /**
     * 查询事件
     *
     * @param filter
     * @return
     */
    public MWZabbixAPIResult eventgets(HashMap<String, Object> filter, String acknowledged, String severity, Long start, Long end, String subject) {
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("filter", filter);
        if (start > 0) {
            params.put("time_from", start);
        }
        if (end > 0) {
            params.put("time_till", end);
        }
        HashMap map = new HashMap();
        if (StringUtils.isNotEmpty(subject)) {
            map.put("name", subject);
        }
        if (StringUtils.isNotEmpty(severity)) {
            params.put("severities", severity);
        }
        if (StringUtils.isNotEmpty(acknowledged)) {
            params.put("acknowledged", acknowledged);
        }
        params.put("search", map);
        params.put("output", "extend");
        params.put("selectHosts", new String[]{"hosts"});
        params.put("sortfield", new String[]{"clock"});
        params.put("select_acknowledges", new String[]{"userid", "alias", "name", "message", "clock"});
        params.put("limit", "1000");
        params.put("searchByAny", true);
        params.put("sortorder", "DESC");
        return callApi(method, params);
    }

    /**
     * 确认事件
     *
     * @param eventids
     * @return
     */
    public MWZabbixAPIResult eventacknowledge(String eventids, String msg, String act) {
        String method = "event.acknowledge";
        HashMap params = new HashMap();
        params.put("eventids", eventids);
        params.put("message", msg);
        if ("cl".equals(act)) {
            params.put("action", 7);
        } else {
            params.put("action", 6);//1 - 关闭问题,2 - 确认事件;4 - 增加消息; 8 - 更改严重等级.(可以是任何值的组合，7就是前面三种组合起来的)
        }
        return callApi(method, params);
    }

    /**
     * 确认事件
     *
     * @param eventids
     * @return
     */
    public MWZabbixAPIResult eventacknowledge(String eventids, String act) {
        String method = "event.acknowledge";
        HashMap params = new HashMap();
        params.put("eventids", eventids);
        if ("cl".equals(act)) {
            params.put("action", 1);
            params.put("message", "关闭");
        } else {
            params.put("action", 2);//1 - 关闭问题,2 - 确认事件;4 - 增加消息; 8 - 更改严重等级.(可以是任何值的组合，7就是前面三种组合起来的)
            params.put("message", "确认");
        }
        return callApi(method, params);
    }


    /**
     * 获取告警
     *
     * @param
     * @return
     */
    public MWZabbixAPIResult alertGet(List<String> hostids) {
        String method = "alert.get";
        HashMap filter = new HashMap();
        HashMap params = new HashMap();
        filter.put("value", 1);
        params.put("filter", filter);
        params.put("output", "extend");
        List selectHosts = new ArrayList();
        selectHosts.add("hostid");
        params.put("selectHosts", selectHosts);
        params.put("hostids", hostids);
        return callApi(method, params);
    }

    /**
     * 获取告警
     *
     * @param
     * @return
     */
    public MWZabbixAPIResult alertEventGet(List<String> hostids) {
        String method = "event.get";
        HashMap filter = new HashMap();
        HashMap params = new HashMap();
        filter.put("value", 1);
        params.put("filter", filter);
        params.put("output", "extend");
        List selectHosts = new ArrayList();
        selectHosts.add("hostid");
        params.put("selectHosts", selectHosts);
        params.put("select_alerts", "extend");
        params.put("hostids", hostids);
        return callApi(method, params);
    }


    public MWZabbixAPIResult getEventSeverity(String eventid) {
        String method = "event.get";
        HashMap params = new HashMap();
        List output = new ArrayList();
        output.add("severity");
        params.put("eventids", eventid);
        params.put("output", output);
        return callApi(method, params);
    }


    //获得当前事件的历史告警
    public MWZabbixAPIResult getEventHistAlert(String objectid) {
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("output", "extend");
        HashMap filter = new HashMap();
        filter.put("value", 1);
        params.put("filter", filter);
        params.put("objectids", objectid);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        return callApi(method, params);
    }


    public MWZabbixAPIResult itemgetbyhostidList(List<String> hostidList) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid", "type", "key_", "name", "hostid"});
        param.put("hostids", hostidList);
        return callApi(method, param);
    }

    //创建触发器
    public MWZabbixAPIResult triggerCreate(String description, String expression, String priority) {
        String method = "trigger.create";
        HashMap params = new HashMap();
        params.put("description", description);
        params.put("expression", expression);
        params.put("priority", priority);
        params.put("manual_close", "1");
        return callApi(method, params);
    }

    //创建带有恢复条件的触发器
    public MWZabbixAPIResult triggerCreate(String description, String expression, String rexpression, String priority) {
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


    /**
     * 创建动作
     *
     * @return
     */
    public MWZabbixAPIResult actioncreate(String name, String shortdata, String longdata, Map filter, List operations, Integer status) {
        String method = "action.create";
        HashMap params = new HashMap();
        params.put("name", name);//动作的名称
        params.put("eventsource", 0);//动作将处理的事件源的类型
        params.put("status", status);//动作是启动还是禁用0 - (默认) 启用 1 - 禁用
        params.put("esc_period", "30m");//默认操作步骤持续时间。必须大于 60 秒。接受秒，带后缀的时间单位
        params.put("def_shortdata", shortdata);//异常消息主题      默认就是名称
        params.put("def_longdata", longdata);//异常消息文本   告警消息
        params.put("filter", filter);//动作过滤器对象
        params.put("operations", operations);//创建的动作操作
        ArrayList<HashMap> rec = new ArrayList<>();
        HashMap recmap = new HashMap();
        //recmap.put("operationtype","11");
        recmap.put("operationtype", "0");
        HashMap msgmap = new HashMap();
        msgmap.put("default_msg", "1");
        recmap.put("opmessage", msgmap);
        rec.add(recmap);
        return callApi(method, params);
    }

    public MWZabbixAPIResult actioncreate(String name, String def_longData, String def_shortData, String r_longData, String r_shortData, Integer status, Map<String, Object> filter, List operations, List recovery_operation) {
        String method = "action.create";
        HashMap params = new HashMap();
        params.put("name", name);//动作的名称
        params.put("eventsource", 0);//动作将处理的事件源的类型 触发器类型
        params.put("status", status);//动作是启动还是禁用0 - (默认) 启用 1 - 禁用
        params.put("esc_period", "30m");//默认操作步骤持续时间。必须大于 60 秒。接受秒，带后缀的时间单位
        params.put("def_shortdata", def_shortData);//异常消息主题      默认就是名称
        params.put("def_longdata", def_longData);//异常消息文本   告警消息
        params.put("filter", filter);//动作过滤器对象
        params.put("operations", operations);//创建的动作操作
        if (recovery_operation.size() > 0) {
            params.put("recovery_operations", recovery_operation);//创建的动作操作
        }
        if (StringUtils.isNotEmpty(r_longData) & StringUtils.isNotEmpty(r_shortData)) {
            params.put("r_longdata", r_longData);
            params.put("def_shortdata", r_shortData);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult actioncreate(String name, String def_longData, String def_shortData, Map<String, Object> filter, List operations) {
        String method = "action.create";
        HashMap params = new HashMap();
        params.put("name", name);//动作的名称
        params.put("eventsource", 0);//动作将处理的事件源的类型 触发器类型
        params.put("status", 0);//动作是启动还是禁用0 - (默认) 启用 1 - 禁用
        params.put("esc_period", "30m");//默认操作步骤持续时间。必须大于 60 秒。接受秒，带后缀的时间单位
        params.put("def_shortdata", def_shortData);//异常消息主题      默认就是名称
        params.put("def_longdata", def_longData);//异常消息文本   告警消息
        params.put("filter", filter);//动作过滤器对象
        params.put("operations", operations);//创建的动作操作
        return callApi(method, params);
    }

    /**
     * 修改动作
     *
     * @return
     */
    public MWZabbixAPIResult actionupdate(String actionId, String name, String def_longData, String def_shortData, String r_longData, String r_shortData, Map<String, Object> filter, List operations, List recovery_operation) {
        String method = "action.update";
        HashMap params = new HashMap();
        params.put("actionid", actionId);//动作的名称
        params.put("name", name);//动作的名称
        params.put("eventsource", 0);//动作将处理的事件源的类型 触发器类型
        params.put("status", 0);//动作是启动还是禁用0 - (默认) 启用 1 - 禁用
        params.put("esc_period", "30m");//默认操作步骤持续时间。必须大于 60 秒。接受秒，带后缀的时间单位
        params.put("def_shortdata", def_shortData);//异常消息主题      默认就是名称
        params.put("def_longdata", def_longData);//异常消息文本   告警消息
        params.put("filter", filter);//动作过滤器对象
        params.put("operations", operations);//创建的动作操作
        if (recovery_operation != null) {
            params.put("recovery_operation", recovery_operation);//创建的动作操作
        }
        if (StringUtils.isNotEmpty(r_longData) & StringUtils.isNotEmpty(r_shortData)) {
            params.put("r_longdata", r_longData);
            params.put("def_shortdata", r_shortData);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult actionDelete(String actionIds) {
        String method = "action.delete";
        List<String> params = new ArrayList<>();
        params.add(actionIds);
        return callApi(method, actionIds);
    }

    /**
     * 修改动作
     *
     * @return
     */
    public MWZabbixAPIResult actionDelete(List<String> actionIds) {
        String method = "action.delete";
        return callApi(method, actionIds);
    }

    public MWZabbixAPIResult triggerDelete(List<String> triggerIds) {
        String method = "trigger.delete";
        return callApi(method, triggerIds);
    }

    /**
     * 根据主机ID查询主机信息
     *
     * @param hostid
     * @return
     */
    public MWZabbixAPIResult hostGetById(String hostid) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> list = new ArrayList<>();
        list.add(hostid);
        params.put("hostids", hostid);
        return callApi(method, params);
    }

    /**
     * 获取数据源为触发器的动作
     *
     * @return
     */
    public MWZabbixAPIResult actionGetByEventSourceTrigger(String name, String status) {
        String method = "action.get";
        HashMap params = new HashMap();
        params.put("output", "extend");
        params.put("selectFilter", "extend");
        params.put("selectOperations", "extend");
        if (StringUtils.isNoneBlank(name)) {
            params.put("name", name);
        }
        if (StringUtils.isNoneBlank(status)) {
            params.put("status", status);
        }
        HashMap filter = new HashMap();
        filter.put("eventsource", EventSourceEnum.TRIGGER.getCode());
        params.put("filter", filter);
        return callApi(method, params);
    }

    /**
     * 获取模板
     *
     * @param name
     * @return
     */
    public MWZabbixAPIResult templateGet(String name, Boolean isFilter) {
        String method = "template.get";
        HashMap params = new HashMap();
        HashMap params1 = new HashMap();
        if (null != name && StringUtils.isNotEmpty(name)) {
            //查询指定
            params1.put("name", name);
            if (isFilter) {
                params.put("filter", params1);
            }
            params.put("search", params1);

        }
        params.put("output", new String[]{"name", "templateid"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult templateGet(String name) {
        return templateGet(name, false);
    }

    /**
     * 获取主机组
     *
     * @return
     */
    public MWZabbixAPIResult hostGroupGet(String name, Boolean isFilter) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        HashMap<String, Object> search = new HashMap();
        if (null != name && StringUtils.isNotEmpty(name)) {
            search.put("name", name);
            if (isFilter) {
                params.put("filter", search);
            }
            params.put("search", search);
        }
        params.put("output", new String[]{"name"});
        return callApi(method, params);
    }

    /**
     * 根据eventid 查询trigger 的itemid 通过itemid 查询item的name和itemid 和lastclock
     * 再通过itemid 去history取前后一个小时的历史记录
     *
     * @param triggerids
     * @return
     */
    public MWZabbixAPIResult triggerGetItemid(String triggerids) {
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        param.put("selectFunctions", new String[]{"itemid"});
        param.put("triggerids", triggerids);
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostListGetByHostName(ArrayList<String> hostNameList) {
        String method = "host.get";
        HashMap<String, HashMap<String, List<String>>> params = new HashMap();
        HashMap<String, List<String>> filter = new HashMap();
        filter.put("host", hostNameList);
        params.put("filter", filter);

        return callApi(method, params);
    }

    public MWZabbixAPIResult alertGetByCurrent(List<String> hostIds) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        filter.put("value", 1);
        filter.put("status", 0);
        ArrayList<String> output = new ArrayList<>();
        output.add("triggerid");
        output.add("description");
        output.add("lastchange");
        output.add("priority");
        param.put("sortfield", "priority");
        param.put("limit", 1000);
        param.put("sortorder", "DESC");
        param.put("maintenance", false);
        param.put("active", true);
        param.put("output", output);
        param.put("only_true", 1);
        param.put("monitored", true);
        param.put("withUnacknowledgeEvents", true);
        if (null != hostIds && hostIds.size() > 0) {
            param.put("hostIds", hostIds);
        }
        return triggerGet(param, filter);
    }

    public MWZabbixAPIResult getHistEvent(Integer count, List<String> hostids) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        filter.put("value", 1);
        filter.put("status", 0);
        //排序
        ArrayList<String> sortfield = new ArrayList<>();
        sortfield.add("clock");

        param.put("output", "extend");
        param.put("selectHosts", "extend");
        param.put("select_alerts", "extend");
        param.put("select_acknowledges", "extend");
        param.put("selectTags", "extend");
        param.put("limit", count);
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        //param.put("hostids", "hostids");
        return eventGet(param, filter);
    }

    public MWZabbixAPIResult itemGetbyType(String name, List<String> hostIdList) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", name);//模糊查询名称
        params.put("search", search);
        if (hostIdList != null && hostIdList.size() > 0) {
            params.put("hostids", hostIdList);
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("lastvalue");
        output.add("hostid");
        output.add("name");
        output.add("key_");
        output.add("units");
        output.add("value_type");
        params.put("output", output);
        return callApi(method, params);
    }

    //创建web监测
    public MWZabbixAPIResult HttpTestCreate(MWWebDto webDto) {
        String method = "httptest.create";
        HashMap params = new HashMap();
        params.put("name", webDto.getName());
        params.put("hostid", webDto.getHostId());
        params.put("agent", webDto.getAgent());
        params.put("delay", webDto.getDelay());
        params.put("status", webDto.getStatus());
        params.put("retries", webDto.getRetries());
        params.put("http_proxy", webDto.getHttpProxy());
        HashMap map = new HashMap();
        List setpList = new ArrayList();
        List<MWStep> steps = webDto.getSteps();
        for (MWStep step : steps) {
            map.put("name", step.getName());
            map.put("no", step.getNo());
            map.put("url", step.getUrl());
            if (null != step.getRequired()) {
                map.put("required", step.getRequired());
            }
            map.put("status_codes", step.getStatus_codes());
            map.put("timeout", step.getTimeout());
            map.put("follow_redirects", step.getFollowRedirects());
            setpList.add(map);
        }
        params.put("steps", setpList);
        return callApi(method, params);
    }

    //修改启用状态
    public MWZabbixAPIResult HttpTestUpdate(MWWebDto webDto) {
        String method = "httptest.update";
        HashMap params = new HashMap();
        params.put("httptestid", webDto.getHttptestids());
        params.put("name", webDto.getName());
        params.put("hostid", webDto.getHostId());
        params.put("agent", webDto.getAgent());
        params.put("delay", webDto.getDelay());
        params.put("status", webDto.getStatus());
        params.put("retries", webDto.getRetries());
        params.put("http_proxy", webDto.getHttpProxy());
        HashMap map = new HashMap();
        List setpList = new ArrayList();
        List<MWStep> steps = webDto.getSteps();
        for (MWStep step : steps) {
            map.put("name", step.getName());
            map.put("no", step.getNo());
            map.put("url", step.getUrl());
            if (null != step.getRequired()) {
                map.put("required", step.getRequired());
            }
            map.put("status_codes", step.getStatus_codes());
            map.put("timeout", step.getTimeout());
            map.put("follow_redirects", step.getFollowRedirects());
            setpList.add(map);
        }
        params.put("steps", setpList);
        return callApi(method, params);
    }

    //修改启用状态
    public MWZabbixAPIResult HttpTestUpdate(String httptestid, Integer status) {
        String method = "httptest.update";
        HashMap params = new HashMap();
        params.put("status", status);
        params.put("httptestid", httptestid);
        return callApi(method, params);
    }

    //删除web场景,根据场景id
    public MWZabbixAPIResult HttpTestDelete(List<String> webids) {
        String method = "httptest.delete";
        return callApi(method, webids);
    }

    //根据httptestid获得创建的记录
    public MWZabbixAPIResult HttpTestGet(String httptestid) {
        String method = "httptest.get";
        HashMap params = new HashMap();
        params.put("httptestid", httptestid);
        return callApi(method, params);
    }

    //根据hostidd获得创建web的记录
    public MWZabbixAPIResult HttpTestGet(List<String> hostids) {
        String method = "httptest.get";
        HashMap params = new HashMap();
        params.put("hostids", hostids);
        params.put("output", new String[]{"name", "hostid"});
        params.put("selectSteps", new String[]{"name"});
        return callApi(method, params);
    }


    //获得web最新的一条历史记录
    public MWZabbixAPIResult HistoryGetByItemid(String itemids, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);//0 下载速度和响应时间 3状态和响应码
        return callApi(method, params);
    }

    //根据时间,history查询所有历史   下载记录和响应时间的数据
    public MWZabbixAPIResult HistoryGetByTimeAndType(String itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        //  params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "ASC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetByTimeAndType(List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetByTime(String itemids, Long timeFrom, Long timeTill) {
        return HistoryGetByTimeAndType(itemids, timeFrom, timeTill, 0);
    }

    //    ---------------------------------------------------------proxy---------------------------------------------------------------------------------
//    代理查询
    public MWZabbixAPIResult proxyGetByServerIp(String serverIp, String port, String dns) {
        String method = "proxy.get";
        HashMap params = new HashMap();
        HashMap selectInterface = new HashMap();
        if (serverIp != null && !"".equals(serverIp)) {
            selectInterface.put("dns", null);
            selectInterface.put("ip", serverIp);
            selectInterface.put("useip", "1");
        } else {
            if (dns != null && !"".equals(dns)) {
                selectInterface.put("dns", dns);
                selectInterface.put("ip", null);
                selectInterface.put("useip", "0");
            }
        }
        selectInterface.put("port", port);
        params.put("selectInterface", selectInterface);
        ArrayList<String> output = new ArrayList<>();
        output.add("proxyid");
        output.add("host");
        output.add("proxy_address");
        output.add("status");
        output.add("port");
        output.add("ip");
        params.put("output", output);
        return callApi(method, params);
    }

    //    创建代理
    public MWZabbixAPIResult createProxy(String engineName, String serverIp, String status, String port, String dns) {
        String method = "proxy.create";
        HashMap params = new HashMap();
        HashMap interfaces = new HashMap();

        if (engineName != null && !"".equals(engineName)) {
            params.put("host", engineName);
        }
        if (status != null && !"".equals(status)) {
            params.put("status", status);
            if ("6".equals(status)) {
                if (serverIp != null && !"".equals(serverIp)) {
                    interfaces.put("dns", dns);
                    interfaces.put("ip", serverIp);
                    interfaces.put("useip", "1");
//                }else{
//                    if(dns!=null && !"".equals(dns)){
//                        interfaces.put("dns",dns);
//                        interfaces.put("ip",null);
//                        interfaces.put("useip","0");
//                    }
                }
                if (port != null && !"".equals(port)) {
                    interfaces.put("port", port);
                }
                params.put("interface", interfaces);
            }
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("proxyid");
        params.put("output", output);
        return callApi(method, params);
    }

    //    修改代理
    public MWZabbixAPIResult updateProxy(String proxyId, String engineName, String serverIp, String status, String port, String dns) {
        String method = "proxy.update";
        HashMap params = new HashMap();
        HashMap interfaces = new HashMap();
        if (proxyId != null && !"".equals(proxyId)) {
            params.put("proxyid", proxyId);
        }
        if (engineName != null && !"".equals(engineName)) {
            params.put("host", engineName);
        }
        if (status != null && !"".equals(status)) {
            params.put("status", status);
            if ("6".equals(status)) {
                if (serverIp != null && !"".equals(serverIp)) {
                    interfaces.put("dns", "");
                    interfaces.put("ip", serverIp);
                    interfaces.put("useip", "1");
//                }else{
//                    if(dns!=null && !"".equals(dns)){
//                        interfaces.put("dns",dns);
//                        interfaces.put("ip",null);
//                        interfaces.put("useip","0");
//                    }
                }
                if (port != null && !"".equals(port)) {
                    interfaces.put("port", port);
                }
            }
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("proxyid");
        output.add("hosts");
        params.put("output", output);
        return callApi(method, params);
    }

    //    删除代理
    public MWZabbixAPIResult proxyDelete(List<String> proxyIds) {
        String method = "proxy.delete";
        return callApi(method, proxyIds);
    }

    //根据主机和监控名称批量查询lastvalue    filter过滤 true  search搜索 false
    public MWZabbixAPIResult itemGetbyType(String name, String hostid, Boolean isFilter) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", name);
        if (isFilter) {
            params.put("filter", search);
        } else {
            params.put("search", search);
        }
        params.put("hostids", hostid);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        output.add("units");
        output.add("lastclock");
        output.add("lastvalue");
        output.add("hostid");
        output.add("name");
        output.add("key_");
        output.add("value_type");
        output.add("valuemapid");
        params.put("output", output);
        return callApi(method, params);
    }

    //根据主机和监控名称批量查询lastvalue filter过滤
    public MWZabbixAPIResult itemGetbyFilter(String name, List<String> hostids) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();
        filter.put("name", name);
        params.put("filter", filter);
        params.put("hostids", hostids);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("hostid");
        output.add("lastvalue");
        output.add("units");
        params.put("output", output);
        return callApi(method, params);
    }

    //根据主机和监控名称批量查询lastvalue search查询
    public MWZabbixAPIResult itemGetbySearch(List<String> nameList, String hostid) {
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
        output.add("hostid");
        output.add("name");
        output.add("key_");
        params.put("output", output);
        return callApi(method, params);
    }

    //获得web最新的一条历史记录
    public MWZabbixAPIResult HistoryGetByItemid(List<String> itemids, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("limit", 2);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);//0 下载速度和响应时间 3状态和响应码
        return callApi(method, params);
    }

    //通过应用集获取数据 获取应用集下面的所有item
    public MWZabbixAPIResult getItemDataByAppName(String hostid, String applicationName, String type) {
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
        param.put("application", applicationName);
        param.put("output", new String[]{"itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult getApplication(String hostid) {
        HashMap param = new HashMap<>();
        HashMap filter = new HashMap<>();
        String method = "application.get";
        // filter.put("name","接口");
        param.put("search", filter);
        param.put("output", new String[]{"itemids", "name", "applicationid"});
        if (null != hostid && StringUtils.isNotEmpty(hostid)) {
            param.put("hostids", hostid);
        }
        param.put("selectItems", "item");
        // param.put("countOutput",true);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getApplicationName(String hostid, String applicationName) {
        HashMap param = new HashMap<>();
        HashMap filter = new HashMap<>();
        String method = "application.get";
        filter.put("name", applicationName);
        param.put("search", filter);
        param.put("output", new String[]{"itemids", "name", "applicationid"});
        param.put("hostids", hostid);
        param.put("selectItems", "item");
        // param.put("countOutput",true);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemgetbyhostid(String hostid, String name, Boolean isName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (isName) {
            param.put("application", name);
        }
        param.put("output", new String[]{"itemid", "name", "application", "delay", "lastvalue", "lastclock", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemgetbykey(String hostid, String key) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        HashMap filter = new HashMap<>();
        filter.put("key_", key);
        param.put("search", filter);

        param.put("output", new String[]{"itemid", "name", "application", "delay", "lastvalue", "lastclock", "units", "value_type", "state", "valuemapid"});
     //   param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemgetbykey(List<String> hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid", "name", "key_","type"});
        HashMap filter = new HashMap<>();
        HashMap search = new HashMap<>();
       // search.put("key_", "web");
     //   search.put("name", "Download speed for scenario");
   //     search.put("name", "Download speed for step \"$2\" of scenario \"$1\".");
        param.put("search", search);
        param.put("hostids", hostid);
        param.put("webitems", 1);
//        filter.put("type", "9");
    //    param.put("filter", filter);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemGetbyHostId(String hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult problemget(List<String> hostids) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("output", new String[]{"clock", "ns", "name", "severity", "eventid", "r_clock", "r_ns", "userid", "objectid", "acknowledged"});//new String[]{objectids}
        if (null != hostids && hostids.size() > 0) {
            params.put("hostids", hostids);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult problemget(List<String> hostids, Boolean acknowledged) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("output", new String[]{"clock", "ns", "name", "severity", "eventid", "r_clock", "r_ns", "userid", "objectid", "acknowledged"});//new String[]{objectids}
        if (null != hostids && hostids.size() > 0) {
            params.put("hostids", hostids);
        }
        params.put("acknowledged", acknowledged);
        return callApi(method, params);
    }

    public MWZabbixAPIResult problemget(List<String> hostids,String timeTill) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("hostids",hostids);
        params.put("time_till",timeTill);
        params.put("countOutput", true);
        return callApi(method, params);
    }

    //修改接口启用状态
    public MWZabbixAPIResult itemUpdate(String itemid, Integer status) {
        String method = "item.update";
        HashMap params = new HashMap();
        params.put("itemid", itemid);
        params.put("status", status);
        return callApi(method, params);
    }

    public MWZabbixAPIResult trendGet(String itemid, Long timeFrom, Long timeTill) {
        String method = "trend.get";
        HashMap params = new HashMap();
        params.put("itemids", itemid);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        params.put("limit", 1);
        params.put("output", new String[]{"itemid", "clock", "num", "value_max", "value_avg", "value_min"});
        return callApi(method, params);
    }

    //host 主机名称+Ip地址 添加一个主机
    public MWZabbixAPIResult hostCreate(String host, ArrayList<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, ArrayList<String> templates,
                                        List<Map> macro, Integer status ) {
        String method = "host.create";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> groups = new ArrayList();
        groupIdList.forEach(groupId -> {
            HashMap<String, String> group = new HashMap();
            group.put("groupid", groupId);
            groups.add(group);
        });
        ArrayList<HashMap> temps = new ArrayList();
        templates.forEach(templateid -> {
            HashMap<String, String> templ = new HashMap();
            templ.put("templateid", templateid);
            temps.add(templ);
        });
        if (null != macro && macro.size() > 0) {
            params.put("macros", macro);
        }
        params.put("status", status);
        params.put("host", host);
        params.put("groups", groups);
        params.put("templates", temps);
        params.put("interfaces", hostInterfaces);
        return callApi(method, params);
    }

    //host 主机名称+Ip地址 添加一个主机
    public MWZabbixAPIResult  hostCreate(String host, ArrayList<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, ArrayList<String> templates,
                                        List<Map> macro, Integer status,String proxyID ) {
        String method = "host.create";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> groups = new ArrayList();
        groupIdList.forEach(groupId -> {
            HashMap<String, String> group = new HashMap();
            group.put("groupid", groupId);
            groups.add(group);
        });
        ArrayList<HashMap> temps = new ArrayList();
        templates.forEach(templateid -> {
            HashMap<String, String> templ = new HashMap();
            templ.put("templateid", templateid);
            temps.add(templ);
        });
        if (null != macro && macro.size() > 0) {
            params.put("macros", macro);
        }
        params.put("status", status);
        params.put("host", host);
        params.put("groups", groups);
        params.put("templates", temps);
        params.put("interfaces", hostInterfaces);
        params.put("proxy_hostid", proxyID);
        return callApi(method, params);
    }

    //删除多个主机
    public MWZabbixAPIResult hostDelete(List<String> ids) {
        String method = "host.delete";
        return callApi(method, ids);
    }

    public MWZabbixAPIResult hostUpdate(String hostid, String proxyId) {
        String method = "host.update";
        HashMap<String, Object> params = new HashMap();

        params.put("hostid", hostid);
        params.put("proxy_hostid", proxyId);
        return callApi(method, params);
    }

    //修改主机启用状态 0表示启用 1表示禁用 监控启用/禁用 可批量
    public MWZabbixAPIResult hostUpdate(List<String> hostids, Integer status) {
        String method = "host.massupdate";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> host = new ArrayList();
        hostids.forEach(hostid -> {
            HashMap<String, String> hostMap = new HashMap();
            hostMap.put("hostid", hostid);
            host.add(hostMap);
        });
        params.put("hosts", host);
        params.put("status", status);
        return callApi(method, params);
    }

    /**
     * 批量
     * 对主机添加链路  添加模板到对应的主机
     *
     * @param hostids
     * @param templateids
     * @return
     */
    public MWZabbixAPIResult hostUpdate(List<String> hostids, List<String> templateids, List<Map<String, Object>> hostInterfaces) {
        String method = "host.massupdate";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> host = new ArrayList();
        ArrayList<HashMap> temp = new ArrayList();
        hostids.forEach(hostid -> {
            HashMap<String, String> hostMap = new HashMap();
            hostMap.put("hostid", hostid);
            host.add(hostMap);
        });
        templateids.forEach(tem -> {
            HashMap<String, String> temMap = new HashMap();
            temMap.put("templateid", tem);
            temp.add(temMap);
        });
        params.put("hosts", host);
        params.put("templates", temp);
        params.put("interfaces", hostInterfaces);
        return callApi(method, params);
    }


    /**
     * 单个
     * 对主机添加链路  添加模板到对应的主机
     *
     * @param hostid
     * @param templateid
     * @return
     */
    public MWZabbixAPIResult hostMassUpdate(String hostid, String templateid) {
        String method = "host.massupdate";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> host = new ArrayList();
        ArrayList<HashMap> temp = new ArrayList();
        HashMap<String, String> hostMap = new HashMap();
        hostMap.put("hostid", hostid);
        host.add(hostMap);
        HashMap<String, String> temMap = new HashMap();
        temMap.put("templateid", templateid);
        temp.add(temMap);
        params.put("hosts", host);
        params.put("templates", temp);
        return callApi(method, params);
    }

    /**
     * 单个
     * 对主机添加链路  添加模板到对应的主机
     *
     * @param hostid
     * @param templateid
     * @return
     */
    public MWZabbixAPIResult hostMassRemove(String hostid, String templateid) {
        String method = "host.massreomver";
        HashMap<String, Object> params = new HashMap();
        params.put("hostids", hostid);
        params.put("templateids_clear", templateid);
        return callApi(method, params);
    }

    //删除主机 可批量
    public MWZabbixAPIResult hostListDeleteById(ArrayList<String> hostIdList) {
        String method = "host.delete";
        return callApi(method, hostIdList);
    }


    /*
     * @describe 根据传入的参数创建主机组
     * @author bkc
     * @date 2020/6/16
     * @param [name] 主机组名
     * @return cn.mw.zbx.MWZabbixAPIResult
     */
    public MWZabbixAPIResult hostgroupCreate(String name) {
        String method = "hostgroup.create";
        HashMap params = new HashMap();
        params.put("name", name);
        return callApi(method, params);
    }

    /*
     * @describe 修改主机组信息
     * @author bkc
     * @date 2020/6/16
     * @param [groupid, name] 指定主机组id  主机组名
     * @return cn.mw.zbx.MWZabbixAPIResult
     */
    public MWZabbixAPIResult hostgroupUpdate(String groupid, String name) {
        String method = "hostgroup.update";
        HashMap params = new HashMap();
        params.put("name", name);
        params.put("groupid", groupid);
        return callApi(method, params);
    }

    /*
     * @describe 删除主机组s
     * @author bkc
     * @date 2020/6/16
     * @param [groupids] 删除主机组id集合
     * @return cn.mw.zbx.MWZabbixAPIResult
     */
    public MWZabbixAPIResult hostgroupDelete(ArrayList<String> groupids) {
        String method = "hostgroup.delete";
        return callApi(method, groupids);
    }

    /*
     * @describe 根据查询主机组
     * @author bkc
     * @date 2020/6/16
     * @param [groupids] 主机组id集合
     * @return cn.mw.zbx.MWZabbixAPIResult
     */
    public MWZabbixAPIResult hostgroupGetById(List<String> groupids) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("groupids", groupids);
        return callApi(method, params);
    }


    //通过key_过滤对应的监控项
    public MWZabbixAPIResult ItemGetBykey(String hostid, String key) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();

        if (null != hostid && StringUtils.isNotEmpty(hostid)) {
            params.put("hostids", hostid);
        }
        if (null != key && StringUtils.isNotEmpty(key)) {
            filter.put("key_", key);
            params.put("filter", filter);
        }
        List<String> output = new ArrayList();
        output.add("lastvalue");
        params.put("output", output);

        return callApi(method, params);
    }

    public MWZabbixAPIResult getValueMap() {
        String method = "valuemap.get";
        HashMap params = new HashMap();
        List<String> output = new ArrayList();
        output.add("valuemapid");
        output.add("name");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getValueMapById(List<String> valuemapIds) {
        String method = "valuemap.get";
        HashMap params = new HashMap();
        List<String> mappings = new ArrayList();
        mappings.add("value");
        mappings.add("newvalue");
        params.put("selectMappings", mappings);
        params.put("valuemapids", valuemapIds);

        List<String> output = new ArrayList();
        output.add("valuemapid");
        output.add("name");
        params.put("output", output);

        return callApi(method, params);
    }

    //根据主机id查询所有item
    public MWZabbixAPIResult itemGet(List<String> hostIds) {
        String method = "item.get";
        HashMap params = new HashMap();
        params.put("hostids", hostIds);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemids");
        output.add("name");
        output.add("value_type");
        output.add("valuemapid");
        output.add("units");
        params.put("output", output);
        return callApi(method, params);
    }

    //根据主机id查询所有的自动发现规则
    public MWZabbixAPIResult getDRuleByHostId(String hostId) {
        String method = "discoveryrule.get";
        HashMap params = new HashMap();
        params.put("hostids", hostId);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("name");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostprototype(String discoveryId) {
        String method = "hostprototype.get";
        HashMap params = new HashMap();
        params.put("discoveryids", discoveryId);
        params.put("selectGroupLinks", "extend");
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostGroup() {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("output", "extend");
        params.put("selectDiscoveryRule", new String[]{"itemid"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHostsByGroupIds(List<String> groupId) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("groupids");
        output.add("name");
        params.put("output", output);
        params.put("groupids", groupId);
        params.put("selectHosts", new String[]{"hostid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHosts(String groupId) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("groupids", groupId);
        params.put("selectHosts", new String[]{"hostid", "name"});
        params.put("selectDiscoveryRule", new String[]{"itemid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHostByNames(List<String> groupNames) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        HashMap nameMap = new HashMap();
        HashMap filter = new HashMap();
        filter.put("name", groupNames);
        params.put("filter", filter);
//        nameMap.put("name", groupName);
//        params.put("search", nameMap);
        params.put("selectHosts", new String[]{"hostid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHostByName(String groupName) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        HashMap nameMap = new HashMap();
        nameMap.put("name", groupName);
        params.put("search", nameMap);
        params.put("selectHosts", new String[]{"hostid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostByHostId(String hostId, String hostName) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("status");
        output.add("name");
        output.add("hostid");
        if (null != hostId) {
            params.put("hostids", hostId);
        }
        if (null != hostName) {
            HashMap nameMap = new HashMap();
            nameMap.put("name", hostName);
            params.put("filter", nameMap);
        }
        params.put("output", output);
        params.put("selectInterfaces", new String[]{"ip"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult itemGetbyFilter(List<String> name, String hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();
        filter.put("name", name);
        params.put("search", filter);
        params.put("hostids", hostid);
        params.put("searchByAny", true);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("name");
        output.add("hostid");
        output.add("lastvalue");
        output.add("units");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getItemsbyHostId(String hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getItemDataByAppName(List<String> hostid, String applicationName, String type) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != type) {
            HashMap search = new HashMap<>();
            search.put("name", type);//分区类型
            param.put("search", search);
        }
        if (hostid.size() > 0) {
            param.put("hostids", hostid);
        }
        param.put("application", applicationName);
        param.put("output", new String[]{"itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemsGet(String hostid, String itemName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != itemName) {
            HashMap search = new HashMap<>();
            search.put("name", itemName);
            param.put("search", search);
        }
        param.put("output", new String[]{"itemid", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getItemName(List<String> itemids) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("itemids", itemids);
        param.put("output", new String[]{"itemid", "name", "units", "value_type"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostgetByTempalteid(Integer monitorServerId, String hostId, String templateid){
        HashMap param = new HashMap<>();
        String method = "host.get";
        param.put("hostids", hostId);
        param.put("templateids",templateid);
        param.put("templated_hosts",1);
        param.put("output", new String[]{"hostid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostMassUpdate(int serverId, String hostid, String templateid) {
        String method = "host.massadd";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> host = new ArrayList();
        ArrayList<HashMap> temp = new ArrayList();
        HashMap<String, String> hostMap = new HashMap();
        hostMap.put("hostid", hostid);
        host.add(hostMap);
        HashMap<String, String> temMap = new HashMap();
        temMap.put("templateid", templateid);
        temp.add(temMap);
        params.put("hosts", host);
        params.put("templates", temp);
        return callApi(method, params);
    }

    public MWZabbixAPIResult hostgetByTempalteid(Integer monitorServerId, String hostId){
        HashMap param = new HashMap<>();
        String method = "host.get";
        param.put("hostids", hostId);
        param.put("selectParentTemplates",new String[]{"templateid"});
        param.put("output", new String[]{"parentTemplates"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostGetTemplatesById(Integer monitorServerId, List<String> templateIds) {
        HashMap param = new HashMap<>();
        String method = "template.get";
        param.put("templateids", templateIds);
        param.put("selectParentTemplates",new String[]{"templateid"});
        param.put("output", new String[]{"parentTemplates"});
        return callApi(method, param);    }


}
