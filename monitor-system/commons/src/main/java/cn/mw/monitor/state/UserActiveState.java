package cn.mw.monitor.state;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.user.exception.UserDisactiveException;
import cn.mw.monitor.service.user.listener.LoginContext;

import java.util.List;

public enum UserActiveState {

    ACTIVE(0,"ACTIVE"),
    DISACTIVE(1,"DISACTIVE");

    private int code;

    private String name;

    public static UserActiveState DEFAULT;

    static {
        DEFAULT = ACTIVE;
    }

    UserActiveState(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<Reply> process(LoginContext loginContext) {
        UserActiveState userActiveState = loginContext.getUserState();
        if (UserActiveState.DISACTIVE == userActiveState) {
            throw new UserDisactiveException(loginContext.getLoginName());
        }
        return null;
    }

}
