package cn.mw.monitor.user.service;

import cn.mw.monitor.user.advcontrol.RequestInfo;

public interface IUserControlService {
    public static final String REQINFO = "REQINFO";
    boolean check(RequestInfo requestInfo);
}
