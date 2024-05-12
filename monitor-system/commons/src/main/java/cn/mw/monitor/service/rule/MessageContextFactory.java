package cn.mw.monitor.service.rule;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mwpaas.common.utils.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MessageContextFactory {
    private static Map<String, MessageContext> messageContextMap = new HashMap<>();

    synchronized public static MessageContext getMessageContext(String model){
        MessageContext messageContext = messageContextMap.get(model);
        if (null != messageContext){
            return messageContext;
        }

        messageContext = new MessageContext();
        Properties patternProperties = new Properties();
        Properties keyMapProperties = new Properties();
        Properties dropDownProperties = new Properties();

        String ruleDir = SpringUtils.getPropertiesValue("rule.dir");
        Resource resource = null;
        Resource resourceKeyMap = null;
        Resource resourceDropDown = null;
        if(StringUtils.isEmpty(ruleDir)) {
            resource = new ClassPathResource("rule" + File.separator + model + "RulePattern.properties");
            resourceKeyMap = new ClassPathResource("rule" + File.separator + model + "RuleKeyMap.properties");
            resourceDropDown = new ClassPathResource("rule" + File.separator + model + "RuleDropDown.properties");
        }else{
            resource = new PathResource(ruleDir + File.separator + model + "RulePattern.properties");
            resourceKeyMap = new PathResource(ruleDir + File.separator + model + "RuleKeyMap.properties");
            resourceDropDown = new PathResource(ruleDir + File.separator + model + "RuleDropDown.properties");
        }

        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
            patternProperties.load(bf);
            List<String> patterns = new ArrayList(patternProperties.values());
            for(String pattern : patterns){
                messageContext.addPattern(pattern);
            }

            BufferedReader bf1 = new BufferedReader(new InputStreamReader(resourceKeyMap.getInputStream(), "UTF-8"));
            keyMapProperties.load(bf1);
            for(String key :keyMapProperties.stringPropertyNames()){
                String value = keyMapProperties.getProperty(key);
                messageContext.addKeyMap(key, value);
            }

            BufferedReader bf2 = new BufferedReader(new InputStreamReader(resourceDropDown.getInputStream(), "UTF-8"));
            dropDownProperties.load(bf2);
            for(String keyStr :dropDownProperties.stringPropertyNames()){
                String value = dropDownProperties.getProperty(keyStr);
                String[] values = value.split(",");
                List<String> valueList = Arrays.asList(values);
                String[] keys = keyStr.split(",");
                for(String key : keys){
                    messageContext.addDropDown(key, valueList);
                }
            }

            MessageRuleOpType[] types = MessageRuleOpType.values();
            for(MessageRuleOpType type: types){
                messageContext.addRelation(type);
            }
            messageContextMap.put(model, messageContext);
        } catch (IOException ex) {
            throw new IllegalStateException("load resource exception" + resource, ex);
        }

        return messageContext;
    }

    /*
    public static void main(String[] args){
        MessageContextFactory messageContextFactory = new MessageContextFactory();
        messageContextFactory.getMessageContext("topo");
    }
     */
}
