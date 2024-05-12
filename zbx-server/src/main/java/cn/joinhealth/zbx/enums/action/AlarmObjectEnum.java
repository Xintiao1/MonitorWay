package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AlarmObjectEnum {

    GROUP(1,"分组"),
    ASSET_BELONGED(2,"资产负责人"),
    PERSONAL(3,"指定人员");

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

    AlarmObjectEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static AlarmObjectEnum getAlarmObjectEnum(int code){
        for (AlarmObjectEnum alarmObjectEnum : AlarmObjectEnum.values()){
            if(alarmObjectEnum.getCode() == code){
                return alarmObjectEnum;
            }
        }
        return null;
    }

    public static List getAlarmObjectList(){
        List list = new ArrayList();
        for (AlarmObjectEnum alarmObjectEnum : AlarmObjectEnum.values()){
            Map map = new HashMap();
            map.put("code",alarmObjectEnum.getCode());
            map.put("name",alarmObjectEnum.getName());
            list.add(map);
        }
        return list;
    }

}
