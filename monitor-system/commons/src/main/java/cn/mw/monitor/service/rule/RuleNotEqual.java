package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleNotEqual extends AbstractMessageRule {

    public RuleNotEqual(){
        super("notEqual");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        if(StringUtils.isNotEmpty(content) && !content.equals(value)){
            ret = true;
        }

        if(debug){
            log.info("key:{},content:{} not equal {}" ,key ,content ,value);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "RuleNotEqual{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}';
    }
}
