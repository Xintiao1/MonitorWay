package cn.mw.monitor.service.alert.dto;


import java.util.*;

public class MonitorServerNameUtil {

    public static Map<Integer,String> listMapConvertMap(List<Map> mapList){
        Map<Integer,String> result = new HashMap<>();
        for(Map map : mapList){
            result.put(Integer.parseInt(String.valueOf(map.get("id"))), String.valueOf(map.get("monitorServerName")));
        }
        return result;
    }

}
