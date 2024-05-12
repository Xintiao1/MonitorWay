package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ConditionTypeEnum {

    HOST_GROUP(0,"主机群组"),
    HOST(1,"主机"),
    TRIGGER(2,"触发器"),
    TRIGGER_NAME(3,"触发器名称"),
    TRIGGER_ALARM_LEVEL(4,"触发器示警度"),
    TIME(6,"时间期间"),
    TEMPLATE(13,"模板"),
    APPLICATIONS(15,"应用集"),
    PROBLEM_STOPPED(16,"问题已被制止"),
    TAG(25,"标签"),
    MARKED_VALUE(26,"标记值");

    private int code;
    private String name;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ConditionTypeEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static ConditionTypeEnum getConditionTypeEnum(int code){
        for (ConditionTypeEnum conditionTypeEnum : ConditionTypeEnum.values()){
            if(conditionTypeEnum.getCode() == code){
                return conditionTypeEnum;
            }
        }
        return null;
    }

    public static List getConditionTypeList(){
        List list = new ArrayList();
        for (ConditionTypeEnum conditionTypeEnum : ConditionTypeEnum.values()){
            Map map = new HashMap();
            map.put("code",conditionTypeEnum.getCode());
            map.put("name",conditionTypeEnum.getName());
            list.add(map);
        }
        return list;
    }
}
