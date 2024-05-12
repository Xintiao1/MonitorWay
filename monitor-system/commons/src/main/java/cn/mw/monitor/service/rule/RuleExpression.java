package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RuleExpression extends AbstractMessageRule {

    public RuleExpression(){
        super("expression");
    }

    @Override
    public boolean validate(MessageEvent messageEvent) {
        boolean ret = false;
        String content = messageEvent.getValue(key);
        Pattern pattern = Pattern.compile(value);
        Matcher matcher = pattern.matcher(content);
        if(StringUtils.isNotEmpty(content) && matcher.find()){
            ret = true;
        }
        if(debug){
            log.info("key:{},content:{} expression {}" ,key ,content ,value);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "RuleExpression{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", debug=" + debug +
                '}';
    }
}
