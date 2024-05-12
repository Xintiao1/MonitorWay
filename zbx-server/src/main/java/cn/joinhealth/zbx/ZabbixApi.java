package cn.joinhealth.zbx;


import cn.joinhealth.zbx.enums.action.AlterDto;
import cn.joinhealth.zbx.enums.action.EventSourceEnum;
import cn.joinhealth.zbx.enums.action.ProxyDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j
public class ZabbixApi implements ZabbixAPIInterface {

    private URI uri;
    private volatile String auth;
    private CloseableHttpClient httpClient;

//  @Value("${zabbix.url}")
   private String zabbixUrl;

//  @Value("${zabbix.user}")
  private String zabbixUser;

//   @Value("${zabbix.password}")
  private String zabbixPassword;

//    @Autowired
    private Environment env;

//  public ZabbixApi(String url) {
//    try {
//      uri = new URI(url.trim());
//    } catch (URISyntaxException e) {
//      throw new RuntimeException("url invalid", e);
//    }
//  }

//  public ZabbixApi(){
//
//  }
//
//  public ZabbixApi(URI uri) {
//    this.uri = uri;
//  }
//
//  public ZabbixApi(String url, CloseableHttpClient httpClient) {
//    this(url);
//    this.httpClient = httpClient;
//  }
//
//  public ZabbixApi(URI uri, CloseableHttpClient httpClient) {
//    this(uri);
//    this.httpClient = httpClient;
//  }

 @PostConstruct
  public void init() {
    if ((null != zabbixUrl) || "".equals(zabbixUrl)){
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
    login(zabbixUser,zabbixPassword);
  }

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

    ZabbixAPIResult zabbixAPIResult = callApi(method, params);
    if (zabbixAPIResult.isFail()) {
      log.info("User {} login failure. Error Info:{}", user, zabbixAPIResult.getData());
      return false;
    } else {
      String auth = ((TextNode) zabbixAPIResult.getData()).asText();
      if (auth != null && !auth.isEmpty()) {
        this.auth = auth;
        log.info("User:{} login success.", user);
        return true;
      }
      return false;
    }
  }

  public ZabbixAPIResult callApi(String method) {
    return callApi(method, Collections.emptyList());
  }

  public ZabbixAPIResult callApi(String method, Object params) {
    ZabbixAPIResult zabbixAPIResult = new ZabbixAPIResult();
    RequestBuilder requestBuilder = RequestBuilder.newBuilder().initRequest(params).method(method);
    log.info("callApi ,method:{},params:{}", method, JSON.toJSON(params));
    JsonNode response = call(requestBuilder.build());
    if (response.has("error")) {
      zabbixAPIResult.setCode(response.get("error").get("code").asInt());
      zabbixAPIResult.setMessage(response.get("error").get("message").asText());
      zabbixAPIResult.setData(response.get("error").get("data").asText());
    } else {
      zabbixAPIResult.setMessage("Call Zabbix API Success.");
      zabbixAPIResult.setCode(ZabbixAPIResultCode.SUCCESS.code());
      zabbixAPIResult.setData(response.get("result"));
    }

    printAPIResult(zabbixAPIResult);

    return zabbixAPIResult;
  }

  public JsonNode call(RequestAbstract request) {
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

  private void printAPIResult(ZabbixAPIResult zabbixAPIResult) {
    try {
      log.info("Call API. Result is :{}", new ObjectMapper().
              writerWithDefaultPrettyPrinter().writeValueAsString(zabbixAPIResult));
    } catch (Exception exception) {
        log.error("fail to printAPIResult with  Param={}, cause:{}", zabbixAPIResult, exception);
    }
  }

  /**
   * Get Zabbix API version. No need to login before that.
   * @return
   */
  public ZabbixAPIResult apiVersion() {
    return callApi("apiinfo.version");
  }

  /**
   * Create one host group.
   *
   * @param groupname host group name
   * @return ZabbixAPIResult.data contains "groupids" field and fetch the first one of groupids.
   * If error happened, refer to code, message, data for information.
   */
  public ZabbixAPIResult hostgroupCreate(String groupname) {
    String method = "hostgroup.create";
    HashMap params = new HashMap();
    params.put("name", groupname);

    return callApi(method, params);
  }

  /**
   * Create multiple host groups.
   *
   * @param groupNameList List of host group names
   * @return ZabbixAPIResult.data.groupids is the host group id array created
   */
  public ZabbixAPIResult hostgroupListCreate(ArrayList<String> groupNameList) {
    String method = "hostgroup.create";
    ArrayList<HashMap<String, String>> params = new ArrayList<>();

    groupNameList.forEach(groupname -> {
      HashMap<String, String> map = new HashMap();
      map.put("name", groupname);
      params.add(map);
    });

    return callApi(method, params);
  }

  /**
   * Get multiple host groups by list of of host group names.
   *
   * @param groupNameList List of host group names
   * @return ZabbixAPIResult.data is the host group array found, with each one including groupid,name,flags,internal
   */
  public ZabbixAPIResult hostgroupListGetByName(ArrayList<String> groupNameList) {
    String method = "hostgroup.get";
    HashMap<String, HashMap<String, List<String>>> params = new HashMap();
    HashMap<String, List<String>> nameMap = new HashMap();
    nameMap.put("name", groupNameList);
    params.put("filter", nameMap);

    return callApi(method, params);
  }

  /**
   * Get multiple host group ids by list of of host group names.
   *
   * @param groupNameList List of host group names
   * @return ZabbixAPIResult.data is the host group id array found
   */
  public ZabbixAPIResult hostgroupIdListGetByName(ArrayList<String> groupNameList) {
    ZabbixAPIResult hostgroupGetResult = hostgroupListGetByName(groupNameList);
    ArrayList<String> groupIdList = new ArrayList<>();

    if (hostgroupGetResult.isFail()) return hostgroupGetResult;
    JsonNode data = (JsonNode) hostgroupGetResult.getData();
    data.forEach(group -> {
      groupIdList.add(group.get("groupid").asText());
    });

    hostgroupGetResult.setData(groupIdList);

    return hostgroupGetResult;
  }

  /**
   * Get host group by host group name.
   *
   * @param groupname host group name
   * @return ZabbixAPIResult.data is the host group array found, with each one including groupid,name,flags,internal.
   * If found, get the first item.
   */
  public ZabbixAPIResult hostgroupGetByGroupName(String groupname) {
    ArrayList<String> groupNameList = new ArrayList<>();
    groupNameList.add(groupname);

    return hostgroupListGetByName(groupNameList);
  }

  /**
   * Get host groups by host names
   *
   * @param hostNameList List of host name
   * @return ZabbixAPIResult.data is the host group array found, with each one including groupid,name,flags,internal.
   */
  public ZabbixAPIResult hostgroupGetByHostNameList(ArrayList<String> hostNameList) {
    ZabbixAPIResult hostGetResult = hostListGetByHostName(hostNameList);
    if (hostGetResult.isFail()) return hostGetResult;

    JsonNode data = (JsonNode) hostGetResult.getData();
    ArrayList<String> hostIdList = new ArrayList<>();
    data.get("hostids").forEach(hostid -> {
      hostIdList.add(hostid.asText());
    });

    HashMap<String, List<String>> params = new HashMap();
    params.put("hostids", hostIdList);

    String method = "hostgroup.get";
    return callApi(method, params);
  }

  /**
   * Get host group by host name
   *
   * @param host host name
   * @return ZabbixAPIResult.data is the host group array found, with each one including groupid,name,flags,internal.
   * If found, get the first item.
   */
  public ZabbixAPIResult hostgroupGetByHostName(String host) {
    ArrayList<String> hostNameList = new ArrayList<>();
    hostNameList.add(host);

    return hostgroupGetByHostNameList(hostNameList);
  }

  /**
   * Check host group whether exists.
   *
   * @param groupname host group name
   * @return True means the host group exists. False means the host group not exists or search request returns some error.
   */
  public boolean hostgroupExists(String groupname) {
    boolean exists = false;
    ZabbixAPIResult hostgroupGetResult = hostgroupGetByGroupName(groupname);
    if (!hostgroupGetResult.isFail()) {
      JsonNode data = (JsonNode) hostgroupGetResult.getData();
      if (data.size() > 0) exists = true;
    }
    return exists;
  }

  /**
   * Delete multiple host groups by group ids
   *
   * @param hostGroupIdList List of host group ids
   * @return ZabbixAPIResult.data.groupids returns groupids that have been deleted
   */
  public ZabbixAPIResult hostgroupListDeleteById(ArrayList<String> hostGroupIdList) {
    String method = "hostgroup.delete";
    return callApi(method, hostGroupIdList);
  }

  /**
   * Delete multiple host groups by group name
   *
   * @param groupNameList List of host group names
   * @return ZabbixAPIResult.data.groupids returns groupids that have been deleted
   */
  public ZabbixAPIResult hostgroupListDeleteByName(ArrayList<String> groupNameList) {
    ZabbixAPIResult hostgroupGetResult = hostgroupIdListGetByName(groupNameList);
    if (hostgroupGetResult.isFail()) return hostgroupGetResult;

    ArrayList<String> hostGroupIdList = (ArrayList<String>) hostgroupGetResult.getData();
    if (hostGroupIdList.size() > 0) {
      return hostgroupListDeleteById(hostGroupIdList);
    } else {
      ZabbixAPIResult zabbixAPIResult = new ZabbixAPIResult();
      zabbixAPIResult.setCode(ZabbixAPIResultCode.SUCCESS.code());
      zabbixAPIResult.setMessage("Call Zabbix API Success.");
      HashMap<String, List<String>> groupids = new HashMap();
      groupids.put("groupids", hostGroupIdList);
      zabbixAPIResult.setData(groupids);

      return zabbixAPIResult;
    }
  }

  /**
   * Delete one host group by group id
   *
   * @param hostGroupId host group id
   * @return ZabbixAPIResult.data.groupids.get(0) returns groupid that has been deleted
   */
  public ZabbixAPIResult hostgroupDeleteById(String hostGroupId) {
    ArrayList<String> hostGroupIdList = new ArrayList<>();
    hostGroupIdList.add(hostGroupId);

    return hostgroupListDeleteById(hostGroupIdList);
  }

  /**
   * Delete one host group by group name
   *
   * @param groupname host group name
   * @return ZabbixAPIResult.data.groupids.get(0) returns groupid that has been deleted
   */
  public ZabbixAPIResult hostgroupDeleteByName(String groupname) {
    ArrayList<String> groupNameList = new ArrayList<>();
    groupNameList.add(groupname);

    return hostgroupListDeleteByName(groupNameList);
  }

  /**
   * Create one host
   *
   * @param host           host name
   * @param groupIdList    List of groupId to add the host to
   * @param hostInterfaces Interfaces to be created for the host
   * @return ZabbixAPIResult.data.hostids is the host id array created
   */
  public ZabbixAPIResult hostCreate(String host, ArrayList<String> groupIdList, Object hostInterfaces,ArrayList<String>templates,List<Map> macro) {
    String method = "host.create";

    ArrayList<HashMap> groups = new ArrayList();
    groupIdList.forEach(groupId -> {
      HashMap<String, String> group = new HashMap();
      group.put("groupid", groupId);
      groups.add(group);
    });
    ArrayList<HashMap> temps = new ArrayList();
    templates.forEach(templateid->{
      HashMap<String, String> templ = new HashMap();
      templ.put("templateid",templateid);
      temps.add(templ);
    });
    HashMap<String, Object> params = new HashMap();
    params.put("host", host);
    params.put("groups", groups);
    params.put("templates",temps);
    params.put("macros",macro);
    params.put("interfaces", hostInterfaces);
    return callApi(method, params);
  }


    /**
     * 更新主机名称
     * @param hostid
     * @param groupIdList
     * @return
     */
    public ZabbixAPIResult hostupdate(String hostid,String host, ArrayList<String> groupIdList, Object hostInterfaces){
        String method = "host.update";
        HashMap<String, Object> params = new HashMap();
        params.put("hostid",hostid);
        params.put("host",host);
        ArrayList<HashMap> groups = new ArrayList();
        groupIdList.forEach(groupId -> {
            HashMap<String, String> group = new HashMap();
            group.put("groupid", groupId);
            groups.add(group);
        });
        params.put("groups",groups);
        //params.put("interfaces", hostInterfaces);
        return callApi(method, params);
    }

    /**
     * 更新主机接口
     * @param hostid
     * @param hostInterfaces
     * @return
     */
    public ZabbixAPIResult updateinterface(String hostid,Object hostInterfaces) {
        String method = "hostinterface.replacehostinterfaces";
        HashMap<String, Object> params = new HashMap();
        params.put("hostid", hostid);
        params.put("interfaces", hostInterfaces);
        return callApi(method, params);
    }

  /**
   * Create multiple hosts.
   *
   * @param hostParamList List of host params. Each param should contain host, groups, interfaces.
   * @return ZabbixAPIResult.data.hostids is the host id array created
   */
  public ZabbixAPIResult hostListCreate(ArrayList<HashMap> hostParamList) {
    String method = "host.create";

    return callApi(method, hostParamList);
  }

  /**
   * Check host whether exists.
   *
   * @param host host name
   * @return True means the host exists. False means the host not exists or search request returns some error.
   */
  public boolean hostExists(String host) {
    boolean exists = false;
    ZabbixAPIResult hostGetResult = hostGetByHostName(host);
    if (!hostGetResult.isFail()) {
      JsonNode data = (JsonNode) hostGetResult.getData();
      if (data.size() > 0) exists = true;
    }
    return exists;
  }

  /**
   * Get hosts by host name list.
   *
   * @param hostNameList List of host name
   * @return ZabbixAPIResult.data is the host array, with each one including host properties such as
   * host, hostid, name, status etc.
   */
  public ZabbixAPIResult hostListGetByHostName(ArrayList<String> hostNameList) {
    String method = "host.get";
    HashMap<String, HashMap<String, List<String>>> params = new HashMap();
    HashMap<String, List<String>> filter = new HashMap();
    filter.put("host", hostNameList);
    params.put("filter", filter);

    return callApi(method, params);
  }

  /**
   * 得到主机宏
   * @param hostid
   * @return
   */
  public ZabbixAPIResult getHostMacrosByHostId(String hostid){
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("output", "hostid");
    param.put("hostids", hostid);
    param.put("selectMacros","extend");

//    filter.put("key_", itemKeyList);

    return hostGet(param, filter);
  }

  /**
   * 得到模板宏
   * @param templid
   * @return
   */
  public ZabbixAPIResult getTemplateMacroByTempId(String templid){
    String method = "template.get";
    HashMap params = new HashMap();
    HashMap params1 = new HashMap();
    params.put("templateids",templid);
    params.put("output",new String[]{"host","macro"});
    params.put("selectMacros","extend");
    return callApi(method, params);
  }

  public ZabbixAPIResult hostGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "host.get";
    param.put("filter", filter);

    return callApi(method, param);
  }
    public ZabbixAPIResult getHostList(String name){
        String method = "host.get";
        HashMap<String, HashMap<String, List<String>>> params = new HashMap();
        if(StringUtils.isNotEmpty(name)){
            ArrayList<String> list = new ArrayList<>();
            HashMap<String, List<String>> filter = new HashMap();
            list.add(name);
            filter.put("host", list);
            params.put("filter", filter);
        }

        return callApi(method, params);
    }

