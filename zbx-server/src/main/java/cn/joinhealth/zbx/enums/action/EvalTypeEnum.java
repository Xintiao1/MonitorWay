package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EvalTypeEnum {

    DEFAULT(0,"默认"),
    AND(1,"AND"),
    OR(2,"OR");

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

    EvalTypeEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static EvalTypeEnum getEvalTypeEnum(int code){
        for (EvalTypeEnum evalTypeEnum : EvalTypeEnum.values()){
            if(evalTypeEnum.getCode() == code){
                return evalTypeEnum;
            }
        }
        return null;
    }

    public static List getEvalTypeList(){
        List list = new ArrayList();
        for (EvalTypeEnum evalTypeEnum : EvalTypeEnum.values()){
            Map map = new HashMap();
            map.put("code",evalTypeEnum.getCode());
            map.put("name",evalTypeEnum.getName());
            list.add(map);
        }
        return list;
    }
}
