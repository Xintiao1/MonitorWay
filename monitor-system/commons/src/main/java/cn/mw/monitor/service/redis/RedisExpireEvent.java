package cn.mw.monitor.service.redis;

import cn.mw.monitor.event.Event;

public class RedisExpireEvent extends Event {
    private String key;

    public RedisExpireEvent(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
