package cn.mw.monitor.state;

public enum DateTimeTypeEnum {
    TODAY(0,"今天"),
    YESTERDAY(1,"昨天"),
    LAST_WEEK(2,"上周"),
    LAST_MONTH(3,"上月"),
    CURRENT_QUARTER(4,"本季度"),
    CURRENT_YEAR(5,"本年"),
    USER_DEFINED(6,"自定义");
    private int code;
    private String name;

    DateTimeTypeEnum(int code,String name){
        this.code=code;
        this.name=name;
    }

    public  Integer getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public static DateTimeTypeEnum getByValue(int value){
        for (DateTimeTypeEnum code:DateTimeTypeEnum.values()) {
            if(code.getCode()==value){
                return code;
            }
        }
        return null;
    }

}
