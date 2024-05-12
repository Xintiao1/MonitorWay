package cn.mw.monitor.service.alert.dto;

public enum AlertHuaXingLevelENEnum {
    WARNING("Warning","三级"),
    ERROR("High","二级"),
    DISASTER("Disaster","一级");


    private String nameEN;
    private String name;
    AlertHuaXingLevelENEnum(String nameEN, String name){
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
        for (AlertHuaXingLevelENEnum levelEnum : AlertHuaXingLevelENEnum.values()){
            if(levelEnum.getNameEN().equals(nameEN)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
