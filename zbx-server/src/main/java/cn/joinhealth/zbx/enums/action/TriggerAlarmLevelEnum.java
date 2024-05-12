package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TriggerAlarmLevelEnum {

    OTHER(0,"未分类"),
    INFO(1,"信息"),
    WARNING(2,"警告"),
    NORMAL(3,"一般严重"),
    ERROR(4,"严重"),
    DISASTER(5,"灾难");


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

    TriggerAlarmLevelEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static TriggerAlarmLevelEnum getTriggerAlarmLevelEnum(int code){
        for (TriggerAlarmLevelEnum triggerAlarmLevelEnum : TriggerAlarmLevelEnum.values()){
            if(triggerAlarmLevelEnum.getCode() == code){
                return triggerAlarmLevelEnum;
            }
        }
        return null;
    }

    public static List getTriggerAlarmLevelList(){
        List list = new ArrayList();
        for (TriggerAlarmLevelEnum triggerAlarmLevelEnum : TriggerAlarmLevelEnum.values()){
            Map map = new HashMap();
            map.put("code",triggerAlarmLevelEnum.getCode());
            map.put("name",triggerAlarmLevelEnum.getName());
            list.add(map);
        }
        return list;
    }
}
