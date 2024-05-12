package cn.mw.monitor.event;

import cn.mw.monitor.service.user.param.LoginParam;
import lombok.Data;

@Data
public class LoginBeforeEvent<T> extends Event<T> {
    private LoginParam loginParam;

    public LoginBeforeEvent(LoginParam loginParam){
        this.loginParam = loginParam;
    }
}
