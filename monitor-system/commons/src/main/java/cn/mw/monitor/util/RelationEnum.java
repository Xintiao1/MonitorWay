package cn.mw.monitor.util;


public enum RelationEnum {
    and("and")
    ,or("or")
    ,contain("包含")
    ,notContain("不包含")
    ,equal("等于")
    ,notEqual("不等于")
    ,startWith("开头")
    ,endWith("结尾")
    ,expression("正则匹配")
    ,greater("大于")
    ,less("小于")
    ;

    private String chName;

    RelationEnum(String chName){
        this.chName = chName;
    }

    public String getChName() {
        return chName;
    }
}
