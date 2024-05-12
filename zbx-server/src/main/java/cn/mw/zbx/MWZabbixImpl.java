package cn.mw.zbx;

import cn.joinhealth.zbx.enums.action.EventSourceEnum;
import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.alert.dto.MWAlertLevelParam;
import cn.mw.monitor.service.model.param.MwModelWebMonitorTriggerParam;
import cn.mw.monitor.service.model.param.MwSyncZabbixAssetsParam;
import cn.mw.monitor.service.runtimeCache.CacheManager;
import cn.mw.monitor.service.zbx.model.HostCreateParam;
import cn.mw.monitor.service.zbx.model.HostProblemType;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.service.zbx.param.CloseDto;
import cn.mw.zbx.dto.MWAlertParamDto;
import cn.mw.zbx.dto.MWStep;
import cn.mw.zbx.dto.MWWebDto;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.IOUtils;
import cn.mwpaas.common.utils.Md5Utils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/*
 * 默认zabbix api基于zabbix5.0版本
 */
@Data
@Slf4j
public class MWZabbixImpl implements MWTPServerAPI {

    private boolean debug;

    private int serverId;
    private URI uri;
    private volatile String auth;
    private CloseableHttpClient httpClient;
    private static String HostCreate = "host.create";
    private static Set<String> cacheCmd = new HashSet<>();
    private static final String WEB_TEST_FAIL = "web.test.fail";
    private static final String WEB_TEST_TIME = "web.test.time";
    private static final String WEB_TEST_RSPCODE = "web.test.rspcode";
    private static final String WEB_TEST_ERROR = "web.test.error";

    /**
     * 服务器类别
     */
    private TPServerTypeEnum serverType;

    //    @Value("${zabbix.url}")
    private String zabbixUrl;

    //    @Value("${zabbix.user}")
    private String zabbixUser;

    //    @Value("${zabbix.password}")
    private String zabbixPassword;

    private PoolingHttpClientConnectionManager cm = null;
    private final int count = 32;
    private final int totalCount = 1000;
    private final int Http_Default_Keep_Time = 15000;

    @Autowired
    private Environment env;

    private CacheManager cacheManager;

    static {
        cacheCmd.add("item.get");
    }

