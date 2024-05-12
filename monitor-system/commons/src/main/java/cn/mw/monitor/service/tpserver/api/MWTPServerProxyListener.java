package cn.mw.monitor.service.tpserver.api;

import cn.mw.monitor.service.tpserver.model.ProxyServerInfo;
import cn.mw.monitor.service.tpserver.model.TPResult;

public interface MWTPServerProxyListener {
    TPResult refreshServers();
    boolean check(ProxyServerInfo proxyServerInfo);
}
