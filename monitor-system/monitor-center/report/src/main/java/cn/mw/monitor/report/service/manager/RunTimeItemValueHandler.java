package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;

import java.util.List;
import java.util.Map;

public interface RunTimeItemValueHandler {
    void handle(List<HostGroup> hostGroups, Map<String, RunTimeItemValue> runTimeItemValueMap, Map<String, String> itemHostMap);
}