    public MWZabbixImpl(String url) {
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new RuntimeException("url invalid", e);
        }
    }

    public MWZabbixImpl() {

    }

    public MWZabbixImpl(URI uri) {
        this.uri = uri;
    }

    public MWZabbixImpl(String url, CloseableHttpClient httpClient) {
        this(url);
        this.httpClient = httpClient;
    }

    public MWZabbixImpl(URI uri, CloseableHttpClient httpClient) {
        this(uri);
        this.httpClient = httpClient;
    }

    public String message() {
        return "MWZabbixImpl{" +
                "serverId='" + serverId + '\'' +
                ", zabbixUrl='" + zabbixUrl + '\'' +
                ", zabbixUser='" + zabbixUser + '\'' +
                '}';
    }

    @PostConstruct
    public boolean init() {
        cacheManager = SpringUtils.getBean(CacheManager.class);

        if ((null == zabbixUrl) || "".equals(zabbixUrl)) {
            log.error("zabbixUrl is null or empty");
        }
        try {
            log.info("init获取zabbixUrl：" + zabbixUrl);
            uri = new URI(zabbixUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException("url invalid", e);
        }

        if (httpClient == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setDefaultMaxPerRoute(count);
            cm.setMaxTotal(totalCount);

            ConnectionKeepAliveStrategy defaultStrategy = new ConnectionKeepAliveStrategy() {
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    int keepTime = Http_Default_Keep_Time;
                    while (it.hasNext()) {
                        HeaderElement he = it.nextElement();
                        String param = he.getName();
                        String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (Exception e) {
                                log.error("format KeepAlive timeout exception" + e);
                            }
                        }
                    }
                    return keepTime * 1000;
                }
            };

            httpClient = HttpClients.custom().setKeepAliveStrategy(defaultStrategy).setConnectionManager(cm).build();
            httpClient = HttpClients.custom().build();
        }
        return login(zabbixUser, zabbixPassword);
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

        InputStream inputStream = null;
        try {
            HttpUriRequest httpRequest = org.apache.http.client.methods.RequestBuilder.post().setUri(uri)
                    .addHeader("Content-Type", "application/json")
                    .setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON)).build();
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            log.info("call api数据返回记录inputStream:" + IOUtils.toString(inputStream));
            if (!debug) {
                return new ObjectMapper().readTree(inputStream);
            } else {
                String content = null;
                if (null != inputStream) {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[2048];
                        int length = 0;
                        while ((length = inputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, length);//写入输出流
                        }
                        inputStream.close();//读取完毕，关闭输入流
                        content = new String(bos.toByteArray(), "UTF-8");
                        log.info("call result:{}", content);
                    } catch (Exception e1) {
                        log.error("zabbix call", e1);
                    }
                }
                return new ObjectMapper().readTree(content);
            }


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
        MWZabbixAPIResult.setCreatTime(start.getTime());

        try {
            MWRequestBuilder requestBuilder = null;
            String param = JSON.toJSONString(params);
            log.info("callApi ,method:{},params:{}", method, param);
            requestBuilder = MWRequestBuilder.newBuilder().initRequest(params).method(method);
            if (!cacheCmd.contains(method)) {
                requestBuilder = MWRequestBuilder.newBuilder().initRequest(params).method(method);
                doCallApi(requestBuilder.build(), MWZabbixAPIResult);
                long interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("callApi cost {}s", interval);
                return MWZabbixAPIResult;
            }

            String keyData = serverId + method + param;
            String key = Md5Utils.encode(keyData);

            MWZabbixAPIResult cacheData = null;
            synchronized (cacheManager) {
                cacheData = cacheManager.getCacheData(MWZabbixAPIResult.class, key);
                if (null != cacheData && !cacheData.checkFakeData()) {
                    return cacheData;
                }
            }

            //针对key对应的数据加锁
            synchronized (cacheData) {
                MWZabbixAPIResult data = cacheManager.getCacheData(MWZabbixAPIResult.class, key);
                if (null != data && !data.checkFakeData()) {
                    log.info("use cache key {}", key);
                    return data;
                }

                log.info("find no cache key {}", key);
                doCallApi(requestBuilder.build(), MWZabbixAPIResult);
                long interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("callApi cost {}s", interval);
                cacheManager.register(key, MWZabbixAPIResult);
            }
        } catch (Exception e) {
            log.error("callApi {}", e);
        }
        return MWZabbixAPIResult;
    }

    private void doCallApi(MWRequestAbstract request, MWZabbixAPIResult mwZabbixAPIResult) {
        JsonNode response = call(request);
        if (response.has("error")) {
            mwZabbixAPIResult.setCode(response.get("error").get("code").asInt());
            mwZabbixAPIResult.setMessage(response.get("error").get("message").asText());
            mwZabbixAPIResult.setData(response.get("error").get("data").asText());
        } else {
            mwZabbixAPIResult.setMessage("Call Zabbix API Success.");
            mwZabbixAPIResult.setCode(MWZabbixAPIResultCode.SUCCESS.code());
            mwZabbixAPIResult.setData(response.get("result"));
        }
        mwZabbixAPIResult.setInterval(cacheManager.getDefaultInterval());
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
    public MWZabbixAPIResult alertGetByCurrent(int serverId, AlertParam dto) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        filter.put("value", 1);
        filter.put("status", 0);
        ArrayList<String> output = new ArrayList<>();
        output.add("triggerid");
        output.add("description");
        output.add("lastchange");
        output.add("priority");
        param.put("selectLastEvent", "extend");
        param.put("sortfield", "priority");
        param.put("limit", 1000);
        param.put("sortorder", "DESC");
        param.put("skipDependent", 1);
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

        return triggerGet(serverId, param, filter);
    }

    public MWZabbixAPIResult triggerGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "trigger.get";
        param.put("filter", filter);
        return callApi(method, param);
    }

    /**
     * 得到当前告警的事件2
     *
     * @return
     */
    public MWZabbixAPIResult eventGettByTriggers2(int serverId, List<String> eventids) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        //排序
        ArrayList<String> sortfield = new ArrayList<>();
        sortfield.add("clock");
        filter.put("value", 1);
        param.put("output", "extend");
        param.put("select_acknowledges", "extend");
        param.put("selectTags", "extend");
        param.put("selectHosts", "extend");
        //param.put("select_alerts", "extend");
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        param.put("eventids", eventids);
        if (CollectionUtils.isNotEmpty(MWAlertLevelParam.severities)) {
            param.put("severities", MWAlertLevelParam.severities);
        }
        //filter.put("clock", clock);
        return eventGet(serverId, param, filter);
    }


    public MWZabbixAPIResult eventGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter) {
        String method = "event.get";
        param.put("filter", filter);
        log.info("event get strat");
        return callApi(method, param);
    }

    public MWZabbixAPIResult eventGetByHistory(int serverId, AlertParam dto) {
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
        long endTime = 0;
        long beginTime = 0;
        endTime = new Date().getTime() / 1000;
        int m = 7;
        if (StringUtils.isNotEmpty(dto.getDays())) {
            m = Integer.parseInt(dto.getDays());
        }
        beginTime = curTimeMinusN(m) / 1000;
        if (null != dto.getStartTime()) {
            beginTime = Long.parseLong(dto.getStartTime()) / 1000;
        }
        if (null != dto.getEndTime()) {
            endTime = Long.parseLong(dto.getEndTime()) / 1000;
        }
        param.put("output", "extend");
        param.put("selectHosts", "extend");
        //param.put("select_alerts", "extend");
        param.put("select_acknowledges", "extend");
        param.put("selectTags", "extend");
        param.put("time_from", beginTime);
        param.put("time_till", endTime);
        param.put("limit", dto.getHisAlertCount());
        param.put("sortfield", sortfield);
        param.put("sortorder", "DESC");
        return eventGet(serverId, param, filter);
    }

    public MWZabbixAPIResult getHistAlarmByEventGet(int serverId, MWAlertParamDto dto) {
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
        return eventGet(serverId, param, filter);
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
    public MWZabbixAPIResult eventGetByEventid(int serverId, String eventid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        param.put("eventids", eventid);
        param.put("selectTags", "extend");
        param.put("selectHosts", "extend");
        param.put("select_alerts", "extend");
        param.put("output", "extend");
        return eventGet(serverId, param, filter);
    }

    public MWZabbixAPIResult eventGetByEventids(int serverId, List<String> eventids) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        param.put("eventids", eventids);
        param.put("selectTags", "extend");
        param.put("selectHosts", "extend");
        param.put("output", "extend");
        return eventGet(serverId, param, filter);
    }

    /**
     * 根据事件ID得到触发器
     *
     * @param triggerid
     * @return
     */
    public MWZabbixAPIResult triggerGetByTriggerid(int serverId, String triggerid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("triggerids", triggerid);
        param.put("output", "extend");
        return triggerGet(serverId, param, filter);
    }

    /**
     * 根据事件ID得到触发器
     *
     * @param
     * @return
     */
    public MWZabbixAPIResult itemGetByTriggerid(int serverId, String triggerid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("triggerids", triggerid);
        param.put("output", "extend");
        return itemGet(serverId, param, filter);
    }

    public MWZabbixAPIResult itemGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter) {
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
    public MWZabbixAPIResult alertGetByEventid(int serverId, String eventid) {
        HashMap<String, Object> param = new HashMap();
        HashMap<String, Object> filter = new HashMap();

        param.put("eventids", eventid);
        param.put("output", "extend");
        return alertGet(serverId, param, filter);
    }

    public MWZabbixAPIResult alertGet(int serverId, HashMap<String, Object> param, HashMap<String, Object> filter) {
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
    public MWZabbixAPIResult eventgetByObjectid(int serverId, String objectid, HashMap<String, Object> filter, long time_from, long time_till) {
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
    public MWZabbixAPIResult eventgets(int serverId, HashMap<String, Object> filter, String acknowledged, String severity, Long start, Long end, String subject) {
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
    public MWZabbixAPIResult eventacknowledge(int serverId, String eventids, String msg, String act) {
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
    public MWZabbixAPIResult eventacknowledge(int serverId, String eventids, String act) {
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
     * 批量确认事件
     *
     * @param eventids
     * @return
     */
    public MWZabbixAPIResult eventacknowledge(int serverId, List<String> eventids, String act) {
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
    public MWZabbixAPIResult alertGet(int serverId, List<String> hostids) {
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
    public MWZabbixAPIResult alertEventGet(int serverId, List<String> hostids) {
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


    public MWZabbixAPIResult getEventSeverity(int serverId, Object eventid) {
        String method = "event.get";
        HashMap params = new HashMap();
        List output = new ArrayList();
        output.add("severity");
        params.put("eventids", eventid);
        params.put("selectHosts", new String[]{"hosts"});
        params.put("output", output);
        return callApi(method, params);
    }


    //获得当前事件的历史告警
    public MWZabbixAPIResult getEventHistAlert(int serverId, String objectid) {
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


    public MWZabbixAPIResult itemgetbyhostidList(int serverId, List<String> hostidList) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid", "type", "key_", "name", "hostid"});
        param.put("hostids", hostidList);
        return callApi(method, param);
    }

    //创建触发器
    public MWZabbixAPIResult triggerCreate(int serverId, String description, String expression, String priority) {
        String method = "trigger.create";
        HashMap params = new HashMap();
        params.put("description", description);//名称
        params.put("expression", expression);//表达式
        params.put("priority", priority);//触发的严重性
        params.put("manual_close", "1");
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
    public MWZabbixAPIResult triggerCreateByKey(int serverId, String description, String hostName, String webMonitorName, String key, String code, String priority) {
        String webFailExpression = getWebExpression(hostName, webMonitorName, key, code);
        String webFailRexExpression = getWebRexExpression(hostName, webMonitorName, key, code);
        return triggerCreate(serverId, description, webFailExpression, webFailRexExpression, priority);
    }

    //批量创建带有恢复条件的触发器
    public MWZabbixAPIResult triggerBatchCreate(int serverId, List<MwModelWebMonitorTriggerParam> triggerParams) {
        String method = "trigger.create";
        List<HashMap> paramList = new ArrayList<>();
        for (MwModelWebMonitorTriggerParam param : triggerParams) {
            String webFailExpression = getWebExpression(param.getHostName(), param.getWebName(), param.getKey(), param.getCode());
            String webFailRexExpression = getWebRexExpression(param.getHostName(), param.getWebName(), param.getKey(), param.getCode());
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


    //更新带有恢复条件的触发器
    public MWZabbixAPIResult triggerUpdate(int serverId, String triggerid, String description, String expression, String rexpression, String priority) {
        String method = "trigger.uodate";
        HashMap params = new HashMap();
        params.put("triggerid", triggerid);
        params.put("description", description);
        params.put("expression", expression);
        params.put("recovery_mode", 1);//恢复模式为恢复表达式
        params.put("recovery_expression", rexpression);
        params.put("priority", priority);
        params.put("manual_close", "1");
        return callApi(method, params);
    }


    public MWZabbixAPIResult triggerClose(int serverId, CloseDto dto) {
        String method = "trigger.update";
        HashMap params = new HashMap();
        params.put("triggerid", dto.getObjectId());
        if (dto.getClose()) {
            params.put("status", "1");
        } else {
            params.put("status", "0");
        }
        return callApi(method, params);
    }


    /**
     * 创建动作
     *
     * @return
     */
    public MWZabbixAPIResult actioncreate(int serverId, String name, String shortdata, String longdata, Map filter, List operations, Integer status) {
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

    public MWZabbixAPIResult actioncreate(int serverId, String name, String def_longData, String def_shortData, String r_longData, String r_shortData, Integer status, Map<String, Object> filter, List operations, List recovery_operation) {
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

    public MWZabbixAPIResult actioncreate(int serverId, String name, String def_longData, String def_shortData, Map<String, Object> filter, List operations) {
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
    public MWZabbixAPIResult actionupdate(int serverId, String actionId, String name, String def_longData, String def_shortData, String r_longData, String r_shortData, Map<String, Object> filter, List operations, List recovery_operation) {
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

    public MWZabbixAPIResult actionDelete(int serverId, String actionIds) {
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
    public MWZabbixAPIResult actionDelete(int serverId, List<String> actionIds) {
        String method = "action.delete";
        return callApi(method, actionIds);
    }

    public MWZabbixAPIResult triggerDelete(int serverId, List<String> triggerIds) {
        String method = "trigger.delete";
        return callApi(method, triggerIds);
    }

    /**
     * 根据主机ID查询主机信息
     *
     * @param hostid
     * @return
     */
    public MWZabbixAPIResult hostGetById(int serverId, String hostid) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> list = new ArrayList<>();
        list.add(hostid);
        params.put("hostids", hostid);
        return callApi(method, params);
    }

    /**
     * 根据问题查询主机信息
     *
     * @param severities 问题级别
     * @return
     */
    @Override
    public MWZabbixAPIResult hostGetBySeverity(int serverId, List<HostProblemType> severities) {
        String method = "host.get";
        List<Integer> problems = severities.stream().map(HostProblemType::getCode).collect(Collectors.toList());

        HashMap params = new HashMap();
        params.put("severities", problems);
        params.put("with_monitored_triggers", true);
        params.put("selectTriggers", "extend");
        params.put("output", new String[]{"hostid", "host", "name", "triggers"});
        return callApi(method, params);
    }

    /**
     * 获取数据源为触发器的动作
     *
     * @return
     */
    public MWZabbixAPIResult actionGetByEventSourceTrigger(int serverId, String name, String status) {
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
    public MWZabbixAPIResult templateGet(int serverId, String name, Boolean isFilter) {
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

    public MWZabbixAPIResult templateGet(int serverId, String name) {

        return templateGet(serverId, name, false);
    }

    /**
     * 获取主机组
     *
     * @return
     */
    public MWZabbixAPIResult hostGroupGet(int serverId, String name, Boolean isFilter) {
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
        params.put("output", "extend");
        return callApi(method, params);
    }

    /**
     * 根据eventid 查询trigger 的itemid 通过itemid 查询item的name和itemid 和lastclock
     * 再通过itemid 去history取前后一个小时的历史记录
     *
     * @param triggerids
     * @return
     */
    public MWZabbixAPIResult triggerGetItemid(int serverId, String triggerids) {
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        param.put("selectFunctions", new String[]{"itemid"});
        param.put("triggerids", triggerids);
        return callApi(method, param);
    }


    public MWZabbixAPIResult triggerGetHostId(int serverId, String hostId) {
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        param.put("hostids", hostId);
        param.put("selectFunctions", "extend");
        param.put("output", "extend");
        param.put("expandData", 1);
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostListGetByHostName(int serverId, ArrayList<String> hostNameList) {
        String method = "host.get";
        HashMap params = new HashMap();
        HashMap<String, Object> filter = new HashMap();
        filter.put("host", hostNameList);
        params.put("filter", filter);
        ArrayList<String> output = new ArrayList<>();
        output.add("hostid");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult alertGetByCurrent(int serverId, List<String> hostIds) {
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
        return triggerGet(serverId, param, filter);
    }

    public MWZabbixAPIResult getHistEvent(int serverId, Integer count, List<String> hostids) {
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
        return eventGet(serverId, param, filter);
    }

    public MWZabbixAPIResult itemGetbyType(int serverId, String name, List<String> hostIdList) {
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

    public MWZabbixAPIResult itemGetbyTypeNames(int serverId, List<String> names, List<String> hostIdList) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", names);//模糊查询名称
        params.put("search", search);
        params.put("searchByAny", true);
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

    //批量创建web监测
    public MWZabbixAPIResult HttpTestBatchCreate(int serverId, List<MWWebDto> webDtoList) {
        String method = "httptest.create";
        List<HashMap> paramList = new ArrayList<>();
        for (MWWebDto webDto : webDtoList) {
            HashMap params = new HashMap();
            params.put("name", webDto.getName());
            params.put("hostid", webDto.getHostId());
            params.put("agent", webDto.getAgent());
            params.put("delay", webDto.getDelay());
            params.put("status", webDto.getStatus());
            params.put("retries", webDto.getRetries());
            if (StringUtils.isNotEmpty(webDto.getHttpProxy())) {
                params.put("http_proxy", webDto.getHttpProxy());
            }
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
            paramList.add(params);
        }
        return callApi(method, paramList);
    }


    //创建web监测
    public MWZabbixAPIResult HttpTestCreate(int serverId, MWWebDto webDto) {
        String method = "httptest.create";
        HashMap params = new HashMap();
        params.put("name", webDto.getName());
        params.put("hostid", webDto.getHostId());
        params.put("agent", webDto.getAgent());
        params.put("delay", webDto.getDelay());
        params.put("status", webDto.getStatus());
        params.put("retries", webDto.getRetries());
        if (StringUtils.isNotEmpty(webDto.getHttpProxy())) {
            params.put("http_proxy", webDto.getHttpProxy());
        }
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

    //批量修改web监测信息
    public MWZabbixAPIResult HttpTestBatchUpdate(int serverId, List<MWWebDto> webDtoList) {
        String method = "httptest.update";
        List<HashMap> paramsList = new ArrayList<>();
        for (MWWebDto webDto : webDtoList) {
            HashMap params = new HashMap();
            params.put("httptestid", webDto.getHttptestids());
            params.put("name", webDto.getName());
            params.put("agent", webDto.getAgent());
            params.put("delay", webDto.getDelay());
            params.put("status", webDto.getStatus());
            params.put("retries", webDto.getRetries());
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
            paramsList.add(params);
        }
        return callApi(method, paramsList);
    }

    //修改启用状态
    public MWZabbixAPIResult HttpTestUpdate(int serverId, MWWebDto webDto) {
        String method = "httptest.update";
        HashMap params = new HashMap();
        params.put("httptestid", webDto.getHttptestids());
        params.put("name", webDto.getName());
//        params.put("hostid", webDto.getHostId());
        params.put("agent", webDto.getAgent());
        params.put("delay", webDto.getDelay());
        params.put("status", webDto.getStatus());
        params.put("retries", webDto.getRetries());
//        params.put("http_proxy", webDto.getHttpProxy());
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
    public MWZabbixAPIResult HttpTestUpdate(int serverId, String httptestid, Integer status) {
        String method = "httptest.update";
        HashMap params = new HashMap();
        params.put("status", status);
        params.put("httptestid", httptestid);
        return callApi(method, params);
    }

    //删除web场景,根据场景id
    public MWZabbixAPIResult HttpTestDelete(int serverId, List<String> webids) {
        String method = "httptest.delete";
        return callApi(method, webids);
    }

    //根据httptestid获得创建的记录
    public MWZabbixAPIResult HttpTestGet(int serverId, String httptestid) {
        String method = "httptest.get";
        HashMap params = new HashMap();
        params.put("httptestid", httptestid);
        return callApi(method, params);
    }

    //根据hostidd获得创建web的记录
    public MWZabbixAPIResult HttpTestGet(int serverId, List<String> hostids) {
        String method = "httptest.get";
        HashMap params = new HashMap();
        params.put("hostids", hostids);
        params.put("output", new String[]{"name", "hostid"});
        params.put("selectSteps", new String[]{"name"});
        return callApi(method, params);
    }


    //获得web最新的一条历史记录
    public MWZabbixAPIResult HistoryGetByItemid(int serverId, String itemids, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);//0 下载速度和响应时间 3状态和响应码
        return callApi(method, params);
    }

    //获得最近60条历史数据
    public MWZabbixAPIResult HistoryGetByItemid(int serverId, String itemid, Integer type, int limit) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemid);
        initHistoryGetByItemidParams(params, type, limit);
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetByItemids(int serverId, List<String> itemids, Integer type, int limit) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        initHistoryGetByItemidParams(params, type, limit);
        return callApi(method, params);
    }

    private void initHistoryGetByItemidParams(HashMap params, Integer type, int limit) {
        params.put("limit", limit);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);
    }

    //根据时间,history查询所有历史   下载记录和响应时间的数据
    public MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, String itemids, Long timeFrom, Long timeTill, Integer type) {
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

    public MWZabbixAPIResult HistoryGetInfoByTimeAll(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
//        params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetInfoForLimitOne(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }


    //无线设备数据展示接口
    public MWZabbixAPIResult GetHistoryByTimeAndType(int serverId, String itemid, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemid);
        params.put("sortfield", "clock");
        params.put("sortorder", "ASC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        return HistoryGetByTimeAndType(serverId, null, itemids, timeFrom, timeTill, type);
    }

    public MWZabbixAPIResult HistoryGetByTimeAndType(int serverId, List<String> hostids, List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        if (null != hostids) {
            params.put("hostids", hostids);
        }

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

    public MWZabbixAPIResult HistoryGetByTimeAndTypeASC(int serverId, List<String> itemids, Long timeFrom, Long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        params.put("sortfield", "clock");
        params.put("sortorder", "ASC");
        params.put("history", type);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult HistoryGetByTime(int serverId, String itemids, Long timeFrom, Long timeTill) {
        return HistoryGetByTimeAndType(serverId, itemids, timeFrom, timeTill, 0);
    }

    //    ---------------------------------------------------------proxy---------------------------------------------------------------------------------
//    代理查询
    public MWZabbixAPIResult proxyGetByServerIp(int serverId, String serverIp, String port, String dns) {
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

    public MWZabbixAPIResult proxyGet(int serverId, Object proxyIds) {
        String method = "proxy.get";
        HashMap params = new HashMap();
        if (proxyIds != null) {
            params.put("proxyid", proxyIds);
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("proxyid");
        params.put("selectHosts", new String[]{"hostid", "name"});
        params.put("output", output);
        return callApi(method, params);
    }

    /**
     * 获取轮询引擎
     *
     * @return
     */
    public MWZabbixAPIResult proxyInfoget(int serverId) {
        HashMap params = new HashMap();
        String method = "proxy.get";
        params.put("output", new String[]{"host", "proxyid", "proxy_address"});
        params.put("selectInterface", "extend");
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult hostInterfaceGet(int serverId, Object hostIds) {
        String method = "hostinterface.get";
        HashMap<String, Object> params = new HashMap();
        params.put("hostids", hostIds);
        params.put("output", new String[]{"interfaceid"});
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult hostInterfaceUpdate(int serverId, Object interfaceIds, Map<String, Object> interfaceInfo) {
        String method = "hostinterface.update";
        HashMap<String, Object> params = new HashMap();
        params.put("interfaceid", interfaceIds);
        params.putAll(interfaceInfo);
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult taskItems(int serverId, String type, List<String> itemIds) {
        String method = "task.create";
        List<Map<String, Object>> params = new ArrayList<>();
        if (itemIds != null && itemIds.size() > 0) {
            for (String itemId : itemIds) {
                HashMap<String, Object> param = new HashMap();
                HashMap<String, Object> request = new HashMap();
                request.put("itemid", itemId);
                param.put("type", type);
                param.put("request", request);
                params.add(param);
            }
        }
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult HistoryGetByTimeAndHistoryListByitem(int serverId, List<String> itemids, long timeFrom, long timeTill, Integer type) {
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        //  params.put("limit", 1);
        params.put("sortfield", "clock");
        params.put("sortorder", "ASC");
        params.put("history", type);
        params.put("time_from", timeFrom);
        params.put("time_till", timeTill);
        return callApi(method, params);
    }

    //    创建代理
    public MWZabbixAPIResult createProxy(int serverId, String engineName, String serverIp, String status, String port, String dns, String proxyAddress) {
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
            } else if ("5".equals(status)) {
                if (proxyAddress != null && !"".equals(proxyAddress)) {
                    params.put("proxy_address", proxyAddress);
                }
            }
        }
        //注释输出内容，因高版本zabbix不支持创建输出，注释代码不影响老版本
//        ArrayList<String> output = new ArrayList<>();
//        output.add("proxyid");
//        params.put("output", output);
        return callApi(method, params);
    }

    //    修改代理
    public MWZabbixAPIResult updateProxy(int serverId, String proxyId, String engineName, String serverIp, String status, String port, String dns, String proxyAddress) {
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
            } else if ("5".equals(status)) {
                if (proxyAddress != null && !"".equals(proxyAddress)) {
                    params.put("proxy_address", proxyAddress);
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
    public MWZabbixAPIResult proxyDelete(int serverId, List<String> proxyIds) {
        String method = "proxy.delete";
        return callApi(method, proxyIds);
    }

    //根据主机和监控名称批量查询lastvalue    filter过滤 true  search搜索 false
    public MWZabbixAPIResult itemGetbyType(int serverId, Object name, Object hostid, Boolean isFilter) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", name);
        if (isFilter) {
            params.put("filter", search);
        } else {
            params.put("searchByAny", true);
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
        output.add("delay");
        params.put("output", output);
        return callApi(method, params);
    }

    //根据主机和监控名称批量查询lastvalue filter过滤
    public MWZabbixAPIResult itemGetbyFilter(int serverId, String name, List<String> hostids) {
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

    //redis 数据优化 根据nameList获取最新数据信息
    public MWZabbixAPIResult itemGetbyNameList(int serverId, List<String> nameList, Object hostid, Boolean isFilter) {
        return itemGetbyNameList(serverId, nameList, hostid, isFilter, false);
    }

    public MWZabbixAPIResult itemGetbyNameList(int serverId, List<String> nameList, Object hostid, Boolean isFilter
            , boolean searchByAny) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        search.put("name", nameList);
        if (isFilter) {
            params.put("filter", search);
        } else {
            params.put("search", search);
        }

        if (searchByAny) {
            params.put("searchByAny", searchByAny);
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
        output.add("delay");
        params.put("output", output);
        return callApi(method, params);
    }

    //redis 数据优化 根据itemIds获取最新数据信息
    public MWZabbixAPIResult itemGetbyItemidList(int serverId, List<String> itemIdList, Object hostid, Boolean isFilter) {
        String method = "item.get";
        HashMap params = new HashMap();
        params.put("hostids", hostid);
        params.put("itemids", itemIdList);
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
        output.add("master_itemid");
        output.add("delay");
        params.put("output", output);
        return callApi(method, params);
    }


    //根据主机和监控名称批量查询lastvalue search查询
    //有改动-zabbix
    public MWZabbixAPIResult itemGetbySearch(int serverId, Object nameList, Object hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        if (nameList != null) {
            search.put("name", nameList);
        }
        params.put("search", search);
        params.put("searchByAny", true);
        if (hostid != null) {
            params.put("hostids", hostid);
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        output.add("units");
        output.add("lastvalue");
        output.add("value_type");
        output.add("valuemapid");
        output.add("delay");
        output.add("hostid");
        output.add("name");
        output.add("key_");
        output.add("lastclock");
        output.add("master_itemid");
        params.put("selectItemDiscovery", new String[]{"parent_itemid", "itemid"});
        params.put("output", output);
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult itemGetbyHostIdsSearch(int serverId, Object nameList, List<String> hostids) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        if (nameList != null) {
            search.put("name", nameList);
        }
        params.put("search", search);
        params.put("searchByAny", true);
        if (hostids != null) {
            params.put("hostids", hostids);
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        output.add("units");
        output.add("lastvalue");
        output.add("value_type");
        output.add("valuemapid");
        output.add("delay");
        output.add("hostid");
        output.add("name");
        output.add("lastclock");
        params.put("output", output);
        return callApi(method, params);
    }

    //根据主机和监控名称批量查询lastvalue search查询
    public MWZabbixAPIResult itemGetbySearchItemId(int serverId, Object itemList, Object hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap search = new HashMap();
        if (itemList != null) {
            search.put("itemids", itemList);
        }
        if (hostid != null) {
            params.put("hostids", hostid);
        }
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        output.add("units");
        output.add("lastvalue");
        output.add("value_type");
        output.add("valuemapid");
        output.add("delay");
        output.add("hostid");
        output.add("name");
        output.add("key_");
        output.add("lastclock");
        output.add("master_itemid");
        params.put("selectItemDiscovery", new String[]{"parent_itemid", "itemid"});
        params.put("output", output);
        return callApi(method, params);
    }


    //根据主机和监控名称批量查询lastvalue search查询
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
        params.put("application", applicationName);
        params.put("selectApplications", new String[]{"applicationid", "name"});
        params.put("output", output);
        return callApi(method, params);
    }


    //获得web最新的一条历史记录
    public MWZabbixAPIResult HistoryGetByItemid(int serverId, List<String> itemids, Integer type) {
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
        param.put("application", applicationName);
        param.put("output", new String[]{"itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    //通过应用集获取数据 获取应用集下面的所有item
    public MWZabbixAPIResult getItemDataByAppNameList(int serverId, List<String> hostIdList, String applicationName, List<String> type) {
        return getItemDataByAppNameList(serverId, hostIdList, applicationName, type, false);
    }

    public MWZabbixAPIResult getItemDataByAppNameList(int serverId, List<String> hostIdList, String applicationName
            , List<String> type, boolean searchByAny) {
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
        param.put("application", applicationName);
        param.put("output", new String[]{"hostid", "itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    //获取全部资产下面的所有item信息
    public MWZabbixAPIResult getItemDataByAllAssets(int serverId, List<String> hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != hostid && hostid.size() > 0) {
            param.put("hostids", hostid);
        }
        param.put("output", new String[]{"hostid", "itemids", "name", "lastvalue", "value_type"});
        return callApi(method, param);
    }

    //通过应用集获取数据 获取应用集下面的所有item
    public MWZabbixAPIResult getItemNameByAppName(int serverId, String hostid, String applicationName, String type) {
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
        param.put("output", new String[]{"name"});
        return callApi(method, param);
    }


    public MWZabbixAPIResult getApplication(int serverId, String hostid) {
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

    public MWZabbixAPIResult getApplicationName(int serverId, String hostid, String applicationName) {
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

    public MWZabbixAPIResult itemgetbyhostid(int serverId, String hostid, String name, Boolean isName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (isName) {
            param.put("application", name);
        }
        param.put("output", new String[]{"itemid", "name", "application", "delay", "lastvalue", "lastclock", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult itemgetbyhostid(int serverId, List<String> hostids, String name, Boolean isName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (isName) {
            param.put("application", name);
        }
        param.put("output", new String[]{"hostid", "itemid", "name", "application", "delay", "lastvalue", "lastclock", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostids);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemGetbyHostId(int serverId, Object hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemGetbyHostIdList(int serverId, List<String> hostids) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid"});
        param.put("hostids", hostids);
        return callApi(method, param);
    }

    public MWZabbixAPIResult problemget(int serverId, List<String> hostids) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("output", new String[]{"clock", "ns", "name", "severity", "eventid", "r_clock", "r_ns", "userid", "objectid", "acknowledged"});//new String[]{objectids}
        if (null != hostids && hostids.size() > 0) {
            params.put("hostids", hostids);
        }
        return callApi(method, params);
    }

    public MWZabbixAPIResult problemget(int serverId, List<String> hostids, Boolean acknowledged) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("output", new String[]{"clock", "ns", "name", "severity", "eventid", "r_clock", "r_ns", "userid", "objectid", "acknowledged"});//new String[]{objectids}
        if (null != hostids && hostids.size() > 0) {
            params.put("hostids", hostids);
        }
        params.put("acknowledged", acknowledged);
        return callApi(method, params);
    }

    public MWZabbixAPIResult problemget(int serverId, List<String> hostids, String timeTill) {
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("hostids", hostids);
        params.put("time_till", timeTill);
        params.put("countOutput", true);
        return callApi(method, params);
    }

    //修改接口启用状态
    public MWZabbixAPIResult itemUpdate(int serverId, String itemid, Integer status) {
        String method = "item.update";
        HashMap params = new HashMap();
        params.put("itemid", itemid);
        params.put("status", status);
        return callApi(method, params);
    }

    public MWZabbixAPIResult trendGet(int serverId, String itemid, Long timeFrom, Long timeTill) {
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

    @Override
    public MWZabbixAPIResult hostBatchCreate(int serverId, List<HostCreateParam> params) {
        List<Map> zabbixParams = new ArrayList();
        for (HostCreateParam hostCreateParam : params) {
            Map zabbixParam = hostCreateParam.genZabbixParams();
            zabbixParams.add(zabbixParam);
        }
        return callApi(HostCreate, zabbixParams);
    }

    //host 主机名称+Ip地址 添加一个主机
    public MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, List<String> templates, List<Map> macro, Integer status) {
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
        params.put("name", visibleName);
        params.put("groups", groups);
        params.put("templates", temps);
        params.put("interfaces", hostInterfaces);
        return callApi(HostCreate, params);
    }

    //host 主机名称+Ip地址 添加一个主机
    public MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList
            , List<Map<String, Object>> hostInterfaces, List<String> templates,
                                        List<Map> macro, Integer status, String proxyID) {
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
        params.put("host", host);//主机名称
        params.put("name", visibleName);//可见名称
        params.put("groups", groups);
        params.put("templates", temps);
        params.put("interfaces", hostInterfaces);
        params.put("proxy_hostid", proxyID);
        return callApi(HostCreate, params);
    }

    public MWZabbixAPIResult hostUpdateGroup(int serverId, String hostid, ArrayList<String> groupIdList) {
        String method = "host.update";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> groups = new ArrayList();
        groupIdList.forEach(groupId -> {
            HashMap<String, String> group = new HashMap();
            group.put("groupid", groupId);
            groups.add(group);
        });
        params.put("hostid", hostid);
        params.put("groups", groups);
        return callApi(method, params);
    }

    //删除多个主机
    public MWZabbixAPIResult hostDelete(int serverId, List<String> ids) {
        String method = "host.delete";
        return callApi(method, ids);
    }

    public MWZabbixAPIResult hostProxyUpdate(int serverId, Object hostid, String proxyId) {
        String method = "host.update";
        HashMap<String, Object> params = new HashMap();

        params.put("hostid", hostid);
        params.put("proxy_hostid", proxyId);
        return callApi(method, params);
    }

    //修改主机启用状态 0表示启用 1表示禁用 监控启用/禁用 可批量
    public MWZabbixAPIResult hostUpdate(int serverId, List<String> hostids, Integer status) {
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

    public MWZabbixAPIResult hostBatchUpdate(int serverId, List<String> hostids, Map<String, Object> updateParams) {
        String method = "host.massupdate";
        HashMap<String, Object> params = new HashMap();
        ArrayList<HashMap> host = new ArrayList();
        hostids.forEach(hostid -> {
            HashMap<String, String> hostMap = new HashMap();
            hostMap.put("hostid", hostid);
            host.add(hostMap);
        });
        params.put("hosts", host);
        params.putAll(updateParams);
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
    public MWZabbixAPIResult hostUpdate(int serverId, List<String> hostids, List<String> templateids, List<Map<String, Object>> hostInterfaces) {
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

    /**
     * 单个
     * 对主机删除链路  删除模板到对应的主机
     *
     * @param hostid
     * @param templateid
     * @return
     */
    public MWZabbixAPIResult hostMassRemove(int serverId, String hostid, String templateid) {
        String method = "host.massremove";
        HashMap<String, Object> params = new HashMap();
        params.put("hostids", new String[]{hostid});
        params.put("templateids_clear", templateid);
        return callApi(method, params);
    }

    //删除主机 可批量
    public MWZabbixAPIResult hostListDeleteById(int serverId, ArrayList<String> hostIdList) {
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
    public MWZabbixAPIResult hostgroupCreate(int serverId, String name) {
        String method = "hostgroup.create";
        HashMap params = new HashMap();
        params.put("name", name);
        return callApi(method, params);
    }

    /**
     * 批量创建主机组
     *
     * @param serverId
     * @param params
     * @return
     */
    public MWZabbixAPIResult batchCreateHostGroup(int serverId, List<Map> params) {
        String method = "hostgroup.create";
        return callApi(method, params);
    }

    /*
     * @describe 修改主机组信息
     * @author bkc
     * @date 2020/6/16
     * @param [groupid, name] 指定主机组id  主机组名
     * @return cn.mw.zbx.MWZabbixAPIResult
     */
    public MWZabbixAPIResult hostgroupUpdate(int serverId, String groupid, String name) {
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
    public MWZabbixAPIResult hostgroupDelete(int serverId, ArrayList<String> groupids) {
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
    public MWZabbixAPIResult hostgroupGetById(int serverId, List<String> groupids) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("groupids", groupids);
        return callApi(method, params);
    }


    //通过key_过滤对应的监控项
    public MWZabbixAPIResult ItemGetBykey(int serverId, String hostid, String key) {
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

    public MWZabbixAPIResult getValueMap(int serverId) {
        String method = "valuemap.get";
        HashMap params = new HashMap();
        List<String> output = new ArrayList();
        output.add("valuemapid");
        output.add("name");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getValueMapById(int serverId, List<String> valuemapIds) {
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
    public MWZabbixAPIResult itemGet(int serverId, List<String> hostIds) {
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

    //根据触发器Id查询Item监控的项key_数据
    public MWZabbixAPIResult itemKeyGetByTriggerids(int serverId, List<String> triggerIds) {
        String method = "item.get";
        HashMap params = new HashMap();
        params.put("triggerids", triggerIds);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemids");
        output.add("name");
        output.add("key_");
        params.put("output", output);
        return callApi(method, params);
    }


    //根据主机id查询所有的自动发现规则
    public MWZabbixAPIResult getDRuleByHostId(int serverId, String hostId) {
        String method = "discoveryrule.get";
        HashMap params = new HashMap();
        params.put("hostids", hostId);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("name");
        output.add("delay");
        output.add("state");
        params.put("output", output);
        return callApi(method, params);
    }


    //根据主机id查询所有的自动发现规则
    public MWZabbixAPIResult getDRuleByHostIdList(int serverId, List<String> hostIds) {
        String method = "discoveryrule.get";
        HashMap params = new HashMap();
        params.put("hostids", hostIds);
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("hostid");
        output.add("name");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostprototype(int serverId, String discoveryId) {
        String method = "hostprototype.get";
        HashMap params = new HashMap();
        params.put("discoveryids", discoveryId);
        params.put("selectGroupLinks", "extend");
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostGroup(int serverId) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("output", "extend");
        params.put("selectDiscoveryRule", new String[]{"itemid"});
        params.put("selectHosts", new String[]{"name", "hostid"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHostsByGroupIds(int serverId, List<String> groupId) {
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

    public MWZabbixAPIResult getGroupHostsByHostIds(int serverId, List<String> hostId) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("groupids");
        output.add("name");
        params.put("output", output);
        params.put("hostids", hostId);
        params.put("selectHosts", new String[]{"hostid", "name"});
        return callApi(method, params);
    }


    public MWZabbixAPIResult getGroupHosts(int serverId, String groupId) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        params.put("groupids", groupId);
        params.put("selectHosts", new String[]{"hostid", "name"});
        params.put("selectDiscoveryRule", new String[]{"itemid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getGroupHostByNames(int serverId, List<String> groupNames) {
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

    public MWZabbixAPIResult getGroupHostByName(int serverId, String groupName) {
        String method = "hostgroup.get";
        HashMap params = new HashMap();
        HashMap nameMap = new HashMap();
        nameMap.put("name", groupName);
        params.put("search", nameMap);
        params.put("selectHosts", new String[]{"hostid", "name"});
        return callApi(method, params);
    }

    /**
     * 获取proxy_host_id、templateid,资产管理同步zabbix数据接口调用
     *
     * @param serverId
     * @param hostId
     * @param hostName
     * @return
     */
    public MWZabbixAPIResult getHostInfosById(int serverId, String hostId, String hostName) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("status");
        output.add("name");
        output.add("hostid");
        output.add("proxy_hostid");
        if (null != hostId) {
            params.put("hostids", hostId);
        }
        if (null != hostName) {
            HashMap nameMap = new HashMap();
            nameMap.put("name", hostName);
            params.put("filter", nameMap);
        }
        params.put("output", output);
        params.put("selectParentTemplates", new String[]{"templateid", "name"});
        params.put("selectInterfaces", new String[]{"ip"});
        return callApi(method, params);
    }


    public MWZabbixAPIResult getHostInfoByName(int serverId, String hostName) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("hostid");
        if (null != hostName) {
            HashMap nameMap = new HashMap();
            nameMap.put("name", hostName);
            params.put("search", nameMap);
        }
        params.put("output", output);
        params.put("selectParentTemplates", new String[]{"templateid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getTemplatesByHostId(int serverId, String hostId) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("hostid");
        params.put("hostids", hostId);
        params.put("output", output);
        params.put("selectParentTemplates", new String[]{"templateid", "name"});
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostByHostId(int serverId, String hostId, String hostName) {
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

    /**
     * 对hostName进行模糊查询
     *
     * @param serverId
     * @param hostId
     * @param hostName
     * @return
     */
    public MWZabbixAPIResult getHostByHostIdByFuzzy(int serverId, String hostId, String hostName) {
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
            params.put("search", nameMap);
        }
        params.put("output", output);
        params.put("selectInterfaces", new String[]{"ip"});
        return callApi(method, params);
    }

    /**
     * 对hostName进行模糊查询
     *
     * @param serverId
     * @param hostIds
     * @return
     */
    public MWZabbixAPIResult getHostDetailsByHostIds(int serverId, List<String> hostIds) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("name");
        output.add("hostid");
        params.put("hostids", hostIds);
        params.put("output", output);
        params.put("selectInterfaces", new String[]{"port", "details"});
        return callApi(method, params);
    }


    public MWZabbixAPIResult itemGetbyFilter(int serverId, List<String> name, Object hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();
        filter.put("name", name);
        params.put("filter", filter);
        if (hostid != null && StringUtils.isNotEmpty(hostid.toString())) {
            params.put("hostids", hostid);
        }
        params.put("searchByAny", true);
        params.put("selectHosts", new String[]{"hostid", "name", "status"});
        params.put("selectInterfaces", new String[]{"ip"});
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
        output.add("delay");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult itemGetbyVm(int serverId, List<String> name, Object hostid) {
        String method = "item.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();
        filter.put("name", name);
        params.put("filter", filter);
        params.put("hostids", hostid);
        params.put("selectHosts", new String[]{"hostid", "name", "status"});
        ArrayList<String> output = new ArrayList<>();
        output.add("itemid");
        output.add("status");
        params.put("output", output);
        return callApi(method, params);
    }

    /**
     * 根据hostIDS获取一组Items数据
     *
     * @param serverId
     * @param hostids
     * @return
     */
    public MWZabbixAPIResult getItemsbyHostIds(int serverId, Object hostids) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("searchByAny", true);
        param.put("selectHosts", new String[]{"hostid", "name", "status"});
        param.put("selectInterfaces", new String[]{"ip"});
        param.put("output", new String[]{"itemid", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostids);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getItemsbyHostId(int serverId, String hostid) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output", new String[]{"itemid", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult hostGetbyFilterByUUID(int serverId, List<String> hostList) {
        String method = "host.get";
        HashMap params = new HashMap();
        HashMap filter = new HashMap();
        filter.put("host", hostList);
        params.put("filter", filter);
        ArrayList<String> output = new ArrayList<>();
        output.add("name");
        output.add("host");
        output.add("hostid");
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getItemDataByAppName(int serverId, List<String> hostid, String applicationName, String type) {
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
        param.put("output", new String[]{"hostid", "itemids", "name", "lastvalue", "units", "value_type", "valuemapid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult itemsGet(int serverId, String hostid, String itemName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        if (null != itemName) {
            HashMap search = new HashMap<>();
            search.put("name", itemName);
            param.put("search", search);
        }
        param.put("output", new String[]{"itemid", "name", "lastvalue", "units", "value_type", "state", "valuemapid", "lastclock", "hostid"});
        param.put("hostids", hostid);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getItemName(int serverId, List<String> itemids) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("itemids", itemids);
        param.put("output", new String[]{"hostid", "itemid", "name", "application", "delay", "lastvalue", "lastclock", "units", "value_type", "state", "valuemapid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, List<String> value_types, String valuemapid, String units, String itemName) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("hostids", hostId);
        param.put("webitems", 1);
        HashMap filter = new HashMap<>();
        if (value_types != null && value_types.size() > 0) {
            filter.put("value_type", value_types);
        }
        if (valuemapid != null && !"".equals(valuemapid)) {
            filter.put("valuemapid", valuemapid);
        }
        if (units != null && !"".equals(units)) {
            filter.put("units", units);
        }
        if (!filter.isEmpty()) {
            param.put("filter", filter);
        }
        if (null != itemName && !"".equals(itemName)) {
            HashMap search = new HashMap<>();
            search.put("name", itemName);
            param.put("search", search);
        }
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "type", "key_"});
        return callApi(method, param);
    }


    public MWZabbixAPIResult getWebItemId(Integer monitorServerId, List<String> hostids, String key) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("hostids", hostids);
        HashMap filter = new HashMap<>();
        filter.put("key_", key);
        filter.put("type", "9");
        param.put("filter", filter);
        param.put("webitems", 1);
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "master_itemid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult getWebItemByhostId(Integer monitorServerId, List<String> hostids) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        HashMap filter = new HashMap<>();
        filter.put("type", "9");
        param.put("hostids", hostids);
        HashMap search = new HashMap<>();
        param.put("webitems", 1);
        param.put("output", new String[]{"itemid", "key_", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        return callApi(method, param);
    }

    public MWZabbixAPIResult getWebValue(Integer monitorServerId, List<String> hostIds, String key) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        HashMap filter = new HashMap<>();
        HashMap search = new HashMap<>();
        search.put("key_", "web");
        param.put("search", search);
        param.put("hostids", hostIds);
        param.put("webitems", 1);
        filter.put("type", "9");
        param.put("filter", filter);
        param.put("output", new String[]{"itemid", "hostid", "key_", "name", "lastvalue", "units", "value_type", "state", "valuemapid"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult hostCreate(int serverId, String host,String visibleName, List<String> groupIdList, List<Map<String, Object>> hostInterfaces, List<String> templates, List<Map> macro, Map<String, Object> otherParam) {
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
        params.put("host", host);
        params.put("name",visibleName);
        params.put("groups", groups);
        params.put("templates", temps);
        params.put("interfaces", hostInterfaces);
        params.putAll(otherParam);
        return callApi(HostCreate, params);
    }

    @Override
    public MWZabbixAPIResult hostgetByTempalteid(Integer monitorServerId, String hostId, String templateid) {
        HashMap param = new HashMap<>();
        String method = "host.get";
        param.put("hostids", hostId);
        param.put("templateids", templateid);
        param.put("output", new String[]{"hostid"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getMacrosByTemplateId(int serverId, String templateId) {

        String method = "usermacro.get";
        HashMap params = new HashMap();
        params.put("hostids", templateId);
        params.put("output", new String[]{"macro", "value"});
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult getMacrosByTemplateIdList(int serverId, List<String> templateIds) {

        String method = "usermacro.get";
        HashMap params = new HashMap();
        params.put("hostids", templateIds);
        params.put("output", new String[]{"macro", "value"});
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult getTemplateIdByHostId(int serverId, String hostId) {
        String method = "template.get";
        HashMap params = new HashMap();
        ArrayList<String> hostids = new ArrayList<>();
        hostids.add(hostId);
        params.put("hostids", hostids);
        ArrayList<String> output = new ArrayList<>();
        output.add("templateid");
        params.put("selectParentTemplates", new String[]{"templateid"});
        params.put("output", output);
        return callApi(method, params);
    }

    public MWZabbixAPIResult getHostInfosById(int serverId, List<String> hostIds) {
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> output = new ArrayList<>();
        output.add("status");
        output.add("name");
        output.add("hostid");
        output.add("proxy_hostid");
        params.put("hostids", hostIds);
        params.put("output", output);
        params.put("selectParentTemplates", new String[]{"templateid", "name"});
        params.put("selectInterfaces", new String[]{"ip"});
        return callApi(method, params);
    }


    @Override
    public MWZabbixAPIResult getItemPrototypeFilter(int serverId, String discoveryId, Map<String, Object> filter) {
        HashMap param = new HashMap<>();
        String method = "itemprototype.get";
        param.put("discoveryids", discoveryId);
        if (!filter.isEmpty()) {
            param.put("filter", filter);
        }
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "type", "key_"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, Map<String, Object> filter) {
        HashMap param = new HashMap<>();
        String method = "item.get";
//        param.put("hostids", hostId);
        param.put("templated", true);
        param.put("templateids", hostId);
        if (!filter.isEmpty()) {
            param.put("filter", filter);
        }
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "type", "key_"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemsByTemplateIdsFilter(int serverId, List<String> templateIds, Map<String, Object> filter) {
        HashMap param = new HashMap<>();
        String method = "item.get";
//        param.put("hostids", hostId);
        param.put("templated", true);
        param.put("templateids", templateIds);
        if (!filter.isEmpty()) {
            param.put("filter", filter);
        }
        param.put("selectDiscoveryRule", new String[]{"itemid", "name", "hostid"});
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "type", "key_"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getItemsByHostIdFilter(int serverId, String hostId, String templateid, Map<String, Object> filter) {
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("hostids", hostId);
        param.put("templated", false);
        param.put("templateids", templateid);
        if (!filter.isEmpty()) {
            param.put("filter", filter);
        }
        param.put("output", new String[]{"itemid", "name", "delay", "lastvalue", "units", "value_type", "state", "valuemapid", "type", "key_", "templateid"});
        return callApi(method, param);
    }

    @Override
    public MWZabbixAPIResult getHostsByIpPort(int serverId, String ip, String port) {
        String method = "host.get";
        HashMap params = new HashMap();
        if (ip != null && StringUtils.isNotEmpty(ip)) {
            HashMap search = new HashMap();
            search.put("ip", ip);
            params.put("search", search);
        }
        if (port != null && StringUtils.isNotEmpty(port)) {
            HashMap filter = new HashMap();
            filter.put("port", port);
            params.put("filter", filter);
        }
        params.put("output", new String[]{"hostid", "name"});
        params.put("selectGroups", new String[]{"groupid", "name"});
        params.put("selectDiscoveryRule", new String[]{"itemid", "name", "hostid"});
        params.put("selectHostDiscovery", new String[]{"parent_hostid"});
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult maintenanceCreate(int serverId, Map<String, Object> param, List<HashMap> times) {
        String method = "maintenance.create";
        HashMap params = new HashMap();
        params.put("maintenanceid", param.get("maintenanceid"));
//        params.put("description",param.get("description"));
        params.put("maintenance_type", param.get("maintenanceType"));
        params.put("name", param.get("name"));
        params.put("active_since", param.get("activeSince"));
        params.put("active_till", param.get("activeTill"));
//        params.put("tags_evaltype",param.get("tagsEvalType"));
//        params.put("groupids",param.get("groupids"));
        params.put("hostids", param.get("hostids"));
        params.put("timeperiods", times);
        params.put("descripyion", param.get("description"));
//        params.put("tags",tags);
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult maintenanceDelete(int serverId, List<String> params) {
        String method = "maintenance.delete";
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult maintenanceGet(int serverId, Map params) {
        String method = "maintenance.get";
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult maintenanceUpdate(int serverId, Map<String, Object> param, List<HashMap> times) {
        String method = "maintenance.update";
        HashMap params = new HashMap();
        params.put("maintenanceid", param.get("maintenanceid"));
//        params.put("description",param.get("description"));
        params.put("maintenance_type", param.get("maintenanceType"));
        params.put("name", param.get("name"));
        params.put("active_since", param.get("activeSince"));
        params.put("active_till", param.get("activeTill"));
//        params.put("tags_evaltype",param.get("tagsEvalType"));
//        params.put("groupids",param.get("groupids"));
        params.put("hostids", param.get("hostids"));
        params.put("timeperiods", times);
        params.put("descripyion", param.get("description"));
//        params.put("tags",tags);
        return callApi(method, params);
    }

    public MWZabbixAPIResult trendBatchGet(int serverId, List<String> itemids, Long timeFrom, Long timeTill) {
        String method = "trend.get";
        HashMap params = new HashMap();
        params.put("itemids", itemids);
        if (null != timeFrom && null != timeTill) {
            params.put("time_from", timeFrom);
            params.put("time_till", timeTill);
        }
        params.put("output", new String[]{"itemid", "clock", "num", "value_max", "value_avg", "value_min"});
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult hostUpdateSoName(Integer serverId, String hostid, String name) {
        String method = "host.update";
        HashMap<String, Object> params = new HashMap();

        params.put("hostid", hostid);
        params.put("name", name);
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult hostUpdateSoName(Integer serverId, List<MwSyncZabbixAssetsParam> params) {
        String method = "host.update";
        List<Map<String, Object>> paramList = new ArrayList<>();
        for (MwSyncZabbixAssetsParam param : params) {
            HashMap<String, Object> map = new HashMap();
            map.put("hostid", param.getHostId());
            map.put("name", param.getInstanceName());
            paramList.add(map);
        }
        return callApi(method, paramList);
    }


    @Override
    public MWZabbixAPIResult hostCreateMacro(Integer serverId, String hostid, String macro, Double value) {
        String method = "usermacro.create";
        HashMap<String, Object> params = new HashMap();
        params.put("hostid", hostid);
        params.put("macro", macro);
        params.put("value", value);
        return callApi(method, params);
    }

    @Override
    public MWZabbixAPIResult executeScript(Integer serverId, String scriptid, String hostid) {
        HashMap<String, Object> params = new HashMap();
        params.put("scriptid", scriptid);
        params.put("hostid", hostid);
        return callApi("script.execute", params);
    }


    @Override
    public MWZabbixAPIResult getScript(Integer serverId) {
        HashMap<String, Object> params = new HashMap();
        params.put("output", "extend");
        //params.put("hostid", "15216");
        return callApi("script.get", params);
    }

    /**
     * 获取当前服务器类别
     *
     * @param serverId
     * @return
     */
    @Override
    public TPServerTypeEnum getServerType(int serverId) {
        return serverType;
    }


    private String getWebRexExpression(String hostName, String webName, String key, String code) {
        StringBuffer sb = new StringBuffer("{");
        sb.append(hostName);
        sb.append(":");
        sb.append(key);
        if (key.equals(WEB_TEST_RSPCODE)) {
            sb.append("[" + webName + "," + webName + "]");
            sb.append(".last(,360s)}=");
            sb.append(code);
        } else if (key.equals(WEB_TEST_TIME)) {
//            sb.append("[" + webName + "," + webName + "," + "resp]");
//            sb.append(".last(,360s)}>200");
        } else if (key.equals(WEB_TEST_ERROR)) {
            sb.append("[" + webName + "]");
            sb.append(".str(Failed)}=1");
        } else {
            sb.append("[" + webName + "]");
            sb.append(".last(#3)}=0");
        }
        return sb.toString();
    }

    private String getWebExpression(String hostName, String webName, String key, String code) {
        StringBuffer sb = new StringBuffer("{");
        sb.append(hostName);
        sb.append(":");
        sb.append(key);
        if (key.equals(WEB_TEST_RSPCODE)) {
            sb.append("[" + webName + "," + webName + "]");
            sb.append(".last(,360s)}<>");
            sb.append(code);
        } else if (key.equals(WEB_TEST_TIME)) {
            sb.append("[" + webName + "," + webName + "," + "resp]");
            sb.append(".last(,360s)}>200");
        } else if (key.equals(WEB_TEST_ERROR)) {
            sb.append("[" + webName + "]");
            sb.append(".str(Failed)}=0");
        } else {
            sb.append("[" + webName + "]");
            sb.append(".last(#3)}=1");
        }
        return sb.toString();
    }

    public MWZabbixAPIResult getTriggeInfo(int serverId, List<String> triggerids) {
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        param.put("triggerids", triggerids);
        return callApi(method, param);
    }

    public MWZabbixAPIResult getAcknowledgeidByObjectId(int serverId, List<String> triggerids) {
        HashMap param = new HashMap<>();
        String method = "problem.get";
        param.put("output", new String[]{"clock", "ns", "name", "severity", "eventid", "r_clock", "r_ns", "userid", "objectid", "acknowledged"});
        param.put("objectids", triggerids);
        return callApi(method, param);
    }


}
