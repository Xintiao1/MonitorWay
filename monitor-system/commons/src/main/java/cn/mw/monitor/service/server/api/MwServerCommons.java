package cn.mw.monitor.service.server.api;

import cn.mw.monitor.service.server.api.dto.HostPerformanceInfoDto;

import java.util.List;

/**
 * @author xhy
 * @date 2021/1/21 17:19
 */
public interface MwServerCommons {
    //item中英文转换
    String getChName(String itemName);
    HostPerformanceInfoDto getHostPerformanceInfo(int monitorServerId, String hostId);
    List<HostPerformanceInfoDto> getHostPerformanceInfo(int monitorServerId, List<String> hostIds, List<String> interfaceNames);

}
