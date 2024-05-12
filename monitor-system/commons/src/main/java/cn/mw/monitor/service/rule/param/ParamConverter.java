package cn.mw.monitor.service.rule.param;

import cn.mw.monitor.service.rule.RuleManager;

public interface ParamConverter<T> {
    static final String GLOBAl_TYPE = "global";
    String getId();
    RuleManager convert(T param);
    String genRuleKey(T param, String model);
}
