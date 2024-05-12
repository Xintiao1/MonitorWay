package cn.mw.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginRegistContext {
    public static final String RequireKey = "require";
    private Map<String, Object> contextMap = new HashMap<>();

    public Object getValue(String key){
        return contextMap.get(key);
    }

    public void addValue(String key ,Object value){
        contextMap.put(key ,value);
    }
}
