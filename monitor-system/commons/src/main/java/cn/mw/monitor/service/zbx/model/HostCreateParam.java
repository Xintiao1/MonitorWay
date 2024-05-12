package cn.mw.monitor.service.zbx.model;

import com.google.common.base.Strings;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HostCreateParam {
    private Integer batchIndex;
    private int serverId;
    private String host;
    private String visibleName;
    private List<String> groups;
    private List<Map<String, Object>> interfaces;
    private List<String> templates;
    private List<Map> macros;
    private Integer status;
    private String proxyID;
    private String userName;
    private String passWord;

    public Map<String, Object> genZabbixParams(){
        Map map = new HashMap();
        String hostName = host.replaceAll("#","");
        map.put("host" ,hostName);
        map.put("name" ,visibleName);

        List groupParams = new ArrayList();
        groups.forEach(group -> {
            HashMap<String, String> templ = new HashMap();
            templ.put("groupid", group);
            groupParams.add(templ);
        });
        map.put("groups" ,groupParams);

        map.put("interfaces" ,interfaces);

        List temps = new ArrayList();
        templates.forEach(templateid -> {
            HashMap<String, String> templ = new HashMap();
            templ.put("templateid", templateid);
            temps.add(templ);
        });
        map.put("templates" ,temps);

        map.put("macros" ,macros);
        map.put("status" ,status);
        map.put("proxy_hostid" ,proxyID);
        if(!Strings.isNullOrEmpty(userName)){
            map.put("ipmi_username" ,userName);
        }if(!Strings.isNullOrEmpty(passWord)){
            map.put("ipmi_password" ,passWord);
        }
        return map;
    }
}
