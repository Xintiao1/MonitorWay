package cn.mw.monitor.service.rule;

public abstract class AbstractMessageRule implements MessageRule {
    protected MessageRuleOpType type;
    protected String key;
    protected String value;
    protected boolean debug;

    AbstractMessageRule(String type){
        this.type = MessageRuleOpType.valueOf(type);
    }

    public MessageRuleOpType getType() {
        return type;
    }

    public void setType(MessageRuleOpType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
