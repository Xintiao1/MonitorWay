package cn.mw.monitor.service.rule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleOr extends RuleGroup {
    public RuleOr(){
        super("or");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        if(debug){
            log.info("RuleOr messageProcessorList size:{}" ,messageProcessorList.size());
        }
        boolean ret = false;
        for(MessageRule processor : messageProcessorList){
            if(processor.validate(messageEvent)){
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("RuleOr{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}');

        for(MessageRule processor : messageProcessorList){
            sb.append("\r\n").append(processor.toString());
        }

        return sb.toString();
    }
}
