package cn.mw.monitor.weixinapi;

import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcheFilter implements MessageFilter{
    MessageContext messageContext;
    MwRuleSelectParam dto;

    public MatcheFilter(MessageContext messageContext, MwRuleSelectParam dto) {
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
                String result = "^" + value + "$";
                String str = dto.getValue().substring(0,dto.getValue().length()-1);
                Pattern pattern = Pattern.compile(result,Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(str);
                return matcher.matches();
            }
            if(obj instanceof String){
                String result = obj.toString();
                String str = dto.getValue();
                Pattern pattern = Pattern.compile(str);
                Matcher matcher = pattern.matcher(result);
                return matcher.find();
            }else if(obj instanceof ArrayList){
                List<String> ruselts = (List<String>) obj;
                for(String s : ruselts){
                    String str = dto.getValue();
                    Pattern pattern = Pattern.compile(str);
                    Matcher matcher = pattern.matcher(s);
                    if(matcher.find()){
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
