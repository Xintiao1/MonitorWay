package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/8/27 15:52
 */
@Data
@Builder
public class ActionSeverityMapper {
    private String actionId;
    private String severity;
}
