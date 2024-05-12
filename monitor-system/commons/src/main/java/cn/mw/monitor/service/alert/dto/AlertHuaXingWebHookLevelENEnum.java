package cn.mw.monitor.service.alert.dto;

public enum AlertHuaXingWebHookLevelENEnum {
    CRITICAL("critical","一级"),
    warning("warning","二级"),
    FIRING("firing","告警"),
    RESOLVED("resolved","恢复"),
    INFORMATION("information","三级");


    private String nameEN;
    private String name;
    AlertHuaXingWebHookLevelENEnum(String nameEN, String name){
        this.nameEN = nameEN;
        this.name = name;
    }
    public String getNameEN() {
        return nameEN;
    }
    public String getName() {
        return name;
    }
    public static String getName(String nameEN) {
        for (AlertHuaXingWebHookLevelENEnum levelEnum : AlertHuaXingWebHookLevelENEnum.values()){
            if(levelEnum.getNameEN().equals(nameEN)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
