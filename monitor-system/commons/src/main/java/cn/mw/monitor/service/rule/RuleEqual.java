package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuleEqual extends AbstractMessageRule {

    public RuleEqual(){
        super("equal");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        if(StringUtils.isNotEmpty(content)){
            String contentTrim = content.trim();
            String valueTrim = value.trim();
            if(contentTrim.equals(valueTrim)) {
                ret = true;
            }
        }
        if(debug){
            log.info("key:{},content:{} equal {}" ,key ,content ,value);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "RuleEqual{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}';
    }
}
