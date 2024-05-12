package cn.mw.monitor.weixin.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ActionRule {

    private Integer id;
    private String actionId;
    private String ruleId;

    private Integer actionType;
}
