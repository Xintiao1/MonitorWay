package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleStartWith extends AbstractMessageRule {

    public RuleStartWith(){
        super("startWith");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        if(StringUtils.isNotEmpty(content) && 0 == content.indexOf(value)){
            ret = true;
        }
        if(debug){
            log.info("key:{},content:{} startWith {}" ,key ,content ,value);
        }

        return ret;
    }
}