    /**
     * 获取主机接口
     * @return
     */
    public ZabbixAPIResult getHostinterface(String hostids){
        String method = "hostinterface.get";
        HashMap<String, Object> params = new HashMap();
        params.put("output","extend");
        if(StringUtils.isNotEmpty(hostids)){
            params.put("hostids",hostids);
        }
        return callApi(method, params);
    }

  /**
   * Get host by host name.
   *
   * @param host host name
   * @return ZabbixAPIResult.data is the host array, with each one including host properties such as
   * host, hostid, name, status etc. If exists, fetch the first one.
   */
  public ZabbixAPIResult hostGetByHostName(String host) {
    ArrayList<String> hostNameList = new ArrayList();
    hostNameList.add(host);

    return hostListGetByHostName(hostNameList);
  }

  /**
   * Get host by group names.
   *
   * @param groupNameList List of host group name
   * @return ZabbixAPIResult.data is the host array found.
   */
  public ZabbixAPIResult hostGetByGroupName(ArrayList<String> groupNameList) {
    String method = "host.get";

    ZabbixAPIResult hostgroupGetResult = hostgroupIdListGetByName(groupNameList);
    if (hostgroupGetResult.isFail()) return hostgroupGetResult;
    ArrayList<String> groupIdList = (ArrayList<String>) hostgroupGetResult.getData();

    HashMap<String, List<String>> params = new HashMap();
    if (groupIdList.size() > 0) {
      params.put("groupids", groupIdList);
    }

    return callApi(method, params);
  }

  /**
   * Get host by group names.
   *
   * @param groupIds List of host group id
   * @param selectInterfaces List of host interfaces
   * @param output List of output item
   * @return ZabbixAPIResult.data is the host array found.
   */
  public ZabbixAPIResult hostGetByGroupId(List<String> groupIds
          , List<String> selectInterfaces, List<String> output) {

    String method = "host.get";

    HashMap<String, List<String>> params = new HashMap();
    if (groupIds.size() > 0) {
      params.put("groupids", groupIds);
    }

    if(selectInterfaces.size() > 0){
      params.put("selectInterfaces", selectInterfaces);
    }

    if(output.size() > 0){
      params.put("output", output);
    }

    return callApi(method, params);
  }

  /**
   * Get host by host names and group names.
   *
   * @param hostNameList  List of host name.
   * @param groupNameList List of host group name
   * @return ZabbixAPIResult.data is the host array found.
   */
  public ZabbixAPIResult hostGetByHostNameAndGroupName(ArrayList<String> hostNameList,
                                                       ArrayList<String> groupNameList) {
    String method = "host.get";

    ZabbixAPIResult hostgroupGetResult = hostgroupIdListGetByName(groupNameList);
    if (hostgroupGetResult.isFail()) return hostgroupGetResult;

    ArrayList<String> groupIdList = (ArrayList<String>) hostgroupGetResult.getData();
    HashMap<String, Object> params = new HashMap();
    if (groupIdList.size() > 0) {
      params.put("groupids", groupIdList);
    }

    HashMap<String, List<String>> filter = new HashMap();
    filter.put("host", hostNameList);
    params.put("filter", filter);

    return callApi(method, params);
  }

  /**
   * Delete multiple hosts by host ids.
   *
   * @param hostIdList List of host id
   * @return ZabbixAPIResult.data.hostids is host id array that have been deleted
   */
  public ZabbixAPIResult hostListDeleteById(ArrayList<String> hostIdList) {
    String method = "host.delete";
    return callApi(method, hostIdList);
  }

  /**
   * Delete host by host id
   *
   * @param hostId host id
   * @return ZabbixAPIResult.data.hostids is host id array that have been deleted.
   * If host id exists, fetch the first one.
   */
  public ZabbixAPIResult hostDeleteById(String hostId) {
    ArrayList<String> hostIdList = new ArrayList<>();
    hostIdList.add(hostId);

    return hostgroupListDeleteById(hostIdList);
  }

