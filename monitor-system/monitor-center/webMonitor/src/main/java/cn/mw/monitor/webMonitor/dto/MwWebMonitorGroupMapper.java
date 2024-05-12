package cn.mw.monitor.webMonitor.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
@Builder
public class MwWebMonitorGroupMapper {
    private Integer monitorId;

    private Integer groupId;
}
