package cn.mw.monitor.util;


public enum RelationTypeEnum {
    contain("contain","包含")
    ,notContain("notContain","不包含")
    ,equal("equal","等于")
    ,notEqual("notEqual","不等于")
    ,startWith("startWith","开头")
    ,endWith("endWith","结尾")
    ,expression("expression","正则匹配")
    ,greater("greater","大于")
    ,less("less","小于")
    ;

    private String chName;
    private String type;
    RelationTypeEnum(String type, String chName){
        this.chName = chName;
        this.type = type;
    }

    public String getChName() {
        return chName;
    }
    public static String getTypeByChName(String chName) {
        for (RelationTypeEnum s : RelationTypeEnum.values()){
            if(s.chName.equals(chName)){
                return s.type;
            }
        }
        return chName;
    }
}
