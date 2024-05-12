package cn.mw.monitor.service.rule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleSetContain extends RuleSet{
    String[] keyStrs = null;

    public RuleSetContain(){
        super("setContain");
    }
    @Override
    public boolean validate(MessageEvent messageEvent) {
        doInit();
        if(null != keyStrs){
            StringBuffer stringBuffer = new StringBuffer();
            try{
                for(String key : keyStrs){
                    Object value = messageEvent.getValue(key);
                    if(null == value){
                        break;
                    }
                    stringBuffer.append("-").append(value.toString());
                }

                String match = "null";
                if(stringBuffer.length() > 1) {
                    match = stringBuffer.substring(1);
                    if (valueSet.contains(match)) {
                        if (debug) {
                            log.info("RuleSetContain match key {}", match);
                        }
                        return true;
                    }
                }
                if(debug){
                    log.info("RuleSetContain not contain {}" ,match);
                }
            }catch (Exception e){
                log.error("RuleSetContain" ,e);
            }

        }

        return false;
    }

    private void doInit(){
        if(null == keyStrs){
            synchronized (this) {
                if (null == keyStrs) {
                    keyStrs = getKey().split(",");
                }
                if (debug) {
                    log.info("RuleSetContain size:{},keys:{}", keyStrs.length, getKey());
                }
            }
        }
    }
}
