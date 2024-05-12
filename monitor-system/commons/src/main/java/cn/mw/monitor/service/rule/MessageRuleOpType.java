package cn.mw.monitor.service.rule;

public enum MessageRuleOpType {
    and("且")
    ,or("或")
    ,orSet("或集合")
    ,contain("包含")
    ,notContain("不包含")
    ,equal("相等")
    ,notEqual("不相等")
    ,startWith("开始")
    ,endWith("结尾")
    ,expression("正则匹配")
    ,setContain("集合包含")
    ;

    private String chName;

    MessageRuleOpType(String chName){
        this.chName = chName;
    }

    public String getChName() {
        return chName;
    }
}
