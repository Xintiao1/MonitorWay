package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.InstanceSyncParam;
import cn.mwpaas.common.model.Reply;

public interface MwPrometheusContainerService {
    Reply syncContainerDeviceInfo(InstanceSyncParam param) throws Exception;

    Reply getSelectDropPrometheus(Integer instanceId) throws Exception;
}
