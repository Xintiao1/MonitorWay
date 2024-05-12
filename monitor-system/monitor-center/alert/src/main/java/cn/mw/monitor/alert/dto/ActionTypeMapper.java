package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/14 16:29
 */
@Data
@Builder
public class ActionTypeMapper {
    private String actionId;
    private Integer actionTypeId;
}
