package cn.mw.monitor.ipaddressmanage.dto;

import cn.mw.monitor.ipaddressmanage.model.IPConflictView;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class IpConfictDTO {
    private List<IPConflictView> itemNameRankList;
    private List<String> titleNode;
    private List<Map<String,String>> title;

    public void initTitle(){
        titleNode = new ArrayList<>();
        titleNode.add("IP地址");
        titleNode.add("MAC1");
        titleNode.add("MAC2");

        title = new ArrayList<>();
        Map<String, String> ipMap = new HashMap<>();
        ipMap.put("prop" ,"ip");
        ipMap.put("name" ,"IP地址");
        title.add(ipMap);

        Map<String, String> mac1Map = new HashMap<>();
        mac1Map.put("prop" ,"mac1");
        mac1Map.put("name" ,"MAC1");
        title.add(mac1Map);

        Map<String, String> mac2Map = new HashMap<>();
        mac2Map.put("prop" ,"mac2");
        mac2Map.put("name" ,"MAC2");
        title.add(mac2Map);
    }
}
