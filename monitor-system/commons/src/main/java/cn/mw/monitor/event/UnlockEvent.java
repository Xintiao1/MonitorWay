package cn.mw.monitor.event;

import lombok.Data;

@Data
public class UnlockEvent<T> extends Event<T> {
    private String loginName;

    public UnlockEvent(String loginName){
        this.loginName = loginName;
    }
}
