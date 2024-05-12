package cn.mw.monitor.weixinapi;

import cn.mw.monitor.weixinapi.MessageContext;

public interface MessageFilter {
    boolean filter(MessageContext messageContext);
}
