package cn.mw.monitor.service.rule;

import lombok.Data;

@Data
public class RuleProcessor implements MessageRule{
    private String ruleName;
    private MessageRule messageRule;


    @Override
    public boolean validate(MessageEvent messageEvent) {
        return messageRule.validate(messageEvent);
    }

    @Override
    public void setDebug(boolean debug) {
        messageRule.setDebug(debug);
    }

    @Override
    public String toString() {
        return "RuleProcessor{" +
                "ruleName='" + ruleName + '\'' +
                ", messageRule=" + messageRule.toString() +
                '}';
    }
}
