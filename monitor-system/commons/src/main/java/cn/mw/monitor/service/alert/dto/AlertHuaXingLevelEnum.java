package cn.mw.monitor.service.alert.dto;

public enum AlertHuaXingLevelEnum {
    OTHER("0","未分类"),
    WARNING("2","三级"),
    ERROR("4","二级"),
    DISASTER("5","一级");


    private String code;
    private String name;
    AlertHuaXingLevelEnum(String code, String name){
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getName(String code) {
        for (AlertHuaXingLevelEnum levelEnum : AlertHuaXingLevelEnum.values()){
            if(levelEnum.getCode().equals(code)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
