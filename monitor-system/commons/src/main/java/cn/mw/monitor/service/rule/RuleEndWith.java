package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleEndWith extends AbstractMessageRule {

    public RuleEndWith(){
        super("endWith");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        int lastIndex = content.lastIndexOf(value);
        if(StringUtils.isNotEmpty(content) && value.length() == (content.length() - lastIndex)){
            ret = true;
        }
        if(debug){
            log.info("key:{},content:{} endWith {}" ,key ,content ,value);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "RuleEndWith{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}';
    }
}