  /**
   * Delete hosts by host names.
   *
   * @param hostNameList List of host name
   * @return ZabbixAPIResult.data.hostids is host id array that have been deleted.
   * If host id exists, fetch the first one.
   */
  public ZabbixAPIResult hostListDeleteByName(ArrayList<String> hostNameList) {
    ZabbixAPIResult hostListGetResult = hostListGetByHostName(hostNameList);
    if (hostListGetResult.isFail()) return hostListGetResult;
    JsonNode data = (JsonNode) hostListGetResult.getData();
    ArrayList<String> hostIdList = new ArrayList<>();
    if (data.size() > 0) {
      data.forEach(host -> {
        hostIdList.add(host.get("hostid").asText());
      });
    }

    if (hostIdList.size() > 0) {
      return hostListDeleteById(hostIdList);
    }

    ZabbixAPIResult zabbixAPIResult = new ZabbixAPIResult();
    zabbixAPIResult.setCode(ZabbixAPIResultCode.SUCCESS.code());
    zabbixAPIResult.setMessage("Call Zabbix API Success.");
    HashMap<String, List<String>> groupids = new HashMap();
    groupids.put("hostids", hostIdList);
    zabbixAPIResult.setData(groupids);

    return zabbixAPIResult;
  }

  /**
   * Delete host by host name.
   *
   * @param host host name
   * @return ZabbixAPIResult.data.hostids is host id array that have been deleted.
   * If host id exists, fetch the first one.
   */
  public ZabbixAPIResult hostDeleteByName(String host) {
    ArrayList<String> hostNameList = new ArrayList<>();
    hostNameList.add(host);

    return hostListDeleteByName(hostNameList);
  }

  /**
   * Create host interface.
   *
   * @param dns    required property. DNS name used by the interface. Can be empty if the connection is made via IP.
   * @param hostid required property. ID of the host the interface belongs to.
   * @param ip     required property. IP address used by the interface. Can be empty if the connection is made via DNS.
   * @param main   该接口是否在主机上用作默认接口 主机上只能有一种类型的接口作为默认设置.
   *               0 - 不是默认;
   *               1 - 默认.
   * @param port   	接口使用的端口号.
   * @param type   接口类型 .
   *               1 - agent;
   *               2 - SNMP;
   *               3 - IPMI;
   *               4 - JMX.
   * @param useip  是否应通过IP进行连接.
   *               0 - 使用主机DNS名称连接;;
   *               1 - 使用该主机接口的主机IP地址进行连接.
   * @param bulk   是否使用批量SNMP请求.
   *               0 - 不要使用批量请求;
   *               1 - (默认) 使用批量请求.
   * @return ZabbixAPIResult.data.interfaceids is the interface id array
   */
  public ZabbixAPIResult hostInterfaceCreate(String dns, String hostid, String ip, String main,
                                             String port, String type, String useip, String bulk) {
    String method = "hostinterface.create";

    HashMap<String, String> param = new HashMap();
    HashMap<String, Object> param0 = new HashMap();
    param.put("dns", dns);
    param.put("hostid", hostid);
    param.put("ip", ip);
    param.put("main", main);
    param.put("port", port);
    param.put("type", type);
    param.put("useip", useip);
    param.put("bulk", bulk);
    param0.put("params",param);
    return callApi(method, param);
  }

  /**
   * Create multiple host interfaces.
   *
   * @param hostInterfaceList List of host interface params
   * @return ZabbixAPIResult.data.interfaceids is the interface id array
   */
  public ZabbixAPIResult hostInterfaceListCreate(ArrayList<HashMap> hostInterfaceList) {
    String method = "hostinterface.create";
    String[] requiredProperties = {"dns", "hostid", "ip", "main", "port", "type", "useip"};
    ZabbixAPIResult zabbixAPIResult = new ZabbixAPIResult();

    for (int i = 0; i < hostInterfaceList.size(); i++) {
      for (int j = 0; j < requiredProperties.length; j++) {
        if (!hostInterfaceList.get(i).containsKey(requiredProperties[j])) {
          zabbixAPIResult.setCode(ZabbixAPIResultCode.PARAM_IS_INVALID.code());
          zabbixAPIResult.setMessage(ZabbixAPIResultCode.PARAM_IS_INVALID.message() +
                  requiredProperties + " are required.");
          zabbixAPIResult.setData("Param has no property : " + requiredProperties[j]);
          return zabbixAPIResult;
        }
      }
    }
    return callApi(method, hostInterfaceList);
  }

  /**
   * Get host interface by host ids.
   *
   * @param hostIdList List of host id
   * @return ZabbixAPIResult.data is host interface array found, with each one including
   * interfaceid, hostid, main, type, useip, ip, etc.
   */
  public ZabbixAPIResult hostInterfaceGetByHostIds(ArrayList<String> hostIdList) {
    String method = "hostinterface.get";

    HashMap<String, List<String>> param = new HashMap();
    param.put("hostids", hostIdList);

    return callApi(method, param);
  }

    public ZabbixAPIResult hostinterfaceget(String hostids){
        String method = "hostinterface.get";
        HashMap params = new HashMap();
        params.put("output",new String[]{"ip","port"});
        params.put("hostids",hostids);
        return callApi(method, params);
    }

  /**
   * Get host interfaces by host names.
   *
   * @param hostNameList List of host name
   * @return ZabbixAPIResult.data is host interface array found, with each one including
   * interfaceid, hostid, main, type, useip, ip, etc.
   */
  public ZabbixAPIResult hostInterfaceGetByHostNames(ArrayList<String> hostNameList) {
    String method = "hostinterface.get";

    ZabbixAPIResult hostGetResult = hostListGetByHostName(hostNameList);
    if (hostGetResult.isFail()) return hostGetResult;

    ArrayList hostIdList = new ArrayList();
    JsonNode data = (JsonNode) hostGetResult.getData();
    data.forEach(host -> {
      hostIdList.add(host.get("hostid"));
    });

    HashMap<String, List<String>> param = new HashMap();
    param.put("hostids", hostIdList);

    return callApi(method, param);
  }

  /**
   * Delete multiple host interfaces by host interface ids
   *
   * @param hostInterfaceIdList List of h ids
   * @return ZabbixAPIResult.data.interfaceids returns interfaceids that have been deleted
   */
  public ZabbixAPIResult hostInterfaceListDeleteById(ArrayList<String> hostInterfaceIdList) {
    String method = "hostinterface.delete";

    return callApi(method, hostInterfaceIdList);
  }

  /**
   * Create item.
   *
   * @param param item parameter.
   *              It should contain the required properties:delay,hostid,interfaceid,key_,name,type,value_type.
   * @return ZabbixAPIResult.data contains "itemids" field and fetch the first one of itemids.
   * *         If error happened, refer to code, message, data for information.
   */
  public ZabbixAPIResult itemCreate(HashMap<String, Object> param) {
    ArrayList<HashMap<String, Object>> params = new ArrayList<>();

    params.add(param);

    return itemListCreate(params);
  }

  /**
   * Create multiple items.
   *
   * @param params List of item parameter .
   *               Each parameter should contain the required properties:delay,hostid,interfaceid,key_,name,type,value_type.
   * @return ZabbixAPIResult.data contains "itemids".
   * *         If error happened, refer to code, message, data for information.
   */
  public ZabbixAPIResult itemListCreate(ArrayList<HashMap<String, Object>> params) {
    String method = "item.create";

    String[] requiredProperties = {"delay", "hostid", "interfaceid", "key_", "name", "type", "value_type"};
    ZabbixAPIResult zabbixAPIResult = new ZabbixAPIResult();
    for (int i = 0; i < params.size(); i++) {
      HashMap param = params.get(i);
      for (int j = 0; j < requiredProperties.length; j++) {
        if (!param.containsKey(requiredProperties[j])) {
          zabbixAPIResult.setCode(ZabbixAPIResultCode.PARAM_IS_INVALID.code());
          zabbixAPIResult.setMessage(ZabbixAPIResultCode.PARAM_IS_INVALID.message() +
                  requiredProperties + " are required.");
          zabbixAPIResult.setData("Param has no property : " + requiredProperties[j]);
          return zabbixAPIResult;
        }
      }
    }

    return callApi(method, params);
  }

  /**
   * Check whether item exists by host name and item key.
   *
   * @param hostname host name
   * @param itemKey  item key_
   * @return True means item exists. False means item not exists.
   */
  public boolean itemExistsByItemKey(String hostname, String itemKey) {
    ArrayList<String> itemKeyList = new ArrayList();

    itemKeyList.add(itemKey);
    ZabbixAPIResult itemGetResult = itemGetByHostNameAndItemKey(hostname, itemKeyList);

    return itemExistsByCheckResult(itemGetResult);
  }

  /**
   * Check whether item exists by host name and item name.
   *
   * @param host     host name
   * @param itemName item name
   * @return True means item exists. False means item not exists.
   */
  public boolean itemExistsByItemName(String host, String itemName) {
    ArrayList<String> itemNameList = new ArrayList();

    itemNameList.add(itemName);
    ZabbixAPIResult itemGetResult = itemGetByHostNameAndItemName(host, itemNameList);

    return itemExistsByCheckResult(itemGetResult);
  }

