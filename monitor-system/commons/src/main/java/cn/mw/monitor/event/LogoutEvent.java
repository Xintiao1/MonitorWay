package cn.mw.monitor.event;

import lombok.Data;

@Data
public class LogoutEvent<T> extends Event<T> {
    private String loginName;

    public LogoutEvent(String loginName){
        this.loginName = loginName;
    }
}
