package cn.mw.monitor.weixinapi;

import java.util.HashMap;

//预处理告警信息上下文
public class MessageContext {
    private boolean isCommon;

    private HashMap<String,Object> map;

    public boolean isCommon() {
        return isCommon;
    }

    public void setCommon(boolean common) {
        isCommon = common;
    }

    public Object getKey(String key){
        return map.get(key);
    }

    public void addKey(String key ,Object value){
        if(null == map){
            map = new HashMap<>();
        }
        map.put(key ,value);
    }

    public void setKey(HashMap<String,Object> map){
        this.map = map;
    }
}
