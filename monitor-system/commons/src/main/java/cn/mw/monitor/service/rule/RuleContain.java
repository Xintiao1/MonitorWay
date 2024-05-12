package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleContain extends AbstractMessageRule {
    public RuleContain(){
        super("contain");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        if(StringUtils.isNotEmpty(content) && content.indexOf(value) >= 0){
            ret = true;
        }
        if(debug){
            log.info("key:{},content:{} contain {}" ,key ,content ,value);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "RuleContain{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}';
    }
}
