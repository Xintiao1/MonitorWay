package cn.mw.monitor.service.rule;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class RuleGroupSet extends RuleGroup{
    protected String[] keyStrs = null;
    protected Set<String> valueSet = null;

    public RuleGroupSet(String type) {
        super(type);
    }

    public String[] getKeyStrs() {
        return keyStrs;
    }

    public Set<String> getValueSet() {
        return valueSet;
    }

    protected void doInit(){
        if(null == valueSet){
            synchronized (this){
                if(null == valueSet){
                    valueSet = new HashSet<>();
                    String[] values = getValue().split(",");
                    Collections.addAll(valueSet ,values);
                }
                if(debug){
                    log.info("RuleOrSet size:{},keys:{}" ,keyStrs.length ,getKey());
                }

                for(MessageRule processor : messageProcessorList) {
                    if(processor instanceof RuleSet){
                        RuleSet ruleSet = (RuleSet) processor;
                        ruleSet.initFromParent(this);
                    }
                }
            }
        }
    }
}
