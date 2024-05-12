package cn.mw.monitor.ipaddressmanage.service.impl;

import java.util.Set;

public interface IcmpScanCallback {
    void callback(Set<String> onLineIps);
}
