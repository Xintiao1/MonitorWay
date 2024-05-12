package cn.mw.monitor.event;

import cn.mw.monitor.service.user.listener.LoginContext;
import lombok.Data;

@Data
public class RefreshPermEvent<T> extends Event<T>{
    private LoginContext loginContext;
    private String loginName;
    private Integer userId;

    public RefreshPermEvent(LoginContext loginContext, String loginName, Integer userId){
        this.loginContext = loginContext;
        this.loginName = loginName;
        this.userId = userId;
    }
}