  private boolean itemExistsByCheckResult(ZabbixAPIResult itemGetResult) {
    if (!itemGetResult.isFail()) {
      JsonNode data = (JsonNode) itemGetResult.getData();

      if (data.size() > 0) {
        String itemid = data.get(0).get("itemid").asText();
        if (itemid != null && !itemid.isEmpty()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Get item by param and search info.
   *
   * @param param  It can include itemids, groupids, hostids, interfaceids, host, group, etc
   * @param filter It can include key_, name, etc.
   * @return ZabbixAPIResult.data is the item array found, with each one including itemid,hostid,key_,name,etc.
   */
  public ZabbixAPIResult itemGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "item.get";
    param.put("filter", filter);
    return callApi(method, param);
  }

    public ZabbixAPIResult itemget(String itemid){
        String method = "item.get";
        HashMap params = new HashMap();
        params.put("itemid",itemid);
        return callApi(method, params);
    }

    /**
     * 获取异常的触发器（value值未1，说明是有报警的情况）
     * @return
     */
    public ZabbixAPIResult triggerGet() {
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        //param.put("filter",filter);
        param.put("output",new String[]{"hosts","state","triggerid","description","priority","value"});
        param.put("selectHosts",new String[]{"hosts"});
        HashMap map = new HashMap();
        map.put("value","1");
        map.put("only_true",true);
        param.put("filter",map);
        return callApi(method, param);
    }

    /**
     * 根据触发器ID查询触发器
     * @param triggerids
     * @return
     */
    public ZabbixAPIResult triggerGetById(String triggerids){
        HashMap param = new HashMap<>();
        String method = "trigger.get";
        param.put("triggerids",triggerids);
        param.put("output",new String[]{"hosts","state","triggerid","description","priority","value"});
        return callApi(method, param);
    }


    /**
     * 获取应用集
     * @param hostid
     * @return
     */
    public ZabbixAPIResult getApp(String hostid){
    HashMap param = new HashMap<>();
    String method = "application.get";
    param.put("output",new String[]{"itemids","name","applicationid","hostid"});
    param.put("hostids",hostid);
    param.put("selectItems","item");
    return callApi(method, param);
    }

    public ZabbixAPIResult getAppByid(String applicationids){
        HashMap param = new HashMap<>();
        String method = "application.get";
        param.put("output",new String[]{"name"});
        param.put("applicationids",applicationids);
        return callApi(method, param);
    }

    /**
     * 查询监控项
     * @param applicationids
     * @return
     */
    public ZabbixAPIResult getItems(String applicationids){
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output",new String[]{"itemid","name","history","hostid","lastclock","delay","trends","lastvalue","value_type"});
        param.put("applicationids",applicationids);
        return callApi(method, param);
    }

    public ZabbixAPIResult itemgetbyhostid(String hostid){
        HashMap param = new HashMap<>();
        String method = "item.get";
        param.put("output",new String[]{"itemid","name","history","hostid","lastclock","delay","trends","lastvalue","value_type"});
        param.put("hostids",hostid);
        return callApi(method, param);
    }

  public ZabbixAPIResult hostitem(String hostid,ArrayList<String> list){
    String method = "item.get";
    HashMap params = new HashMap();
    params.put("hostids",hostid);
    HashMap params1 = new HashMap();
    params1.put("key_",list);
    params.put("filter",params1);
    params.put("output",new String[]{"itemid","name","history","hostid","lastclock","lastvalue","value_type"});
    //params.put("sortfield",new String[]{"lastclock"});
    //params.put("sortorder","DESC");
    return callApi(method,params);
  }

  public ZabbixAPIResult itemGetSortbyHostid(List<String> output, List<String> hostids
  ,String search ,String sortfield ,List<String> selectItems){
    String method = "item.get";
    HashMap params = new HashMap();

    params.put("output", output);
    params.put("hostids", hostids);
    HashMap params1 = new HashMap();
    params1.put("key_", search);
    params.put("search",params1);
    params.put("sortfield", sortfield);
    params.put("selectItems", selectItems);

    return callApi(method,params);
  }

  /**
   * 查询主机ID
   * @return
   */
  public ZabbixAPIResult getHostId(){
      String method = "host.get";
    HashMap params = new HashMap();
    params.put("output",new String[]{"hostid"});
    return callApi(method,params);
  }

    /**
     * 查询模板
     * @param itemid
     * @return
     */
    public ZabbixAPIResult getitem(String itemid){
        HashMap params = new HashMap<>();
        String method = "item.get";
        params.put("output",new String[]{"itemid","name","history","hostid","lastclock","delay","trends","lastvalue"});
        params.put("itemids",itemid);
        return callApi(method, params);
    }


  /**
   * Get item by host and item key.
   *
   * @param host        host name
   * @param itemKeyList List of item key_
   * @return ZabbixAPIResult.data is the item array found, with each one including itemid,hostid,key_,name,etc.
   */
  public ZabbixAPIResult itemGetByHostNameAndItemKey(String host, ArrayList<String> itemKeyList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("host", host);


    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("key_", itemKeyList);
    param.put("search", search);
    param.put("searchByAny",true);

//    filter.put("key_", itemKeyList);

    return itemGet(param, filter);
  }


  /**
   * Get item by host and item id.
   *
   * @return ZabbixAPIResult.data is the item array found, with each one including itemid,hostid,key_,name,etc.
   */
  public ZabbixAPIResult itemGetById(ArrayList<String> itemids) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("itemids", itemids);


    return itemGet(param, filter);
  }


  public ZabbixAPIResult itemGetByHostNameAndItemKey(String host, String itemname,ArrayList<String> itemKeyList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

//    param.put("host", host);

    ArrayList hostlist = new ArrayList();
    hostlist.add(host);
    param.put("hostids", host);


    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("key_", itemKeyList);

    if(!"".equals(itemname) && itemname !=null){
      search.put("name", itemname);
    }else{
      param.put("searchByAny", true);

    }
    param.put("search", search);


    return itemGet(param, filter);
  }

  public ZabbixAPIResult itemGetByHostNameAndItemKey2(String host, ArrayList<String> itemKeyList, boolean searchByAny) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    ArrayList hostlist = new ArrayList();
    hostlist.add(host);
    param.put("hostids", host);


    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("key_", itemKeyList);
    param.put("searchByAny", searchByAny);
    param.put("search", search);


    return itemGet(param, filter);
  }



  public ZabbixAPIResult itemGetByItemName(String host, String itemname,List<String> itemKeyList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

//    param.put("host", host);

    ArrayList hostlist = new ArrayList();
    hostlist.add(host);
    param.put("hostids", host);


    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("name", itemname);

    //指查询包含关系
//    HashMap<String, Object> search = new HashMap();

    param.put("search", search);


    return itemGet(param, filter);
  }
  /**
   * Get item by host and item name.
   *
   * @param host         host name
   * @param itemNameList List of item name
   * @return ZabbixAPIResult.data is the item array found, with each one including itemid,hostid,key_,name,etc.
   */
  public ZabbixAPIResult itemGetByHostNameAndItemName(String host, ArrayList<String> itemNameList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("host", host);
    filter.put("name", itemNameList);

    return itemGet(param, filter);
  }

  /**
   * Delete multiple item by item ids.
   *
   * @param itemIdList List of item id.
   * @return ZabbixAPIResult.data.itemids is the item array that have been deleted.
   */
  public ZabbixAPIResult itemListDeleteByItemId(ArrayList<String> itemIdList) {
    String method = "item.delete";

    return callApi(method, itemIdList);
  }

  /**
   * Delete item by item id.
   *
   * @param itemId item id.
   * @return ZabbixAPIResult.data.itemids is the item array that have been deleted. Fetch the first one.
   */
  public ZabbixAPIResult itemDeleteByItemId(String itemId) {
    ArrayList<String> itemIdList = new ArrayList();

    itemIdList.add(itemId);

    return itemListDeleteByItemId(itemIdList);
  }

  /**
   * Delete items by item key
   *
   * @param host        host name
   * @param itemKeyList List of item key
   * @return ZabbixAPIResult.data.itemids is the item array that have been deleted.
   */
  public ZabbixAPIResult itemListDeleteByItemKey(String host, ArrayList<String> itemKeyList) {
    ZabbixAPIResult itemGetResult = itemGetByHostNameAndItemKey(host, itemKeyList);
    if (itemGetResult.isFail()) return itemGetResult;

    JsonNode data = (JsonNode) itemGetResult.getData();
    ArrayList itemIdList = new ArrayList();
    data.forEach(item -> {
      itemIdList.add(item.get("itemid"));
    });

    return itemListDeleteByItemId(itemIdList);
  }

  /**
   * Delete items by item name
   *
   * @param host         host name
   * @param itemNameList List of item name
   * @return ZabbixAPIResult.data.itemids is the item array that have been deleted.
   */
  public ZabbixAPIResult itemListDeleteByItemName(String host, ArrayList<String> itemNameList) {
    ZabbixAPIResult itemGetResult = itemGetByHostNameAndItemName(host, itemNameList);
    if (itemGetResult.isFail()) return itemGetResult;

    JsonNode data = (JsonNode) itemGetResult.getData();
    ArrayList<String> itemIdList = new ArrayList();
    data.forEach(item -> {
      itemIdList.add(item.get("itemid").asText());
    });

    return itemListDeleteByItemId(itemIdList);
  }

  /**
   * 创建主机群组
   * @param name
   * @return
   */
  public ZabbixAPIResult hostGroupCreate(String name){
    String method = "hostgroup.create";
    HashMap params = new HashMap();
    params.put("name",name);
    return callApi(method, params);
  }
  /**
   * 根据规则自动发现的主机
   * @param druleids
   * @return
   */
  public ZabbixAPIResult dhostGet(String druleids){
    String method = "dhost.get";
    HashMap params = new HashMap();
    params.put("output",new String[]{"dhostid","druleid","status","lastup"});
//    params.put("selectDServices",new String[]{"value","port","ip"});
    params.put("selectDServices","extend");
    if(StringUtils.isNotEmpty(druleids)){
      params.put("druleids",druleids);
    }
    return callApi(method, params);
  }

  public ZabbixAPIResult dcheckGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "dcheck.get";
    param.put("filter", filter);

    return callApi(method, param);
  }

  public ZabbixAPIResult dcheckGetBydcheckid(List dcheckids) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("dcheckids", dcheckids);
//    filter.put("type", type);

