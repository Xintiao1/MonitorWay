package cn.mw.monitor.alert.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.alert.param.AddAndUpdateAlertRuleParam;
import cn.mw.monitor.alert.param.MwAlertRuleParam;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/1 9:55
 */
public interface MWAlertRuleService {

    Reply insertRule(AddAndUpdateAlertRuleParam param);

    Reply selectRuleList(MwAlertRuleParam mwAlertRuleParam);

    Reply fuzzSeach(MwAlertRuleParam mwAlertRuleParam);

    Reply editorRule(AddAndUpdateAlertRuleParam param);

    Reply deleteRule(List<MwAlertRuleParam> param);

    Reply getActionType();

    Reply getRuleListByActionTypeIds(List<Integer> actionTypeIds);

    Reply selectRuleById(String ruleId);

    Reply sendTest(AddAndUpdateAlertRuleParam param);
}
