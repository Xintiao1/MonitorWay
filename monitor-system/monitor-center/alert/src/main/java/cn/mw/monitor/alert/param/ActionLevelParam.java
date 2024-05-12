package cn.mw.monitor.alert.param;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/14 16:29
 */
@Data
public class ActionLevelParam {
    private String ruleId;
    private Integer timeUnit;
    private Boolean isSendPerson;

}
