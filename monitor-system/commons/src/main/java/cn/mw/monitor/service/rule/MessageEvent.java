package cn.mw.monitor.service.rule;

import cn.mwpaas.common.utils.StringUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public abstract class MessageEvent {
    private MessageContext messageContext;
    private Map<String, String> messageMap = new HashMap<>();
    private Set<String> keyTransSet = new HashSet<>();

    public MessageEvent(String message, String model){
        messageContext = MessageContextFactory.getMessageContext(model);
        addMessageMap(message);
    }

    public MessageEvent(String model){
        messageContext = MessageContextFactory.getMessageContext(model);
    }

    public void addMessageMap(String message){
        String mesValue = message.replaceAll("\\r|\\n" ,"");
        for(Pattern alertMessageContextPattern: messageContext.getAlertMessageContextPatterns()) {
            Matcher matcher = alertMessageContextPattern.matcher(mesValue);
            if (matcher.find()) {
                for (int i = 1; i < matcher.groupCount(); i = i + 2) {
                    String key = matcher.group(i);
                    String keyTrans = messageContext.getKeyMap().get(key);
                    String value = matcher.group(i + 1);
                    if (StringUtils.isNotEmpty(keyTrans) && StringUtils.isNotEmpty(value)) {
                        messageMap.put(keyTrans, value);
                    }else{
                        messageMap.put(key, value);
                    }
                }
                break;
            }
        }
    }

    public String getValue(String key){
        if(!messageContext.getKeyMap().keySet().contains(key)){
            return messageMap.get(key);
        }
        String keyTrans = messageContext.getKeyMap().get(key);
        return messageMap.get(keyTrans);
    }

    public void setValue(String key ,String value){
        messageMap.put(key ,value);
    }

}
