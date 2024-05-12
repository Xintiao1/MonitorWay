package cn.mw.monitor.event;

import lombok.Data;


@Data
public class LoginFailEvent<T> extends Event<T> {
    private String loginName;

    public LoginFailEvent(String loginName){
        this.loginName = loginName;
    }
}