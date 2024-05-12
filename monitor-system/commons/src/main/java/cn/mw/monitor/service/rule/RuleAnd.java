package cn.mw.monitor.service.rule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleAnd extends RuleGroup {
    public RuleAnd(){
        super("and");
    }
    @Override
    public boolean validate(MessageEvent messageEvent) {
        if(debug){
            log.info("RuleAnd messageProcessorList size:{}" ,messageProcessorList.size());
        }

        boolean ret = true;
        for(MessageRule processor : messageProcessorList){
            if(!processor.validate(messageEvent)){
                ret = false;
                break;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("RuleAnd{" +
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
