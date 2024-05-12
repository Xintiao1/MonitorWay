package cn.joinhealth.zbx.enums.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum  OperatorEnum {

    EQUAL(0,"等于"),
    NOT_EQUAL(1,"不等于"),
    INCLUDE(2,"包含"),
    NOT_INCLUDE(3,"不包含"),
    IN(4,"在"),
    GREATER_THAN_OR_EQUAL(5,"大于等于"),
    LESS_THAN_OR_EQUAL(6,"小于等于"),
    NOT_IN(7,"不在");

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

    OperatorEnum(int code, String name){
        this.code = code;
        this.name = name;
    }

    public static OperatorEnum getOperatorEnum(int code){
        for (OperatorEnum operatorEnum : OperatorEnum.values()){
            if(operatorEnum.getCode() == code){
                return operatorEnum;
            }
        }
        return null;
    }

    public static List getOperatorList(){
        List list = new ArrayList();
        for (OperatorEnum operatorEnum : OperatorEnum.values()){
            Map map = new HashMap();
            map.put("code",operatorEnum.getCode());
            map.put("name",operatorEnum.getName());
            list.add(map);
        }
        return list;
    }
}
