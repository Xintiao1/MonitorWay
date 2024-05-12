package cn.mw.monitor.alert.param;

import cn.mw.monitor.service.action.param.UserIdsType;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.List;

/**
 * @author xhy
 * @date 2020/9/14 16:29
 */
@Data
public class ActionLevelRuleParam {
    private String actionId;
    private Integer state;
    private Integer userId;
    private Integer selectLevel;
    private float date;
    private float dateTwo;
    private float dateThree;
    private String eventId;
    private String ruleId;
    private Integer level;
    //1:默认选择；2：自定义
    private Integer isAllUser;
    private String email;
    private Boolean isActionLevel;
    private HashSet<Integer> emailCC;
    private HashSet<Integer> groupUserIds;
    private HashSet<Integer> orgUserIds;
    private UserIdsType userIdsType;
    private List<Integer> emailUserIds;
    private List<Integer> emailGroupUserIds;
}
