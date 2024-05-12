package cn.mw.monitor.weixinapi;
import cn.mw.monitor.util.RelationEnum;
import cn.mw.monitor.util.RelationTypeEnum;

import java.util.List;



/**
 * @author
 * @createTime 202110/2929 10:56
 * @description
 */

public class DelFilter {
    public static Boolean delFilter(List<MwRuleSelectParam> ruleSelectParams, MessageContext messageContext, List<MwRuleSelectParam> ruleSelectList){
        // List<MessageFilter> filterList = new ArrayList<>();
        Boolean result = false;
        for(MwRuleSelectParam s : ruleSelectParams){
            if(s.getKey().equals("empty_null")){
                continue;
            }
            if(s.getName() == null || "".equals(s.getName())){
                result = true;
            }else{
                RelationEnum type =  RelationEnum.valueOf(RelationTypeEnum.getTypeByChName(s.getRelation()));
                switch (type){
                    case contain:
                        ContainFilter containFilter = new ContainFilter(messageContext,s);
                        result = containFilter.filter(messageContext);
                        break;
                    case notContain:
                        ContainFilter notContain = new ContainFilter(messageContext,s);
                        result = !notContain.filter(messageContext);
                        break;
                    case equal:
                        EqualFilter equalFiltere = new EqualFilter(messageContext,s);
                        result = equalFiltere.filter(messageContext);
                        break;
                    case notEqual:
                        EqualFilter notEqual = new EqualFilter(messageContext,s);
                        result = !notEqual.filter(messageContext);
                        break;
                    case startWith:
                        StartFilter startFilter = new StartFilter(messageContext,s);
                        result = startFilter.filter(messageContext);
                        break;
                    case endWith:
                        EndFilter endFilter = new EndFilter(messageContext,s);
                        result = endFilter.filter(messageContext);
                        break;
                    case expression:
                        MatcheFilter matcheFilter = new MatcheFilter(messageContext,s);
                        result = matcheFilter.filter(messageContext);
                        break;
                    case greater:
                        GreaterFilter greaterFilter = new GreaterFilter(messageContext,s);
                        result = greaterFilter.filter(messageContext);
                        break;
                    case less:
                        LessFilter lessFilter = new LessFilter(messageContext,s);
                        result = lessFilter.filter(messageContext);
                        break;
                    default:
                        result = false;
                        break;
                }
            }
            if(result){
                if(s.getConstituentElements() != null && s.getConstituentElements().size() != 0){
                    result = delFilter(s.getConstituentElements(), messageContext,ruleSelectList);
                }
            }
            if(s.getName() != null &&  !"".equals(s.getName())){
                MwRuleSelectParam parten = getMwRuleSelectParten(s.getParentKey(),ruleSelectList);
                RelationEnum type =  RelationEnum.valueOf(parten.getCondition());
                switch (type){
                    case and:
                        if(!result ){
                            return result;
                        }
                        break;
                    case or:
                        if(result){
                            return result;
                        }
                        break;
                }
            }
        }

        return result;
    }

    private static MwRuleSelectParam getMwRuleSelectParten(String partenKey, List<MwRuleSelectParam> rootList){
        for(MwRuleSelectParam s : rootList){
            if(s.getKey().equals(partenKey)){
                return s;
            }
        }
        return null;
    }



}