    return dcheckGet(param, filter);
  }

  /**
   * 根据多个ID查询发现的主机
   * @param dhostids
   * @return
   */
  public ZabbixAPIResult dhostGets(ArrayList<String> dhostids){
    String method = "dhost.get";
    HashMap params = new HashMap();
    params.put("selectDServices",new String[]{"value","port","ip"});
    params.put("output",new String[]{"dhostid","druleid","status","lastup"});

    params.put("dhostids",dhostids);
    return callApi(method, params);
  }
  /**
   * 修改自动发现规则
   * @return
   */
  public ZabbixAPIResult editRule(String druleids, String status){
    String method = "drule.update";
    HashMap params = new HashMap();
    params.put("druleid",druleids);
    params.put("status",status);
    return callApi(method, params);
  }
  /**
   * 自动发现规则
   * @return
   */
  public ZabbixAPIResult druleGet(String name,String status){
    String method = "drule.get";
    HashMap params = new HashMap();
    params.put("output",new String[]{"druleid","name","ports","iprange","status","delay"});
    if(StringUtils.isNotEmpty(name)||StringUtils.isNotEmpty(status)){
      HashMap params1 = new HashMap();

      HashMap<String, Object> search = new HashMap();



      if(StringUtils.isNotEmpty(name)) {
        search.put("name", name);
        params.put("search", search);

      }
      if(StringUtils.isNotEmpty(status)) {
        params1.put("status", status);
      }
      params.put("filter",params1);


      //排序
      ArrayList<String> sortfield = new ArrayList<>();
      sortfield.add("druleid");
      params.put("sortfield",sortfield);
      params.put("sortorder", "DESC");



    }
    return callApi(method, params);
  }

  public ZabbixAPIResult delRule(ArrayList<String> druleids){
    String method = "drule.delete";
    return callApi(method, druleids);
  }

  /**
   * 获取模板
   * @param name
   * @return
     */
  public ZabbixAPIResult templateGet(String name){
    String method = "template.get";
    HashMap params = new HashMap();
    HashMap params1 = new HashMap();
    String[] groups = new String[]{};
    if(StringUtils.isNotEmpty(name)){
      //查询指定
      groups = new String[]{name};

    }
    params1.put("host",groups);
    params.put("output","extend");
    params.put("selectDChecks","extend");
    params.put("filter",params1);
    return callApi(method, params);
  }
  /**
   * 获取主机组
   * @return
   */
  public ZabbixAPIResult hostGroupAll(String name) {
    String method = "hostgroup.get";
    HashMap params = new HashMap();
    HashMap params1 = new HashMap();

    HashMap<String, Object> search = new HashMap();



    if(StringUtils.isNotEmpty(name)) {
      search.put("name", name);
      params.put("search", search);

    }

    params.put("output","extend");
    params.put("filter",params1);

    //排序
    ArrayList<String> sortfield = new ArrayList<>();
    sortfield.add("groupid");
    params.put("sortfield",sortfield);
//    params.put("sortorder", "desc");


    return callApi(method, params);
  }

  /**
   * 创建规则
   * @return
     */
   public ZabbixAPIResult druleCreate(String name,String status,String iprange,String delay,JSONArray arr){
     String method = "drule.create";
     HashMap params = new HashMap();
     params.put("name",name);
     params.put("status",status);
     params.put("delay",delay);
     params.put("iprange",iprange);
     params.put("dchecks",arr);
     ////System.out.println(">>>>>>>>params>>>>>>"+JSON.toJSONString(params));
     return callApi(method, params);
   }

    /**
     * 创建模板
     * @param host
     * @param groupid
     * @param hostid
     * @return
     */
    public ZabbixAPIResult templateCreate(String host,String groupid,String hostid){
        String method = "template.create";
        HashMap params = new HashMap();
        HashMap params1 = new HashMap();
        ArrayList<HashMap> hostsList = new ArrayList();
        HashMap<String, String> hosts = new HashMap();
        hosts.put("hostid", hostid);
        hostsList.add(hosts);
        params1.put("groupid",groupid);
        params.put("host",host);
        params.put("groups",params1);
        params.put("hosts",hostsList);
        return callApi(method, params);
    }

    /**
     * 修改主机状态
     * @param hostid
     * @param status
     * @return
     */
    public ZabbixAPIResult hostUpdate(String hostid,String status){
        HashMap params = new HashMap();
        String method = "host.update";
        params.put("hostid",hostid);
        params.put("status",status);
        return callApi(method, params);
    }

    /**
     * 添加删除模板关联信息
     * @return
     */
    public ZabbixAPIResult addorDelTemplates(String hostid,ArrayList<HashMap> templs ,String type){
        String method = "host.update";
        HashMap params = new HashMap();
        params.put("hostid",hostid);
        if("add".equals(type)){
            params.put("templates",templs);
        }else{
            params.put("templates_clear",templs);
        }
        return callApi(method, params);
    }
    public ZabbixAPIResult templateupdate(){
        String method = "host.update";
        HashMap params = new HashMap();
        return callApi(method, params);
    }

    /**
     * 添加标记
     * @param hostid
     * @param tags
     * @return
     */
    public ZabbixAPIResult addTag(String hostid,ArrayList<HashMap> tags){
        String method = "host.update";
        HashMap params = new HashMap();
        params.put("hostid",hostid);
        params.put("tags",tags);
        return callApi(method, params);
    }
    /**
     * 根据ID查询主机组
     * @param groupidList
     * @return
     */
    public ZabbixAPIResult hostgroupListGetById(ArrayList<String> groupidList) {
        HashMap<String, HashMap<String, List<String>>> params = new HashMap();
        String method = "hostgroup.get";
        HashMap<String, List<String>> nameMap = new HashMap();
        nameMap.put("name", groupidList);
        params.put("filter", nameMap);
        return callApi(method, params);
    }
    /**
     * 维护主机
     * @return
     */
    public ZabbixAPIResult maintenanceCreate(String name,String activeSince,String activeTill,ArrayList<String> hostids,ArrayList<HashMap> times){
        String method = "maintenance.create";
        HashMap params = new HashMap();
        params.put("name",name);
        params.put("active_since",activeSince);
        params.put("active_till",activeTill);
        params.put("tags_evaltype","0");
        params.put("hostids",hostids);
        params.put("timeperiods",times);
        return callApi(method, params);
    }

    /**
     * 获取轮询引擎
     * @return
     */
    public ZabbixAPIResult proxyget(){
        HashMap params = new HashMap();
        String method="proxy.get";
        params.put("output",new String[]{"host","proxyid"});
        params.put("selectInterface","extend");
        return callApi(method, params);
    }

    /**
     * 根据ID查询代理
     * @param proxyids
     * @return
     */
    public ZabbixAPIResult proxygetById(ArrayList<String> proxyids){
        HashMap params = new HashMap();
        String method="proxy.get";
        params.put("proxyids",proxyids);
        return callApi(method, params);
    }


    /**
     * 修改代理
     * @param proxyid
     * @param hostids
     * @return
     */
    public ZabbixAPIResult proxyupdate(String proxyid,ArrayList<String> hostids){
        String method = "proxy.update";
        HashMap params = new HashMap();
        params.put("proxyid",proxyid);
        params.put("hosts",hostids);
        return callApi(method, params);
    }
    /**
     * 根据主机ID查询主机信息
     * @param hostid
     * @return
     */
    public ZabbixAPIResult hostGetById(String hostid){
        String method = "host.get";
        HashMap params = new HashMap();
        ArrayList<String> list = new ArrayList<>();
        list.add(hostid);
        params.put("hostids",hostid);
        return callApi(method, params);
    }


    public ZabbixAPIResult hostById(String hostid){
        String method = "host.get";
        HashMap params = new HashMap();
        params.put("hostids",hostid);
        params.put("output",new String[]{"name"});
        return callApi(method, params);
    }
    /**
     * 删除代理
     * @return
     */
    public ZabbixAPIResult proxydelete(){
        String method = "proxy.delete";
        HashMap params = new HashMap();
        return callApi(method, params);
    }

  /**
   * 创建动作
   * @return
   */
  public ZabbixAPIResult actioncreate(String name,String shortdata,String longdata,Map filter,List operations,String status){
    String method="action.create";
    HashMap params = new HashMap();
    params.put("name",name);//动作的名称
    params.put("eventsource","0");//动作将处理的事件源的类型
    params.put("status",status);//动作是启动还是禁用0 - (默认) 启用 1 - 禁用
    params.put("esc_period","30m");//默认操作步骤持续时间。必须大于 60 秒。接受秒，带后缀的时间单位
    params.put("def_shortdata",shortdata);//异常消息主题      默认就是名称
    params.put("def_longdata",longdata);//异常消息文本   告警消息
    params.put("filter",filter);//动作过滤器对象
    params.put("operations",operations);//创建的动作操作
    ArrayList<HashMap> rec = new ArrayList<>();
    HashMap recmap = new HashMap();
    //recmap.put("operationtype","11");
    recmap.put("operationtype","0");
    HashMap msgmap = new HashMap();
    msgmap.put("default_msg","1");
    recmap.put("opmessage",msgmap);
    rec.add(recmap);
    return callApi(method, params);
  }

    /**
     * 修改动作
     * @return
     */
    public ZabbixAPIResult actionupdate(){
        String method = "action.update";
        HashMap params = new HashMap();
        return callApi(method, params);
    }

    public ZabbixAPIResult usermacroget(String templid){
        String method = "template.get";
        //String method = "usermacro.get";
        HashMap params = new HashMap();
        HashMap params1 = new HashMap();
        params1.put("templateid",new String[]{templid});
        params.put("filter",params1);
        params.put("output",new String[]{"host","macro"});
        params.put("selectMacros","extend");
        return callApi(method, params);
    }

  /**
   * 修改动作
   * @return
   */
  public ZabbixAPIResult actionDelete(List<String> actionIds){
    String method = "action.delete";
    return callApi(method, actionIds);
  }

  /**
   * 获取数据源为触发器的动作
   * @return
   */
  public ZabbixAPIResult actionGetByEventSourceTrigger(String name,String status){
        String method = "action.get";
        HashMap params = new HashMap();
        params.put("output","extend");
        params.put("selectFilter","extend");
        params.put("selectOperations","extend");
        if(StringUtils.isNoneBlank(name)){
          params.put("name",name);
        }
        if(StringUtils.isNoneBlank(status)){
            params.put("status",status);
        }
        HashMap filter = new HashMap();
        filter.put("eventsource", EventSourceEnum.TRIGGER.getCode());
        params.put("filter",filter);
        return callApi(method, params);
    }

    /**
     * 图表监控项
     * @return
     */
    public ZabbixAPIResult graphitemget(String graphids){
        String method = "graphitem.get";
        HashMap params = new HashMap();
        params.put("graphids",graphids);
        return callApi(method,params);
    }

    /**
     * 查询历史
     * @param
     * @return
     */
    public ZabbixAPIResult historygets(ArrayList<String>itemids, Long times){
        String method = "history.get";
        HashMap params = new HashMap();
        params.put("itemids",itemids);
        params.put("time_from",times);
        return callApi(method, params);
    }
    /*******************************图表信息获取*******************************************/
    public ZabbixAPIResult graphget(String hostid){
        String method = "graph.get";
        HashMap params = new HashMap();
        params.put("hostids",hostid);
        params.put("output","extend");
        return callApi(method,params);
    }

  public ZabbixAPIResult graphgetByHostid(String hostid){
    String method = "graph.get";
    HashMap params = new HashMap();
    params.put("hostids",hostid);
    params.put("output",new String[]{"graphid","name"});
    return callApi(method,params);
  }

  public ZabbixAPIResult getGraphById(String hostid,String graphid){
    String method = "graph.get";
    HashMap params = new HashMap();
    params.put("graphids",graphid);
    params.put("hostids",hostid);
    params.put("output",new String[]{"graphid","name"});
    return callApi(method,params);
  }
  /**
   * 查询虚拟机
   * @param groupids
   * @return
   */
    public ZabbixAPIResult gethosts(String[] groupids,String hosstname,String state){
        String method = "host.get";
        HashMap params = new HashMap();
        params.put("output",new String[]{"name","hostid","groupid","os","alias","status","maintenance_status"});
        params.put("selectGroups",new String[]{"groupid","name"});
        params.put("selectItems",new String[]{"itemid","name","hostid","key_","lastvalue","units","applicationid"});
        params.put("groupids",groupids);
        HashMap map = new HashMap();
        if(StringUtils.isNotEmpty(hosstname)){
          map.put("name",hosstname);
        }
      if(StringUtils.isNotEmpty(state)){
        map.put("status",state);
      }
      params.put("filter",map);
        return callApi(method,params);
    }

  /**
   * 查询主机组
   * @return  Hypervisors
   */
    public ZabbixAPIResult getHostGroups(String groupname){
      String method = "hostgroup.get";
      HashMap params = new HashMap();
      HashMap map = new HashMap();
      map.put("name",groupname);
      params.put("filter",map);
      params.put("output",new String[]{"groupid","name"});
      return callApi(method,params);
    }

  /**
   * 根据ip和端口查询接口
   * @param ip
   * @param port
   * @return
   */
    public ZabbixAPIResult getinterface(String ip,String port){
      String method = "hostinterface.get";
      HashMap params = new HashMap();
      HashMap map = new HashMap();
      map.put("ip",ip);
      map.put("port",port);
      params.put("filter",map);
      return callApi(method,params);

    }




