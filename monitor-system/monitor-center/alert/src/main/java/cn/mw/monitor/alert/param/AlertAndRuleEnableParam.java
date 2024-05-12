package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2020/8/13 16:33
 */
@Data
public class AlertAndRuleEnableParam {
    private String ruleId;
    private String actionId;
    private Boolean enable;
    private Boolean isLogo;
}
