package cn.mw.monitor.service.rule;

import org.apache.commons.collections.map.HashedMap;

import java.util.*;
import java.util.regex.Pattern;


public class MessageContext {
    private Map<String, String> keyMap = new HashedMap();
    private List<Pattern> alertMessageContextPatterns = new ArrayList<>();
    private List<MessageRuleOpType> relationList = new ArrayList<>();
    private List<MessageRuleOpType> groupRelationList = new ArrayList<>();
    private Map<String, List<String>> dropDownMap = new HashedMap();

    public void addDropDown(String key ,List<String> valueList){
        this.dropDownMap.put(key, valueList);
    }

    public List<String> getDropDownList(String key) {
        return this.dropDownMap.get(key);
    }

    public List<MessageRuleOpType> getRelationList(boolean isGroup) {
        if(isGroup){
            return groupRelationList;
        }
        return relationList;
    }

    public void addRelation(MessageRuleOpType relation){
        switch (relation){
            case and:
            case or:
                groupRelationList.add(relation);
                break;
            default:
                relationList.add(relation);
        }
    }

    public void addPattern(String patternStr){
        Pattern pattern = Pattern.compile(patternStr);
        alertMessageContextPatterns.add(pattern);
    }

    public void addKeyMap(String key, String value){
        keyMap.put(key, value);
    }

    public Map<String, String> getKeyMap() {
        return keyMap;
    }

    public List<Pattern> getAlertMessageContextPatterns() {
        return alertMessageContextPatterns;
    }
}
