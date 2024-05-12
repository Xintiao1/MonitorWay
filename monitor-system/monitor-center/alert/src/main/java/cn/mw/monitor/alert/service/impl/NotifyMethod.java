package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.weixinapi.MessageContext;

import java.util.Set;

public interface NotifyMethod{
    Set<Integer> getUserIds(MessageContext messageContext);
}
