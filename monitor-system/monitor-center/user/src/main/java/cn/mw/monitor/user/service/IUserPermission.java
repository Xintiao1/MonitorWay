package cn.mw.monitor.user.service;

import cn.mw.monitor.event.Event;

public interface IUserPermission {
    boolean ispermitted(Event event);
}