/*********************************告警管理*************************************************************/
    /**
     * 获取告警
     * @param eventids
     * @return
     */
  public ZabbixAPIResult alertget(String hostids,String eventids,Long time_from,Long time_till,String users,String status,String subject){
      String method = "alert.get";
    HashMap param = new HashMap();
    HashMap params = new HashMap();
    if(StringUtils.isNotEmpty(status)){
      param.put("status",status);
      params.put("filter",param);
    }
    if(StringUtils.isNotEmpty(subject)){
      param.put("subject",status);
      params.put("search",param);
    }

      if(StringUtils.isNotEmpty(hostids)){
          params.put("hostids",hostids);
      }
      if(StringUtils.isNotEmpty(eventids)){
          params.put("eventids",eventids);
      }
      if(time_from>0){
        params.put("time_from",time_from);
      }
    if(time_till>0){
      params.put("time_till",time_till);
    }
    if(StringUtils.isNotEmpty(users)){
      params.put("selectUsers",users);
    }

      params.put("output","extend");
      return callApi(method, params);
    }

    /**
     * 获取问题
     * @param objectids
     * @return
     */
   public ZabbixAPIResult problemget(String objectids){
        String method = "problem.get";
       HashMap params = new HashMap();
       params.put("output",new String[]{"clock","ns","name","severity","eventid","r_clock","r_ns","userid","objectid","acknowledged"});//new String[]{objectids}
       if(StringUtils.isNotEmpty(objectids)){
           params.put("objectids",objectids);
       }
       return callApi(method, params);
    }

    public ZabbixAPIResult problem_get(String objectids){
        String method = "problem.get";
        HashMap params = new HashMap();
        params.put("output","extend");//new String[]{objectids}
        params.put("suppressed",false);
        params.put("source","3");
        ////System.out.println(">>>>>"+JSON.toJSONString(params));
        return callApi(method, params);
    }

    /**
     * 获取触发器事件
     * @param objectid
     * @return
     */
    public ZabbixAPIResult eventgetByObjectid(String objectid,HashMap<String, Object> filter,long time_from,long time_till){
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("filter",filter);
        if(time_from>0){
          params.put("time_from",time_from);
        }
      if(time_till>0){
        params.put("time_till",time_till);
      }
        params.put("output",new String[]{"severity","acknowledged","eventid","objectid","clock","ns","name"});
        params.put("objectids",objectid);
        params.put("select_acknowledges",new String[]{"userid","alias","name","message","clock"});
        params.put("selectHosts",new String[]{"hosts"});
        params.put("sortfield",new String[]{"clock"});
        params.put("only_true",true);
        params.put("sortorder","DESC");
        return callApi(method, params);
    }

  /**
   * 获取当前告警
   * @param list
   * @return
   */
    public ZabbixAPIResult currAlert(ArrayList<String> list){
      String method = "trigger.get";
      HashMap params = new HashMap();
      params.put("output",new String[]{"triggerids"});
      params.put("hostids",list);
      params.put("only_true",true);
      params.put("selectHosts",new String[]{"hostid"});
      return callApi(method, params);

    }

  /**
   * 查询事件
   * @param filter
   * @return
   */
    public ZabbixAPIResult eventgets(HashMap<String, Object> filter,String acknowledged,String severity,Long start,Long end,String subject){
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("filter",filter);
      if(start>0){
        params.put("time_from",start);
      }
      if(end>0){
        params.put("time_till",end);
      }
      HashMap map = new HashMap();
      if(StringUtils.isNotEmpty(subject)){
        map.put("name",subject);
      }
      if(StringUtils.isNotEmpty(severity)){
        params.put("severities",severity);
      }
      if(StringUtils.isNotEmpty(acknowledged)){
        params.put("acknowledged",acknowledged);
      }
      params.put("search",map);
      params.put("output","extend");
      params.put("selectHosts",new String[]{"hosts"});
      params.put("sortfield",new String[]{"clock"});
      params.put("select_acknowledges",new String[]{"userid","alias","name","message","clock"});
      params.put("limit","1000");
      params.put("searchByAny",true);
      params.put("sortorder","DESC");
      return callApi(method, params);
    }

    public ZabbixAPIResult eventgetlist(String eventids){
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("output","extend");
        params.put("eventids",eventids);
        params.put("select_acknowledges",new String[]{"userid","alias","name","message","clock"});
        params.put("selectHosts",new String[]{"hosts"});
        params.put("sortfield",new String[]{"clock"});
        return callApi(method, params);
    }
    public ZabbixAPIResult eventgethostid(String hostid){
        String method = "event.get";
        HashMap params = new HashMap();
        params.put("output","extend");
        params.put("hostids",hostid);
      params.put("select_acknowledges",new String[]{"userid","alias","name","message","clock"});
      params.put("selectHosts",new String[]{"hosts"});
        return callApi(method, params);
    }
    public ZabbixAPIResult userget(String userids){
        String method = "user.get";
        HashMap params = new HashMap();
        params.put("output",new String[]{"userid","name"});
        return callApi(method, params);
    }
    /**
     * 查询动作
     * @param actionids
     * @return
     */
    public ZabbixAPIResult actionget(String actionids){
        String method = "action.get";
        HashMap params = new HashMap();
        params.put("output","extend");
        params.put("actionids",actionids);
        return callApi(method, params);
    }


  public ZabbixAPIResult hostget(String trigid){
    String method = "host.get";
    HashMap params = new HashMap();
    ArrayList<String> list = new ArrayList<>();
    String[] host = new String[]{"hostid","host","name","status"};//指定需要输出的字段
    params.put("output",host);
    return callApi(method, params);
  }


  /**
   * 确认事件
   * @param eventids
   * @return
   */
  public ZabbixAPIResult eventacknowledge(String eventids,String msg,String act){
    String method = "event.acknowledge";
    HashMap params = new HashMap();
    params.put("eventids",eventids);
    params.put("message",msg);
    if("cl".equals(act)){
      params.put("action",7);
    }else{
      params.put("action",6);//1 - 关闭问题,2 - 确认事件;4 - 增加消息; 8 - 更改严重等级.(可以是任何值的组合，7就是前面三种组合起来的)
    }
    return callApi(method, params);
  }

  public ZabbixAPIResult historyget(){
    String method = "history.get";
    HashMap params = new HashMap();
    params.put("output","extend");
    params.put("sortorder","DESC");
    params.put("sortfield","clock");
    return callApi(method, params);
  }
  /**
   * 查询历史
   * @param itemids
   * @return
   */
  public ZabbixAPIResult historyget(String itemids){
    String method = "history.get";
    HashMap params = new HashMap();
    params.put("itemids",itemids);//"49359"
    params.put("output","extend");//new String[]{"itemid","value"}
    params.put("sortorder","DESC");
    params.put("sortfield","clock");
    return callApi(method, params);
  }
  /**
   * 获取通知类型
   * @param mediatypeids
   * @return
   */
  public ZabbixAPIResult mediatypeget(String mediatypeids){
    String method = "mediatype.get";
    HashMap params = new HashMap();
    params.put("output",new String[]{"description"});
    params.put("mediatypeid",mediatypeids);
    return callApi(method, params);
  }

  /**
   * 媒介类型
   * @return
   */
  public ZabbixAPIResult mediaTypeGet(){
    String method = "mediatype.get";
    HashMap params = new HashMap();
    params.put("output",new String[]{"description"});
    return callApi(method, params);
  }

  public ZabbixAPIResult eventgethist(String eventid){
    String method = "event.get";
    HashMap params = new HashMap();
    params.put("hostids",eventid);
      params.put("selectHosts",new String[]{"hosts"});
      params.put("sortfield",new String[]{"clock"});
      params.put("sortorder","DESC");
    return callApi(method, params);
  }

  /***************************用户群组、用户开始**************************************/

  private final static String USERGROUP_DISABLED_FRONTEND_NAME = "MonitorWay禁止访问前端用户组";
  private final static String USERGROUP_DEFAULT_NAME = "MonitorWay系统默认用户组";

  /**
   * 新增用户群组
   * @return
   */
  public ZabbixAPIResult usergroupCreate(String name,Integer debug_mode,Integer gui_access,Integer users_status){
    HashMap params = new HashMap();
    String method="usergroup.create";
    params.put("name",name);
    params.put("debug_mode",debug_mode);
    params.put("gui_access",gui_access);
    params.put("users_status",users_status);
    return callApi(method, params);
  }

  public ZabbixAPIResult usergroupDisabledFrontendCreate() {
    return usergroupCreate(USERGROUP_DISABLED_FRONTEND_NAME,0,3,0);
  }

  public ZabbixAPIResult usergroupDefaultCreate(){
    return usergroupCreate(USERGROUP_DEFAULT_NAME,0,0,0);
  }

  public ZabbixAPIResult usergroupGetByName(String name) {
    HashMap params = new HashMap();
    String method="usergroup.get";
    HashMap search = new HashMap();
    search.put("name",name);
    params.put("search",search);
    return callApi(method, params);
  }

  public ZabbixAPIResult usergroupGetDisabledFrontendByName() {
    return usergroupGetByName(USERGROUP_DISABLED_FRONTEND_NAME);
  }


  public ZabbixAPIResult usergroupGetDefaultByName() {
    return usergroupGetByName(USERGROUP_DEFAULT_NAME);
  }

  public ZabbixAPIResult userCreate(String alias,String name,String passwd,String usrgrpid){

    String method="user.create";

    Map params = new HashMap<>();
    params.put("alias",alias);
    params.put("passwd",passwd);
    params.put("name",name);
    params.put("lang","zh_CN");//默认中文
    List usrgrps = new ArrayList<>();
    Map usrgrp = new HashMap<>();

    usrgrp.put("usrgrpid",usrgrpid);
    params.put("usrgrps",usrgrps);
    usrgrps.add(usrgrp);

    return callApi(method, params);
  }


  public ZabbixAPIResult userUpdate(String userId,String name,String passwd,String usrgrpid){

    String method="user.update";

    Map params = new HashMap<>();
    params.put("userid",userId);
    if(Objects.nonNull(name)){
      params.put("name",name);
    }
    if(Objects.nonNull(passwd)){
      params.put("passwd",passwd);
    }
    if(Objects.nonNull(usrgrpid)){
      Map usrgrp = new HashMap<>();
      usrgrp.put("usrgrpid",usrgrpid);

      List usrgrps = new ArrayList<>();
      usrgrps.add(usrgrp);

      params.put("usrgrps",usrgrps);
    }
    return callApi(method, params);
  }

  /**
   * 删除用户
   * @param userIds
   * @return
     */
  public ZabbixAPIResult userDelete(List<String> userIds){

    String method="user.delete";

    return callApi(method, userIds);
  }

  /***************************用户群组、用户结束**************************************/


