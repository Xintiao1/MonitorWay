package cn.mw.monitor.service.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test {
    public static void main(String[] args){

        String jsonStr = "{\n" +
                "\"ruleProcessors\" : [\n" +
                "        {\n" +
                "            \"ruleName\" : \"severity\",\n" +
                "            \"messageRule\": { \"type\" : \"and\",\n" +
                "                             \"messageProcessorList\" : [{\"type\" : \"contain\"} ,{\"type\" : \"notContain\"}]\n" +
                "                             }\n" +
                "        },\n" +
                "        {\n" +
                "            \"ruleName\" : \"alert\",\n" +
                "            \"messageRule\": { \"type\" : \"or\",\n" +
                "                             \"messageProcessorList\" : [{\"type\" : \"notEqual\"} ,{\"type\" : \"equal\"}]\n" +
                "                             }\n" +
                "        },\n" +
                "        {\n" +
                "            \"ruleName\" : \"urgent\",\n" +
                "            \"messageRule\": { \"type\" : \"and\",\n" +
                "                             \"messageProcessorList\" : [{\"type\" : \"endWith\"} ,{\"type\" : \"startWith\"},{\"type\" : \"expression\"}]\n" +
                "                             }\n" +
                "        }\n" +
                "    ] \n" +
                "}";
        RuleManageFactory ruleManageFactory = new RuleManageFactory();
        RuleManager resultData = ruleManageFactory.getRuleManager(jsonStr);
        //System.out.println(resultData.getRuleProcessors().size());
    }
}
