package cn.mw.monitor.alert.service;

import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author xhy
 * @date 2020/8/26 9:17
 */
public interface MWAlertActionService {

    Reply deleteAction(List<AddAndUpdateAlertActionParam> list);

    Reply selectAction(AlertActionParam param);

    Reply getFielid();

    Reply addAction(MwRuleSelectListParam param);

    Reply updateAction(MwRuleSelectListParam param);

    Reply selectAction(String actionId);

    Reply getTag();

    Reply getAlertLevel();
}
