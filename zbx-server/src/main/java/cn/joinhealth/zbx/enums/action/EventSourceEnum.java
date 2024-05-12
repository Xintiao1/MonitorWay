package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EventSourceEnum {
    TRIGGER(0,"触发器"),
    AUTO_DISCOVER(1,"自动发现"),
    AUTO_REGISTER(2,"自动注册"),
    EVENT_SOURCE(3,"事件源");

    private int code;
    private String name;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    EventSourceEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static EventSourceEnum getEventSourceEnum(int code){
        for (EventSourceEnum eventSourceEnum : EventSourceEnum.values()){
            if(eventSourceEnum.getCode() == code){
                return eventSourceEnum;
            }
        }
        return null;
    }

    public static List getEventSourceList(){
        List list = new ArrayList();
        for (EventSourceEnum eventSourceEnum : EventSourceEnum.values()){
            Map map = new HashMap();
            map.put("code",eventSourceEnum.getCode());
            map.put("name",eventSourceEnum.getName());
            list.add(map);
        }
        return list;
    }
}
