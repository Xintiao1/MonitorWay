package cn.mw.monitor.service.rule;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class RuleManager {
    private String id;
    private List<RuleProcessor> ruleProcessors = new ArrayList<>();
    private transient Map<String, RuleProcessor> ruleProcessorMap;

    public void initRuleProcessorMap(){
        ruleProcessorMap = new HashedMap();
        for(RuleProcessor ruleProcessor : ruleProcessors){
            ruleProcessorMap.put(ruleProcessor.getRuleName(), ruleProcessor);
        }
    }

    public RuleProcessor getRuleProcessor(){
        return ruleProcessors.get(0);
    }

    public RuleProcessor getRuleProcessor(String ruleName){
        return ruleProcessorMap.get(ruleName);
    }

    public void addRuleProcessor(RuleProcessor ruleProcessor){
        ruleProcessors.add(ruleProcessor);
    }

    public void setDebug(boolean debug){
        for(RuleProcessor ruleProcessor : ruleProcessors){
            ruleProcessor.setDebug(debug);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("RuleManager{" +
                "id='" + id + '\'' +
                '}');

        for(RuleProcessor ruleProcessor : ruleProcessors){
            sb.append("\r\n").append(ruleProcessor.toString());
        }
        return sb.toString();
    }
}
