package cn.mw.monitor.service.rule;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
/*
 * 或操作, value带数据共享功能
 */
@Slf4j
public class RuleOrSet extends RuleGroupSet{

    public RuleOrSet(){
        super("orSet");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        if(debug){
            log.info("RuleOrSet messageProcessorList size:{}" ,messageProcessorList.size());
        }
        boolean ret = false;
        doInit();
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
        StringBuffer sb = new StringBuffer("RuleOrSet{" +
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
