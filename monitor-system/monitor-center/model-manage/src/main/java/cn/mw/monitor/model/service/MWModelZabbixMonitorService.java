package cn.mw.monitor.model.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/5/31
 */
public interface MWModelZabbixMonitorService {
    Reply queryMonitorServerInfo();

    Reply syncAssetsDetailsToEs();

    Reply updateInterfaceStatus();
}
