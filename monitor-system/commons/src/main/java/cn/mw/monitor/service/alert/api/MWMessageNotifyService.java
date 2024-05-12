package cn.mw.monitor.service.alert.api;

import cn.mw.monitor.weixinapi.MessageContext;

public interface MWMessageNotifyService {
    public static final String IPMANAGE_MESSAGE_RULE_KEY = "IP地址管理";
    void sendMessage(MessageContext messageContext);
}