//--------------------主机接口----------------------------
  public ZabbixAPIResult hostPortByHostids(String hostid,ArrayList<String> itemKeyList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    ArrayList<String> list = new ArrayList<>();
    list.add("itemid");
    list.add("name");
    list.add("key_");
    list.add("lastvalue");
    list.add("description");



    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("key_", itemKeyList);
    param.put("search", search);

    param.put("output", list);
    param.put("hostids", hostid);
//  param.put("itemids", "41354");
    param.put("searchByAny",true);
//  param.put("output", "extend");



    return itemGet(param, filter);
  }

  /**
   * 统计监控主机 监控告警： 如：信息，一般，严重，的数量
   * @param hostids
   * @return
   */
  public ZabbixAPIResult alterByHostids(ArrayList<String> hostids) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    ArrayList<String> list = new ArrayList<>();
    list.add("hostid");
    list.add("triggerid");

    list.add("priority");
    list.add("description");


    param.put("hostids", hostids);
    param.put("sortfield", "priority");
    param.put("output", list);

    return triggerGet(param, filter);
  }
  //----------------------------------------------------------
  public ZabbixAPIResult triggerGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "trigger.get";
    param.put("filter", filter);

    return callApi(method, param);
  }

  /**
   * 监控项排名
   * @param itemKeyList
   * @return
   */
  public ZabbixAPIResult itemGetTop(ArrayList<String> itemKeyList) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    ArrayList<String> list = new ArrayList<>();
    list.add("hostid");
    list.add("name");
    list.add("key_");
    list.add("lastvalue");
    list.add("ip");


    ArrayList<String> list2 = new ArrayList<>();
    list2.add("host");
    ArrayList<String> list3 = new ArrayList<>();
    list3.add("ip");
    list3.add("port");
    list3.add("type");


    //指查询包含关系
    HashMap<String, Object> search = new HashMap();
    search.put("key_", itemKeyList);
    param.put("search", search);
    param.put("searchByAny",true);


    param.put("output", "extend");
    param.put("history", 0);
    param.put("selectHosts", list2);
    param.put("selectInterfaces", list3);

    return itemGet(param, filter);
  }

  public ZabbixAPIResult hostInterfaceGetByItemIds(ArrayList<String> itemidIdList) {
    String method = "hostinterface.get";

    HashMap<String, Object> param = new HashMap();
    param.put("output", "extend");
    param.put("hostids", itemidIdList);

    return callApi(method, param);
  }

  public ZabbixAPIResult graphGetByGraphId(String graphId) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    ArrayList<String> graphIds = new ArrayList<String>();
    graphIds.add(graphId);

    ArrayList<String> item = new ArrayList<String>();
    item.add("itemid");
    item.add("name");
    item.add("units");
    item.add("value_type");
    param.put("graphids", graphIds);
    param.put("output", "extend");
    param.put("selectItems", item);
    return graphGet(param, filter);
  }

  public ZabbixAPIResult graphGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "graph.get";
    param.put("filter", filter);
    return callApi(method, param);
  }

  public ZabbixAPIResult graphitemGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "graphitem.get";
    param.put("filter", filter);

    return callApi(method, param);
  }

  /**
   *获取某主机某监控项最近的数据
   * @param
   * @return
   */
  public ZabbixAPIResult getItemByHostAndKey(String item ) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();
    param.put("output", "extend");
    param.put("itemids", item);
    return itemGet(param, filter);
  }

  /**
   *
   * @param itemIds
   * @param valueType
   * @param beginTime
   * @param endTime
   * @return
   */
  public ZabbixAPIResult historyGetByItemId(ArrayList<String> itemIds,int valueType,long beginTime, long endTime) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();


    param.put("itemids", itemIds);
    param.put("output", "extend");
    param.put("sortfield", "clock");
    //param.put("sortorder", "DESC");
    param.put("sortorder", "ASC");
    param.put("history", valueType);

    param.put("time_from",beginTime);//查询开始时间
    param.put("time_till",endTime);//查询截止时间
    return historyGet(param, filter);
  }
  /***********************重写的方法************************************/
  public ZabbixAPIResult eventGetByHistory(AlterDto dto) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();
    if(StringUtils.isNotEmpty(dto.getAltype())){
      filter.put("value",1);
    }
    //排序
    ArrayList<String> sortfield = new ArrayList<>();
    sortfield.add("clock");
    sortfield.add("eventid");
    //事件列
    ArrayList<String> event = new ArrayList<>();
    event.add("eventid");
    event.add("value");
    event.add("objectid");
    event.add("clock");
    event.add("acknowledged");
    event.add("ns");
    event.add("name");
    event.add("r_eventid");
    event.add("userid");
    //事件列
    ArrayList<String> host = new ArrayList<>();
    host.add("hostid");
    host.add("proxy_hostid");
    host.add("host");
    host.add("acknowledged");
    host.add("ns");

    //查询条件
    if(!"".equals(dto.getHostid())&& null !=dto.getHostid()){
      param.put("hostids", dto.getHostid());

    }

    if(!"".equals(dto.getObjectids())&& null !=dto.getObjectids()){
      param.put("objectids", dto.getObjectids());

    }

    if(!"".equals(dto.getEventid()) && null !=dto.getEventid()){
      param.put("eventids", dto.getEventid());

    }

    //
    long endTime = new Date().getTime()/1000;
    long beginTime = curTimeMinusN(1440)/1000;


    param.put("output", "extend");

    param.put("selectHosts", "extend");
    param.put("select_alerts", "extend");
    param.put("select_acknowledges", "extend");
    param.put("selectTags", "extend");
    param.put("time_from",beginTime );
    param.put("time_till", endTime);
