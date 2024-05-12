package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/8/28 12:10
 */
@Data
@Builder
public class ActionRuleMapper {
    private String ActionId;
    private String ruleId;
}
