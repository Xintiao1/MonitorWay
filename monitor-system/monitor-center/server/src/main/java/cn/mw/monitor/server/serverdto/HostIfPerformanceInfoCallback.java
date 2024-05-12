package cn.mw.monitor.server.serverdto;

import cn.mw.monitor.service.server.api.dto.HostIfPerformanceInfo;
import com.fasterxml.jackson.databind.JsonNode;

public interface HostIfPerformanceInfoCallback {
    void callback(HostIfPerformanceInfo hostIfPerformanceInfo , JsonNode item);
}