//    param.put("limit", 1000);
    param.put("sortfield", sortfield);
    param.put("sortorder", "desc");
    return eventGet(param, filter);
  }

  private static Long curTimeMinusN(int m){
    Date date=new Date();
    Date date1=new Date(date.getTime()-m*60*1000);
    return date1.getTime();
  }
  /**
   * 活动告警信息
   * @param eventid
   * @return
   */
  public ZabbixAPIResult alertGetByEventid(String eventid) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("eventids", eventid);
    param.put("output", "extend");
    return alertGet(param, filter);
  }
  /**
   * 得到事件详情
   * @param eventid
   * @return
   */
  public ZabbixAPIResult eventGetByEventid(String eventid) {
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
   * @param triggerid
   * @return
   */
  public ZabbixAPIResult triggerGetByTriggerid(String triggerid) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("triggerids", triggerid);
    param.put("output", "extend");
    return triggerGet(param, filter);
  }
  /**
   * 根据事件ID得到触发器
   * @param
   * @return
   */
  public ZabbixAPIResult itemGetByTriggerid(String triggerid) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("triggerids", triggerid);
    param.put("output", "extend");
    return itemGet(param, filter);
  }

  /**
   * 得到当前告警的事件
   * @param triggers
   * @return
   */
  public ZabbixAPIResult eventGettByTriggers(ArrayList<String> triggers,AlterDto dto) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();
    if(StringUtils.isNotEmpty(dto.getSeverity())){
      filter.put("severity",dto.getSeverity());
    }
    if(null != dto.getStartTime() && null != dto.getEndTime()){
      filter.put("time_from",dto.getStartTime());
      filter.put("time_till",dto.getEndTime());
    }
    if(StringUtils.isNotEmpty(dto.getSubject())){
      filter.put("name",dto.getSubject());
    }
    if(StringUtils.isNotEmpty(dto.getAcknowledged())){
      filter.put("acknowledged",dto.getAcknowledged());
    }
    ArrayList<String> event = new ArrayList<>();
    event.add("eventid");
    event.add("value");
    event.add("objectid");
    event.add("clock");
    event.add("acknowledged");
    event.add("ns");
    event.add("name");
    event.add("r_eventid");
    event.add("userid");
    //排序
    ArrayList<String> sortfield = new ArrayList<>();
    sortfield.add("clock");
    sortfield.add("eventid");
      filter.put("value",1);

    param.put("objectids", triggers);

    param.put("output", "extend");
    param.put("selectHosts", "extend");
    param.put("select_alerts", "extend");
    param.put("select_acknowledges", "extend");
    param.put("selectTags", "extend");
//    param.put("limt", 1000);
    param.put("sortfield", sortfield);
    param.put("sortorder", "desc");

    return eventGet(param, filter);
  }



  /**
   * 得到当前告警的事件2
   * @param clocks
   * @return
   */
  public ZabbixAPIResult eventGettByTriggers2(ArrayList<String> clocks,AlterDto dto) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();
    if(StringUtils.isNotEmpty(dto.getSeverity())){
      filter.put("severity",dto.getSeverity());
    }
    if(null != dto.getStartTime() && null != dto.getEndTime()){
      filter.put("time_from",dto.getStartTime());
      filter.put("time_till",dto.getEndTime());
    }
    if(StringUtils.isNotEmpty(dto.getSubject())){
      filter.put("name",dto.getSubject());
    }
    if(StringUtils.isNotEmpty(dto.getAcknowledged())){
      filter.put("acknowledged",dto.getAcknowledged());
    }
    ArrayList<String> event = new ArrayList<>();
    event.add("eventid");
    event.add("value");
    event.add("objectid");
    event.add("clock");
    event.add("acknowledged");
    event.add("ns");
    event.add("name");
    event.add("r_eventid");
    event.add("userid");
    //排序
    ArrayList<String> sortfield = new ArrayList<>();
    sortfield.add("clock");
    sortfield.add("eventid");
//    filter.put("value",1);

//    param.put("objectids", triggers);
    filter.put("clock", clocks);

    param.put("output", "extend");
    param.put("selectHosts", "extend");
    param.put("select_alerts", "extend");
    param.put("select_acknowledges", "extend");
    param.put("selectTags", "extend");
//    param.put("limt", 1000);
    param.put("sortfield", sortfield);
    param.put("sortorder", "desc");

    return eventGet(param, filter);
  }

  /**
   * 当前告警
   * @return
   */
  public ZabbixAPIResult alertGetByCurrent(Long beginTime,Long endTime) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();
    filter.put("value",1);
    filter.put("status",0);


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
//    param.put("lastChangeSince",beginTime);//查询开始时间
//    param.put("lastChangeTill",endTime);//查询截止时间

    return triggerGet(param, filter);
  }

  public ZabbixAPIResult eventGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "event.get";
    param.put("filter", filter);
    return callApi(method, param);
  }

  public ZabbixAPIResult alertGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "alert.get";
    param.put("filter", filter);
    return callApi(method, param);
  }

  /**
   * 查询代理
   * @param dto
   * @return
   */
  public ZabbixAPIResult getProxy(ProxyDto dto){
    String method = "proxy.get";
    HashMap params = new HashMap();
    if(StringUtils.isNotEmpty(dto.getProxyid())){
      params.put("proxyids",dto.getProxyid());
    }
    if(StringUtils.isNotEmpty(dto.getHost())){
      HashMap parm = new HashMap();
      parm.put("search",dto.getHost());
      params.put("key_",parm);
    }
    params.put("output", new String[]{"host","status","lastaccess","proxyid","description"});
    params.put("selectInterface", new String[]{"hostid","interfaceid","useip","ip","port","dns"});
    return callApi(method, params);
  }

  /**
   * 添加代理
   * @param dto
   * @return
   */
  public ZabbixAPIResult addProxy(ProxyDto dto){
    String method = "proxy.create";
    HashMap params = new HashMap();
    params.put("host",dto.getHost());
    String status = dto.getStatus();
    params.put("status",status);
    if("6".equals(status)){
      HashMap parm = new HashMap();
      parm.put("ip",dto.getIp());
      parm.put("dns",dto.getDns());
      parm.put("useip",dto.getUseip());
      parm.put("port",dto.getPort());
      params.put("interface",parm);
    }
    params.put("description",dto.getDescription());
    return callApi(method, params);
  }

  /**
   * 删除代理
   * @param list
   * @return
   */
  public ZabbixAPIResult delProxy(ArrayList<String> list ){
    String method = "proxy.delete";
    return callApi(method, list);
  }

  /**
   * 修改代理信息
   * @param dto
   * @return
   */
  public ZabbixAPIResult updateProxy(ProxyDto dto){
    String method = "proxy.update";
    String status = dto.getStatus();
    HashMap params = new HashMap();
    params.put("host",dto.getHost());
    params.put("proxyid",dto.getProxyid());
    params.put("status",status);
    if("6".equals(status)){
      HashMap parm = new HashMap();
      parm.put("dns",dto.getDns());
      parm.put("ip",dto.getIp());
      parm.put("useip",dto.getUseip());
      parm.put("port",dto.getPort());
      params.put("interface",parm);
    }
    return callApi(method, params);
  }


  //-------------------------------------------history.get-----------------------------------------------

  public ZabbixAPIResult historyGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "history.get";
    param.put("filter", filter);

    return callApi(method, param);
  }


  public ZabbixAPIResult getApplicationByHostidAndName(ArrayList<String> hostids,String appName) {
    HashMap<String, Object> param = new HashMap();
    HashMap<String, Object> filter = new HashMap();

    param.put("output", "extend");
    ArrayList hostout = new ArrayList();
    hostout.add("host");
    param.put("selectHost", hostout);
    ArrayList itemout = new ArrayList();
    itemout.add("itemid");
    itemout.add("hostid");
    itemout.add("name");
    itemout.add("units");
    itemout.add("lastvalue");
    itemout.add("description");
    param.put("selectItems", itemout);
    param.put("hostids", hostids);
    filter.put("name", appName);

    return applicationGet(param, filter);
  }
  public ZabbixAPIResult applicationGet(HashMap<String, Object> param, HashMap<String, Object> filter) {
    String method = "application.get";
    param.put("filter", filter);

    return callApi(method, param);
  }



  /**
   * 创建触发器
   * @return
   */
  public ZabbixAPIResult triggerCreate(String description,String expression,String priority,String recovery_expression) {

    String method = "trigger.create";
    HashMap params = new HashMap();

    params.put("description",description);
    params.put("expression",expression);
    params.put("priority",priority);
    params.put("recovery_expression",recovery_expression);
    params.put("manual_close","1");
//    params.put("description",);

    return callApi(method, params);
  }

  public ZabbixAPIResult triggerCreate(String description,String expression,String priority) {

    String method = "trigger.create";
    HashMap params = new HashMap();

    params.put("description",description);
    params.put("expression",expression);
    params.put("priority",priority);
    params.put("manual_close","1");
//    params.put("description",);

    return callApi(method, params);
  }

  public ZabbixAPIResult triggerDelete(List<String> triggerIds){
    String method = "trigger.delete";
    return callApi(method, triggerIds);
  }

  public ZabbixAPIResult valuemapid(List<String> valuemapids){
    String method = "valuemap.get";
    HashMap params = new HashMap();
    params.put("selectMappings", "extend");
    params.put("valuemapids",valuemapids);
    return callApi(method, params);
  }
}
