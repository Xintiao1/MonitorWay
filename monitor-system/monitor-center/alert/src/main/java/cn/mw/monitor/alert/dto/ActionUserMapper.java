package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/8/28 10:00
 */
@Data
@Builder
public class ActionUserMapper {
    private String actionId;
    private Integer userId;
}
