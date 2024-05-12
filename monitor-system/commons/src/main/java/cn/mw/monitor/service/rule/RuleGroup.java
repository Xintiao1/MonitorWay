package cn.mw.monitor.service.rule;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleGroup extends AbstractMessageRule{
    protected List<MessageRule> messageProcessorList;

    public List<MessageRule> getMessageProcessorList() {
        return messageProcessorList;
    }

    public RuleGroup(String type){
        super(type);
        messageProcessorList = new ArrayList<>();
    }

    public void addMessageRule(MessageRule messageRule){
        messageProcessorList.add(messageRule);
    }

    @Override
    public void setDebug(boolean debug) {
        super.setDebug(debug);
        for(MessageRule messageRule : messageProcessorList){
            messageRule.setDebug(debug);
        }
    }
}
