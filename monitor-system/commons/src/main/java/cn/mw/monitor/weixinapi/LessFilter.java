package cn.mw.monitor.weixinapi;

import cn.mwpaas.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LessFilter implements MessageFilter{
    MessageContext messageContext;
    MwRuleSelectParam dto;

    public LessFilter(MessageContext messageContext, MwRuleSelectParam dto) {
        this.messageContext = messageContext;
        this.dto = dto;
    }

    @Override
    public boolean filter(MessageContext messageContext) {
        try{
            String name = dto.getName();
            if(dto.getName().contains("指标")){
                name = name.substring(0,2) + "-" + name.substring(name.lastIndexOf("-") + 1, name.lastIndexOf(","));
            }
            Object obj = messageContext.getKey(name);
            if(obj == null || obj == ""){
                return false;
            }
            if(name.contains("指标")){
                Map<String,String> itemMap = (Map<String,String>)obj;
                String itemName = dto.getName().substring(dto.getName().lastIndexOf(",")+1);
                String value = itemMap.get(itemName);
                if(StringUtils.isEmpty(value)){
                    return false;
                }
                String result = dto.getValue().substring(0,dto.getValue().length()-1);
                return Double.parseDouble(value) < Double.parseDouble(result);
            }
            if(obj instanceof String){
                String s = (String) obj;
                return Double.parseDouble(s.substring(0, s.length() - 1)) < Double.parseDouble(dto.getValue());
            }else if(obj instanceof ArrayList){
                List<String> ruselt = (List<String>) obj;
                for(String s : ruselt){
                    if(Double.parseDouble(s.substring(0,s.length()-1)) < Double.parseDouble(dto.getValue())){
                        return true;
                    }
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
}
