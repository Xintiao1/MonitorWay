package cn.mw.monitor.service.scan.model;

import java.util.List;

public class SeachIpContext {
    private List<String> ipList;
    private List<ProxyInfo> proxyInfoList;

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public List<ProxyInfo> getProxyInfoList() {
        return proxyInfoList;
    }

    public void setProxyInfoList(List<ProxyInfo> proxyInfoList) {
        this.proxyInfoList = proxyInfoList;
    }
}
