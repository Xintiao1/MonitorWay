package cn.mwpaas.common.utils;

import java.util.*;

/**
 * @author phzhou
 * @ClassName MapUtils
 * @CreateDate 2019/2/22
 * @Description
 */
public class MapUtils {

    /**
     * 使用Map按key进行排序
     *
     * @param map
     * @param flag true:升序,flase:降序
     * @return
     */
    public static <T> Map<String, T> sortMapByKey(Map<String, T> map, Boolean flag) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, T> sortMap;
        if (flag) {
            sortMap = new TreeMap<>();
        } else {
            sortMap = new TreeMap<>(Comparator.reverseOrder());
        }
        sortMap.putAll(map);

        return sortMap;
    }


    public static List getList(List<Map<String, Object>> iPdisByApplicant, String key) {
        List<Object> strings = new ArrayList<>();
        for (Map<String, Object> map : iPdisByApplicant) {
            Object s = map.get(key);
            strings.add(s);
        }

        return strings;
    }

    public static List<Map<String, Object>> getTree(List<Map<String, Object>> ipdistri, String o, String key, String parentKey) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Map<String, Object> s : ipdistri) {
            if (s.get(key).toString().equals(o)) {
                Map<String, Object> map = new HashMap<>();
                map = s;
                map.put("childrens", getChildren(ipdistri, parentKey, s.get(key).toString(), key));
                maps.add(map);
            } else {
            /*    Map<String, Object> map = new HashMap<>();
                map = s;
                map.put("childrens", getChildren(ipdistri, parentKey, s.get(key).toString(), key));
                maps.add(map);*/
            }

        }
        return maps;
    }

    private static List<Map<String, Object>> getChildren(List<Map<String, Object>> ipdistri, String parentKey, String o, String key) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Map<String, Object> s : ipdistri) {
            if (s.get(parentKey).toString().equals(o) && !s.get(key).toString().equals(o)) {
                Map<String, Object> map = new HashMap<>();
                map = s;
                map.put("childrens", getChildren(ipdistri, parentKey, s.get(key).toString(), key));
                maps.add(map);
            }
        }
        return maps;
    }


    public static Map<String, List<Map<String, Object>>> groupBykey(String ipgroup_id, List<Map<String, Object>> ipdistri) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        for (Map<String, Object> m : ipdistri) {
            if (map.get(m.get(ipgroup_id).toString()) == null || map.get(m.get(ipgroup_id).toString()).equals("")) {
                List<Map<String, Object>> group = new ArrayList<>();
                group.add(m);
                map.put(m.get(ipgroup_id).toString(), group);
            } else {
                List<Map<String, Object>> group = map.get(m.get(ipgroup_id).toString());
                group.add(m);
                map.put(m.get(ipgroup_id).toString(), group);
            }
        }
        return map;
    }

}