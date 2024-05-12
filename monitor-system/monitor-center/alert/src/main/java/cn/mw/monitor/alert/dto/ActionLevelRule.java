package cn.mw.monitor.alert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/14 16:29
 */
@Data
@Builder
public class ActionLevelRule {
    private String actionId;
    private Integer state;
    private Integer userId;
    private Integer level;
    private float date;
    private float dateTwo;
    private float dateThree;
    private String ruleId;
    private Integer timeUnit;//时间单位
    //1:默认选择；2：自定义
    private Integer isAllUser;
    private String email;
    private Boolean isSendPerson;
}
