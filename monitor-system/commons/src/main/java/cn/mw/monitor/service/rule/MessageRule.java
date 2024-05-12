package cn.mw.monitor.service.rule;

public interface MessageRule {
    boolean validate(MessageEvent messageEvent);
    void setDebug(boolean debug);
}
