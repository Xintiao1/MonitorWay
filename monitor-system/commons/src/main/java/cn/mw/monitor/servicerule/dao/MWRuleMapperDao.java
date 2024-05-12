package cn.mw.monitor.servicerule.dao;

import cn.mw.monitor.service.rule.param.RuleDBParam;

public interface MWRuleMapperDao {
    int existById(String id);
    void saveRule(RuleDBParam ruleDBParam);
    RuleDBParam selectById(String id);
    void updateRule(RuleDBParam ruleDBParam);
    void deleteById(String id);
}
