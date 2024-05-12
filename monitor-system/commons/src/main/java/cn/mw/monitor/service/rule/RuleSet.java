package cn.mw.monitor.service.rule;

import java.util.Set;

public abstract class RuleSet extends AbstractMessageRule{
    protected Set<String> valueSet = null;

    RuleSet(String type) {
        super(type);
    }

    public void initFromParent(RuleGroupSet ruleGroupSet) {
        this.valueSet = ruleGroupSet.getValueSet();
    }
}
